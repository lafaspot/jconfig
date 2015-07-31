package org.commons.jconfig.config;

import org.commons.jconfig.annotations.ByteRange;
import org.commons.jconfig.annotations.Config;
import org.commons.jconfig.annotations.ConfigGet;
import org.commons.jconfig.annotations.ConfigResource;
import org.commons.jconfig.annotations.ConfigResourceId;
import org.commons.jconfig.annotations.ConfigSet;
import org.commons.jconfig.annotations.NumberRange;
import org.commons.jconfig.annotations.StringNotEmpty;
import org.commons.jconfig.annotations.TimeRange;
import org.commons.jconfig.config.KeyNotFound;
import org.commons.jconfig.datatype.ByteValue;
import org.commons.jconfig.datatype.TimeValue;
import org.commons.jconfig.datatype.ValueType;


/**
 * This second example uses keys with annotations that enforce that values
 * loaded from the configmanager, follow some restrictions. In case the value
 * being set fails one of the restrictions, the default value will be used. The
 * default value is not validated against the restrictions.
 * 
 * Here are some example of restrictions that can be applied to the set method
 * to a particular key.
 * 
 * @ByteRange(min = "0 MiB", max = "5 MiB") for keys of type ValueType.Byte.
 * @TimeRange(min = "0 ms", max = "5 d") for keys of type ValueType.Time.
 * @StringNotEmpty for keys of type ValueType.String.
 * @NumberRange(min = 0, max = 100) ofr keys fo type Number.
 * 
 *                  Also in this example the annotation @ConfigResource is used
 *                  to annotate a method that will return a config resource
 *                  name, to be use by the ConfigManager to load it from JVM
 *                  classpath. This is mostly used for backwards compatibility,
 *                  and is only a hint for the ConfigManager to use a file with
 *                  this name. But take in consideration that both the
 *                  COnfigManager or ConfigLoader can overwrite this setting
 *                  with other files, when instructed to be loaded into this
 *                  config class. The app1.properties file is available in the
 *                  source code user src/test/resources.
 * 
 *                  The annotation @ConfigResourceId() is used in this example
 *                  to tell ConfigManager to skip it's default behavior and try
 *                  to load a key value with the @ConfigResourceId() name. By
 *                  default ConfigManager would use the method name to load keys
 *                  from config files. The @ConfigResourceId should be avoided
 *                  and was added to provide backwards compatibility with old
 *                  config files.
 * 
 */
@Config(description = "App2 common config object example 2")
@ConfigResource(name = "app2.json")
public class App2Config {

    private TimeValue mTimeout;

    /**
     * Timeout is of type Time and default value 4 days.
     * 
     * @return TimeValue
     */
    @ConfigGet(description = "Timeout value to test time values.", type = ValueType.Time, defaultValue = "4 d")
    public TimeValue getTimeout() {
        return mTimeout;
    }

    /**
     * Timeout
     * 
     * @param value
     *            - a TimeValue.
     */
    @ConfigSet
    @TimeRange(min = "0 ms", max = "5 d")
    public void setTimeout(final TimeValue value) {
        mTimeout = value;
    }

    private TimeValue mTimeoutProblem1;

    @ConfigGet(description = "Timeout value to test time values.", type = ValueType.Time, defaultValue = "4 d")
    public TimeValue getTimeoutProblem1() {
        return mTimeoutProblem1;
    }

    @ConfigSet
    @TimeRange(min = "0 ms", max = "5 d")
    public void setTimeoutProblem1(final TimeValue value) {
        mTimeoutProblem1 = value;
    }

    private TimeValue mTimeoutProblem2;

    @ConfigGet(description = "Timeout value to test time values.", type = ValueType.Time, defaultValue = "16 h")
    public TimeValue getTimeoutProblem2() {
        return mTimeoutProblem2;
    }

    @ConfigSet
    @TimeRange(min = "0 ms", max = "5 d")
    public void setTimeoutProblem2(final TimeValue value) {
        mTimeoutProblem2 = value;
    }

