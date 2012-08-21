/*
 * Copyright (c) 2012, Sveinung Kvilhaugsvik
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

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class TypedCodeTest {
    @Test public void annotatedField() {
        Annotate annotation = new Annotate("IsAField");
        Var field = Var.field(Arrays.asList(annotation),
                              Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO,
                              "int", "number", null);
        CodeAtoms asAtoms = new CodeAtoms(field);

        assertEquals("@IsAField", asAtoms.get(0).get());
        assertEquals("private", asAtoms.get(1).get());
        assertEquals("final", asAtoms.get(2).get());
        assertEquals("int", asAtoms.get(3).get());
        assertEquals("number", asAtoms.get(4).get());
    }
}
