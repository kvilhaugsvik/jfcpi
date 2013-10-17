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

package org.freeciv.test;

import org.freeciv.types.StructArrayField;
import org.junit.Test;

import static org.junit.Assert.*;

public class GeneratedStructTest {
    @Test
    public void structWhereFieldIsArray() {
        StructArrayField a = new StructArrayField(5, new int[]{7, 8, 9});
        assertEquals("Wrong number in a field", 5, a.getANumber());
        assertArrayEquals("Wrong array in a field", new int[]{7, 8, 9}, a.getTheArray());
    }
}
