package org.commons.jconfig.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.commons.jconfig.config.ConfigLoaderAdapterID;


/**
 * Specifies the @common.config.ConfigManager.ConfigLoaderAdapterID to use
 * for the annotated config class.
 * 
 * If this is not specified by annotated config class, the ConfigLoaderAdapterID.JSON
 * is used.
 * 
 * @author lafa
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConfigLoaderAdapter {

    /**
     * Uri for the ConfigLoader adapter used to load the config.
     * The default ConfigLoader adapter used is "config:json" if the uri is empty,
     * if uri is set it takes precedence of the adapter setting.
     * For built-in adapter list see {@link ConfigLoaderAdapterID}
     * 
     * @return
     */
    String uri() default "";

    /**
     * Uri for the ConfigLoader adapter used to load the config. The default
     * ConfigLoader adapter used "config:json", even in the case where the
     * annotation is not present in the class definition.
     * 
     * For built-in adapter list see {@link ConfigLoaderAdapterID}
     * 
     * @return
     */
    ConfigLoaderAdapterID adapter() default ConfigLoaderAdapterID.JSON_FILE;
}
