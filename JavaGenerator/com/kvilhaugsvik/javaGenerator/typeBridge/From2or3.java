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

public interface From2or3<Returns extends HasAtoms, Argument1 extends HasAtoms, Argument2 extends HasAtoms, Argument3 extends HasAtoms>
        extends From2<Returns, Argument1, Argument2>, From3<Returns, Argument1, Argument2, Argument3> {
}
