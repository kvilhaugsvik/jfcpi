/*
 * Copyright (c) 2011, 2012. Sveinung Kvilhaugsvik
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

import org.freeciv.types.FCEnum;

import java.util.*;

public class Enum extends ClassWriter implements IDependency.ManyFulfiller, FieldTypeBasic.Generator {
    private final boolean bitwise;
    private final EnumElement invalidDefault;
    private final EnumElement countElement;

    public Enum(String enumName, boolean bitwise, ClassWriter.EnumElement... values) {
        this(enumName, bitwise, null, null, values);
    }

    public Enum(String enumName, String cntCode, ClassWriter.EnumElement... values) {
        this(enumName, cntCode, null, values);
    }

    public Enum(String enumName, String cntCode, String cntString, ClassWriter.EnumElement... values) {
        this(enumName, false, cntCode, cntString, values);
    }

    public Enum(String enumName, boolean bitwise, String cntCode, String cntString, ClassWriter.EnumElement... values) {
        super(ClassKind.ENUM, FCEnum.class.getPackage(), null, "Freeciv C code", enumName, "FCEnum");

        this.bitwise = bitwise;

        int numberOfElements = 0;
        for (ClassWriter.EnumElement value: values) {
            this.addEnumerated(value);

            if (value.isValid()) numberOfElements++;

            if (bitwise)
                if (!(value instanceof EnumElementKnowsNumber))
                    throw new IllegalArgumentException("Only spec enums can be declared bitwise");
                else if (0 < ((EnumElementKnowsNumber)value).getNumber())
                    for (int testAgainst = 1; testAgainst < ((EnumElementKnowsNumber)value).getNumber() * 2; testAgainst = testAgainst * 2)
                        if (((EnumElementKnowsNumber)value).getNumber() < testAgainst)
                            throw new IllegalArgumentException("Claims to be bitwise but is not.");
        }
        if (null != cntCode) {
            if (bitwise) throw new IllegalArgumentException("");
            this.countElement = EnumElementKnowsNumber.newInvalidEnum(cntCode,
                    (null == cntString ? '"' + cntCode + '"' : cntString),
                    numberOfElements);
            this.addEnumerated(this.countElement);
        } else {
            this.countElement = null;
        }
        if (enums.containsKey("INVALID")) {
            this.invalidDefault = enums.get("INVALID");
        } else {
            this.invalidDefault = EnumElementKnowsNumber.newInvalidEnum(-1);
            this.addEnumerated(this.invalidDefault);
        }

        addObjectConstant("int", "number");
        addObjectConstant("boolean", "valid");
        addObjectConstant("String", "toStringName");

        //TODO: test private constructor generation. perhaps do via Methods.newPrivateConstructor
        addMethod(null, Visibility.PRIVATE, Scope.OBJECT, null, enumName, "int number, String toStringName", null,
                "this(number, toStringName, true);");
        addMethod(null, Visibility.PRIVATE, Scope.OBJECT, null, enumName,
                "int number, String toStringName, boolean valid",
                null,
                "this.number = number;",
                "this.toStringName = toStringName;",
                "this.valid = valid;");

        addMethodPublicReadObjectState(null, "int", "getNumber", "return number;");
        addMethodPublicReadObjectState(null, "boolean", "isValid", "return valid;");
        addMethodPublicReadObjectState(null, "String", "toString", "return toStringName;");

        addMethodReadClassState("/**" + "\n" +
                " * Is the enum bitwise? An enum is bitwise if it's number increase by two's" + "\n" +
                " * exponent." + "\n" +
                " * @return true if the enum is bitwise" + "\n" +
                " */",
                "boolean", "isBitWise", "return " + bitwise + ";");

        addMethod("", Visibility.PUBLIC, Scope.CLASS, this.getName(), "valueOf", "int number", null,
                "for (" + this.getName() + " element: values()) {",
                "if (element.getNumber() == number) {",
                "return element;",
                "}",
                "}",
                "return INVALID;");
    }

    public void addEnumerated(String comment,
                              String enumName,
                              int number,
                              String toStringName) {
        addEnumerated(EnumElementKnowsNumber.newEnumValue(comment, enumName, number, toStringName));
    }

    public boolean isBitwise() {
        return bitwise;
    }

    public ClassWriter.EnumElement getInvalidDefault() {
        return invalidDefault;
    }

    public ClassWriter.EnumElement getCount() {
        return countElement;
    }

    ClassWriter.EnumElement getEnumValue(String named) {
        return enums.get(named);
    }

    Collection<IDependency> getEnumConstants() {
        Collection<IDependency> out = new LinkedList<IDependency>();
        for (String valueName : enums.keySet()) {
            out.add(new Constant(valueName, IntExpression.readFromOther(this,
                    this.getPackage() + "." + this.getName() + "." + valueName + ".getNumber()")));
        }
        return out;
    }

    @Override
    public Collection<Requirement> getReqs() {
        return Collections.EMPTY_SET;
    }

    @Override
    public Requirement getIFulfillReq() {
        return new Requirement(super.getName(), Requirement.Kind.ENUM);
    }

    @Override
    public Collection<Requirement> getIAlsoFulfillReqs() {
        return Arrays.<Requirement>asList(new Requirement("enum " + super.getName(), Requirement.Kind.AS_JAVA_DATATYPE));
    }

    @Override
    public FieldTypeBasic getBasicFieldTypeOnInput(NetworkIO io) {
        String named = this.getName();
        HashSet<Requirement> req = new HashSet<Requirement>();
        req.add(new Requirement(named, Requirement.Kind.ENUM));
        return new FieldTypeBasic(io.getIFulfillReq().getName(), "enum " + named,
                named,
                new String[]{"this.value = value;"},
                "value = " + named + ".valueOf(" + io.getRead() + ");",
                io.getWrite() + "(value.getNumber());",
                io.getSize(),
                false, req);
    }

    @Override
    public Requirement.Kind needsDataInFormat() {
        return Requirement.Kind.FROM_NETWORK_TO_INT;
    }

    static class EnumElementKnowsNumber extends ClassWriter.EnumElement {
        private final int number;

        EnumElementKnowsNumber(String comment, String elementName, int number, String toStringName, boolean valid) {
            super(comment, elementName, number + "", toStringName, valid);

            if (null == elementName)
                throw new IllegalArgumentException("All elements of enums must have names");

            // Look up numbers in a uniform way
            if (null == toStringName)
                throw new IllegalArgumentException("All elements of enums must have toStringNames");

            this.number = number;
        }

        public int getNumber() {
            return number;
        }

        public int compareTo(EnumElementKnowsNumber other) {
            if (other.getNumber() == this.getNumber())
                return 0;
            if (0 <= this.getNumber() && 0 > other.getNumber())
                return -1;
            if (0 > this.getNumber() && 0 <= other.getNumber())
                return 1;
            return (this.getNumber() < other.getNumber())? -1: 1;
        }

        @Override
        public int compareTo(EnumElement that) {
            return that instanceof EnumElementKnowsNumber ? compareTo((EnumElementKnowsNumber) that) : -1;
        }

        static EnumElementKnowsNumber newEnumValue(String enumValueName, int number) {
            return newEnumValue(enumValueName, number, '"' + enumValueName +  '"');
        }

        static EnumElementKnowsNumber newEnumValue(String enumValueName, int number, String toStringName) {
            return newEnumValue(null, enumValueName, number, toStringName);
        }

        static EnumElementKnowsNumber newEnumValue(String comment, String enumValueName, int number, String toStringName) {
            return new EnumElementKnowsNumber(comment, enumValueName, number, toStringName, true);
        }

        public static EnumElementKnowsNumber newInvalidEnum(int value) {
            return newInvalidEnum("INVALID", "\"INVALID\"",  value);
        }

        public static EnumElementKnowsNumber newInvalidEnum(String nameInCode, String toStringName, int value) {
            return new EnumElementKnowsNumber(null, nameInCode, value, toStringName, false);
        }
    }
}
