package org.commons.jconfig.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;

import org.commons.jconfig.annotations.ConfigLoaderAdapter;
import org.commons.jconfig.annotations.ConfigResource;
import org.commons.jconfig.annotations.ConfigResourceId;
import org.commons.jconfig.config.ConfigManager;


/**
 * For reference use
 * http://blogs.oracle.com/jmxetc/entry/dynamicmbeans,_modelmbeans,_and_pojos...
 * 
 * @author lafa
 * 
 */
public class ConfigMBean implements DynamicMBean {
    
    // Utilitary tuple
    private class Tuple {
        public Tuple(final Object object, final Method m) {
            instance = object;
            method = m;
        }
        Object instance;
        Method method;
    };
    
    private final Map<String, Tuple> getters;
    private final Map<String, Tuple> setters;
    private final Set<Tuple> operations;
    private final Class<?> configClazz;
    private final MBeanInfo info;
    private final ConfigManager manager;
    /** Encapsulation to hold {@link ConfigLoaderAdapter} value and insert it to MBean attribute */
    private final LoaderAdapter adapter;
    /** Encapsulation to holder {@link ConfigResource} value and insert it to MBean attribute */
    private final ConfigResourceAttr configResource;

    /**
     * Creates a new instance of ConfigObjectMBean
     * 
     * @param obj
     */
    public ConfigMBean(@Nonnull final ConfigManager configManager, @Nonnull final Class<?> configClazz) {
        manager = configManager;
        getters = new LinkedHashMap<String,Tuple>();
        setters = new LinkedHashMap<String,Tuple>();
        operations = new LinkedHashSet<Tuple>();
        this.configClazz = configClazz;
        adapter = new LoaderAdapter(configClazz);
        configResource = new ConfigResourceAttr(configClazz);
        try {
            info = initialize();
        } catch (IntrospectionException ex) {
            throw new IllegalArgumentException(configClazz.getName(),ex);
        }
    }

    private MBeanInfo initialize() throws IntrospectionException {
        final List<MBeanAttributeInfo> attributesInfo =
                new ArrayList<MBeanAttributeInfo>();
        final List<MBeanOperationInfo> operationsInfo =
                new ArrayList<MBeanOperationInfo>();
        final Set<String> attributesName = new HashSet<String>();
        final ArrayList<Tuple> ops = new ArrayList<Tuple>();
        for (Method m:configClazz.getMethods()) {
            if (m.getDeclaringClass().equals(Object.class)) continue;
            if (m.getName().startsWith("get") &&
                    !m.getName().equals("get") &&
                    !m.getName().equals("getClass") &&
                    m.getParameterTypes().length == 0 &&
                    m.getReturnType() != void.class) {
                
                if (m.isAnnotationPresent(ConfigResourceId.class)) {
                    getters.put(m.getAnnotation(ConfigResourceId.class).value(), new Tuple(null,m));
                } else {
                    getters.put(m.getName().substring(3), new Tuple(null,m));    
                }
            } else if (m.getName().startsWith("is") &&
                    !m.getName().equals("is") &&
                    m.getParameterTypes().length == 0 &&
                    m.getReturnType() == boolean.class) {
                getters.put(m.getName().substring(2),new Tuple(null,m));
            } else if (m.getName().startsWith("set") &&
                    !m.getName().equals("set") &&
                    m.getParameterTypes().length == 1 &&
                    m.getReturnType().equals(void.class)) {
                
                if (m.isAnnotationPresent(ConfigResourceId.class)) {
                    setters.put(m.getAnnotation(ConfigResourceId.class).value(), new Tuple(null,m));
                } else {
                    setters.put(m.getName().substring(3), new Tuple(null,m));    
                }
            } else {
                ops.add(new Tuple(null,m));
            }
        }

        attributesName.addAll(getters.keySet());
        attributesName.addAll(setters.keySet());

        // Register for ConfigLoaderAdapter
        getters.put("ConfigLoaderAdapter", new Tuple(adapter, adapter.getGetMethod()));
        getters.put("ConfigResource", new Tuple(configResource, configResource.getGetMethod()));
        MBeanAttributeInfo adapterAttr = new MBeanAttributeInfo("ConfigLoaderAdapter", "ConfigLoaderAdapter",
                adapter.getGetMethod(), adapter.getSetMethod());
        MBeanAttributeInfo configResourceAttr = new MBeanAttributeInfo("ConfigResource", "ConfigResource",
                configResource.getGetMethod(), configResource.getSetMethod());
        attributesInfo.add(adapterAttr);
        attributesInfo.add(configResourceAttr);
        
        for (String attrName : attributesName) {
            final Tuple get = getters.get(attrName);
            Tuple set = setters.get(attrName);
            if (get != null && set != null &&
                    get.method.getReturnType() != set.method.getParameterTypes()[0]) {
                set = null;
                ops.add(setters.remove(attrName));
            }
            final MBeanAttributeInfo mbi =  new MBeanAttributeInfo(attrName,attrName, get == null ? null:get.method, set == null ? null:set.method);
            if (mbi != null) attributesInfo.add(mbi);
        }

        for (Tuple t:ops) {
            if(t.method.getDeclaringClass()!= Object.class) {
                operations.add(t);
                operationsInfo.add(new MBeanOperationInfo(t.method.getName(),t.method));
            }
        }
        
        
        final MBeanAttributeInfo[] attrsOI = attributesInfo.toArray(new MBeanAttributeInfo[attributesInfo.size()]);
        final MBeanOperationInfo[] opsOI = operationsInfo.toArray(new MBeanOperationInfo[operationsInfo.size()]);
        return new MBeanInfo(configClazz.getName(), configClazz.getName(),attrsOI,null,opsOI,null);
    }

