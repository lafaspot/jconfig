package org.commons.jconfig.internal;

import java.lang.reflect.Method;

import org.commons.jconfig.annotations.ConfigResource;


public class ConfigResourceAttr {
    private final Class<?> configClazz;
    private final String configResource;

    /**
     * @param resource
     */
    public ConfigResourceAttr(Class<?> resource) {
        configClazz = resource;
        ConfigResource anno = configClazz.getAnnotation(ConfigResource.class);
        if (anno == null) {
            configResource = "";
        } else {
            configResource = anno.name();
        }
    }

    public String getConfigResource() {
        return configResource;
    }

    /**
     * @return
     * @throws SecurityException
     */
    public Method getGetMethod() throws SecurityException {
        Class<?>[] params = new Class<?>[0];
        try {
            return ConfigResourceAttr.class.getMethod("getConfigResource", params);
        } catch (NoSuchMethodException e) {
            // never happens
        }
        return null;
    }

    /**
     * @return
     */
    public Method getSetMethod() throws SecurityException {
        return null;
    }

}
