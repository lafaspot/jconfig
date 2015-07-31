package org.commons.jconfig.internal.jmx;

import java.lang.management.ManagementFactory;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.commons.jconfig.internal.jmx.ConfigLoaderJvm;
import org.commons.jconfig.internal.jmx.ConfigManagerJmx;
import org.commons.jconfig.internal.jmx.VirtualMachineException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ConfigLoaderJvmTest {

    @Test
    public void testAttachConfigLoaderRunning() throws Throwable {

        // ConfigApp is an MBean class we have handy
        ConfigManagerJmx mbean = new ConfigManagerJmx("configLoader");

        //
        // register our ConfigApp MBean, and give it the
        // CONFIG_LOADER_MBEAN_NAME. So we are faking a configLoader VM.
        //
        ManagementFactory.getPlatformMBeanServer().registerMBean(mbean, new ObjectName(ConfigLoaderJvm.CONFIG_LOADER_MBEAN_NAME));

        ConfigLoaderJvm vm = new ConfigLoaderJvm();

        vm.attach();
        Assert.assertNotNull(vm.getJMXConnector().getMBeanServerConnection());
        vm.close();
    }

    @Test(expectedExceptions = VirtualMachineException.class)
    public void testAttachConfigLoaderNotRunning() throws VirtualMachineException, MalformedObjectNameException, NullPointerException, MBeanRegistrationException, InstanceNotFoundException {
        ObjectName mbean = new ObjectName(ConfigLoaderJvm.CONFIG_LOADER_MBEAN_NAME);
        if(ManagementFactory.getPlatformMBeanServer().isRegistered(mbean)) {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(mbean);
        }
        ConfigLoaderJvm vm = new ConfigLoaderJvm();
        vm.attach();
    }
}
