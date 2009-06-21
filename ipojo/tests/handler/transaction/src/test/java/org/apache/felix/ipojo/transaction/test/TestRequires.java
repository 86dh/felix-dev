package org.apache.felix.ipojo.transaction.test;

import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;
import static org.ops4j.pax.exam.MavenUtils.asInProject;
import static org.ops4j.pax.tinybundles.core.TinyBundles.with;

import java.io.File;
import java.net.URL;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.pax.exam.target.BundleAsiPOJO;
import org.apache.felix.ipojo.transaction.test.component.FooDelegator;
import org.apache.felix.ipojo.transaction.test.component.FooImpl;
import org.apache.felix.ipojo.transaction.test.service.CheckService;
import org.apache.felix.ipojo.transaction.test.service.Foo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.tinybundles.core.TinyBundles;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

@RunWith( JUnit4TestRunner.class )
public class TestRequires {
    
    @Inject
    private BundleContext context;
    
    private OSGiHelper osgi;
    
    private IPOJOHelper ipojo;
    
    public static final File ROOT = new File("target/tmp");
    public static final File TEST = new File("src/test/resources");

    
    @Before
    public void init() {
        osgi = new OSGiHelper(context);
        ipojo = new IPOJOHelper(context);
    }
    
    @After
    public void stop() {
        ipojo.dispose();
        osgi.dispose();
    }
    
    @Configuration
    public static Option[] configure() {    
        ROOT.mkdirs();

        URL service = TinyBundles.newBundle()
            .addClass(CheckService.class)
            .addClass(Foo.class)
           .prepare( 
                with()
                .set(Constants.BUNDLE_SYMBOLICNAME,"Service")
                .set(Constants.EXPORT_PACKAGE, "org.apache.felix.ipojo.transaction.test.service")
                .set(Constants.IMPORT_PACKAGE, "javax.transaction")
                )
            .build( TinyBundles.asURL());
            
        String fooimpl = TinyBundles.newBundle()
            .addClass(FooImpl.class)
            .prepare( 
                    with()
                    .set(Constants.BUNDLE_SYMBOLICNAME,"Foo Provider")
                    .set(Constants.IMPORT_PACKAGE, "org.apache.felix.ipojo.transaction.test.service")
                )
                .build( new BundleAsiPOJO(new File(ROOT, "FooImpl.jar"), new File(TEST, "foo.xml"))  ).toExternalForm();
        
        String test = TinyBundles.newBundle()
        .addClass(FooDelegator.class)
        .prepare( 
                with()
                .set(Constants.BUNDLE_SYMBOLICNAME,"Required Transaction Propgatation")
                .set(Constants.IMPORT_PACKAGE, "org.apache.felix.ipojo.transaction.test.service, javax.transaction")
            )
            .build( new BundleAsiPOJO(new File(ROOT, "requires.jar"), new File(TEST, "requires.xml"))  ).toExternalForm();
    
        
        Option[] opt =  options(
 
                provision(
                        mavenBundle().groupId("org.ops4j.pax.logging").artifactId("pax-logging-api").version(asInProject()),
                        mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.ipojo").version(asInProject()),
                        mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.ipojo.transaction").version(asInProject()),
                        mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.transaction").version(asInProject()),
                        mavenBundle()
                        .groupId( "org.ops4j.pax.tinybundles" )
                        .artifactId( "pax-tinybundles-core" )
                        .version( "0.5.0-SNAPSHOT" ),
                        bundle(service.toExternalForm()),
                        bundle(fooimpl),
                        bundle(test)
                    )
                )

                ;
        return opt;
    }
    
    
    @Test
    public void testOkOutsideTransaction() {
        ComponentInstance prov = ipojo.createComponentInstance("org.apache.felix.ipojo.transaction.test.component.FooImpl");
        ComponentInstance under = ipojo.createComponentInstance("requires-ok");
        
        Assert.assertEquals(ComponentInstance.VALID, prov.getState());
        Assert.assertEquals(ComponentInstance.VALID, under.getState());
        
        ServiceReference ref = ipojo.getServiceReferenceByName(CheckService.class.getName(), under.getInstanceName());
        Assert.assertNotNull(ref);

        ((CheckService) osgi.getServiceObject(ref)).doSomethingGood();
    }
    
