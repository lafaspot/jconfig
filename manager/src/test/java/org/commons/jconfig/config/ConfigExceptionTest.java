package org.commons.jconfig.config;

import org.commons.jconfig.config.ConfigException;
import org.testng.Assert;
import org.testng.annotations.Test;


public class ConfigExceptionTest {

    @Test
    public void ConfigExceptions() {

        Throwable t = new Throwable();
        ConfigException ex = new ConfigException(t);
        Assert.assertEquals(ex.getCause(), t);

        ex = new ConfigException("some exception");
        Assert.assertEquals(ex.getMessage(), "some exception");

        ex = new ConfigException("some exception", t);
        Assert.assertEquals(ex.getCause(), t);
        Assert.assertEquals(ex.getMessage(), "some exception");
    }
}
