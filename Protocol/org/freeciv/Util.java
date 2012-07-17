/*
 * Copyright (c) 2012. Sveinung Kvilhaugsvik
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package org.freeciv;

import java.lang.reflect.Array;

public class Util {
    private static String joinStringArrayGeneric(Object elements, String separator, String begin, String end) {
        if (0 == Array.getLength(elements))
            return begin + end;

        StringBuilder build = new StringBuilder(begin);
        build.append(Array.get(elements, 0));
        for (int index = 1; index < Array.getLength(elements); index++) {
            build.append(separator);
            build.append(Array.get(elements, index));
        }
        build.append(end);

        return build.toString();
    }

    public static String joinStringArray(String[] elements, String separator, String begin, String end) {
        return joinStringArrayGeneric(elements, separator, begin, end);
    }

    public static String joinStringArray(byte[] elements, String separator) {
        return joinStringArrayGeneric(elements, separator, "(", ")");
    }
}
