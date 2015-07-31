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
package org.commons.jconfig.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.commons.jconfig.datatype.ValueType;


/**
 * ConfigGet annotation is used to identify a method to be called to retrieve
 * the current value for this configuration key. This annotation also requires
 * the description, type and default value for this key.
 * 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ConfigGet {

    /**
     * Description of functionality this config Key is used for.
     * 
     * @return
     */
    String description();

    /**
     * Type of value for this config key, see {@link ValueType} for all types
     * that be can used.
     * 
     * @return
     */
    ValueType type();

    /**
     * Default value in a string format in the same format as it would appear in
     * a config file.
     * 
     * @return
     */
    String defaultValue();
}
