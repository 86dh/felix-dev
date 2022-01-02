/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.felix.http.base.internal.javaxwrappers;

import java.util.Enumeration;

import org.jetbrains.annotations.NotNull;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionContext;

/**
 * http session context wrapper
 */
@SuppressWarnings("deprecation")
public class HttpSessionContextWrapper implements javax.servlet.http.HttpSessionContext {

    private final HttpSessionContext context;

    /**
     * Create new context
     * @param c Wrapped context
     */
    public HttpSessionContextWrapper(@NotNull final HttpSessionContext c) {
        this.context = c;
    }

    @Override
    public javax.servlet.http.HttpSession getSession(final String sessionId) {
        final HttpSession session = context.getSession(sessionId);
        if ( session != null ) {
            return new HttpSessionWrapper(session);
        }
        return null;
    }

    @Override
    public Enumeration<String> getIds() {
        return context.getIds();
    }
}
