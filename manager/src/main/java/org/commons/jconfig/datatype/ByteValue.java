/*
 * Copyright 2011 Yahoo! Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.commons.jconfig.datatype;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ByteValue implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2344361821969665171L;

    /**
     * A regular expression separating the configuration string into a number
     * and units, in such a way that the number will parse as a long without
     * exception, and the units specifier cannot be null or missing (but may be
     * any case).
     */
    private static Pattern byteValueRegex = Pattern.compile("\\s*(\\d+)(\\s*)([a-zA-Z]+)\\s*");

    private final ByteUnit mByteUnit;
    private final long mValue;

    public ByteValue(final long value, final ByteUnit byteUnit) {
        super();
        mValue = value;
        mByteUnit = byteUnit;
    }

    public long getValue() {
        return mValue;
    }

    public ByteUnit getByteUnit() {
        return mByteUnit;
    }

    public long toBytes() {
        return mByteUnit.toBytes(mValue);
    }

    public long toKibibytes() {
        return mByteUnit.toKibibytes(mValue);
    }

    public long toMebibytes() {
        return mByteUnit.toMebibytes(mValue);
    }

    public long toGibibytes() {
        return mByteUnit.toGibibytes(mValue);
    }

    @Override
    public String toString() {
        return mValue + " " + mByteUnit;
    }

    @Override
    public boolean equals(final Object that) {
        if (that == null)
            return false;
        if (!(that instanceof ByteValue)) {
            return false;
        } else {
            ByteValue thatT = (ByteValue) that;
            long thisValue = ByteUnit.Byte.convert(mValue, mByteUnit);
            long thatValue = ByteUnit.Byte.convert(thatT.getValue(), thatT.getByteUnit());
            return thisValue == thatValue;
        }
    }

    public static ByteValue parse(final String value) throws TypeFormatException {
        Matcher matcher = byteValueRegex.matcher(value);
        if (matcher.find()) {
            long longVal = Long.parseLong(matcher.group(1));
            ByteUnit units = ByteUnit.getByteUnit(matcher.group(3).toLowerCase());
            if (units != null) {
                return new ByteValue(longVal, units);
            } else {
                throw new TypeFormatException("Byte units [B,KiB,MiB,GiB] missing from '" + value + "'");
            }
        } else {
            throw new TypeFormatException("Unable to parse byte 'NUMBER B|KiB|MiB|GiB' from '" + value + "'");
        }
    }
}
