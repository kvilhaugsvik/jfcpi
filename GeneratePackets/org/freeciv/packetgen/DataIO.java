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

public class DataIO {
    public static String writeWriteUInt(int bytenumber) {
        String out = "";
        while (bytenumber >= 1) {
            out += "\t\t" + "to.writeByte((int) ((value >>> ((" + bytenumber + " - 1) * 8)) & 0xFF));\n";
            bytenumber--;
        }
        return out;
    }

    public static String readUIntCode(int bytenumber, String Javatype, String var) {
        String out = var + " = ";
        out += "(" + Javatype.toLowerCase() + ")";
        while (bytenumber >= 1) {
            out += "\t\t" + "(from.readUnsignedByte() << 8 * (" + bytenumber + " - 1)) " +
                    (bytenumber > 1 ? "+" : ";") + "\n";
            bytenumber--;
        }

        return out;
    }
}
