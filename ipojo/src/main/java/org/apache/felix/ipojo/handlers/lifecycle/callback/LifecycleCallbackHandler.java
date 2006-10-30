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
package org.apache.felix.ipojo.handlers.lifecycle.callback;

import java.lang.reflect.InvocationTargetException;
import java.util.Dictionary;
import java.util.logging.Level;

import org.apache.felix.ipojo.Activator;
import org.apache.felix.ipojo.ComponentManagerImpl;
import org.apache.felix.ipojo.Handler;
import org.apache.felix.ipojo.metadata.Element;

/**
 * Lifecycle callback handler.
 * @author <a href="mailto:felix-dev@incubator.apache.org">Felix Project Team</a>
 */
public class LifecycleCallbackHandler extends Handler {

    /**
     * The list of the callback of the component.
     */
    private LifecycleCallback[] m_callbacks = new LifecycleCallback[0];

    /**
     * State of the component manager (unresolved at the beginning).
     */
    private int m_state = ComponentManagerImpl.INVALID;

    /**
     * The component manager.
     */
    private ComponentManagerImpl m_componentManager;

    /**
     * Add the given Hook to the hook list.
     * @param hk : the element to add
     */
    private void addCallback(LifecycleCallback hk) {
        for (int i = 0; (m_callbacks != null) && (i < m_callbacks.length); i++) {
            if (m_callbacks[i] == hk) { return; }
        }

        if (m_callbacks.length > 0) {
            LifecycleCallback[] newHk = new LifecycleCallback[m_callbacks.length + 1];
            System.arraycopy(m_callbacks, 0, newHk, 0, m_callbacks.length);
            newHk[m_callbacks.length] = hk;
            m_callbacks = newHk;
        }
        else {
            m_callbacks = new LifecycleCallback[] {hk};
        }

    }

    /**
     * @see org.apache.felix.ipojo.Handler#configure(org.apache.felix.ipojo.ComponentManagerImpl, org.apache.felix.ipojo.metadata.Element)
     */
    public void configure(ComponentManagerImpl cm, Element metadata, Dictionary configuration) {
        m_componentManager = cm;
        m_callbacks = new LifecycleCallback[0];

        Element[] hooksMetadata = metadata.getElements("callback");
        for (int i = 0; i < hooksMetadata.length; i++) {
            // Create an HookMetadata object
            String initialState = hooksMetadata[i].getAttribute("initial");
            String finalState = hooksMetadata[i].getAttribute("final");
            String method = hooksMetadata[i].getAttribute("method");
            boolean isStatic = false;
            if (hooksMetadata[i].containsAttribute("isStatic") && hooksMetadata[i].getAttribute("isStatic").equals("true")) { isStatic = true; }

            LifecycleCallbackMetadata hm = new LifecycleCallbackMetadata(initialState, finalState, method, isStatic);

            LifecycleCallback hk = new LifecycleCallback(this, hm);
            addCallback(hk);
        }
        if (m_callbacks.length > 0) { m_componentManager.register(this); }
    }

    /**
     * @see org.apache.felix.ipojo.Handler#start()
     */
    public void start() { } //Do nothing during the start

    /**
     * @see org.apache.felix.ipojo.Handler#stop()
     */
    public void stop() {
        m_state = ComponentManagerImpl.INVALID;
    }

    /**
     * @return the component manager
     */
    protected ComponentManagerImpl getComponentManager() { return m_componentManager; }

    /**
     * When the state change call the associated hooks.
     * @see org.apache.felix.ipojo.Handler#stateChanged(int)
     */
    public void stateChanged(int state) {
        Activator.getLogger().log(Level.INFO, "[" + m_componentManager.getComponentMetatada().getClassName() + "] State changed in callback handler, check " + m_callbacks.length + " callbacks. Transition : " + m_state + " -> " + state);
        for (int i = 0; i < m_callbacks.length; i++) {
            if (m_callbacks[i].getMetadata().getInitialState() == m_state && m_callbacks[i].getMetadata().getFinalState() == state) {
                try {
                    Activator.getLogger().log(Level.INFO, "[" + m_componentManager.getComponentMetatada().getClassName() + "] Call the callback : " + m_callbacks[i].getMetadata().getMethod());
                    m_callbacks[i].call();
                } catch (NoSuchMethodException e) {
                    Activator.getLogger().log(Level.SEVERE, "[" + m_componentManager.getComponentMetatada().getClassName() + "] The callback method " + m_callbacks[i].getMetadata().getMethod() + " is not found : " + e.getMessage());
                } catch (IllegalAccessException e) {
                    Activator.getLogger().log(Level.SEVERE, "[" + m_componentManager.getComponentMetatada().getClassName() + "] The callback method " + m_callbacks[i].getMetadata().getMethod() + " is not accessible : " + e.getMessage());
                } catch (InvocationTargetException e) {
                    Activator.getLogger().log(Level.SEVERE, "[" + m_componentManager.getComponentMetatada().getClassName() + "] The callback method " + m_callbacks[i].getMetadata().getMethod() + " has throws an exception : " + e.getMessage() + " -> " + e.getCause());
                }
            }
        }
        // Update to internal state
        m_state = state;
    }

}