    @Test
    public void testOkInsideTransaction() throws NotSupportedException, SystemException, SecurityException, HeuristicMixedException, HeuristicRollbackException, RollbackException {
        ComponentInstance prov = ipojo.createComponentInstance("org.apache.felix.ipojo.transaction.test.component.FooImpl");
        ComponentInstance under = ipojo.createComponentInstance("requires-ok");
        
        Assert.assertEquals(ComponentInstance.VALID, prov.getState());
        Assert.assertEquals(ComponentInstance.VALID, under.getState());
        
        ServiceReference ref = ipojo.getServiceReferenceByName(CheckService.class.getName(), under.getInstanceName());
        Assert.assertNotNull(ref);
        
        CheckService cs = (CheckService) osgi.getServiceObject(ref);
        TransactionManager tm = (TransactionManager) osgi.getServiceObject(TransactionManager.class.getName(), null);
        tm.begin();
        Transaction t = tm.getTransaction();
        cs.doSomethingGood();
        Transaction t2 = cs.getCurrentTransaction();
        Assert.assertSame(t2, t);
        t.commit();
    }
    
    @Test(expected=NullPointerException.class)
    public void testExceptionOutsideTransaction() {
        ComponentInstance prov = ipojo.createComponentInstance("org.apache.felix.ipojo.transaction.test.component.FooImpl");
        ComponentInstance under = ipojo.createComponentInstance("requires-ok");
        
        Assert.assertEquals(ComponentInstance.VALID, prov.getState());
        Assert.assertEquals(ComponentInstance.VALID, under.getState());
        
        ServiceReference ref = ipojo.getServiceReferenceByName(CheckService.class.getName(), under.getInstanceName());
        Assert.assertNotNull(ref);

        ((CheckService) osgi.getServiceObject(ref)).doSomethingBad();
    }
    
    @Test(expected=RollbackException.class)
    public void testExceptionInsideTransaction() throws NotSupportedException, SystemException, SecurityException, HeuristicMixedException, HeuristicRollbackException, RollbackException {
        ComponentInstance prov = ipojo.createComponentInstance("org.apache.felix.ipojo.transaction.test.component.FooImpl");
        ComponentInstance under = ipojo.createComponentInstance("requires-ok");
        
        Assert.assertEquals(ComponentInstance.VALID, prov.getState());
        Assert.assertEquals(ComponentInstance.VALID, under.getState());
        
        ServiceReference ref = ipojo.getServiceReferenceByName(CheckService.class.getName(), under.getInstanceName());
        Assert.assertNotNull(ref);
        
        CheckService cs = (CheckService) osgi.getServiceObject(ref);
        TransactionManager tm = (TransactionManager) osgi.getServiceObject(TransactionManager.class.getName(), null);
        tm.begin();
        Transaction t = tm.getTransaction();
        try {
            cs.doSomethingBad();
            Assert.fail("NullPointerException expected");
        } catch(Exception e) {
            Assert.assertTrue(e instanceof NullPointerException);
        }
        Transaction t2 = cs.getCurrentTransaction();
        Assert.assertSame(t2, t);
        Assert.assertEquals(Status.STATUS_MARKED_ROLLBACK, t.getStatus());
        
        t.commit(); // Throw a rollback exception.
    }
    
