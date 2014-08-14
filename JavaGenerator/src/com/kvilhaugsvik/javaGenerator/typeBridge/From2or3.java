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

package com.kvilhaugsvik.javaGenerator.typeBridge;

import com.kvilhaugsvik.javaGenerator.representation.HasAtoms;

/**
 * An expression that returns a value of type Return given Argument1 and Argument2 or given Argument1, Argument2 and Argument3
 * @param <Returns> the type of the return value
 * @param <Argument1> the type of the first argument
 * @param <Argument2> the type of the second argument
 * @param <Argument3> the type of the third argument
 */
public interface From2or3<Returns extends HasAtoms, Argument1 extends HasAtoms, Argument2 extends HasAtoms, Argument3 extends HasAtoms>
        extends From2<Returns, Argument1, Argument2>, From3<Returns, Argument1, Argument2, Argument3> {
}
