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

import org.apache.felix.ipojo.ComponentManagerImpl;

/**
 * Lifecycle callback metadata.
 * @author <a href="mailto:felix-dev@incubator.apache.org">Felix Project Team</a>
 */
public class LifecycleCallbackMetadata {

    /**
     * Initial state of the transition.
     */
    private int m_initialState;

    /**
     * Final state of the transition.
     */
    private int m_finalState;

    /**
     * Method to call.
     */
    private String m_method;

    /**
     * is the method a static method ?
     */
    private boolean m_isStatic;

    // Constructor

    /**
     * Constructor.
     * @param initialState : initial state
     * @param finalState : final state
     * @param method : method name
     * @param isStatic : is the method a static method ?
     */
    public LifecycleCallbackMetadata(String initialState, String finalState, String method, boolean isStatic) {
        if (initialState.equals("VALID")) { m_initialState = ComponentManagerImpl.VALID; }
        if (initialState.equals("INVALID")) { m_initialState = ComponentManagerImpl.INVALID; }
        if (finalState.equals("VALID")) { m_finalState = ComponentManagerImpl.VALID; }
        if (finalState.equals("INVALID")) { m_finalState = ComponentManagerImpl.INVALID; }

        m_method = method;
        m_isStatic = isStatic;
    }

    // Getters

    /**
     * @return Returns the m_finalState.
     */
    public int getFinalState() {
        return m_finalState;
    }

    /**
     * @return Returns the m_initialState.
     */
    public int getInitialState() {
        return m_initialState;
    }

    /**
     * @return Returns the m_isStatic.
     */
    public boolean isStatic() {
        return m_isStatic;
    }

    /**
     * @return Returns the m_method.
     */
    public String getMethod() {
        return m_method;
    }

}
