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

package org.freeciv.packetgen;

import org.junit.Test;

import static org.junit.Assert.*;

public class EnumTest {
    @Test(expected = IllegalArgumentException.class)
    public void testBitWiseHasNoNumber3() {
        new Enum("test", true,
                Enum.newEnumValue("TWO", 2),
                Enum.newEnumValue("THREE", 3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBitWiseHasNoNumber18() {
        new Enum("test", true,
                Enum.newEnumValue("TWO", 2),
                Enum.newEnumValue("NOT2EXP", 18));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBitWiseHasNoNumber15() {
        new Enum("test", true,
                Enum.newEnumValue("TWO", 2),
                Enum.newEnumValue("NOT2EXP", 15));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBitWiseHasNoNumber17() {
        new Enum("test", true,
                Enum.newEnumValue("TWO", 2),
                Enum.newEnumValue("NOT2EXP", 17));
    }

    @Test public void testBitWiseHasANumber16() {
        new Enum("test", true,
                Enum.newEnumValue("TWO", 2),
                Enum.newEnumValue("NOT2EXP", 16));
    }

    @Test public void testBitWiseIsBitWise() {
        Enum result = new Enum("test", true,
                Enum.newEnumValue("TWO", 2));
        assertTrue("A bitwise Enum should report to be bitwise", result.isBitwise());
    }

    private Enum enumWithValues() {
        return new Enum("test", false,
                Enum.newEnumValue("ZERO", 0, "\"nothing\""),
                Enum.newEnumValue("ONE", 1),
                Enum.newEnumValue("TWO", 2));
    }

    @Test public void testNotBitWiseIsNotBitWise() {
        Enum result = enumWithValues();
        assertFalse("A bitwise Enum should report to be bitwise", result.isBitwise());
    }

    @Test public void testNameIsCorrect() {
        Enum result = enumWithValues();
        assertEquals("Wrong name", "test", result.getEnumClassName());
    }

    @Test public void testGetEnumValueLast() {
        Enum result = enumWithValues();
        assertNotNull("Enum element not found", result.getEnumValue("TWO"));
    }

    @Test public void testGetEnumValueMiddle() {
        Enum result = enumWithValues();
        assertNotNull("Enum element not found", result.getEnumValue("ONE"));
    }

    @Test public void testGetEnumValueFirst() {
        Enum result = enumWithValues();
        assertNotNull("Enum element not found", result.getEnumValue("ZERO"));
    }

    @Test public void enumValueGetName() {
        Enum result = enumWithValues();
        assertEquals("Enum element code name not found", "ONE", result.getEnumValue("ONE").getEnumValueName());
    }

    @Test public void enumValueGetNumber() {
        Enum result = enumWithValues();
        assertEquals("Enum element number not found", 1, result.getEnumValue("ONE").getNumber());
    }

    @Test public void enumValueGetToStringName() {
        Enum result = enumWithValues();
        assertEquals("Enum element code name not found", "\"nothing\"", result.getEnumValue("ZERO").getToStringName());
    }

    @Test public void enumValueGetToStringFrom() {
        Enum result = enumWithValues();
        assertEquals("Enum element code name not found", "\"TWO\"", result.getEnumValue("TWO").getToStringName());
    }
}
