package org.commons.jconfig.internal.jmx;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;

import org.apache.log4j.Logger;
import org.commons.jconfig.config.ConfigRuntimeException;

import com.sun.tools.attach.VirtualMachineDescriptor;

/**
 * Exposes @MBeanServerConnection for a VirtualMachine with a registered ConfigManagerMXBean.<BR>
 * 
 * <B>Usage:</B><BR>
 * 
 *       //Attach to a VM with a registered ConfigManagerMXBean with the objectName attribute of "myApplication"<BR>
 *       ConfigManagerVM vm = new ConfigManagerVM("myApplicatoin");<BR>
 *       try {<BR>
 *              vm.Attach();<BR>
 *       } catch ( VirtualMachineException e ) {<BR>
 *              //do something<BR>
 *       }<BR>
 *       MBeanServerConnection mbsc = vm.getMBeanServerConnection();<BR><BR>
 * 
 *       //execute operations using the server connection<BR>
 *       mbsc.doSomething();<BR><BR>
 * 
 *       //Detach<BR>
 *       vm.Detach();<BR>
 * 
 * <BR><B>Note</B> ConfigLoader application identified by com.yahoo.configldr:type=configLoader MBean
 *
 * @author aabed
 */
public class ConfigManagerJvm extends VirtualMachine {

    /**
     * MBean Name identifying VirtualMachine with a registered @ConfigManagerMXBean
     */
    public static final String CONFIG_MGR_MBEAN_NAME = "com.yahoo.configmgr:type=ConfigManager,appName=";
    public static final String CONFIG_MGR_MBEAN_SEARCH_PATTERN = "com.yahoo.configmgr:type=ConfigManager,appName=*";
    public static final String APPNAME_KEY = "appName";
    private final ObjectName objectName;
    private String vmId = "-1";

    /**
     * Our Logger
     */
    private static final Logger logger = Logger.getLogger(ConfigLoaderJvm.class);

    public ConfigManagerJvm(final ObjectName bname) {
        if (bname == null) {
            throw new IllegalArgumentException("Config manager ObjectName cannot be null.");
        }
        if (!bname.getCanonicalName().contains(APPNAME_KEY)) {
            throw new IllegalArgumentException("Config manager ObjectName should contain appname key.");
        }
        this.objectName = bname;
    }
    
    /**
     * Attach to the VM with a registered @ConfigManagerMXBean who's name attribute matches
     * objectName.
     * @throws @VirtualMachineException
     */
    @Override
    public void attach() throws VirtualMachineException {

        // Iterate through the running vms ...
        List<VirtualMachineDescriptor> vms = com.sun.tools.attach.VirtualMachine.list();
        for (VirtualMachineDescriptor vmd : vms) {
            JMXConnector jmxc = null;
            try {
                jmxc = connect(vmd);
                MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
                // Look for one that has the ConfigLoader MBean registered
                if (!mbsc.isRegistered(objectName)) {
                    jmxc.close();
                    continue;
                } else {
                    //
                    // now does it match the objectName we are looking for
                    //
                    ConfigManagerJmxMXBean managerBean = JMX.newMBeanProxy(mbsc, objectName,
                            ConfigManagerJmxMXBean.class, true);

                    if (managerBean.getVMName().equals(objectName.getKeyProperty(APPNAME_KEY))) {
                        vmId = vmd.id();
                        // Set the JMXConnector and the MBeanServerConnection
                        setJMXConnector(jmxc);
                        return;
                    }
                }
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
        throw new VirtualMachineException("Unable to find config manager jvm with appname: " + objectName);
    }
    
    /**
     * @return Set of {@link ConfigManagerJvm} all in attached state
     */
    public static Set<ConfigManagerJvm> find() {
        Set<ConfigManagerJvm> jvms = new HashSet<ConfigManagerJvm>();
        JMXConnector jmxc = null;

        List<VirtualMachineDescriptor> vms = com.sun.tools.attach.VirtualMachine.list();
        ObjectName mbeanName;
        try {
            mbeanName = new ObjectName(CONFIG_MGR_MBEAN_SEARCH_PATTERN);
        } catch (MalformedObjectNameException e) {
            throw new ConfigRuntimeException("Fail to instantiate config manager object bean ", e);
        }

        // Iterate through the running vms ...
        for (VirtualMachineDescriptor vmd : vms) {
            try {
                jmxc = connect(vmd);
                MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

                Set<ObjectName> registeredConfigManagers = new TreeSet<ObjectName>(mbsc.queryNames(mbeanName, null));
                if (registeredConfigManagers.isEmpty()) {
                    jmxc.close();
                    continue;
                } else {
                    jmxc.close();
                    for (ObjectName bname : registeredConfigManagers) {
                        ConfigManagerJvm vm = new ConfigManagerJvm(bname);
                        vm.setJMXConnector(connect(vmd));
                        vm.setVmId(vmd.id());
                        jvms.add(vm);
                    }
                }
            } catch (VirtualMachineException e) {
                logger.debug("Fail fetching config manager vms ", e);
            } catch (IOException e) {
                logger.debug("Fail fetching config manager vms ", e);
            }
        }
        return jvms;
    }

    public String getVmId() {
        return vmId;
    }
    
    public void setVmId(final String id) {
        vmId = id;
    }
    
    public ObjectName getObjectName() {
        return objectName;
    }
    
    @Override
    public String toString() {
        return "[" + vmId + ", " + objectName + "]";
    } 

}
