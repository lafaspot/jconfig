package org.commons.jconfig.config;

import java.util.concurrent.TimeUnit;

import org.commons.jconfig.annotations.Config;
import org.commons.jconfig.annotations.ConfigGet;
import org.commons.jconfig.annotations.ConfigResource;
import org.commons.jconfig.annotations.ConfigSet;
import org.commons.jconfig.annotations.NumberRange;
import org.commons.jconfig.annotations.StringNotEmpty;
import org.commons.jconfig.annotations.TimeRange;
import org.commons.jconfig.datatype.TimeValue;
import org.commons.jconfig.datatype.ValueType;

/**
 * ConfigManager Config
 */
@Config(description = "ConfigManager default config")
@ConfigResource(name = "configmanager.json")
public class ConfigManagerConfig {

    private String mConfigPath = "/etc/configmanager";

    @ConfigGet(description = "ConfigManager config location path. If not found here - it will be picked up from classpath.", type = ValueType.String, defaultValue = "/etc/configmanager")
    public String getConfigPath() {
        return mConfigPath;
    }

    @ConfigSet
    @StringNotEmpty
    public void setConfigPath(final String path) {
        mConfigPath = path;
    }

    private String loadFrom = "FILESYSTEM";

    @ConfigGet(description = "Config's can be loaded, updated via multiple options. Options enabled so far are 'FILESYSTEM' and 'JMX'", type = ValueType.String, defaultValue = "FILESYSTEM")
    public String getLoadFrom() {
        return loadFrom;
    }

    @ConfigSet
    @StringNotEmpty
    public void setLoadFrom(final String name) {
        loadFrom = name;
    }

    private Number maxCacheSize = 10000;

    @ConfigGet(description = "Max size for config object cache size. The size is the maximun number of config objects in cache.", type = ValueType.Number, defaultValue = "10000")
    public Number getMaxCacheSize() {
        return maxCacheSize;
    }

    @ConfigSet
    @NumberRange(min = 10, max = Integer.MAX_VALUE)
    public void setMaxCacheSize(final Number size) {
        maxCacheSize = size;
    }

    private TimeValue configLoaderSyncInterval = new TimeValue(15,
            TimeUnit.SECONDS);

    /**
     * How much time to wait for Config Loader to push in new values
     * 
     * @return TimeValue
     */
    @ConfigGet(description = "ConfigLoade synch interval", type = ValueType.Time, defaultValue = "15s")
    public TimeValue getConfigLoaderSyncInterval() {
        return configLoaderSyncInterval;
    }

    @ConfigSet
    @TimeRange(min = "0 ms", max = "60 m")
    public void setConfigLoaderSyncInterval(final TimeValue timeValue) {
        configLoaderSyncInterval = timeValue;
    }
}
