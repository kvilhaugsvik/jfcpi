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

public class EnumElement extends MethodCall {
    protected EnumElement(String comment, String elementName, String... params) {
        super(comment, elementName, params);
    }

    public String getEnumValueName() {
        return super.method;
    }

    public static EnumElement newEnumValue(String enumValueName) {
        return new EnumElement(null, enumValueName);
    }

    public static EnumElement newEnumValue(String comment, String enumValueName, String... params) {
        return new EnumElement(comment, enumValueName, params);
    }
}
