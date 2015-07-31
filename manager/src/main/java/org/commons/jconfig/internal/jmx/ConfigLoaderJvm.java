package org.commons.jconfig.internal.jmx;

import java.io.IOException;
import java.util.List;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;

import com.sun.tools.attach.VirtualMachineDescriptor;

/**
 * Exposes @MBeanServerConnection for the ConfigLoader VirtualMachine Object.<BR>
 * 
 * <B>Usage:</B><BR>
 * ConfigLoaderVM vm = new ConfigLoaderVM();<BR>
 * try {<BR>
 * vm.Attach();<BR>
 * } catch ( VirtualMachineException e ) {<BR>
 * //do something<BR>
 * }<BR>
 * MBeanServerConnection mbsc = vm.getMBeanServerConnection();<BR>
 * <BR>
 * 
 * //execute operations using the server connection<BR>
 * mbsc.doSomething();<BR>
 * <BR>
 * 
 * //Detach when done<BR>
 * vm.Detach();<BR>
 * 
 * <BR>
 * <B>Note</B> ConfigLoader application identified by
 * com.yahoo.configldr:type=configLoader MBean
 * 
 * @author aabed
 */
public class ConfigLoaderJvm extends VirtualMachine {

    /**
     * MBean Name identifying Config Loader application
     */
    public static final String CONFIG_LOADER_MBEAN_NAME = "com.yahoo.configldr:type=configLoader";

    /**
     * Attach to the ConfigLoader VM
     * 
     * @throws @VirtualMachineException
     */
    @Override
    public void attach() throws VirtualMachineException {

        // Iterate through the running vms ...
        List<VirtualMachineDescriptor> vms = com.sun.tools.attach.VirtualMachine.list();
        for (VirtualMachineDescriptor vmd : vms) {
            JMXConnector jmxc = null;
            try {
                ObjectName mbeanName = new ObjectName(CONFIG_LOADER_MBEAN_NAME);
                jmxc = connect(vmd);
                MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

                // Look for one that has the ConfigLoader MBean registered
                if (!mbsc.isRegistered(mbeanName)) {
                    jmxc.close();
                    continue;
                } else {
                    // Set the JMXConnector and the MBeanServerConnection
                    setJMXConnector(jmxc);
                    return;
                }
            } catch (MalformedObjectNameException e) {
                // ignore exception to allow gc to collect the object ad
                // continue to loop to find the correct jvm
            } catch (IOException e) {
                if (jmxc != null) {
                    try {
                        jmxc.close();
                    } catch (IOException e1) {

                    }
                }
                // ignore exception to allow gc to collect the object ad
                // continue to loop to find the correct jvm
            }
        }
        throw new VirtualMachineException("Unable to find config loader jvm.");
    }

    /**
     * invoke the "subscribeConfigs" MBean operation on the config Loader VM
     * 
     * @param appName
     * @throws InstanceNotFoundException
     * @throws MBeanException
     * @throws ReflectionException
     * @throws IOException
     */
    public void subscribeConfigs(final String appName) throws VirtualMachineException {

        try {
            ObjectName mbeanName = new ObjectName(ConfigLoaderJvm.CONFIG_LOADER_MBEAN_NAME);

            String[] params = { appName };
            String[] signature = { "java.lang.String" };
            getJMXConnector().getMBeanServerConnection().invoke(mbeanName, "subscribeConfigs", params, signature);

        } catch (MalformedObjectNameException e) {
            throw new VirtualMachineException(e);
        }catch (InstanceNotFoundException e) {
            throw new VirtualMachineException(e);
        } catch (MBeanException e) {
            throw new VirtualMachineException(e);
        } catch (ReflectionException e) {
            throw new VirtualMachineException(e);
        } catch (IOException e) {
            throw new VirtualMachineException(e);
        }
    }

    public void addNotificationListener(final NotificationListener listener) throws VirtualMachineException {
        try {
            getJMXConnector().getMBeanServerConnection().addNotificationListener(new ObjectName(ConfigLoaderJvm.CONFIG_LOADER_MBEAN_NAME), listener, null, null);
        } catch (InstanceNotFoundException e) {
            throw new VirtualMachineException(e);
        } catch (MalformedObjectNameException e) {
            throw new VirtualMachineException(e);
        } catch (IOException e) {
            throw new VirtualMachineException(e);
        }
    }
}
