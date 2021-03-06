package org.commons.jconfig.internal.jmx;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.commons.jconfig.internal.jmx.ConfigManagerJmx;
import org.commons.jconfig.internal.jmx.ConfigManagerJvm;
import org.commons.jconfig.internal.jmx.VirtualMachineException;
import org.testng.annotations.Test;

public class ConfigManagerJvmTest {

    @Test(expectedExceptions = VirtualMachineException.class)
    public void testAttachConfigAppNotRunning() throws VirtualMachineException, MalformedObjectNameException {

        ConfigManagerJvm vm = new ConfigManagerJvm(new ObjectName("com.yahoo.configmgr:type=ConfigManager,appName=foo"));
        vm.attach();
    }

    @Test
    public void testAttachConfigAppRunning() throws MalformedObjectNameException, NullPointerException, MBeanRegistrationException, InstanceNotFoundException,
            InstanceAlreadyExistsException, NotCompliantMBeanException, VirtualMachineException {

        //
        // register our ConfigApp MBean, and give it the CONFIG_MGR_MBEAN_NAME.
        //
        ConfigManagerJmx mbean = new ConfigManagerJmx("foo");
        ObjectName mbeanName = new ObjectName(ConfigManagerJvm.CONFIG_MGR_MBEAN_NAME + "foo");
        if (ManagementFactory.getPlatformMBeanServer().isRegistered(mbeanName)) {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(mbeanName);
        }
        ManagementFactory.getPlatformMBeanServer().registerMBean(mbean, mbeanName);

        ConfigManagerJvm vm = new ConfigManagerJvm(new ObjectName("com.yahoo.configmgr:type=ConfigManager,appName=foo"));

        vm.attach();
        vm.close();
        ManagementFactory.getPlatformMBeanServer().unregisterMBean(mbeanName);
    }

    @Test(expectedExceptions = VirtualMachineException.class)
    public void testAttachConfigAppWrongRunning() throws MalformedObjectNameException, NullPointerException, MBeanRegistrationException, InstanceNotFoundException,
            InstanceAlreadyExistsException, NotCompliantMBeanException, VirtualMachineException {

        //
        // register our ConfigApp MBean, and give it the
        // CONFIG_LOADER_MBEAN_NAME. So we are faking a configLoader VM.
        //
        ConfigManagerJmx mbean = new ConfigManagerJmx("ConfigAppUnitTest");
        ObjectName mbeanName = new ObjectName(ConfigManagerJvm.CONFIG_MGR_MBEAN_NAME + "ConfigAppUnitTest");
        if (ManagementFactory.getPlatformMBeanServer().isRegistered(mbeanName)) {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(mbeanName);
        }
        ManagementFactory.getPlatformMBeanServer().registerMBean(mbean, mbeanName);
        ConfigManagerJvm vm = new ConfigManagerJvm(new ObjectName("com.yahoo.configmgr:type=ConfigManager,appName=foo"));
        vm.attach();
    }
}