    private String mLocalCluster;

    @ConfigGet(
            description = "Local cluster used in rocket stat logging.",
            type = ValueType.String, defaultValue = ""
    )
    @ConfigResourceId("xyz.xmas.common.local_cluster")
    public String getLocalCluster() {
        return mLocalCluster;
    }

    @ConfigSet
    @ConfigResourceId("xyz.xmas.common.local_cluster")
    @StringNotEmpty
    public void setLocalCluster(final String localCluster) {
        mLocalCluster = localCluster;
    }

    private String mLocalFarm;

    @ConfigGet(
            description = "Local farm used in rocket stat logging.",
            type = ValueType.String,
            defaultValue = "NO_FARM"
    )
    @ConfigResourceId("xyz.xmas.common.local_farm")
    public String getLocalFarm() {
        return mLocalFarm;
    }

    @ConfigSet
    @ConfigResourceId("xyz.xmas.common.local_farm")
    @StringNotEmpty
    public void setLocalFarm(final String localFarm) {
        mLocalFarm = localFarm;
    }


    private Number mRocketstatSamplePercent;

    @ConfigGet(
            description = "Rocketstat Sample Percent. Specifies then amounts of stats sent to the rocketstat server in percentage points.",
            type = ValueType.Number,
            defaultValue = "20"
    )
    @ConfigResourceId("xyz.xmas.common.rocketstat_sample_percent")
    public Number getRocketstatSamplePercent() {
        return mRocketstatSamplePercent;
    }

    @ConfigSet
    @ConfigResourceId("xyz.xmas.common.rocketstat_sample_percent")
    @NumberRange(min = 0, max = 100)
    public void setRocketstatSamplePercent(final Number rocketstatSamplePercent) {
        mRocketstatSamplePercent = rocketstatSamplePercent;
    }

    private ByteValue mCacheSize;

    @ConfigGet(description = "Cache size with no default value.", type = ValueType.Bytes, defaultValue = "NO_DEFAULT")
    public ByteValue getCacheSize() {
        return mCacheSize;
    }

    @ConfigSet(useDefault = false)
    @ByteRange(min = "0 MiB", max = "5 MiB")
    public void setCacheSize(final ByteValue value) {
        mCacheSize = value;
    }

    private ByteValue mBufferSize;

    @ConfigGet(
            description = "Missing buffer size with no default value.",
            type = ValueType.Bytes,
            defaultValue = "NO_DEFAULT")
            public ByteValue getBufferSize() {
        if (mBufferSize == null) {
            throw new KeyNotFound("BufferSize key not set, value is required.");
        }
        return mBufferSize;
    }

    @ConfigSet(useDefault = false)
    @ByteRange(min = "0 MiB", max = "5 MiB")
    public void setBufferSize(final ByteValue value) {
        mBufferSize = value;
    }

    //
    // Config "Sets" values
    //
    private String mHostUrl;

    @ConfigGet(
            description = "HostUrl \"Sets\" config value",
            type = ValueType.String,
            defaultValue = "mail.yahoo.com")
            public String getHostUrl() {
        return mHostUrl;
    }

    @ConfigSet
    public void setHostUrl(final String value) {
        mHostUrl = value;
    }

    private Number mHostPort;

    @ConfigGet(
            description = "HostPort \"Sets\" config value",
            type = ValueType.Number,
            defaultValue = "80")
            public Number getHostPort() {
        return mHostPort;
    }

    @ConfigSet
    public void setHostPort(final Number value) {
        mHostPort = value;
    }

    private String mHostProxy;

    @ConfigGet(
            description = "HostProxy \"Sets\" config value",
            type = ValueType.String,
            defaultValue = "proxy.mail.yahoo.com")
            public String getHostProxy() {
        return mHostProxy;
    }

    @ConfigSet
    public void setHostProxy(final String value) {
        mHostProxy = value;
    }

}

