/*
 * Copyright (c) 2011, 2012, Sveinung Kvilhaugsvik
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

package org.freeciv.packetgen.javaGenerator;

import org.freeciv.Util;

public class EnumElement {
    private final String comment;
    private final String elementName;
    private final String[] paramlist;

    protected EnumElement(String comment, String elementName, String... params) {
        if (null == elementName)
            throw new IllegalArgumentException("All elements of enums must have names");

        this.comment = comment;
        this.elementName = elementName;
        this.paramlist = params;
    }

    public String getEnumValueName() {
        return elementName;
    }

    public String toString() {
        return elementName + "(" + Util.joinStringArray(paramlist, ", ", "", "") + ")" + ClassWriter.ifIs(" /* ", comment, " */");
    }

    public static EnumElement newEnumValue(String enumValueName) {
        return new EnumElement(null, enumValueName);
    }

    public static EnumElement newEnumValue(String comment, String enumValueName, String... params) {
        return new EnumElement(comment, enumValueName, params);
    }
}
