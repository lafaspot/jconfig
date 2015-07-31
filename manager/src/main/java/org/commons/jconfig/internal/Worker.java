package org.commons.jconfig.internal;


public interface Worker<T> {
    /**
     * This method will be called multiple times. Make sure to do your work and
     * return as soon as possible.
     * 
     * @return true when Worker is done return false otherwise
     * @throws WorkerException
     */
    boolean execute() throws WorkerException;

    /**
     * Should only be called after execute method returns true. In case there is
     * no errors this method will return null.
     * 
     * @return
     */
    Exception getCause();

    /**
     * Should only be called after execute method returns true. Returns true is
     * the worker failed during the execution.
     * 
     * @return
     */
    boolean hasErrors();

    /**
     * getData will return null in case of error or when no data is produced.
     * Should only be called after execute method returns true.
     * 
     * @return the data
     */
    T getData();
}
