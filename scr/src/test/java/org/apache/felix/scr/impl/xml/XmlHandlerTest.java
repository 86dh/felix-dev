/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.scr.impl.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.felix.scr.impl.logger.MockBundleLogger;
import org.apache.felix.scr.impl.metadata.ComponentMetadata;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;

public class XmlHandlerTest {

    @Test
    public void testPropertiesWithoutValue() throws Exception {
        final URL url = this.getClass().getClassLoader().getResource("parsertest-nopropvalue.xml");
        final List<ComponentMetadata> components = parse(url);
        assertEquals(1, components.size());

        final ComponentMetadata cm = components.get(0);
        cm.validate();
        // the xml has four properties, two of them with no value, so they should not be part of the
        // component metadata
        assertEquals(2, cm.getProperties().size());
        assertNotNull(cm.getProperties().get("service.vendor"));
        assertNotNull(cm.getProperties().get("jmx.objectname"));
    }

    private List<ComponentMetadata> parse(final URL descriptorURL) throws Exception {
        final Bundle bundle = Mockito.mock(Bundle.class);
        Mockito.when(bundle.getLocation()).thenReturn("bundle");

        InputStream stream = null;
        try {
            stream = descriptorURL.openStream();

            XmlHandler handler = new XmlHandler(bundle, new MockBundleLogger(), false, false);
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            final SAXParser parser = factory.newSAXParser();

            parser.parse(stream, handler);

            return handler.getComponentMetadataList();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignore) {
                }
            }
        }

    }
}
