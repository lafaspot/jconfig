package org.commons.jconfig.config;


/**
 * Config RuntimeException
 * 
 * Used to report config failures that are not recoverable.
 * 
 * @author lafa
 * 
 */
public class ConfigRuntimeException extends RuntimeException {

    /**
     * @see RuntimeException#RuntimeException(String)
     * @param message
     */
    public ConfigRuntimeException(final String message) {
        super(message);
    }

    /**
     * @see RuntimeException#RuntimeException(String, Throwable)
     * @param message
     * @param cause
     */
    public ConfigRuntimeException(final String message, final Throwable e) {
        super(message, e);
    }

    private static final long serialVersionUID = -7697153472691616793L;

}
