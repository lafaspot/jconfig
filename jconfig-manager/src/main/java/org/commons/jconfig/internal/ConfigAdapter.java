package org.commons.jconfig.internal;

import org.commons.jconfig.config.ConfigException;


public interface ConfigAdapter<T> {
    void loadValue(ConfigManagerCache jmxloader)throws ConfigException;
}