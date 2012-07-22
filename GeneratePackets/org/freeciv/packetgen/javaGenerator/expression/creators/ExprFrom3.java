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

package org.freeciv.packetgen.javaGenerator.expression.creators;

import org.freeciv.packetgen.javaGenerator.expression.willReturn.Returnable;

public interface ExprFrom3<Returns extends Returnable,
        Argument1 extends Returnable,
        Argument2 extends Returnable,
        Argument3 extends Returnable> {
    /**
     * Source code for an expression that will return a value of the type represented by Returns
     * @param arg1 the first argument taken
     * @param arg2 the second argument taken
     * @param arg3 the third argument taken
     * @return The source code
     */
    public abstract Returns getCodeFor(Argument1 arg1, Argument2 arg2, Argument3 arg3);
}
