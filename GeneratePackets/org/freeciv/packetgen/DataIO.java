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
            out += "to.writeByte((int) ((value >>> ((" + bytenumber + " - 1) * 8)) & 0xFF));" + "\n";
            bytenumber--;
        }
        return out;
    }

    public static String readUIntCode(int bytenumber, String Javatype, String var) {
        String out = var + " = ";
        out += "(" + Javatype.toLowerCase() + ")";
        while (bytenumber >= 1) {
            out += "(from.readUnsignedByte() << 8 * (" + bytenumber + " - 1)) " +
                    (bytenumber > 1 ? "+" : ";") + "\n";
            bytenumber--;
        }

        return out;
    }

    public static String publicConstructor(String comment,
                                           String name,
                                           String paramList,
                                           String exceptionList,
                                           String... body) {
        return publicDynamicMethod(comment, null, name, paramList, exceptionList, body);
    }

    public static String publicConstructorNoExceptions(String comment,
                                           String name,
                                           String paramList,
                                           String... body) {
        return publicConstructor(comment, name, paramList, null, body);
    }

    public static String publicReadObjectState(String comment,
                                    String type,
                                    String name,
                                    String... body) {
        return publicDynamicMethod(comment, type, name, null, null, body);
    }

    public static String publicDynamicMethod(String comment,
                                             String type,
                                             String name,
                                             String paramList,
                                             String exceptionList,
                                             String... body) {
        return fullMethod(comment, "public", null, type, name, paramList, exceptionList, body);
    }

    public static String fullMethod(String comment,
                                       String access,
                                       String place,
                                       String type,
                                       String name,
                                       String paramList,
                                       String exceptionList,
                                       String... body) {
        String out = (null == comment? "" : indent(comment) + "\n");
        out += ifIs("\t", access, " ") + ifIs(place, " ") + ifIs(type, " ") +
                name + "(" + ifIs(paramList) + ") " + ifIs("throws ", exceptionList, " ") + "{" + "\n";
        for (String line: body) {
            out += (line != ""? "\t" + "\t" + line : "") + "\n";
        }
        out += "\t" + "}" + "\n";
        return out;
    }

    private static String indent(String code) {
        return "\t" + code.replace("\n", "\n\t");
    }

    private static String ifIs(String element) {
        return ifIs("", element, "");
    }

    private static String ifIs(String element, String after) {
        return ifIs("", element, after);
    }

    private static String ifIs(String before, String element, String after) {
        return (null == element? "" : before + element + after);
    }
}
