package org.commons.jconfig.internal.jmx;

import java.util.List;

import org.commons.jconfig.internal.jmx.VirtualMachine;
import org.commons.jconfig.internal.jmx.VirtualMachineException;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sun.tools.attach.VirtualMachineDescriptor;

public class VirtualMachineTest {

    @Test
    public void testConnect() throws VirtualMachineException {

        // test connect
        List<VirtualMachineDescriptor> vms = com.sun.tools.attach.VirtualMachine.list();

        for (VirtualMachineDescriptor vmd : vms) {
            if (vmd.displayName().contains("RemoteTestNG")) {

                VirtualMachine vm = new VirtualMachine();
                vm.setJMXConnector(VirtualMachine.connect(vmd));

                // Should be able to connect to our self
                Assert.assertNotNull(vm.getJMXConnector());

                // should able to get a mbsc

                // After detaching...
                vm.close();
                Assert.assertNull(vm.getJMXConnector());

                // Should be able to call Detach multiple times
                vm.close();
            }
        }
    }
}
