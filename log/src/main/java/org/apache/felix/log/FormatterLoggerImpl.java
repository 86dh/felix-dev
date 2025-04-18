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
package org.apache.felix.log;

import java.util.Formatter;
import java.util.Locale;

import org.osgi.framework.Bundle;
import org.osgi.service.log.FormatterLogger;

public class FormatterLoggerImpl extends LoggerImpl implements FormatterLogger {

    public FormatterLoggerImpl(
        final String name, final Bundle bundle, final Log log, final LoggerAdminImpl loggerAdmin) {

        super(name, bundle, log, loggerAdmin);
    }

    @Override
    String format(String format, LogParameters logParameters) {
        StringBuilder sb = new StringBuilder();

        try (Formatter formatter = new Formatter(sb, Locale.getDefault())) {
            formatter.format(format, logParameters.args);

            return sb.toString();
        }
    }

}
