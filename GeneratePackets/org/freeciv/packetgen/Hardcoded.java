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

import java.util.HashMap;

//TODO: Move data to file
public class Hardcoded {
    private HashMap<String, JavaSrc> data = new HashMap<String, JavaSrc>();
    private static Hardcoded instance;

    private Hardcoded() {
        for (JavaSrc src: new JavaSrc[]{
            new JavaSrc("uint8(int)",
                    "Integer",
                    "value = from.readUnsignedByte();" + "\n",
                    "to.writeByte(value);" + "\n",
                    "return 1;"),
            new JavaSrc("uint16(int)",
                    "Integer",
                    "value = (int) from.readChar();" + "\n",
                    "to.writeChar(value);" + "\n",
                    "return 2;"),
            new JavaSrc("uint32(int)",
                    "Long",
                    DataIO.readUIntCode(4, "Long", "value"),
                    DataIO.writeWriteUInt(4),
                    "return 4;"),
            new JavaSrc("string(char)",
                    "String",
                    "StringBuffer buf = new StringBuffer();\n" +
                            "\t\tbyte letter = from.readByte();\n" +
                            "\t\twhile (letter != 0) {\n" +
                            "\t\t\tbuf.append((char)letter);\n" +
                            "\t\t\tletter = from.readByte();\n" +
                            "\t\t}\n" +
                            "\t\tvalue = buf.toString();",
                    "to.writeBytes(" + "value" + ");\n" +
                        "\t\t" + "to.writeByte(0);" + "\n",
                    "return " + "value" + ".length() + 1;\n"),
            new JavaSrc("bool8(bool)",
                    "Boolean",
                    "value = from.readBoolean();" + "\n",
                    "to.writeBoolean(value);" + "\n",
                    "return 1;"),
            new JavaSrc("sint8(int)",
                    "Byte",
                    "value = from.readByte();" + "\n",
                    "to.writeByte(value);" + "\n",
                    "return 2;"),
            new JavaSrc("sint16(int)",
                    "Short",
                    "value = from.readShort();" + "\n",
                    "to.writeShort(value);" + "\n",
                    "return 2;"),
            new JavaSrc("sint32(int)",
                    "Integer",
                    "value = from.readInt();" + "\n",
                    "to.writeInt(value);" + "\n",
                    "return 4;")
        }) {
            data.put(src.getCSrc(), src);
        }
    }

    public static JavaSrc getJTypeFor(String src) {
        if (null == instance) {
            instance = new Hardcoded();
        }
        return instance.data.get(src);
    }
}