    @Override
    public Object getAttribute(final String attribute)
            throws AttributeNotFoundException, MBeanException, ReflectionException {
        final Tuple get = getters.get(attribute);
        if (get == null)
            throw new AttributeNotFoundException("Fail to find method: " + configClazz.getName() + ".get" + attribute);
        try {
            return get.method.invoke(get.instance);
        } catch (IllegalArgumentException ex) {
            throw new ReflectionException(ex);
        } catch (InvocationTargetException ex) {
            final Throwable cause = ex.getCause();
            if (cause instanceof Exception)
                throw new MBeanException((Exception) cause);
            throw new RuntimeErrorException((Error) cause);
        } catch (IllegalAccessException ex) {
            throw new ReflectionException(ex);
        }
    }
    
    @Override
    public AttributeList getAttributes(final String[] attributes) {
        if (attributes == null) return new AttributeList();
        final List<Attribute> result =
                new ArrayList<Attribute>(attributes.length);
        for (String attr : attributes) {
            try {
                result.add(new Attribute(attr, getAttribute(attr)));
            } catch (Exception x) {
                continue;
            }
        }
        return new AttributeList(result);
    }

    @Override
    public void setAttribute(final Attribute attribute)
            throws AttributeNotFoundException, InvalidAttributeValueException,
            MBeanException, ReflectionException {
        final Tuple set = setters.get(attribute.getName());
        if (set == null)
            throw new AttributeNotFoundException(attribute.getName());
        ConfigManagerCache adapter = manager.getCache();
        if (attribute.getValue() instanceof String) {
            adapter.insertValue(configClazz.getName(), (String) attribute.getValue());
        } else {
            throw new IllegalArgumentException("Attribute " + configClazz.getName() + ".set" + attribute.getName() + " has value in different datatype " + attribute.getValue());
        }
    }

    @Override
    public AttributeList setAttributes(final AttributeList attributes) {
        if (attributes == null) return new AttributeList();
        final List<Attribute> result =
                new ArrayList<Attribute>(attributes.size());

        for (Object item : attributes) {
            final Attribute attribute = (Attribute)item;
            final String name = attribute.getName();
            try {
                setAttribute(attribute);
                result.add(new Attribute(name, getAttribute(name)));
            } catch (Exception x) {
                continue;
            }
        }
        return new AttributeList(result);
    }

    @Override
    public Object invoke(final String actionName, Object[] params, String[] signature)
            throws MBeanException, ReflectionException {
        Tuple toInvoke = null;
        if (params == null) params = new Object[0];
        if (signature == null) signature = new String[0];
        for (Tuple t : operations) {
            if (!t.method.getName().equals(actionName)) continue;
            final Class<?>[] sig = t.method.getParameterTypes();
            if (sig.length == params.length) {
                if (sig.length == 0) toInvoke=t;
                else if (signature.length == sig.length) {
                    toInvoke = t;
                    for (int i=0;i<sig.length;i++) {
                        if (!sig[i].getName().equals(signature[i])) {
                            toInvoke = null;
                            break;
                        }
                    }
                }
            }
            if (toInvoke != null) break;
        }
        if (toInvoke == null)
            throw new ReflectionException(new NoSuchMethodException(actionName));
        try {
            return toInvoke.method.invoke(toInvoke.instance,params);
        } catch (IllegalArgumentException ex) {
            throw new ReflectionException(ex);
        } catch (InvocationTargetException ex) {
            final Throwable cause = ex.getCause();
            if (cause instanceof Exception)
                throw new MBeanException((Exception)cause);
            throw new RuntimeErrorException((Error)cause);
        } catch (IllegalAccessException ex) {
            throw new ReflectionException(ex);
        }
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return info;
    }

}