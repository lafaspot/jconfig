package org.commons.jconfig.internal;

/**
 * Exception class that should be used by the classes that implement the Worker
 * interface to fail the execution of a worker.
 * 
 * @author lafa
 * 
 */
@SuppressWarnings("serial")
public class WorkerException extends Exception {

    /**
     * Constructs a new exception with null as its detail message.
     */
    public WorkerException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * 
     * @param message
     * @param cause
     */
    public WorkerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified detail message.
     * 
     * @param message
     */
    public WorkerException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified cause and a detail message
     * of (cause==null ? null : cause.toString()) (which typically contains the
     * class and detail message of cause).
     * 
     * @param cause
     */
    public WorkerException(final Throwable cause) {
        super(cause);
    }

}
