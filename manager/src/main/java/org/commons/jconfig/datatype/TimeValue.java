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

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * Physical quantities with units of time, and 64-bit integer values.
 * 
 * @author sgrennan
 *
 */
public final class TimeValue implements java.io.Serializable, Comparable<TimeValue> {

    /**
     * Creates a time quantity with the specified numeric value and units.
     * 
     * @param value the magnitude of the time interval (positive, negative or zero)
     * @param timeUnit the units of the interval
     */
    public TimeValue(final long value, final TimeUnit timeUnit) {
        super();
        this.value = value;
        this.timeUnit = timeUnit;
    }

    /**
     * Returns a time value equal to the current JVM time.
     * 
     * @return the current time
     */
    public static TimeValue now() {
        return new TimeValue(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Compares this value to {@code o}, returning 1, 0 or -1 as this value is greater
     * than, equal to or less than {@code o}, respectively. (Implementation of
     * {@link Comparable}.
     * 
     * @return -1, 0 or 1 as this value is less than, equal to or greater than {@code o}.
     * @throws IllegalArgumentException if {@code o} is null.
     */
    @Override
    public int compareTo(@Nonnull TimeValue o) {
        if (o == null)
            throw new IllegalArgumentException("Cannot compare to null TimeValue.");
        // Same units? Just compare values.
        int unitCompare = timeUnit.compareTo(o.timeUnit);
        if (unitCompare == 0) {
            if (this.value < o.value) return -1;
            if (this.value > o.value) return 1;
            return 0;
        }
        // Convert larger-united quantity to smaller units.
        TimeValue bigU = (unitCompare < 0) ? o : this;
        TimeValue smallU = (unitCompare < 0) ? this : o;
        long convertedValue = smallU.timeUnit.convert(bigU.value, bigU.timeUnit);
        /*
         * Is the converted value now greater or smaller? Overflow or underflow means the
         * larger-united quantity cannot be represented in the smaller units, which means
         * it has to be greater or smaller, respectively.
         * NB: it is not possible for Long.MAX_VALUE or Long.MIN-VALUE to be generated
         * by any unit conversion, as they are not evenly divisible by 24, 60 or 1000.
         */
        if (convertedValue == Long.MAX_VALUE || convertedValue > smallU.value)
            return (bigU == this) ? 1 : -1;
        if (convertedValue == Long.MIN_VALUE || convertedValue < smallU.value)
            return (bigU == this) ? -1 : 1;
        return 0;
    }

    /**
     * A regular expression separating the configuration string into a whole number
     * and units, in such a way that the number will parse as a long without
     * exception, and the units specifier cannot be null or missing (but may be
     * any case).
     * TODO fsg 110504 Add floating-point.
     */
    private static Pattern timeValueRegex = Pattern.compile("\\s*(\\d+)(\\s*)([a-zA-Z]+)\\s*");

    private final long value;
    private final TimeUnit timeUnit;

    public long getValue() {
        return value;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public long toDays() {
        return timeUnit.toDays(value);
    }

    public long toHours() {
        return timeUnit.toHours(value);
    }

    public long toMicros() {
        return timeUnit.toMicros(value);
    }

    public long toMillis() {
        return timeUnit.toMillis(value);
    }

    public long toMinutes() {
        return timeUnit.toMinutes(value);
    }

    public long toSeconds() {
        return timeUnit.toSeconds(value);
    }

    @Override
    public boolean equals(final Object that) {
        if (!(that instanceof TimeValue))
            return false;
        return compareTo((TimeValue)that) == 0;
    }

    public static TimeValue parse(final String value) throws TypeFormatException {
        Matcher matcher = timeValueRegex.matcher(value);
        if (matcher.find()) {
            long longVal = Long.parseLong(matcher.group(1));
            TimeUnit units = abbreviationToUnit.get(matcher.group(3).toLowerCase(Locale.US));
            if (units != null) {
                return new TimeValue(longVal, units);
            } else {
                throw new TypeFormatException("Time units [d,h,m,s,ms,us,ns] missing from '" + value + "'");
            }
        } else {
            throw new TypeFormatException("Unable to parse time 'NUMBER d|h|m|s|ms|us|ns' from '" + value + "'");
        }
    }

    @Override
    public String toString() {
        return value + " " + unitToAbbreviation.get(timeUnit);
    }

    private static final HashMap<TimeUnit,String> unitToAbbreviation = new HashMap<TimeUnit,String>();
    static {
        unitToAbbreviation.put(TimeUnit.NANOSECONDS, "ns");
        unitToAbbreviation.put(TimeUnit.MICROSECONDS, "us");
        unitToAbbreviation.put(TimeUnit.MILLISECONDS, "ms");
        unitToAbbreviation.put(TimeUnit.SECONDS, "s");
        unitToAbbreviation.put(TimeUnit.MINUTES, "m");
        unitToAbbreviation.put(TimeUnit.HOURS, "h");
        unitToAbbreviation.put(TimeUnit.DAYS, "d");
    }

    private static final HashMap<String,TimeUnit> abbreviationToUnit = new HashMap<String,TimeUnit>();
    static {
        abbreviationToUnit.put("ns", TimeUnit.NANOSECONDS);
        abbreviationToUnit.put("us", TimeUnit.MICROSECONDS);
        abbreviationToUnit.put("ms", TimeUnit.MILLISECONDS);
        abbreviationToUnit.put("s", TimeUnit.SECONDS);
        abbreviationToUnit.put("m", TimeUnit.MINUTES);
        abbreviationToUnit.put("h", TimeUnit.HOURS);
        abbreviationToUnit.put("d", TimeUnit.DAYS);
    }

    /** Required by Serializable. */
    private static final long serialVersionUID = 8439678036575756604L;

}
