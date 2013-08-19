package org.commons.jconfig.datatype;

import org.commons.jconfig.datatype.ByteUnit;
import org.commons.jconfig.datatype.ByteValue;
import org.commons.jconfig.datatype.TimeValue;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests of the {@linkplain TimeValue}.
 * 
 * @author lafa
 * 
 */
public class ByteValueTest {

    /**
     * Verifies that we can parse zero and units.
     */
    @Test
    public void testZeroValues() {
        String[] vals = { "0b", "0bytes", "0B", "0kb", "0KB", "0kB", "0KiB", "0mb", "0MB", "0mB", "0MiB", "0gb", "0GB",
                "0gB", "0GiB" };
        ByteUnit[] bus = { ByteUnit.Byte, ByteUnit.Byte, ByteUnit.Byte,
                ByteUnit.Kibibyte, ByteUnit.Kibibyte, ByteUnit.Kibibyte, ByteUnit.Kibibyte,
                ByteUnit.Mebibyte, ByteUnit.Mebibyte, ByteUnit.Mebibyte, ByteUnit.Mebibyte,
                ByteUnit.Gibibyte, ByteUnit.Gibibyte, ByteUnit.Gibibyte, ByteUnit.Gibibyte
        };
        for (int index = 0; index < vals.length; index++) {
            ByteValue bv = ByteValue.parse(vals[index]);
            Assert.assertEquals(bv.getValue(), 0L);
            Assert.assertEquals(bv.getByteUnit(), bus[index]);
        }
    }

    /**
     * Verifies that we can parse nonzero and units.
     */
    @Test
    public void testNonZeroValues() {
        String[] vals = { "150b", "150bytes", "150kb", "150KiB", "150mb", "150MB", "150gb" };
        ByteUnit[] bus = { ByteUnit.Byte, ByteUnit.Byte, ByteUnit.Kibibyte, ByteUnit.Kibibyte, ByteUnit.Mebibyte,
                ByteUnit.Mebibyte, ByteUnit.Gibibyte
        };
        for (int index = 0; index < vals.length; index++) {
            ByteValue bv = ByteValue.parse(vals[index]);
            Assert.assertEquals(bv.getValue(), 150L);
            Assert.assertEquals(bv.getByteUnit(), bus[index]);
        }
    }

    /**
     * Verifies that we can convert units to other units.
     */
    @Test
    public void testConvertValues() {
        String[] vals = { "1024b", "1024bytes", "1024kb", "1024KB", "1024mb", "1024MiB", "1024GB" };
        long[][] longs = {
                { 1024, 1, 0, 0 }, // 150b
                { 1024, 1, 0, 0 }, // 150bytes
                { 1048576, 1024, 1, 0 }, // 150kb
                { 1048576, 1024, 1, 0 }, // 150KB
                { 1073741824L, 1048576, 1024, 1 }, // 150mb
                { 1073741824L, 1048576, 1024, 1 }, // 150MiB
                { 1099511627776L, 1073741824L, 1048576, 1024 }, // 150GB
        };
        for (int index = 0; index < vals.length; index++) {
            ByteValue bv = ByteValue.parse(vals[index]);
            Assert.assertEquals(bv.toBytes(), longs[index][0]);
            Assert.assertEquals(bv.toKibibytes(), longs[index][1]);
            Assert.assertEquals(bv.toMebibytes(), longs[index][2]);
            Assert.assertEquals(bv.toGibibytes(), longs[index][3]);
        }
    }

}
