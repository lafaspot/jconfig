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

package org.commons.jconfig.datatype;

public class TypeFormatException extends IllegalArgumentException {

    private static final long serialVersionUID = 7005469064820675831L;

    public TypeFormatException() {
        super();
        // TODO Auto-generated constructor stub
    }

    public TypeFormatException(final String message, final Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public TypeFormatException(final String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public TypeFormatException(final Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    private String keyName = null;

    /**
     * Set the config key name
     * 
     * @param value
     *            Config key name
     */
    public void setKeyName(final String keyName) {
        this.keyName = keyName;
    }

    /**
     * @see java.lang.Throwable#getMessage()
     */
    @Override
    public String getMessage() {
        return "Invalid key '" + keyName + "' value. " + super.getMessage();
    }

}
