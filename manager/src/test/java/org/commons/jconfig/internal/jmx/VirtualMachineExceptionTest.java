package org.commons.jconfig.internal.jmx;

import org.commons.jconfig.internal.jmx.VirtualMachineException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class VirtualMachineExceptionTest {

    @Test
    public void VirtualMachineException() {
        
        Throwable t = new Throwable();
        VirtualMachineException ex = new VirtualMachineException(t);
        Assert.assertEquals(ex.getCause(), t);
        
        ex = new VirtualMachineException("some exception");
        Assert.assertEquals(ex.getMessage(), "some exception");
        
        ex = new VirtualMachineException("some exception", t);
        Assert.assertEquals(ex.getCause(), t);
        Assert.assertEquals(ex.getMessage(), "some exception");
    }
}