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

import org.freeciv.types.FCEnum;

public class Enum extends ClassWriter {
    private String enumClassName;
    private boolean bitwise = false;
    private EnumElement invalidDefault;
    private EnumElement countElement;

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
        this.enumClassName = enumName;

        int numberOfElements = 0;
        for (ClassWriter.EnumElement value: values) {
            this.addEnumerated(value);

            if (value.isValid()) numberOfElements++;

            if (bitwise && (0 < value.getNumber()))
                for (int testAgainst = 1; testAgainst < value.getNumber() * 2; testAgainst = testAgainst * 2) {
                    if (value.getNumber() < testAgainst)
                        throw new IllegalArgumentException("Claims to be bitwise but is not.");
                }
        }
        if (null != cntCode) {
            if (bitwise) throw new IllegalArgumentException("");
            this.countElement = EnumElement.newInvalidEnum(cntCode,
                    (null == cntString? '"' + cntCode + '"': cntString),
                    numberOfElements);
            this.addEnumerated(this.countElement);
        }
        if (enums.containsKey("INVALID")) {
            this.invalidDefault = enums.get("INVALID");
        } else {
            this.invalidDefault = EnumElement.newInvalidEnum(-1);
            this.addEnumerated(this.invalidDefault);
        }

        addObjectConstant("int", "number");
        addObjectConstant("boolean", "valid");
        addObjectConstant("String", "toStringName");

        //TODO: test private constructor generation. perhaps do via Methods.newPrivateConstructor
        addMethod(null, Visibility.PRIVATE, Scope.OBJECT, null, enumName, "int number, String toStringName", null,
                "this(number, toStringName, false);");
        addMethod(null, Visibility.PRIVATE, Scope.OBJECT, null, enumName,
                "int number, String toStringName, boolean valid",
                null,
                "this.number = number;",
                "this.toStringName = toStringName;",
                "this.valid = valid;");

        addPublicReadObjectState(null, "int", "getNumber", "return number;");
        addPublicReadObjectState(null, "boolean", "isValid", "return valid;");
        addPublicReadObjectState(null, "String", "toString", "return toStringName;");

        addReadClassState("/**" + "\n" +
                " * Is the enum bitwise? An enum is bitwise if it's number increase by two's" + "\n" +
                " * exponent." + "\n" +
                " * @return true if the enum is bitwise" + "\n" +
                " */",
                "boolean", "isBitWise", "return " + bitwise + ";");
    }

    public String getEnumClassName() {
        return enumClassName;
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
}
