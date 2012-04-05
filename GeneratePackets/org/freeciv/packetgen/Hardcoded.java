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

import java.util.*;

//TODO: Move data to file
public class Hardcoded {
    private static final Collection<FieldTypeBasic> primitiveFieldTypes = Arrays.asList(
                new FieldTypeBasic("uint32", "int",
                        "Long",
                        new String[]{"this.value = value;"},
                        readUIntCode(4, "Long", "value"),
                        writeWriteUInt(4),
                        "return 4;",
                        false, Collections.<Requirement>emptySet()),
                getFloat("100"),
                getFloat("10000"),
                getFloat("1000000"),
                new FieldTypeBasic("string", "char",
                        "String",
                        new String[]{
                                arrayEaterScopeCheck("arraySize < value.length()"),
                                "this.value = value;"
                        },
                        "StringBuffer buf = new StringBuffer();" + "\n" +
                                "byte letter = from.readByte();" + "\n" +
                                "int read = 0;" + "\n" +
                                "while (0 != letter) {" + "\n" +
                                "read++;" + "\n" +
                                arrayEaterScopeCheck("arraySize < read") +
                                "buf.append((char)letter);" + "\n" +
                                "letter = from.readByte();" + "\n" +
                                "}" + "\n" +
                                "value = buf.toString();",
                        "to.writeBytes(" + "value" + ");\n" +
                                "to.writeByte(0);",
                        "return " + "value" + ".length() + 1;",
                        true, Collections.<Requirement>emptySet()),
                new TerminatedArray("tech_list", "int",
                        new Requirement("MAX_NUM_TECH_LIST", Requirement.Kind.VALUE),
                        new Requirement("A_LAST", Requirement.Kind.VALUE)),
                new TerminatedArray("unit_list", "int",
                        new Requirement("MAX_NUM_UNIT_LIST", Requirement.Kind.VALUE),
                        new Requirement("U_LAST", Requirement.Kind.VALUE)),
                new TerminatedArray("building_list", "int",
                        new Requirement("MAX_NUM_BUILDING_LIST", Requirement.Kind.VALUE),
                        new Requirement("B_LAST", Requirement.Kind.VALUE)),
                new FieldTypeBasic("bool8", "bool",
                        "Boolean",
                        new String[]{"this.value = value;"},
                        "value = from.readBoolean();",
                        "to.writeBoolean(value);",
                        "return 1;",
                        false, Collections.<Requirement>emptySet())
    );

    public static final Collection<NetworkIO> netCon = Arrays.asList(
        new NetworkIO("bitvector"),
        new NetworkIO("uint8", "return 1;", "from.readUnsignedByte()", "to.writeByte"), // to.writeByte wraps around so
        new NetworkIO("sint8", "return 1;", "(int) from.readByte()", "to.writeByte"),   // -128 shares encoding with 128
        new NetworkIO("uint16", "return 2;", "(int) from.readChar()", "to.writeChar"),
        new NetworkIO("sint16", "return 2;", "(int) from.readShort()", "to.writeShort"),
        new NetworkIO("sint32", "return 4;", "from.readInt()", "to.writeInt"));

    private static final Collection<JavaNative> nativeJava = Arrays.asList(
            new JavaNative("int", "Integer")
    );

    public static String arrayEaterScopeCheck(String check) {
        return "if (" + check + ") " +
                "throw new IllegalArgumentException(\"Value out of scope\");" + "\n";
    }

    public static Collection<IDependency> values() {
        HashSet<IDependency> out = new HashSet<IDependency>(primitiveFieldTypes);
        out.addAll(netCon);
        out.addAll(nativeJava);
        return out;
    }

    private static FieldTypeBasic getFloat(String times) {
        return new FieldTypeBasic("float" + times, "float",
                "Float",
                new String[]{"this.value = value;"},
                "value = from.readFloat() / " + times + ";",
                "to.writeFloat(value * " + times + ");",
                "return 4;",
                false, Collections.<Requirement>emptySet());
    }

    public static String writeWriteUInt(int bytenumber) {
        String out = "";
        while (1 <= bytenumber) {
            out += "to.writeByte((int) ((value >>> ((" + bytenumber + " - 1) * 8)) & 0xFF));" + "\n";
            bytenumber--;
        }
        return out;
    }

    public static String readUIntCode(int bytenumber, String Javatype, String var) {
        String out = var + " = ";
        out += "(" + Javatype.toLowerCase() + ")";
        while (1 <= bytenumber) {
            out += "(from.readUnsignedByte() << 8 * (" + bytenumber + " - 1)) " +
                    (1 < bytenumber ? "+" : ";") + "\n";
            bytenumber--;
        }

        return out;
    }

    private static class JavaNative implements IDependency, FieldTypeBasic.Generator {
        private final Requirement meInC;
        private final String meInJava;

        private JavaNative(String nameInC, String nameInJava) {
            this.meInJava = nameInJava;
            this.meInC = new Requirement(nameInC, Requirement.Kind.AS_JAVA_DATATYPE);
        }

        @Override
        public FieldTypeBasic getBasicFieldTypeOnInput(NetworkIO io) {
            return new FieldTypeBasic(io.getIFulfillReq().getName(), meInC.getName(),
                    meInJava,
                    new String[]{"this.value = value;"},
                    "value = " + io.getRead() + ";",
                    io.getWrite() + "(value);",
                    io.getSize(),
                    false, Collections.<Requirement>emptySet());
        }

        @Override
        public Requirement.Kind needsDataInFormat() {
            return Requirement.Kind.FROM_NETWORK_TO_INT;
        }

        @Override
        public Collection<Requirement> getReqs() {
            return Collections.<Requirement>emptySet();
        }

        @Override
        public Requirement getIFulfillReq() {
            return meInC;
        }
    }
}
