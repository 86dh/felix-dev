/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.http.jetty;

import org.eclipse.jetty.server.HttpConfiguration.Customizer;
import org.osgi.annotation.versioning.ConsumerType;


/**
 * The <code>LoadBalancerCustomizerFactory</code> is a service interface which allows
 * extensions to inject custom Jetty {@code Customizer} instances to add
 * to the Jetty server in order to handle the Proxy Load Balancer connection.
 *
 * {@code LoadBalancerCustomizerFactory } services are responsible for creating the
 * {@code Customizer} instances and providing base configuration.
 *
 * @since 2.1
 */
@ConsumerType
public interface LoadBalancerCustomizerFactory
{
    /**
     * Creates new Jetty {@code Customizer} instances.
     *
     * @return A configured Jetty {@code Customizer} instance or {@code null}
     *         if the customizer can't be created.
     */
    Customizer createCustomizer();

}
