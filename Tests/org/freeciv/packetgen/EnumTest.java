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

import java.security.InvalidParameterException;

import static org.junit.Assert.*;
import static org.freeciv.packetgen.ClassWriter.EnumElement.*;

public class EnumTest {
    @Test public void testBitWiseHasANumber0() {
        new Enum("test", true,
                newEnumValue("NOT2EXP", 0));
    }

    @Test public void testBitWiseHasANumber1() {
        new Enum("test", true,
                newEnumValue("NOT2EXP", 1));
    }

    @Test public void testBitWiseHasANumber2() {
        new Enum("test", true,
                newEnumValue("NOT2EXP", 2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBitWiseHasNoNumber3() {
        new Enum("test", true,
                newEnumValue("TWO", 2),
                newEnumValue("THREE", 3));
    }

    @Test public void testBitWiseHasANumber4() {
        new Enum("test", true,
                newEnumValue("NOT2EXP", 4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBitWiseHasNoNumber15() {
        new Enum("test", true,
                newEnumValue("TWO", 2),
                newEnumValue("NOT2EXP", 15));
    }

    @Test public void testBitWiseHasANumber16() {
        new Enum("test", true,
                newEnumValue("TWO", 2),
                newEnumValue("NOT2EXP", 16));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBitWiseHasNoNumber17() {
        new Enum("test", true,
                newEnumValue("TWO", 2),
                newEnumValue("NOT2EXP", 17));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBitWiseHasNoNumber18() {
        new Enum("test", true,
                newEnumValue("TWO", 2),
                newEnumValue("NOT2EXP", 18));
    }

    @Test public void testBitWiseIsBitWise() {
        Enum result = new Enum("test", true,
                newEnumValue("TWO", 2));
        assertTrue("A bitwise Enum should report to be bitwise", result.isBitwise());
    }

    private Enum enumWithValues() {
        return new Enum("test", false,
                newEnumValue("ZERO", 0, "\"nothing\""),
                newEnumValue("ONE", 1),
                newEnumValue("TWO", 2));
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

    @Test public void enumCountNotSpecified() {
        Enum result = enumWithValues();
        assertNull("Counting element should not be added when unspecified", result.getCount());
    }

    @Test public void enumCount() {
        Enum result = new Enum("test", "ELEMENTS",
                newEnumValue("ZERO", 0, "\"nothing\""),
                newEnumValue("ONE", 1),
                newEnumValue("TWO", 2));
        assertNotNull("Counting element not added", result.getCount());
        assertFalse("Counting element should be invalid", result.getCount().isValid());
        assertEquals("Counting element has wrong name", "ELEMENTS", result.getCount().getEnumValueName());
        assertEquals("Counting element has wrong toStringName", "\"ELEMENTS\"", result.getCount().getToStringName());
        assertEquals("Counting element don't count elements correctly", 3, result.getCount().getNumber());
    }

    @Test public void enumCount2Elements() {
        Enum result = new Enum("test", "ELEMENTS",
                newEnumValue("ZERO", 0, "\"nothing\""),
                newEnumValue("ONE", 1));
        assertNotNull("Counting element not added", result.getCount());
        assertFalse("Counting element should be invalid", result.getCount().isValid());
        assertEquals("Counting element has wrong name", "ELEMENTS", result.getCount().getEnumValueName());
        assertEquals("Counting element has wrong toStringName", "\"ELEMENTS\"", result.getCount().getToStringName());
        assertEquals("Counting element don't count elements correctly", 2, result.getCount().getNumber());
    }

    @Test public void enumCountNamed() {
        Enum result = new Enum("test", "ELEMENTS", "\"the elements\"",
                newEnumValue("ZERO", 0, "\"nothing\""),
                newEnumValue("ONE", 1));
        assertNotNull("Counting element not added", result.getCount());
        assertFalse("Counting element should be invalid", result.getCount().isValid());
        assertEquals("Counting element has wrong name", "ELEMENTS", result.getCount().getEnumValueName());
        assertEquals("Counting element has wrong toStringName", "\"the elements\"", result.getCount().getToStringName());
        assertEquals("Counting element don't count elements correctly", 2, result.getCount().getNumber());
    }

    @Test(expected = IllegalArgumentException.class)
    public void enumCountBitwiseSpecified() {
        Enum result = new Enum("test", true, "ELEMENTS", "\"the elements\"",
                newEnumValue("ONE", 1));
    }

    @Test public void enumInvalidBitwise() {
        Enum result = new Enum("test", true,
                newInvalidEnum(-2),
                newEnumValue("ZERO", 0, "\"nothing\""),
                newEnumValue("ONE", 1),
                newEnumValue("TWO", 2));
        assertNotNull("Invalid element not added", result.getInvalidDefault());
        assertFalse("Invalid element should be invalid", result.getInvalidDefault().isValid());
        assertEquals("Invalid element has wrong name", "INVALID", result.getInvalidDefault().getEnumValueName());
        assertEquals("Invalid element has wrong toStringName", "\"INVALID\"", result.getInvalidDefault().getToStringName());
        assertEquals("Invalid element has wrong number", -2, result.getInvalidDefault().getNumber());
    }
}
