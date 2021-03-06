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

import java.util.Arrays;
import java.util.List;

/** A convenience enum of ByteUnit for more common abbreviations.
 *  Reference: http://en.wikipedia.org/wiki/Binary_prefix
 */
public enum ByteUnit {
    Byte(1, Arrays.asList("B", "b", "bytes")),
    // 1024 bytes
    Kibibyte(1024, Arrays.asList("kb", "KB", "kB", "KiB")),
    // 1048576 bytes
    Mebibyte(1048576, Arrays.asList("mb", "MB", "mB", "MiB")),
    // 1073741824 bytes
    Gibibyte(1073741824, Arrays.asList("gb", "GB", "gB", "GiB"));

    private final long multiplier;
    private final List<String> abbrevList;

    private ByteUnit(final long multiplier, final List<String> abbrev) {
        this.multiplier = multiplier;
        abbrevList = abbrev;
    }

    public static ByteUnit getByteUnit(final String token) {
        for (ByteUnit map : ByteUnit.values()) {
            for (String abbrev : map.abbrevList) {
                if (abbrev.equalsIgnoreCase(token))
                    return map;
            }
        }
        return null;
    }

    public long toBytes(final long d) {
        return d * multiplier;
    }

    public long toKibibytes(final long d) {
        return toBytes(d) / Kibibyte.multiplier;
    }

    public long toGibibytes(final long d) {
        return toBytes(d) / Gibibyte.multiplier;
    }

    public long toMebibytes(final long d) {
        return toBytes(d) / Mebibyte.multiplier;
    }

    public long convert(final long d, final ByteUnit u) {
        return u.toBytes(d) / multiplier;
    }

    @Override
    public String toString() {
        return abbrevList.get(0);
    }
}