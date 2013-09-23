/*
 * Copyright (c) 2013. Sveinung Kvilhaugsvik
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

import org.freeciv.types.bv_enum_typed;
import org.freeciv.types.testCount;
import org.junit.Test;

import static org.junit.Assert.*;

public class GeneratedBVTest {
    @Test
     public void bvTyped() {
        bv_enum_typed en = new bv_enum_typed();

        assertFalse("Should start blank", en.get(testCount.one));
        assertFalse("Should start blank", en.get(testCount.two));
        assertFalse("Should start blank", en.get(testCount.three));

        en.set(testCount.two);

        assertFalse("Shouldn't be set", en.get(testCount.one));
        assertTrue("Should be set", en.get(testCount.two));
        assertFalse("Shouldn't be set", en.get(testCount.three));
    }
}
