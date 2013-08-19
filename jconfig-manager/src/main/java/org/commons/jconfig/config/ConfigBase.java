package org.commons.jconfig.config;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.commons.jconfig.annotations.ConfigModTime;
import org.commons.jconfig.datatype.TimeValue;


/**
 * A superclass for application-specific configuration classes, containing methods
 * and data structures useful in many applications.
 * 
 * @author sgrennan
 *
 */
public abstract class ConfigBase {

    /** The time at which this configuration instance was created or last modified */
    private volatile TimeValue modTime = new TimeValue(System.currentTimeMillis(), TimeUnit.MILLISECONDS);

    /**
     * The time at which this configuration instance was created or last modified.
     * 
     * @return the last-modified time of this configuration instance
     */
    @Nonnull
    public TimeValue modTime() {
        return modTime;
    }

    /**
     * Called by the configuration manager to set the last-modified time for this configuration
     * instance when it is created or modified.
     * 
     * @param timestamp the last-modified time of this configuration instance
     */
    @ConfigModTime
    public void setModTime(@Nonnull TimeValue timestamp) {
        if (timestamp == null)
            throw new IllegalArgumentException("Null timestamp");
        modTime = timestamp;
    }

}
