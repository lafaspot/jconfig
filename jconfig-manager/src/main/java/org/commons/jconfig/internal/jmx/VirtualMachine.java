package org.commons.jconfig.internal.jmx;

import java.io.IOException;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.sun.tools.attach.VirtualMachineDescriptor;

/**
 * Encapsulates a VirtualMachine Object using the @com.sun.tools.attach.VirtualMachine api.<BR>
 * 
 * <B>Usage:</B><BR>
 *      //Grab an VM Descriptor<BR>
 *      VirtualMachineDescriptor vmd = com.sun.tools.attach.VirtualMachine.list().get(0);<BR><BR>
 *
 *      //Connect to it, and set the MBean Server Connection<BR>
 *      VirtualMachine vm = new VirtualMachine();<BR>
 *      vm.setJMXConnector(VirtualMachine.connect(vmd));<BR>
 *      vm.setMBeanServerConnection(vm.getJMXConnector().getMBeanServerConnection());<BR><BR>
 * 
 *      //execute operations using the server connection<BR>
 *      vm.getMBeanServerConnection();<BR><BR>
 * 
 *      //Detach when done<BR>
 *      vm.Detach();<BR>
 */
public class VirtualMachine {

    /**
     * Detaches from VM (if attached)
     */
    @Override
    protected void finalize() throws Throwable {
        close();
    }

    /**
     * Not implemented; Real implementation by derived class.
     * @throws VirtualMachineException
     */
    public void attach() throws VirtualMachineException {
        throw new VirtualMachineException("Attach Method Not Implemented");
    }

    /**
     * closes @JMXConnector
     * @throws VMException
     */
    public void close() throws VirtualMachineException {
        if ( jmxc != null ) {
            try {
                jmxc.close();
                jmxc = null;
            } catch (IOException e) {
                jmxc = null;
                throw new VirtualMachineException(e);
            }
        }
    }

    /**
     * Static method, attaches to given @VirtualMachineDescriptor and returns the corresponding @JMXConnector
     * 
     * @param vmd @VirtualMachineDescriptor to attach to
     * @return @JMXConnector. Null, if connection fails
     * 
     *  <BR><B>Note:</B><BR> Caller is responsible for calling close() on returned @JMXConnector
     * 
     * @throws VMException
     */
    public static JMXConnector connect(final VirtualMachineDescriptor vmd) throws VirtualMachineException {

        com.sun.tools.attach.VirtualMachine vm;
        try {
            vm = com.sun.tools.attach.VirtualMachine.attach(vmd.id());

            String connectorAddr = vm.getAgentProperties().getProperty("com.sun.management.jmxremote.localConnectorAddress");

            if (connectorAddr == null) {
                String agent = vm.getSystemProperties().getProperty("java.home") + "/lib/management-agent.jar";

                vm.loadAgent(agent);
                connectorAddr = vm.getAgentProperties().getProperty("com.sun.management.jmxremote.localConnectorAddress");
            }

            JMXServiceURL url = new JMXServiceURL(connectorAddr);
            return JMXConnectorFactory.connect(url, null);

        } catch (Exception e) {
            throw new VirtualMachineException(e);
        }
    }

    /**
     * Set the @JMXConnector for this Virtual Machine instance.
     *  Also sets the @MBeanServerConnection for this Virtual Machine instance
     * @param jmxc A JMXConnector instance
     * @throws VMException
     */
    public void setJMXConnector(final JMXConnector jmxc) throws VirtualMachineException {
        this.jmxc = jmxc;
    }

    /**
     * 
     * @return the JMXConnector for this VirtaulMachine Instance.  Can be null if
     *  setJMXConnector not called first.
     */
    public JMXConnector getJMXConnector() {
        return jmxc;
    }

    /**
     * the jmx connector
     */
    private JMXConnector jmxc = null;

}

