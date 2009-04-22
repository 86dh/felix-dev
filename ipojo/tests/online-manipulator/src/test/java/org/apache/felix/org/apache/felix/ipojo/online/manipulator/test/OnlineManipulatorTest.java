package org.apache.felix.org.apache.felix.ipojo.online.manipulator.test;


import static org.ops4j.pax.exam.CoreOptions.equinox;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.frameworks;
import static org.ops4j.pax.exam.CoreOptions.knopflerfish;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.tinybundles.core.TinyBundles.asFile;
import static org.ops4j.pax.tinybundles.core.TinyBundles.asURL;
import static org.ops4j.pax.tinybundles.core.TinyBundles.newBundle;
import static org.ops4j.pax.tinybundles.core.TinyBundles.with;

import java.io.File;
import java.io.IOException;

import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.architecture.Architecture;
import org.apache.felix.ipojo.architecture.InstanceDescription;
import org.apache.felix.org.apache.felix.ipojo.online.manipulator.test.impl.Consumer;
import org.apache.felix.org.apache.felix.ipojo.online.manipulator.test.impl.MyProvider;
import org.apache.felix.org.apache.felix.ipojo.online.manipulator.test.service.Hello;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;


@RunWith( JUnit4TestRunner.class )
public class OnlineManipulatorTest {
    
    
    private static File TMP = new File("target/tmp-bundle");
    
    @Inject
    BundleContext context;

    private OSGiHelper helper;

    
    /*
     * <groupId>org.apache.felix</groupId>
  <artifactId>org.apache.felix.ipojo.online.manipulator</artifactId>
  <version>1.3.0-SNAPSHOT</version>
 
     */
    
    @Configuration
    public static Option[] configure() throws IOException
    {
        
        

        String providerWithMetadata = providerWithMetadata();
        String providerWithMetadataInMetaInf = providerWithMetadataInMetaInf();
        String providerWithoutMetadata = providerWithoutMetadata();
        String consumerWithMetadata =  consumerWithMetadata();
        String consumerWithoutMetadata = consumerWithoutMetadata();
        
        return options(
                frameworks(
                        felix(),
                        equinox()
                    ),
            provision(
                mavenBundle()
                    .groupId( "org.ops4j.pax.tinybundles" )
                    .artifactId( "pax-tinybundles-core" )
                    .version( "0.5.0-SNAPSHOT" )),
            provision( 
                    mavenBundle()
                    .groupId("org.apache.felix")
                    .artifactId("org.apache.felix.ipojo")
                    .version("1.3.0-SNAPSHOT")
            ),
            
            provision(
                    mavenBundle()
                    .groupId("org.apache.felix")
                    .artifactId("org.apache.felix.ipojo.online.manipulator")
                    .version("1.3.0-SNAPSHOT")
                    ),
            provision(
                            newBundle()
                                .addClass( Hello.class )
                                .prepare()
                               .set(Constants.BUNDLE_SYMBOLICNAME,"ServiceInterface")
                               .set(Constants.EXPORT_PACKAGE, "org.apache.felix.org.apache.felix.ipojo.online.manipulator.test.service")
                                .build( asURL() ).toExternalForm()
                        ),
           systemProperty( "providerWithMetadata" ).value( providerWithMetadata ),
           systemProperty( "providerWithMetadataInMetaInf" ).value( providerWithMetadataInMetaInf ),
           systemProperty( "providerWithoutMetadata" ).value( providerWithoutMetadata ),
           systemProperty( "consumerWithMetadata").value(consumerWithMetadata),
           systemProperty( "consumerWithoutMetadata").value(consumerWithoutMetadata)
           );
        
       


    }
    
    @Before
    public void before() {
        helper = new OSGiHelper(context);
    }
    
    @After
    public void after() {
        helper.dispose();
    }
    
    private static File getTemporaryFile(String name) throws IOException {
        if (! TMP.exists()) {
            TMP.mkdirs();
            TMP.deleteOnExit();
        }
        File file = File.createTempFile(name, ".jar", TMP);
        //File file = new File(TMP, name + ".jar");
        if (file.exists()) {
            file.delete();
        }
        file.deleteOnExit();
        return file;  
    }
    
