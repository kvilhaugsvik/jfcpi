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
 * An expression that returns a value of type Return given Argument1 and Argument2
 * @param <Returns>
 * @param <Argument1>
 * @param <Argument2>
 */
public abstract class Expr2<Returns extends TypedValueCode, Argument1 extends TypedValueCode, Argument2 extends TypedValueCode> implements Expr<Returns> {
    public abstract Argument1 getArgument1();
    public abstract Argument1 getArgument2();
}
