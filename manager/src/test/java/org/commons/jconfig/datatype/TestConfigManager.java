package org.commons.jconfig.datatype;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


import org.commons.jconfig.config.ConfigManager;
import org.commons.jconfig.datatype.TimeValue;
import org.commons.jconfig.datatype.ValueType;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestConfigManager {

    /**
     * Test if convertValue parses and returns the response correctly for TimeList valuetype
     * 
     * @throws Exception
     */
    @Test
    public void testConvertValue() throws Exception {
        ConfigManager manager = ConfigManager.INSTANCE;

        Method method = ConfigManager.class.getDeclaredMethod("convertValue", new Class[] { ValueType.class,
                String.class });
        method.setAccessible(true);

        // test for one unit
        Object data = method.invoke(manager, new Object[] { ValueType.TimeList, "50ms" });
        Assert.assertTrue(data instanceof ArrayList);
        ArrayList<TimeValue> timeValueArray = (ArrayList<TimeValue>)data;
        Assert.assertEquals(timeValueArray.size(), 1);
        TimeValue timeValue = timeValueArray.get(0);
        Assert.assertEquals(timeValue.getValue(), 50);
        Assert.assertEquals(timeValue.getTimeUnit(), TimeUnit.MILLISECONDS);

        // test for two same units
        data = method.invoke(manager, new Object[] { ValueType.TimeList, "55ms:100ms" });
        Assert.assertTrue(data instanceof ArrayList);
        timeValueArray = (ArrayList<TimeValue>) data;
        Assert.assertEquals(timeValueArray.size(), 2);
        timeValue = timeValueArray.get(0);
        Assert.assertEquals(timeValue.getValue(), 55);
        Assert.assertEquals(timeValue.getTimeUnit(), TimeUnit.MILLISECONDS);
        timeValue = timeValueArray.get(1);
        Assert.assertEquals(timeValue.getValue(), 100);
        Assert.assertEquals(timeValue.getTimeUnit(), TimeUnit.MILLISECONDS);
    
        // test for two different units
        data = method.invoke(manager, new Object[] { ValueType.TimeList, "60ms:120s" });
        Assert.assertTrue(data instanceof ArrayList);
        timeValueArray = (ArrayList<TimeValue>) data;
        Assert.assertEquals(timeValueArray.size(), 2);
        timeValue = timeValueArray.get(0);
        Assert.assertEquals(timeValue.getValue(), 60);
        Assert.assertEquals(timeValue.getTimeUnit(), TimeUnit.MILLISECONDS);
        timeValue = timeValueArray.get(1);
        Assert.assertEquals(timeValue.getValue(), 120);
        Assert.assertEquals(timeValue.getTimeUnit(), TimeUnit.SECONDS);

        // test for three different units
        data = method.invoke(manager, new Object[] { ValueType.TimeList, "60ms:120s:180m" });
        Assert.assertTrue(data instanceof ArrayList);
        timeValueArray = (ArrayList<TimeValue>) data;
        Assert.assertEquals(timeValueArray.size(), 3);
        timeValue = timeValueArray.get(0);
        Assert.assertEquals(timeValue.getValue(), 60);
        Assert.assertEquals(timeValue.getTimeUnit(), TimeUnit.MILLISECONDS);
        timeValue = timeValueArray.get(1);
        Assert.assertEquals(timeValue.getValue(), 120);
        Assert.assertEquals(timeValue.getTimeUnit(), TimeUnit.SECONDS);
        timeValue = timeValueArray.get(2);
        Assert.assertEquals(timeValue.getValue(), 180);
        Assert.assertEquals(timeValue.getTimeUnit(), TimeUnit.MINUTES);

    }

}
