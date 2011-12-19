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

import java.util.LinkedList;
import java.util.Arrays;
import java.util.List;

public class Packet {
    private final String name;
    private final int number;
    private final Field[] fields;
    private final boolean hasTwoBytePacketNumber;

    public Packet(String name, int number, boolean hasTwoBytePacketNumber, Field... fields) {
        this.name = name;
        this.number = number;
        this.fields = fields;
        this.hasTwoBytePacketNumber = hasTwoBytePacketNumber;
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    public String toString() {
        ClassWriter code = new ClassWriter(org.freeciv.packet.Packet.class.getPackage(),
                new String[]{
                        "org.freeciv.packet.fieldtype.*",
                        null,
                        "java.io.DataInput",
                        "java.io.DataOutput",
                        "java.io.IOException"
                },
                "Freeciv's protocol definition",
                name,
                "Packet");

        code.addConstant("int", "number", number + "");
        code.addConstant("boolean", "hasTwoBytePacketNumber", hasTwoBytePacketNumber + "");

        for (Field field: fields) {
            code.addObjectConstant(field.getType(), field.getVariableName());
        }

        LinkedList<String> constructorBody = new LinkedList<String>();
        String arglist = "";
        if (0 < fields.length) {
            for (Field field: fields) {
                arglist += field.getType() + " " + field.getVariableName() + ", ";
                constructorBody.add("this." + field.getVariableName() + " = " + field.getVariableName() + ";");
            }
            arglist = trimArgList(arglist);
        }
        code.addPublicConstructor(null, name, arglist, constructorBody.toArray(new String[0]));

        String javatypearglist = "";
        LinkedList<String> constructorBodyJ = new LinkedList<String>();
        if (0 < fields.length) {
            for (Field field: fields) {
                javatypearglist += field.getJType() + " " + field.getVariableName() + ", ";
                constructorBodyJ.add("this." + field.getVariableName() + " = " +
                        "new " + field.getType() + "(" + field.getVariableName() + ")" + ";");
            }
            javatypearglist = trimArgList(javatypearglist);

            code.addPublicConstructor(null, name, javatypearglist, constructorBodyJ.toArray(new String[0]));
        }

        String constructorBodyStream = "";
        for (Field field: fields) {
            constructorBodyStream += "this." + field.getVariableName() + " = " +
                    "new " + field.getType() + "(from);\n";
        }
        constructorBodyStream += "if (getNumber() != packet) {\n" +
                "\t" + "throw new IOException(\"Tried to create package " +
                name + " but packet number was \" + packet);\n" +
                "}" + "\n" +
                "\n" +
                "if (getEncodedSize() != headerLen) {\n" +
                "\t" + "throw new IOException(\"Package size in header and Java packet not the same. Header: \"" +
                " + headerLen\n" +
                "\t" + "+ \" Packet: \" + getEncodedSize());\n" +
                "}";
        code.addPublicConstructorWithExceptions("/***\n" +
                " * Construct an object from a DataInput\n" +
                " * @param from data stream that is at the start of the package body  \n" +
                " * @param headerLen length from header package\n" +
                " * @param packet the number of the packet specified in the header\n" +
                " * @throws IOException if the DataInput has a problem\n" +
                " */",
                name,
                "DataInput from, int headerLen, int packet",
                "IOException",
                constructorBodyStream.split("\n"));

        code.addPublicReadObjectState(null, "int", "getNumber", "return number;");
        code.addPublicReadObjectState(null, "boolean", "hasTwoBytePacketNumber", "return hasTwoBytePacketNumber;");

        String encodeFields = "// header\n" +
                        "// length is 2 unsigned bytes\n" +
                        "to.writeChar(getEncodedSize());\n" +
                        "// type\n" +
                        (hasTwoBytePacketNumber ? "to.writeChar(number);" : "to.writeByte(number);") + "\n";
        if (0 < fields.length) encodeFields += "\n" +
                "// body" + "\n";
        for (Field field: fields) encodeFields += field.getVariableName() + ".encodeTo(to);" + "\n";
        code.addPublicDynamicMethod(null, "void", "encodeTo", "DataOutput to", "IOException", encodeFields.split("\n"));

        String encodeFieldsLen = "return " + (hasTwoBytePacketNumber ? "4" : "3");
        if (0 < fields.length) {
            encodeFieldsLen += "\n";
            for (Field field: fields)
                encodeFieldsLen += "\t" + "+ " + field.getVariableName() + ".encodedLength()" + "\n";
            encodeFieldsLen = encodeFieldsLen.trim();
        }
        encodeFieldsLen += ";";
        code.addPublicReadObjectState(null, "int", "getEncodedSize", encodeFieldsLen.split("\n"));

        String getToString = "String out = \"" + name + "\" + \"(\" + number + \")\";" + "\n";
        for (Field field: fields)
            getToString += "out += \"\\n\\t" + field.getVariableName() +
                    " = \" + " + field.getVariableName() + ".getValue();" + "\n";
        getToString += "\n";
        getToString += "return out + \"\\n\";";
        code.addPublicReadObjectState(null, "String", "toString", getToString.split("\n"));

        for (Field field: fields) {
            code.addPublicReadObjectState(null, field.getType(), "get"
                + field.getVariableName().substring(0, 1).toUpperCase() + field.getVariableName().substring(1),
                "return " + field.getVariableName() + ";");
        }

        for (Field field: fields) {
            code.addPublicReadObjectState(null, field.getJType(), "get"
                    + field.getVariableName().substring(0, 1).toUpperCase() + field.getVariableName().substring(1)
                    + "Value",
                    "return " + field.getVariableName() + ".getValue();");
        }

        return code.toString();
    }

    private static String trimArgList(String arglist) {
        return arglist.substring(0, arglist.length() - 2);
    }

    public List<? extends Field> getFields() {
        return Arrays.asList(fields);
    }
}
