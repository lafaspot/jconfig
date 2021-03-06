/*
 * Copyright 2011 Yahoo! Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commons.jconfig.config;

/**
 * Exception that is used when a Key is not found by the ConfigManager
 * 
 * @author lafa
 */
public class KeyNotFound extends RuntimeException {

    private static final long serialVersionUID = -4367462076845034721L;

    /**
     * @see RuntimeException#RuntimeException()
     */
    public KeyNotFound() {
    }

    /**
     * @see RuntimeException#RuntimeException(String)
     * @param message
     */
    public KeyNotFound(final String message) {
        super(message);
    }

    /**
     * @see RuntimeException#RuntimeException(Throwable)
     * @param cause
     */
    public KeyNotFound(final Throwable cause) {
        super(cause);
    }

    /**
     * @see RuntimeException#RuntimeException(String, Throwable)
     * @param message
     * @param cause
     */
    public KeyNotFound(final String message, final Throwable cause) {
        super(message, cause);
    }

}
