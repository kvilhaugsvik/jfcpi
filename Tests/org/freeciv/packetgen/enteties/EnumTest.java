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

package org.freeciv.packetgen.enteties;

import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.*;
import org.freeciv.packetgen.enteties.Enum;
import org.freeciv.packetgen.enteties.supporting.IntExpression;
import org.freeciv.packetgen.javaGenerator.Comment;
import org.freeciv.packetgen.javaGenerator.Modifiable;
import org.freeciv.packetgen.javaGenerator.Var;
import org.junit.Test;

import java.util.*;

import static org.freeciv.packetgen.enteties.Enum.EnumElementKnowsNumber.newEnumValue;
import static org.freeciv.packetgen.enteties.Enum.EnumElementKnowsNumber.newInvalidEnum;
import static org.junit.Assert.*;

public class EnumTest {
    @Test public void testBitWiseHasANumber0() {
        Enum.fromArray("test", true,
                newEnumValue("NOT2EXP", 0));
    }

    @Test public void testBitWiseHasANumber1() {
        Enum.fromArray("test", true,
                newEnumValue("NOT2EXP", 1));
    }

    @Test public void testBitWiseHasANumber2() {
        Enum.fromArray("test", true,
                newEnumValue("NOT2EXP", 2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBitWiseHasNoNumber3() {
        Enum.fromArray("test", true,
                newEnumValue("TWO", 2),
                newEnumValue("THREE", 3));
    }

    @Test public void testBitWiseHasANumber4() {
        Enum.fromArray("test", true,
                newEnumValue("NOT2EXP", 4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBitWiseHasNoNumber15() {
        Enum.fromArray("test", true,
                newEnumValue("TWO", 2),
                newEnumValue("NOT2EXP", 15));
    }

    @Test public void testBitWiseHasANumber16() {
        Enum.fromArray("test", true,
                newEnumValue("TWO", 2),
                newEnumValue("NOT2EXP", 16));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBitWiseHasNoNumber17() {
        Enum.fromArray("test", true,
                newEnumValue("TWO", 2),
                newEnumValue("NOT2EXP", 17));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBitWiseHasNoNumber18() {
        Enum.fromArray("test", true,
                newEnumValue("TWO", 2),
                newEnumValue("NOT2EXP", 18));
    }

    @Test public void testBitWiseIsBitWise() {
        Enum result = Enum.fromArray("test", true,
                newEnumValue("TWO", 2));
        assertTrue("A bitwise Enum should report to be bitwise", result.isBitwise());
    }

    private Enum enumWithValues() {
        return Enum.fromArray("test", false,
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
        assertEquals("Wrong name", "test", result.getName());
    }

    @Test public void testPackageIsCorrect() {
        Enum result = enumWithValues();
        assertEquals("Wrong name", "org.freeciv.types", result.getPackage());
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
        assertEquals("Enum element number not found", "1", result.getEnumValue("ONE").getValueGenerator());
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
        Enum result = Enum.fromArray("test", "ELEMENTS",
                newEnumValue("ZERO", 0, "\"nothing\""),
                newEnumValue("ONE", 1),
                newEnumValue("TWO", 2));
        assertNotNull("Counting element not added", result.getCount());
        assertFalse("Counting element should be invalid", result.getCount().isValid());
        assertEquals("Counting element has wrong name", "ELEMENTS", result.getCount().getEnumValueName());
        assertEquals("Counting element has wrong toStringName", "\"ELEMENTS\"", result.getCount().getToStringName());
        assertEquals("Counting element don't count elements correctly", "3", result.getCount().getValueGenerator());
    }

    @Test public void enumCount2Elements() {
        Enum result = Enum.fromArray("test", "ELEMENTS",
                newEnumValue("ZERO", 0, "\"nothing\""),
                newEnumValue("ONE", 1));
        assertNotNull("Counting element not added", result.getCount());
        assertFalse("Counting element should be invalid", result.getCount().isValid());
        assertEquals("Counting element has wrong name", "ELEMENTS", result.getCount().getEnumValueName());
        assertEquals("Counting element has wrong toStringName", "\"ELEMENTS\"", result.getCount().getToStringName());
        assertEquals("Counting element don't count elements correctly", "2", result.getCount().getValueGenerator());
    }

    @Test public void enumCountNamed() {
        Enum result = Enum.fromArray("test", "ELEMENTS", "\"the elements\"",
                newEnumValue("ZERO", 0, "\"nothing\""),
                newEnumValue("ONE", 1));
        assertNotNull("Counting element not added", result.getCount());
        assertFalse("Counting element should be invalid", result.getCount().isValid());
        assertEquals("Counting element has wrong name", "ELEMENTS", result.getCount().getEnumValueName());
        assertEquals("Counting element has wrong toStringName", "\"the elements\"", result.getCount().getToStringName());
        assertEquals("Counting element don't count elements correctly", "2", result.getCount().getValueGenerator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void enumCountBitwiseSpecified() {
        Enum result = new Enum("test", false, true, "ELEMENTS", "\"the elements\"", Collections.<Requirement>emptySet(),
                Arrays.<Enum.EnumElementFC>asList(newEnumValue("ONE", 1)));
    }

    @Test public void enumInvalidBitwise() {
        Enum result = Enum.fromArray("test", true,
                newInvalidEnum(-2),
                newEnumValue("ZERO", 0, "\"nothing\""),
                newEnumValue("ONE", 1),
                newEnumValue("TWO", 2));
        assertNotNull("Invalid element not added", result.getInvalidDefault());
        assertFalse("Invalid element should be invalid", result.getInvalidDefault().isValid());
        assertEquals("Invalid element has wrong name", "INVALID", result.getInvalidDefault().getEnumValueName());
        assertEquals("Invalid element has wrong toStringName", "\"INVALID\"", result.getInvalidDefault().getToStringName());
        assertEquals("Invalid element has wrong number", "-2", result.getInvalidDefault().getValueGenerator());
    }

    @Test public void enumProvidesValues() {
        HashMap<String, Constant> constants = new HashMap<String, Constant>();
        Enum hasValues = Enum.fromArray("Count", false,
                newEnumValue("ONE", 1),
                newEnumValue("TWO", 2));

        for (IDependency constant: hasValues.getEnumConstants())
            constants.put(((Constant)constant).getName(), (Constant)constant);

        assertTrue("ONE is missing", constants.containsKey("ONE"));
        assertTrue("ONE don't require it's enum", constants.get("ONE").getReqs()
                .contains(new Requirement("enum Count", Requirement.Kind.AS_JAVA_DATATYPE)));
        assertEquals("ONE has wrong value", "org.freeciv.types.Count.ONE.getNumber()",
                constants.get("ONE").getExpression());

        assertTrue("TWO is missing", constants.containsKey("TWO"));
        assertTrue("TWO don't require it's enum", constants.get("TWO").getReqs()
                .contains(new Requirement("enum Count", Requirement.Kind.AS_JAVA_DATATYPE)));
        assertEquals("TWO has wrong value", "org.freeciv.types.Count.TWO.getNumber()",
                constants.get("TWO").getExpression());
    }

    @Test public void enumRequiresOther() {
        Requirement constantReferedTo = new Requirement("START_VALUE", Requirement.Kind.VALUE);
        Enum inNeed = Enum.fromArray("NeedOther", Arrays.asList(constantReferedTo),
                Enum.EnumElementFC.newEnumValue("ONE", IntExpression.variable("Constants.START_VALUE")),
                Enum.EnumElementFC.newEnumValue("TWO",
                        IntExpression.binary("+",
                                IntExpression.variable("ONE.getNumber()"),
                                IntExpression.integer("1"))));
        assertTrue("Enum should require the given requirements", inNeed.getReqs().contains(constantReferedTo));
    }

    @Test public void testEnumElement() {
        assertEquals("Generated source not as expected",
                "ONE(1, \"one\")",
                newEnumValue("ONE", 1, "\"one\"").toString());
    }

    @Test public void testEnumElementNoToStringButOkEntryPoint() {
        assertEquals("Generated source not as expected",
                "ONE(1, \"ONE\")",
                newEnumValue("ONE", 1).toString());
    }

    @Test public void testEnumElementInvalid() {
        assertEquals("Generated source not as expected",
                "INVALID(-1, \"INVALID\", false)",
                newInvalidEnum(-1).toString());
    }

    @Test public void testEnumElementInvalidNonDefaultNumber() {
        assertEquals("Generated source not as expected",
                "INVALID(64, \"INVALID\", false)",
                newInvalidEnum(64).toString());
    }

    @Test public void testEnumElementCount() {
        assertEquals("Generated source not as expected",
                "NUMBEROFF(64, \"number off\", false)",
                newInvalidEnum("NUMBEROFF", "\"number off\"", 64).toString());
    }

    @Test public void testEnumElementCommented() {
        assertEquals("Generated source not as expected",
                "/* An integer */ ONE(1, \"one\")",
                newEnumValue(Comment.c("An integer"), "ONE", 1, "\"one\"").toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnumElementNoName() {
        assertEquals("Generated source not as expected",
                "ONE(1, \"one\")",
                newEnumValue(null, 1, "\"one\"").toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnumElementNoToString() {
        assertEquals("Generated source not as expected",
                "ONE(1, \"one\")",
                newEnumValue("ONE", 1, null).toString());
    }
}
