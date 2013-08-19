package org.commons.jconfig.internal.jmx;

import java.io.IOException;
import java.rmi.UnmarshalException;
import java.util.List;

import javax.management.AttributeNotFoundException;
import javax.management.Descriptor;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeMBeanException;
import javax.management.remote.JMXConnector;

import org.apache.log4j.Logger;
import org.commons.jconfig.serializers.ObjectToJsonConverter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class JmxManager extends VirtualMachine {
    private final Logger logger = Logger.getLogger(JmxManager.class);

    public JsonObject listAllJvms() throws VirtualMachineException {
        List<VirtualMachineDescriptor> vms = com.sun.tools.attach.VirtualMachine.list();
        JsonObject vmList = new JsonObject();
        new Gson();
        // Iterate through the running vms ...
        for (VirtualMachineDescriptor vmd : vms) {
            JsonObject jvm = new JsonObject();
            jvm.add("Process", getVirtualMachineDescriptor(vmd));
            vmList.add(vmd.id(), jvm);
        }
        return vmList;
    }

    private JsonObject getVirtualMachineDescriptor(final VirtualMachineDescriptor vmd) {
        JsonObject process = new JsonObject();
        process.addProperty("Pid", vmd.id());
        process.addProperty("Name", vmd.displayName());
        return process;
    }

    public JsonObject readApplicationMbeans() throws VirtualMachineException {
        long time = System.currentTimeMillis();
        logger.debug("Reading application Mbeans " + time);
        JsonObject vmList = new JsonObject();
        JMXConnector jmxc = null;
        MBeanServerConnection mbeanServerConnection = null;

        List<VirtualMachineDescriptor> vms = com.sun.tools.attach.VirtualMachine.list();

        // Iterate through the running vms
        for (VirtualMachineDescriptor vmd : vms) {
            try {
                jmxc = connect(vmd);
                mbeanServerConnection = jmxc.getMBeanServerConnection();
                JsonObject process = getVirtualMachineDescriptor(vmd);
                process.add("Time", new JsonPrimitive(time));
                process.add("MBeans", getMBeans(mbeanServerConnection));
                jmxc.close();
                vmList.add(vmd.id(), process);
            } catch (Exception e) {
                logger.error("Error connection to jvm " + vmd.id(), e);
                if (jmxc != null) {
                    try {
                        jmxc.close();
                    } catch (IOException e1) {
                        logger.error("Error closing JMXConnector for vm " + vmd.id() , e);
                    }
                }
            }
        }

        return vmList;
    }

    /**
     * @param mbeanServerConnection
     * @return
     * @throws IOException
     */
    private JsonObject getMBeans(MBeanServerConnection mbeanServerConnection) throws IOException {
        JsonObject mBeans = new JsonObject();
        for (ObjectName name : mbeanServerConnection.queryNames(null, null)) {
            if (mBeans.get(name.getDomain()) == null) {
                mBeans.add(name.getDomain(), new JsonObject());
            }

            JsonObject bean = new JsonObject();
            mBeans.getAsJsonObject(name.getDomain()).add(name.getCanonicalName(), bean);
            bean.addProperty("ObjectName", name.getCanonicalName());
            MBeanInfo bInfo;
            try {
                bInfo = mbeanServerConnection.getMBeanInfo(name);
                bean.addProperty("ClassName", bInfo.getClassName());
                bean.addProperty("Description", bInfo.getDescription());
                bean.addProperty("Description", bInfo.getDescription());
                Descriptor bDescriptor = bInfo.getDescriptor();
                JsonObject descriptor = new JsonObject();
                for (String field : bDescriptor.getFieldNames()) {
                    Object value = bDescriptor.getFieldValue(field);
                    // skip null fields
                    if (value != null) {
                        descriptor.addProperty(field, value.toString());
                    }
                }

                bean.add("Descriptor", descriptor);

                JsonObject attrData = new JsonObject();
                for (MBeanAttributeInfo attrInfo : bInfo.getAttributes()) {
                    try {
                        if (attrInfo.isReadable()) {
                            Object value = mbeanServerConnection.getAttribute(name, attrInfo.getName());
                            if (value != null) {
                                attrData.add(attrInfo.getName(), new ObjectToJsonConverter().extractObject(value));
                            }
                        }
                    } catch (RuntimeMBeanException e) {
                        if (logger.isTraceEnabled())
                            logger.trace(
                                    " Name: " + attrInfo.getName() + " Attribute Desc: " + attrInfo.getDescription(),
                                e);
                    } catch (UnsupportedOperationException e) {
                        if (logger.isTraceEnabled())
                            logger.trace(
                                    " Name: " + attrInfo.getName() + " Attribute Desc: " + attrInfo.getDescription(), e);
                    } catch (AttributeNotFoundException e) {
                        if (logger.isTraceEnabled())
                            logger.trace(
                                    " Name: " + attrInfo.getName() + " Attribute Desc: " + attrInfo.getDescription(), e);
                    } catch (MBeanException e) {
                        if (logger.isTraceEnabled())
                            logger.trace(
                                    " Name: " + attrInfo.getName() + " Attribute Desc: " + attrInfo.getDescription(), e);
                    } catch (UnmarshalException e) {
                        if (logger.isTraceEnabled())
                            logger.trace(
                                    " Name: " + attrInfo.getName() + " Attribute Desc: " + attrInfo.getDescription(), e);
                    }
                }
                bean.add("Attributes", attrData);
            } catch (InstanceNotFoundException e) {
                logger.error(e);
            } catch (IntrospectionException e) {
                logger.error(e);
            } catch (ReflectionException e) {
                logger.error(e);
            }

        }
        return mBeans;
    }

}