    @Test
    public void testExceptionInsideTransactionRB() throws NotSupportedException, SystemException, SecurityException, HeuristicMixedException, HeuristicRollbackException, RollbackException {
        ComponentInstance prov = ipojo.createComponentInstance("org.apache.felix.ipojo.transaction.test.component.FooImpl");
        ComponentInstance under = ipojo.createComponentInstance("requires-ok");
        
        Assert.assertEquals(ComponentInstance.VALID, prov.getState());
        Assert.assertEquals(ComponentInstance.VALID, under.getState());
        
        ServiceReference ref = ipojo.getServiceReferenceByName(CheckService.class.getName(), under.getInstanceName());
        Assert.assertNotNull(ref);
        
        CheckService cs = (CheckService) osgi.getServiceObject(ref);
        TransactionManager tm = (TransactionManager) osgi.getServiceObject(TransactionManager.class.getName(), null);
        tm.begin();
        Transaction t = tm.getTransaction();
        try {
            cs.doSomethingBad();
            Assert.fail("NullPointerException expected");
        } catch(Exception e) {
            Assert.assertTrue(e instanceof NullPointerException);
        }
        Transaction t2 = cs.getCurrentTransaction();
        Assert.assertSame(t2, t);
        Assert.assertEquals(Status.STATUS_MARKED_ROLLBACK, t.getStatus());
        
        t.rollback();
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void testExpectedExceptionOutsideTransaction() {
        ComponentInstance prov = ipojo.createComponentInstance("org.apache.felix.ipojo.transaction.test.component.FooImpl");
        ComponentInstance under = ipojo.createComponentInstance("requires-ok");
        
        Assert.assertEquals(ComponentInstance.VALID, prov.getState());
        Assert.assertEquals(ComponentInstance.VALID, under.getState());
        
        ServiceReference ref = ipojo.getServiceReferenceByName(CheckService.class.getName(), under.getInstanceName());
        Assert.assertNotNull(ref);

        ((CheckService) osgi.getServiceObject(ref)).doSomethingBad2();
    }
    
    @Test
    public void testExpectedExceptionInsideTransaction() throws NotSupportedException, SystemException, SecurityException, HeuristicMixedException, HeuristicRollbackException, RollbackException {
        ComponentInstance prov = ipojo.createComponentInstance("org.apache.felix.ipojo.transaction.test.component.FooImpl");
        ComponentInstance under = ipojo.createComponentInstance("requires-ok");
        
        Assert.assertEquals(ComponentInstance.VALID, prov.getState());
        Assert.assertEquals(ComponentInstance.VALID, under.getState());
        
        ServiceReference ref = ipojo.getServiceReferenceByName(CheckService.class.getName(), under.getInstanceName());
        Assert.assertNotNull(ref);
        
        CheckService cs = (CheckService) osgi.getServiceObject(ref);
        TransactionManager tm = (TransactionManager) osgi.getServiceObject(TransactionManager.class.getName(), null);
        tm.begin();
        Transaction t = tm.getTransaction();
        try {
            cs.doSomethingBad2();
            Assert.fail("UnsupportedOperationException expected");
        } catch(Exception e) {
            Assert.assertTrue(e instanceof UnsupportedOperationException);
        }
        Transaction t2 = cs.getCurrentTransaction();
        Assert.assertSame(t2, t);
        Assert.assertEquals(Status.STATUS_ACTIVE, t.getStatus());
        
        t.commit();
    }
    
    @Test
    public void testOkOutsideTransactionWithCallback() {
        ComponentInstance prov = ipojo.createComponentInstance("org.apache.felix.ipojo.transaction.test.component.FooImpl");
        ComponentInstance under = ipojo.createComponentInstance("requires-cb");
        
        Assert.assertEquals(ComponentInstance.VALID, prov.getState());
        Assert.assertEquals(ComponentInstance.VALID, under.getState());
        
        ServiceReference ref = ipojo.getServiceReferenceByName(CheckService.class.getName(), under.getInstanceName());
        Assert.assertNotNull(ref);

        
        CheckService cs = (CheckService) osgi.getServiceObject(ref);

        cs.doSomethingGood();
        
        Assert.assertNull(cs.getLastRolledBack());
        Assert.assertNotNull(cs.getLastCommitted());
        Assert.assertEquals(1, cs.getNumberOfCommit());
        Assert.assertEquals(0, cs.getNumberOfRollback());
    }
    
    @Test
    public void testOkInsideTransactionWithCallback() throws NotSupportedException, SystemException, SecurityException, HeuristicMixedException, HeuristicRollbackException, RollbackException {
        ComponentInstance prov = ipojo.createComponentInstance("org.apache.felix.ipojo.transaction.test.component.FooImpl");
        ComponentInstance under = ipojo.createComponentInstance("requires-cb");
        
        Assert.assertEquals(ComponentInstance.VALID, prov.getState());
        Assert.assertEquals(ComponentInstance.VALID, under.getState());
        
        ServiceReference ref = ipojo.getServiceReferenceByName(CheckService.class.getName(), under.getInstanceName());
        Assert.assertNotNull(ref);
        
        CheckService cs = (CheckService) osgi.getServiceObject(ref);
        TransactionManager tm = (TransactionManager) osgi.getServiceObject(TransactionManager.class.getName(), null);
        tm.begin();
        Transaction t = tm.getTransaction();
        cs.doSomethingGood();
        Transaction t2 = cs.getCurrentTransaction();
        Assert.assertSame(t2, t);
        t.commit();
        
        Assert.assertNull(cs.getLastRolledBack());
        Assert.assertNotNull(cs.getLastCommitted());
        Assert.assertEquals(1, cs.getNumberOfCommit());
        Assert.assertEquals(0, cs.getNumberOfRollback());
        
        Assert.assertSame(t, cs.getLastCommitted());
    }
    
    @Test(expected=NullPointerException.class)
    public void testExceptionOutsideTransactionWithCallback() {
        ComponentInstance prov = ipojo.createComponentInstance("org.apache.felix.ipojo.transaction.test.component.FooImpl");
        ComponentInstance under = ipojo.createComponentInstance("requires-cb");
        
        Assert.assertEquals(ComponentInstance.VALID, prov.getState());
        Assert.assertEquals(ComponentInstance.VALID, under.getState());
        
        ServiceReference ref = ipojo.getServiceReferenceByName(CheckService.class.getName(), under.getInstanceName());
        Assert.assertNotNull(ref);

        CheckService cs = (CheckService) osgi.getServiceObject(ref);

        cs.doSomethingBad();
        
        Assert.assertNotNull(cs.getLastRolledBack());
        Assert.assertNull(cs.getLastCommitted());
        Assert.assertEquals(0, cs.getNumberOfCommit());
        Assert.assertEquals(1, cs.getNumberOfRollback());
    }
    
    @Test
    public void testExceptionInsideTransactionWithCallback() throws NotSupportedException, SystemException, SecurityException, HeuristicMixedException, HeuristicRollbackException, RollbackException {
        ComponentInstance prov = ipojo.createComponentInstance("org.apache.felix.ipojo.transaction.test.component.FooImpl");
        ComponentInstance under = ipojo.createComponentInstance("requires-cb");
        
        Assert.assertEquals(ComponentInstance.VALID, prov.getState());
        Assert.assertEquals(ComponentInstance.VALID, under.getState());
        
        ServiceReference ref = ipojo.getServiceReferenceByName(CheckService.class.getName(), under.getInstanceName());
        Assert.assertNotNull(ref);
        
        CheckService cs = (CheckService) osgi.getServiceObject(ref);
        TransactionManager tm = (TransactionManager) osgi.getServiceObject(TransactionManager.class.getName(), null);
        tm.begin();
        Transaction t = tm.getTransaction();
        try {
            cs.doSomethingBad();
            Assert.fail("NullPointerException expected");
        } catch(Exception e) {
            Assert.assertTrue(e instanceof NullPointerException);
        }
        Transaction t2 = cs.getCurrentTransaction();
        Assert.assertSame(t2, t);
        Assert.assertEquals(Status.STATUS_MARKED_ROLLBACK, t.getStatus());
        
        try {
            t.commit(); // Throw a rollback exception.
        } catch (RollbackException e) {
            // Expected
        } catch (Throwable e) {
            Assert.fail(e.getMessage()); // Unexpected
        }
        
        Assert.assertNotNull(cs.getLastRolledBack());
        Assert.assertNull(cs.getLastCommitted());
        Assert.assertEquals(0, cs.getNumberOfCommit());
        Assert.assertEquals(1, cs.getNumberOfRollback());
        
        Assert.assertSame(t, cs.getLastRolledBack());
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void testExpectedExceptionOutsideTransactionWithCallback() {
        ComponentInstance prov = ipojo.createComponentInstance("org.apache.felix.ipojo.transaction.test.component.FooImpl");
        ComponentInstance under = ipojo.createComponentInstance("requires-cb");
        
        Assert.assertEquals(ComponentInstance.VALID, prov.getState());
        Assert.assertEquals(ComponentInstance.VALID, under.getState());
        
        ServiceReference ref = ipojo.getServiceReferenceByName(CheckService.class.getName(), under.getInstanceName());
        Assert.assertNotNull(ref);

        CheckService cs = (CheckService) osgi.getServiceObject(ref);

        cs.doSomethingBad2();
        
        Assert.assertNull(cs.getLastRolledBack());
        Assert.assertNotNull(cs.getLastCommitted());
        Assert.assertEquals(1, cs.getNumberOfCommit());
        Assert.assertEquals(0, cs.getNumberOfRollback());
    }
    
    @Test
    public void testExpectedExceptionInsideTransactionWithCallback() throws NotSupportedException, SystemException, SecurityException, HeuristicMixedException, HeuristicRollbackException, RollbackException {
        ComponentInstance prov = ipojo.createComponentInstance("org.apache.felix.ipojo.transaction.test.component.FooImpl");
        ComponentInstance under = ipojo.createComponentInstance("requires-cb");
        
        Assert.assertEquals(ComponentInstance.VALID, prov.getState());
        Assert.assertEquals(ComponentInstance.VALID, under.getState());
        
        ServiceReference ref = ipojo.getServiceReferenceByName(CheckService.class.getName(), under.getInstanceName());
        Assert.assertNotNull(ref);
        
        CheckService cs = (CheckService) osgi.getServiceObject(ref);
        TransactionManager tm = (TransactionManager) osgi.getServiceObject(TransactionManager.class.getName(), null);
        tm.begin();
        Transaction t = tm.getTransaction();
        try {
            cs.doSomethingBad2();
            Assert.fail("UnsupportedOperationException expected");
        } catch(Exception e) {
            Assert.assertTrue(e instanceof UnsupportedOperationException);
        }
        Transaction t2 = cs.getCurrentTransaction();
        Assert.assertSame(t2, t);
        Assert.assertEquals(Status.STATUS_ACTIVE, t.getStatus());
        
        t.commit();
        
        Assert.assertNull(cs.getLastRolledBack());
        Assert.assertNotNull(cs.getLastCommitted());
        Assert.assertEquals(1, cs.getNumberOfCommit());
        Assert.assertEquals(0, cs.getNumberOfRollback());
        
        Assert.assertSame(t, cs.getLastCommitted());
    }
    

    
}
