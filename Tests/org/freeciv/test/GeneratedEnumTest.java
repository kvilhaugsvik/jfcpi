/*
 * Copyright (c) 2011. Sveinung Kvilhaugsvik
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

import org.freeciv.types.test;
import org.junit.Test;

import static org.junit.Assert.*;

public class GeneratedEnumTest {
    @Test public void getNumberOneIs1() {
        assertEquals("Wrong number for enum constant", 1, test.one.getNumber());
    }

    @Test public void getNumberTwoIs2() {
        assertEquals("Wrong number for enum constant", 2, test.two.getNumber());
    }

    @Test public void getNumberThreeIs3() {
        assertEquals("Wrong number for enum constant", 3, test.three.getNumber());
    }

    @Test public void getNumberInvalidIsMinus3() {
        assertEquals("Wrong number for enum constant", -3, test.INVALID.getNumber());
    }

    @Test public void isValidOneIsValid() {
        assertTrue("Wrong valid value for enum constant", test.one.isValid());
    }

    @Test public void isValidInvalidIsInvalid() {
        assertFalse("Wrong valid value for enum constant", test.INVALID.isValid());
    }

    @Test public void toStringOneIsOne() {
        assertEquals("Wrong String conversion for enum constant", "one", test.one.toString());
    }

    @Test public void toStringTwoIs2n() {
        assertEquals("Wrong String conversion for enum constant", "2nd", test.two.toString());
    }

    @Test public void isBitwiseGivesCorrectValue() {
        assertFalse("Generated enum test should not be bitwise", test.isBitWise());
    }
}