    @Test
    public void installProviderWithMetadata1() throws BundleException, InvalidSyntaxException, IOException {
        String url = context.getProperty("providerWithMetadata");
        Assert.assertNotNull(url);
        context.installBundle("ipojo:"+url).start();
        
        assertBundle("Provider");
        helper.waitForService(Hello.class.getName(), null, 5000);
        assertValidity();
        Assert.assertNotNull(context.getServiceReference(Hello.class.getName()));
    }
    
   

    @Test
    public void installProviderWithMetadata2() throws BundleException, InvalidSyntaxException, IOException {
        String url = context.getProperty("providerWithMetadataInMetaInf");
        Assert.assertNotNull(url);
        context.installBundle("ipojo:"+url).start();
        assertBundle("Provider");
        helper.waitForService(Hello.class.getName(), null, 5000);
        assertValidity();
        Assert.assertNotNull(context.getServiceReference(Hello.class.getName()));
    }
    
    @Test
    public void installProviderWithoutMetadata() throws BundleException, InvalidSyntaxException, IOException {
        String url = context.getProperty("providerWithoutMetadata");
        Assert.assertNotNull(url);
        context.installBundle("ipojo:"+url).start();
        assertBundle("Provider");
        helper.waitForService(Hello.class.getName(), null, 5000);
        assertValidity();
        Assert.assertNotNull(context.getServiceReference(Hello.class.getName()));
    }
    
    @Test
    public void installConsumerWithMetadata() throws BundleException, InvalidSyntaxException, IOException {
        String url = context.getProperty("providerWithoutMetadata");
        Assert.assertNotNull(url);
        context.installBundle("ipojo:"+url).start();
        assertBundle("Provider");
        
        String url2 = context.getProperty("consumerWithMetadata");
        Assert.assertNotNull(url);
        context.installBundle("ipojo:"+url2).start();
        assertBundle("Consumer");
        helper.waitForService(Hello.class.getName(), null, 5000);
        // Wait for activation.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertValidity();
        Assert.assertNotNull(context.getServiceReference(Hello.class.getName()));
    }
    
    @Test
    public void installConsumerWithoutMetadata() throws BundleException, InvalidSyntaxException, IOException {
        String url = context.getProperty("providerWithMetadataInMetaInf");
        Assert.assertNotNull(url);
        context.installBundle("ipojo:"+url).start();
        assertBundle("Provider");
        helper.waitForService(Hello.class.getName(), null, 5000);

        String url2 = context.getProperty("consumerWithoutMetadata");
        Assert.assertNotNull(url);
        context.installBundle("ipojo:"+url2).start();
        assertBundle("Consumer");
        // Wait for activation.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }  
        assertValidity();
        Assert.assertNotNull(context.getServiceReference(Hello.class.getName()));
    }
    
    /**
     * Gets a regular bundle containing metadata file
     * @return the url of the bundle
     * @throws IOException 
     */
    public static String providerWithMetadata() throws IOException {
        return newBundle()
        .addResource("metadata.xml", OnlineManipulatorTest.class.getClassLoader().getResource("provider.xml"))
        .addClass(MyProvider.class)
        .prepare( 
            with()
                .set(Constants.BUNDLE_SYMBOLICNAME,"Provider")
                .set(Constants.IMPORT_PACKAGE, "org.apache.felix.org.apache.felix.ipojo.online.manipulator.test.service")
            )
              .build( asFile(getTemporaryFile("providerWithMetadata")) ).toURL().toExternalForm();
    }
    
    /**
     * Gets a regular bundle containing metadata file in the META-INF directory
     * @return the url of the bundle
     * @throws IOException 
     */
    public static String providerWithMetadataInMetaInf() throws IOException {
        return newBundle()
        .addResource("META-INF/metadata.xml", OnlineManipulatorTest.class.getClassLoader().getResource("provider.xml"))
        .addClass(MyProvider.class)
        .prepare( 
            with()
                .set(Constants.BUNDLE_SYMBOLICNAME,"Provider")
                .set(Constants.IMPORT_PACKAGE, "org.apache.felix.org.apache.felix.ipojo.online.manipulator.test.service")
            )
              .build( asFile(getTemporaryFile("providerWithMetadataInMetaInf")) ).toURL().toExternalForm();
    }
    
