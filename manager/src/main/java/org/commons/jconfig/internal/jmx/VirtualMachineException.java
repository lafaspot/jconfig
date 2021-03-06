package org.commons.jconfig.internal.jmx;

/**
 * Encapsulates config jvm VirtualMachine exceptions
 *  
 * @author aabed
 */
@SuppressWarnings("serial")
public class VirtualMachineException extends Exception {

    public VirtualMachineException(Throwable cause) {
        super(cause);
    }
    
    public VirtualMachineException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public VirtualMachineException(String message) {
        super(message);
    }
}
