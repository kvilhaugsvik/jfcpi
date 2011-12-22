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

    public Enum(String enumName, boolean bitwise, ClassWriter.EnumElement... values) {
        super(ClassKind.ENUM, FCEnum.class.getPackage(), null, "Freeciv C code", enumName, "FCEnum");

        this.bitwise = bitwise;
        this.enumClassName = enumName;

        for (ClassWriter.EnumElement value: values) {
            this.addEnumerated(value);
            if (bitwise && (0 != value.getNumber()))
                for (int testAgainst = 1; testAgainst < value.getNumber() * 2; testAgainst = testAgainst * 2) {
                    if (value.getNumber() < testAgainst)
                        throw new IllegalArgumentException("Claims to be bitwise but is not.");
                }
        }

        addObjectConstant("int", "number");
        addObjectConstant("String", "toStringName");

        //TODO: test private constructor generation. perhaps do via Methods.newPrivateConstructor
        addMethod(null, Visibility.PRIVATE, Scope.OBJECT, null, enumName, "int number, String toStringName", null,
                "this.number = number;",
                "this.toStringName = toStringName;");

        addPublicReadObjectState(null, "int", "getNumber", "return number;");
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

    ClassWriter.EnumElement getEnumValue(String named) {
        return enums.get(named);
    }
}