    /**
     * Gets a provider bundle which does not contain the metadata file.
     * @return the url of the bundle + metadata
     * @throws IOException 
     */
    public static String providerWithoutMetadata() throws IOException {
        String url = newBundle()
        //.addResource("metadata.xml", this.getClass().getClassLoader().getResource("provider.xml"))
        .addClass(MyProvider.class)
        .prepare( 
            with()
                .set(Constants.BUNDLE_SYMBOLICNAME,"Provider")
                .set(Constants.IMPORT_PACKAGE, "org.apache.felix.org.apache.felix.ipojo.online.manipulator.test.service")
            )
              .build( asFile(getTemporaryFile("providerWithoutMetadata")) ).toURL().toExternalForm();
        
        return url + "!" +OnlineManipulatorTest.class.getClassLoader().getResource("provider.xml");
    }
    
    /**
     * Gets a consumer bundle using annotation containing the instance
     * declaration in the metadata.
     * @return the url of the bundle
     * @throws IOException 
     */
    public static String consumerWithMetadata() throws IOException {
        return newBundle()
            .addResource("metadata.xml", OnlineManipulatorTest.class.getClassLoader().getResource("consumer.xml"))
            .addClass(Consumer.class)

        .prepare(
                with()
                        .set(Constants.BUNDLE_SYMBOLICNAME, "Consumer")
                .set(Constants.IMPORT_PACKAGE, "org.apache.felix.org.apache.felix.ipojo.online.manipulator.test.service")
                )
                .build( asFile(getTemporaryFile("consumerWithMetadata")) ).toURL().toExternalForm();
       
    }
    
    /**
     * Gets a consumer bundle using annotation that does not contain
     * metadata
     * @return the url of the bundle + metadata
     * @throws IOException 
     */
    public static String consumerWithoutMetadata() throws IOException {
        String url = newBundle()
        .addClass(Consumer.class)
        .prepare(
                with()
                        .set(Constants.BUNDLE_SYMBOLICNAME, "Consumer")
                .set(Constants.IMPORT_PACKAGE, "org.apache.felix.org.apache.felix.ipojo.online.manipulator.test.service")
                )
                .build( asFile(getTemporaryFile("consumerWithoutMetadata")) ).toURL().toExternalForm();
        
        return url + "!" + OnlineManipulatorTest.class.getClassLoader().getResource("consumer.xml");
    }
    
    
    
    public void dumpServices() throws InvalidSyntaxException {
        ServiceReference[] refs = context.getAllServiceReferences(null, null);
        System.out.println(" === Services === ");
        for (ServiceReference ref : refs) {
            String[] itf = (String[]) ref.getProperty(Constants.OBJECTCLASS);
            System.out.println(itf[0]);
        }
        System.out.println("====");
    }
    
    public void dumpBundles() throws InvalidSyntaxException {
        Bundle[] bundles = context.getBundles();
        System.out.println(" === Bundles === ");
        for (Bundle bundle : bundles) {
            String sn  =  bundle.getSymbolicName();
            System.out.println(sn);
        }
        System.out.println("====");
    }
    
    private void assertBundle(String sn) {
        for (Bundle bundle :context.getBundles()) {
            if (bundle.getSymbolicName().equals(sn)
                    && bundle.getState() == Bundle.ACTIVE) {
                return;
            }
                
        }
        Assert.fail("Cannot find the bundle " + sn);
    }
    
    private void assertValidity() {
        try {
            ServiceReference[] refs = context.getServiceReferences(Architecture.class.getName(), null);
            Assert.assertNotNull(refs);
            for (int i = 0; i < refs.length; i++) {
                InstanceDescription id = ((Architecture) context.getService(refs[i])).getInstanceDescription();
                int state = id.getState();
                Assert.assertEquals("State of " + id.getName(), ComponentInstance.VALID, state);
            }
        } catch (InvalidSyntaxException e) {
            Assert.fail(e.getMessage());
        }
            
    }
    
  

}
