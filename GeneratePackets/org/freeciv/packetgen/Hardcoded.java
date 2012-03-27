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
    private static final HashMap<String, FieldTypeBasic> data = new HashMap<String, FieldTypeBasic>();

    static {
        for (FieldTypeBasic src: new FieldTypeBasic[]{
                new FieldTypeBasic("uint8", "int",
                        "Integer",
                        new String[]{"this.value = value;"},
                        "value = from.readUnsignedByte();",
                        "to.writeByte(value);",
                        "return 1;",
                        false, Collections.<Requirement>emptySet()),
                new FieldTypeBasic("uint16", "int",
                        "Integer",
                        new String[]{"this.value = value;"},
                        "value = (int) from.readChar();",
                        "to.writeChar(value);",
                        "return 2;",
                        false, Collections.<Requirement>emptySet()),
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
                new FieldTypeBasic("bool8", "bool",
                        "Boolean",
                        new String[]{"this.value = value;"},
                        "value = from.readBoolean();",
                        "to.writeBoolean(value);",
                        "return 1;",
                        false, Collections.<Requirement>emptySet()),
                new FieldTypeBasic("sint8", "int",
                        "Byte",
                        new String[]{"this.value = value;"},
                        "value = from.readByte();",
                        "to.writeByte(value);",
                        "return 2;",
                        false, Collections.<Requirement>emptySet()),
                new FieldTypeBasic("sint16", "int",
                        "Short",
                        new String[]{"this.value = value;"},
                        "value = from.readShort();",
                        "to.writeShort(value);",
                        "return 2;",
                        false, Collections.<Requirement>emptySet()),
                new FieldTypeBasic("sint32", "int",
                        "Integer",
                        new String[]{"this.value = value;"},
                        "value = from.readInt();",
                        "to.writeInt(value);",
                        "return 4;",
                        false, Collections.<Requirement>emptySet()),
                getUInt8Enum("unit_activity"),
                getUInt8Enum("airlifting_style"),
                getUInt8Enum("authentication_type"),
                getUInt8Enum("base_gui_type"),
                getUInt8Enum("borders_mode"),
                getUInt8Enum("clause_type"),
                getUInt8Enum("cmdlevel"),
                getUInt8Enum("diplomacy_mode"),
                getUInt8Enum("diplomat_actions"),
                getUInt8Enum("direction8"),
                getUInt8Enum("effect_type"),
                getUInt8Enum("gui_type"),
                getUInt8Enum("impr_genus_id"),
                getUInt8Enum("known_type"),
                getUInt8Enum("unit_orders"),
                getUInt8Enum("phase_mode_types"),
                getUInt8Enum("spaceship_place_type"),
                getUInt8Enum("report_type"),
                getUInt8Enum("req_range"),
                getUInt8Enum("universals_n"),
                getUInt8Enum("special_river_move"),
                getUInt8Enum("sset_class"),
                getUInt8Enum("sset_type"),
                getEnum("tile_special_type", new NetworkIO("uint16", "return 2;", "(int) from.readChar()", "to.writeChar")),
                getEnum("event_type", new NetworkIO("sint16", "return 2;", "from.readShort()", "to.writeShort"))

        }) {
            data.put(src.getFieldTypeBasic(), src);
        }
    }

    public static String arrayEaterScopeCheck(String check) {
        return "if (" + check + ") " +
                "throw new IllegalArgumentException(\"Value out of scope\");" + "\n";
    }

    public static FieldTypeBasic getBasicFieldType(String src) {
        return data.get(src);
    }

    public static Collection<FieldTypeBasic> values() {
        return Collections.unmodifiableCollection(data.values());
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

    private static FieldTypeBasic getUInt8Enum(String named) {
        return getEnum(named, new NetworkIO("uint8", "return 1;", "from.readUnsignedByte()", "to.writeByte"));
    }

    public static FieldTypeBasic getEnum(String named, NetworkIO io) {
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
}
