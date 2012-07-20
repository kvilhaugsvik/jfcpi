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

package org.freeciv.packetgen.javaGenerator.expression;

import org.freeciv.packetgen.javaGenerator.expression.willReturn.SomeExpr;

/**
 * A wrapper for source code of an expression that returns a certain kind of value.
 * In other words: A class extending this represents the type and an object of that class has the source code
 */
public abstract class TypedValueCode implements SomeExpr {
    private final String javaCode;

    /**
     * Constructor that forces the expression to be ready at initialization
     * @param javaCode
     */
    protected TypedValueCode(String javaCode) {
        this.javaCode = javaCode;
    }

    /**
     * Get source code for an expression that returns a value of the type of the class
     * @return the source code
     */
    public String getJavaCode() {
        return javaCode;
    }

    @Override
    public abstract String toString();
}
