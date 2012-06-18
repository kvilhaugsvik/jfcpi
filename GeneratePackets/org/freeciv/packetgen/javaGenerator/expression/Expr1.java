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

/**
 * An expression that returns a value of type Return given Argument1
 * @param <Returns>
 * @param <Argument1>
 */
public interface Expr1<Returns extends TypedValueCode, Argument1 extends TypedValueCode> extends Expr<Returns> {
    /**
     * Source code for an expression that will return a value of the type represented by Returns
     * @param arg1 the argument taken
     * @return The source code
     */
    public abstract Returns getCodeFor(Argument1 arg1);
}
