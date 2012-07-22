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

package org.freeciv.packetgen.javaGenerator.expression.util;

import org.freeciv.packetgen.javaGenerator.expression.willReturn.*;

/**
 * Wrap a string of source code by declaring what it is
 */
public abstract class WrapCodeString {
    private final String javaCode;

    /**
     * Constructor that forces the expression to be ready at initialization
     * @param javaCode
     */
    protected WrapCodeString(String javaCode) {
        this.javaCode = javaCode;
    }

    /**
     * Get source code for an expression that returns a value of the type of the class
     * @return the source code
     */
    public String getJavaCode() {
        return javaCode;
    }

    public final String toString() {
        return getJavaCode();
    }

    public static AString asAString(String javaCode) {
        return new StringTyped(javaCode);
    }

    public static ABool asBool(String javaCode) {
        return new Bool(javaCode);
    }

    public static ALong asALong(String javaCode) {
        return new LongTyped(javaCode);
    }

    public static NoValue asVoid(String javaCode) {
        return new Void(javaCode);
    }
}
