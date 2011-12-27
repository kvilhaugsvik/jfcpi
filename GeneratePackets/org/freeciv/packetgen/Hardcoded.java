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

import java.util.Collection;
import java.util.HashMap;

//TODO: Move data to file
public class Hardcoded {
    private final HashMap<String, FieldTypeBasic> data = new HashMap<String, FieldTypeBasic>();

    Hardcoded() {
        for (FieldTypeBasic src: new FieldTypeBasic[]{
                new FieldTypeBasic("uint8", "int",
                        "Integer",
                        "value = from.readUnsignedByte();",
                        "to.writeByte(value);",
                        "return 1;",
                        false),
                new FieldTypeBasic("uint16", "int",
                        "Integer",
                        "value = (int) from.readChar();",
                        "to.writeChar(value);",
                        "return 2;",
                        false),
                new FieldTypeBasic("uint32", "int",
                        "Long",
                        DataIO.readUIntCode(4, "Long", "value"),
                        DataIO.writeWriteUInt(4),
                        "return 4;",
                        false),
                new FieldTypeBasic("string", "char",
                        "String",
                        "StringBuffer buf = new StringBuffer();" + "\n" +
                                "byte letter = from.readByte();" + "\n" +
                                "while (0 != letter) {" + "\n" +
                                "buf.append((char)letter);" + "\n" +
                                "letter = from.readByte();" + "\n" +
                                "}" + "\n" +
                                "value = buf.toString();",
                        "to.writeBytes(" + "value" + ");\n" +
                                "to.writeByte(0);",
                        "return " + "value" + ".length() + 1;",
                        false),
                new FieldTypeBasic("bool8", "bool",
                        "Boolean",
                        "value = from.readBoolean();",
                        "to.writeBoolean(value);",
                        "return 1;",
                        false),
                new FieldTypeBasic("sint8", "int",
                        "Byte",
                        "value = from.readByte();",
                        "to.writeByte(value);",
                        "return 2;",
                        false),
                new FieldTypeBasic("sint16", "int",
                        "Short",
                        "value = from.readShort();",
                        "to.writeShort(value);",
                        "return 2;",
                        false),
                new FieldTypeBasic("sint32", "int",
                        "Integer",
                        "value = from.readInt();",
                        "to.writeInt(value);",
                        "return 4;",
                        false),
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
                new FieldTypeBasic("sint16", "enum event_type",
                        "event_type",
                        "value = event_type.valueOf(from.readShort());",
                        "to.writeShort(value.getNumber());",
                        "return 2;",
                        true)
        }) {
            data.put(src.getFieldTypeBasic(), src);
        }
    }

    public FieldTypeBasic getBasicFieldType(String src) {
        return data.get(src);
    }

    public Collection<FieldTypeBasic> values() {
        return data.values();
    }

    private static FieldTypeBasic getUInt8Enum(String named) {
        return new FieldTypeBasic("uint8", "enum " + named,
                named,
                "value = " + named + ".valueOf(from.readUnsignedByte());",
                "to.writeByte(value.getNumber());",
                "return 1;",
                true);
    }
}
