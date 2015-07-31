package org.commons.jconfig.config;

/**
 * Config Exception
 * 
 */
@SuppressWarnings("serial")
public class ConfigException extends Exception {

    public ConfigException(final Throwable cause) {
        super(cause);
    }

    public ConfigException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ConfigException(final String message) {
        super(message);
    }
}
