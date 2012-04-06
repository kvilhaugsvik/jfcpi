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

import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.Requirement;

import java.util.*;

//TODO: Move data to file
public class Hardcoded {
    private static final Collection<FieldTypeBasic> primitiveFieldTypes = Arrays.asList(
            new FieldTypeBasic("uint32", "int",
                               "Long",
                               new String[]{"this.value = value;"},
                               "int bufferValue = from.readInt();" + "\n" +
                                       "if (0 <= bufferValue) {" + "\n" +
                                       "value = (long)bufferValue;" + "\n" +
                                       "} else {" + "\n" +
                                       "final long removedByCast = (-1L * Integer.MIN_VALUE) + Integer.MAX_VALUE + " +
                                       "1L;" + "\n" +
                                       "value = (long)bufferValue + removedByCast;" + "\n" +
                                       "}",
                               "to.writeInt(value.intValue()); // int is two's compliment so a uint32 don't lose " +
                                       "information",
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
            NetworkIO.withBytesAsIntermediate("bitvector"),
            NetworkIO.witIntAsIntermediate("uint8", "1", "from.readUnsignedByte()", "to.writeByte"),
            // to.writeByte wraps around so -128 shares encoding with 128
            NetworkIO.witIntAsIntermediate("sint8", "1", "(int) from.readByte()", "to.writeByte"),
            NetworkIO.witIntAsIntermediate("uint16", "2", "(int) from.readChar()", "to.writeChar"),
            NetworkIO.witIntAsIntermediate("sint16", "2", "(int) from.readShort()", "to.writeShort"),
            NetworkIO.witIntAsIntermediate("sint32", "4", "from.readInt()", "to.writeInt"));

    private static final Collection<IDependency> nativeJava = Arrays.asList(
            (IDependency)(new SimpleTypeAlias("int", "Integer", null))
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
}
