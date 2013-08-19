package org.commons.jconfig.datatype;

import java.util.concurrent.TimeUnit;

import org.commons.jconfig.datatype.TimeValue;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests of the {@linkplain TimeValue}.
 * @author sgrennan
 *
 */
public class TimeValueTest {

    @Test
    public void comparisons() {
        Assert.assertTrue((new TimeValue(1, TimeUnit.NANOSECONDS)).compareTo(new TimeValue(1, TimeUnit.NANOSECONDS)) == 0);
        Assert.assertTrue((new TimeValue(1, TimeUnit.NANOSECONDS)).compareTo(new TimeValue(1, TimeUnit.MICROSECONDS)) < 0);
        Assert.assertTrue((new TimeValue(1, TimeUnit.MICROSECONDS)).compareTo(new TimeValue(1, TimeUnit.NANOSECONDS)) > 0);
        Assert.assertTrue((new TimeValue(1000, TimeUnit.NANOSECONDS)).compareTo(new TimeValue(1, TimeUnit.MICROSECONDS)) == 0);
        Assert.assertTrue((new TimeValue(999, TimeUnit.NANOSECONDS)).compareTo(new TimeValue(1, TimeUnit.MICROSECONDS)) < 0);
        Assert.assertTrue((new TimeValue(1, TimeUnit.MICROSECONDS)).compareTo(new TimeValue(999, TimeUnit.NANOSECONDS)) > 0);
        Assert.assertTrue((new TimeValue(1001, TimeUnit.NANOSECONDS)).compareTo(new TimeValue(1, TimeUnit.MICROSECONDS)) > 0);
        Assert.assertTrue((new TimeValue(1999, TimeUnit.NANOSECONDS)).compareTo(new TimeValue(1, TimeUnit.MICROSECONDS)) > 0);
        Assert.assertTrue((new TimeValue(107000, TimeUnit.DAYS)).compareTo(new TimeValue(1, TimeUnit.NANOSECONDS)) > 0);
        Assert.assertTrue((new TimeValue(106000, TimeUnit.DAYS)).compareTo(new TimeValue(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) < 0);

        Assert.assertTrue((new TimeValue(-1, TimeUnit.NANOSECONDS)).compareTo(new TimeValue(1, TimeUnit.NANOSECONDS)) < 0);
        Assert.assertTrue((new TimeValue(-1, TimeUnit.NANOSECONDS)).compareTo(new TimeValue(1, TimeUnit.MICROSECONDS)) < 0);
        Assert.assertTrue((new TimeValue(-1, TimeUnit.MICROSECONDS)).compareTo(new TimeValue(1, TimeUnit.NANOSECONDS)) < 0);
        Assert.assertTrue((new TimeValue(-1000, TimeUnit.NANOSECONDS)).compareTo(new TimeValue(1, TimeUnit.MICROSECONDS)) < 0);
        Assert.assertTrue((new TimeValue(-999, TimeUnit.NANOSECONDS)).compareTo(new TimeValue(1, TimeUnit.MICROSECONDS)) < 0);
        Assert.assertTrue((new TimeValue(-1, TimeUnit.MICROSECONDS)).compareTo(new TimeValue(999, TimeUnit.NANOSECONDS)) < 0);
        Assert.assertTrue((new TimeValue(-1001, TimeUnit.NANOSECONDS)).compareTo(new TimeValue(1, TimeUnit.MICROSECONDS)) < 0);
        Assert.assertTrue((new TimeValue(-1999, TimeUnit.NANOSECONDS)).compareTo(new TimeValue(1, TimeUnit.MICROSECONDS)) < 0);
        Assert.assertTrue((new TimeValue(-107000, TimeUnit.DAYS)).compareTo(new TimeValue(1, TimeUnit.NANOSECONDS)) < 0);
        Assert.assertTrue((new TimeValue(-106000, TimeUnit.DAYS)).compareTo(new TimeValue(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) < 0);

        Assert.assertTrue((new TimeValue(-1, TimeUnit.NANOSECONDS)).compareTo(new TimeValue(-1, TimeUnit.NANOSECONDS)) == 0);
        Assert.assertTrue((new TimeValue(-1, TimeUnit.NANOSECONDS)).compareTo(new TimeValue(-1, TimeUnit.MICROSECONDS)) > 0);
        Assert.assertTrue((new TimeValue(-1, TimeUnit.MICROSECONDS)).compareTo(new TimeValue(-1, TimeUnit.NANOSECONDS)) < 0);
        Assert.assertTrue((new TimeValue(-1000, TimeUnit.NANOSECONDS)).compareTo(new TimeValue(-1, TimeUnit.MICROSECONDS)) == 0);
        Assert.assertTrue((new TimeValue(-999, TimeUnit.NANOSECONDS)).compareTo(new TimeValue(-1, TimeUnit.MICROSECONDS)) > 0);
        Assert.assertTrue((new TimeValue(-1, TimeUnit.MICROSECONDS)).compareTo(new TimeValue(-999, TimeUnit.NANOSECONDS)) < 0);
        Assert.assertTrue((new TimeValue(-1001, TimeUnit.NANOSECONDS)).compareTo(new TimeValue(-1, TimeUnit.MICROSECONDS)) < 0);
        Assert.assertTrue((new TimeValue(-1999, TimeUnit.NANOSECONDS)).compareTo(new TimeValue(-1, TimeUnit.MICROSECONDS)) < 0);
        Assert.assertTrue((new TimeValue(-107000, TimeUnit.DAYS)).compareTo(new TimeValue(-1, TimeUnit.NANOSECONDS)) < 0);
        Assert.assertTrue((new TimeValue(-107000, TimeUnit.DAYS)).compareTo(new TimeValue(1, TimeUnit.NANOSECONDS)) < 0);
        Assert.assertTrue((new TimeValue(-106000, TimeUnit.DAYS)).compareTo(new TimeValue(Long.MIN_VALUE, TimeUnit.NANOSECONDS)) > 0);

        // Verify that overflow and underflow are caught.
        Assert.assertTrue((new TimeValue(107000, TimeUnit.DAYS)).compareTo(new TimeValue(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) > 0);
        Assert.assertTrue((new TimeValue(-107000, TimeUnit.DAYS)).compareTo(new TimeValue(Long.MIN_VALUE, TimeUnit.NANOSECONDS)) < 0);
    }

    /**
     * Verifies that we can parse zero and units.
     */
    @Test
    public void testZeroValues() {
        String[] vals = { "0ns", "0us", "0ms", "0s", "0m", "0h", "0d" };
        TimeUnit[] tus = { TimeUnit.NANOSECONDS, TimeUnit.MICROSECONDS, TimeUnit.MILLISECONDS,
                TimeUnit.SECONDS, TimeUnit.MINUTES, TimeUnit.HOURS, TimeUnit.DAYS
        };
        for (int index = 0; index < vals.length; index++) {
            TimeValue tv = TimeValue.parse(vals[index]);
            Assert.assertEquals(tv.getValue(), 0L);
            Assert.assertEquals(tv.getTimeUnit(), tus[index]);
        }
    }

    /**
     * Verifies that we can parse nonzero and units.
     */
    @Test
    public void testNonZeros() {
        String[] vals = { "150ns", "150us", "150ms", "150s", "150m", "150h", "150d" };
        TimeUnit[] tus = { TimeUnit.NANOSECONDS, TimeUnit.MICROSECONDS, TimeUnit.MILLISECONDS,
                TimeUnit.SECONDS, TimeUnit.MINUTES, TimeUnit.HOURS, TimeUnit.DAYS
        };
        for (int index = 0; index < vals.length; index++) {
            TimeValue tv = TimeValue.parse(vals[index]);
            Assert.assertEquals(tv.getValue(), 150L);
            Assert.assertEquals(tv.getTimeUnit(), tus[index]);
        }
    }

    /**
     * Verifies that we can convert units to other units.
     */
    @Test
    public void testConvertValues() {
        String[] vals = { "150ns", "150us", "150ms", "150s", "150m", "150h", "150d" };
        long[][] longs = {
                { 0, 0, 0, 0, 0, 0 },                   // 150ns
                { 150, 0, 0, 0, 0, 0 },                 // 150us
                { 150000, 150, 0, 0, 0, 0 },            // 150ms
                { 150000000, 150000, 150, 2, 0, 0 },    // 150s
                { 9000000000L, 9000000, 9000, 150, 2, 0 },        // 150m
                { 540000000000L, 540000000, 540000, 9000, 150, 6 },        // 150h
                { 12960000000000L, 12960000000L, 12960000, 216000, 3600, 150 },        // 150d
        };
        for (int index = 0; index < vals.length; index++) {
            TimeValue tv = TimeValue.parse(vals[index]);
            Assert.assertEquals(tv.toMicros(), longs[index][0]);
            Assert.assertEquals(tv.toMillis(), longs[index][1]);
            Assert.assertEquals(tv.toSeconds(), longs[index][2]);
            Assert.assertEquals(tv.toMinutes(), longs[index][3]);
            Assert.assertEquals(tv.toHours(), longs[index][4]);
            Assert.assertEquals(tv.toDays(), longs[index][5]);
        }
    }

}
