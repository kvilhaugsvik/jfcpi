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

public class Packet extends ClassWriter {
    private final int number;
    private final Field[] fields;

    public Packet(String name, int number, boolean hasTwoBytePacketNumber, Field... fields) {
        super(org.freeciv.packet.Packet.class.getPackage(),
                new String[]{
                        "org.freeciv.packet.fieldtype.*",
                        org.freeciv.types.FCEnum.class.getPackage().getName() + ".*",
                        null,
                        "java.util.LinkedList",
                        "java.io.DataInput",
                        "java.io.DataOutput",
                        "java.io.IOException"
                },
                "Freeciv's protocol definition",
                name,
                "Packet");

        this.number = number;
        this.fields = fields;

        addConstant("int", "number", number + "");
        addConstant("boolean", "hasTwoBytePacketNumber", hasTwoBytePacketNumber + "");

        for (Field field: fields) {
            addObjectConstant(field.getType() + field.getArrayDeclaration(), field.getVariableName());
        }

        LinkedList<String> constructorBody = new LinkedList<String>();
        String arglist = "";
        if (0 < fields.length) {
            for (Field field: fields) {
                arglist += field.getType() + field.getArrayDeclaration() + " " + field.getVariableName() + ", ";
                constructorBody.add("this." + field.getVariableName() + " = " + field.getVariableName() + ";");
            }
            arglist = trimArgList(arglist);
        }
        addPublicConstructor(null, name, arglist, constructorBody.toArray(new String[0]));

        String javatypearglist = "";
        LinkedList<String> constructorBodyJ = new LinkedList<String>();
        if (0 < fields.length) {
            for (Field field: fields) {
                javatypearglist += field.getJType() + field.getArrayDeclaration() + " " + field.getVariableName() + ", ";
                constructorBodyJ.addAll(field.hasDeclarations()?
                        Arrays.asList(forElementsInField(field,
                                "this." + field.getVariableName() + " = new " + field.getType() + field.getNewCreation("") + ";",
                                "this." + field.getVariableName() + "[i] = new " + field.getType() + "(" + field.getVariableName() + "[i]);",
                                "")):
                        Arrays.asList(new String[]{"this." + field.getVariableName() + " = " +
                                "new " + field.getType() + "(" + field.getVariableName() + ")" + ";"}));
            }
            javatypearglist = trimArgList(javatypearglist);

            addPublicConstructor(null, name, javatypearglist, constructorBodyJ.toArray(new String[0]));
        }

        LinkedList<String> constructorBodyStream = new LinkedList<String>();
        for (Field field: fields) {
            constructorBodyStream.addAll((field.hasDeclarations())?
                    Arrays.asList(forElementsInField(field, "this." + field.getVariableName() +
                            " = new " + field.getType() +
                            field.getNewCreation(".getValue()") + ";",
                            "this." + field.getVariableName() + "[i] = " + "new " + field.getType() + "(from);",
                            ""))
                    :
                    Arrays.asList(new String[]{"this." + field.getVariableName() +
                            " = " + "new " + field.getType() + "(from);"}));
        }
        constructorBodyStream.add("if (getNumber() != packet) {");
        constructorBodyStream.add("throw new IOException(\"Tried to create package " +
                name + " but packet number was \" + packet);");
        constructorBodyStream.add("}");
        constructorBodyStream.add("");
        constructorBodyStream.add("if (getEncodedSize() != headerLen) {");
        constructorBodyStream.add(
                "throw new IOException(\"Package size in header and Java packet not the same. Header: \"" +
                " + headerLen");
        constructorBodyStream.add("+ \" Packet: \" + getEncodedSize());");
        constructorBodyStream.add("}");
        addPublicConstructorWithExceptions("/***\n" +
                " * Construct an object from a DataInput\n" +
                " * @param from data stream that is at the start of the package body  \n" +
                " * @param headerLen length from header package\n" +
                " * @param packet the number of the packet specified in the header\n" +
                " * @throws IOException if the DataInput has a problem\n" +
                " */",
                name,
                "DataInput from, int headerLen, int packet",
                "IOException",
                constructorBodyStream.toArray(new String[0]));

        addPublicReadObjectState(null, "int", "getNumber", "return number;");
        addPublicReadObjectState(null, "boolean", "hasTwoBytePacketNumber", "return hasTwoBytePacketNumber;");

        LinkedList<String> encodeFields = new LinkedList<String>();
        encodeFields.add("// header");
        encodeFields.add("// length is 2 unsigned bytes");
        encodeFields.add("to.writeChar(getEncodedSize());");
        encodeFields.add("// type");
        encodeFields.add(hasTwoBytePacketNumber ? "to.writeChar(number);" : "to.writeByte(number);");
        if (0 < fields.length) {
            encodeFields.add("");
            encodeFields.add("// body");
            for (Field field: fields)
                encodeFields.addAll(Arrays.asList(forElementsInField(field, "",
                        field.getVariableName() + "[i].encodeTo(to);", "")));
        }
        addPublicDynamicMethod(null, "void", "encodeTo", "DataOutput to", "IOException", encodeFields.toArray(new String[0]));

        LinkedList<String> encodeFieldsLen = new LinkedList<String>();
        for (Field field: fields)
            if (field.hasDeclarations())
                encodeFieldsLen.addAll(Arrays.asList(forElementsInField(field,
                        "int " + field.getVariableName() + "Len" + " = " + "0;",
                        field.getVariableName() + "Len" + "+=" + field.getVariableName() + "[i].encodedLength();",
                        "")));
        encodeFieldsLen.add("return " + (hasTwoBytePacketNumber ? "4" : "3"));
        if (0 < fields.length) {
            for (Field field: fields)
                encodeFieldsLen.add("\t" + "+ " + ((field.hasDeclarations())?
                        field.getVariableName() + "Len":
                        field.getVariableName() + ".encodedLength()"));
        }
        encodeFieldsLen.add(encodeFieldsLen.removeLast() + ";");
        addPublicReadObjectState(null, "int", "getEncodedSize", encodeFieldsLen.toArray(new String[0]));

        LinkedList<String> getToString = new LinkedList<String>();
        getToString.add("String out = \"" + name + "\" + \"(\" + number + \")\";");
        for (Field field: fields)
            if (field.hasDeclarations())
                getToString.addAll(Arrays.asList(forElementsInField(field, "out += \"\\n\\t" + field.getVariableName() +
                        " += (\";", "out += " + field.getVariableName() + "[i].getValue();", "out += \")\";")));
            else
                getToString.add("out += \"\\n\\t" + field.getVariableName() +
                        " = \" + " + field.getVariableName() + ".getValue();");
        getToString.add("");
        getToString.add("return out + \"\\n\";");
        addPublicReadObjectState(null, "String", "toString", getToString.toArray(new String[0]));

        for (Field field: fields) {
            addPublicReadObjectState(null, field.getType() + field.getArrayDeclaration(), "get"
                    + field.getVariableName().substring(0, 1).toUpperCase() + field.getVariableName().substring(1),
                    "return " + field.getVariableName() + ";");
        }

        for (Field field: fields) {
            addPublicReadObjectState(null, field.getJType() + field.getArrayDeclaration(), "get"
                    + field.getVariableName().substring(0, 1).toUpperCase() + field.getVariableName().substring(1)
                    + "Value",
                    (field.hasDeclarations())?
                            forElementsInField(field,
                                    "LinkedList<" + field.getJType() + "> out = new LinkedList<" + field.getJType() + ">();",
                                    "out.add(" + field.getVariableName() + "[i].getValue());",
                                    "return out.toArray(new " + field.getJType() + field.getNewCreation(".getValue()") + ");"):
                            new String[]{"return " + field.getVariableName() + ".getValue();"}
            );
        }
    }

    private String[] forElementsInField(Field field, String before, String in, String after) {
        assert(null != in && !in.isEmpty());

        LinkedList<String> out = new LinkedList<String>();
        final int level = field.getNumberOfDeclarations();

        if (null != before && !before.isEmpty()) {
            out.add(before);
        }

        String[] wrappedInFor = new String[1 + level * 2];
        String replaceWith = "";
        for (int counter = 0; counter < level; counter++) {
            wrappedInFor[counter] = "for(int " + getCounterNumber(counter) + " = 0; " + getCounterNumber(counter) + " < " + field.getVariableName() + ".length; " + getCounterNumber(counter) + "++) {";
            wrappedInFor[1 + counter + level] = "}";
            replaceWith += "[" + getCounterNumber(counter) + "]";
        }
        wrappedInFor[level] = in.replaceAll("\\[i\\]", replaceWith);

        out.addAll(Arrays.asList(wrappedInFor));

        if (null != after && !after.isEmpty()) {
            out.add(after);
        }

        return out.toArray(new String[0]);
    }

    private char getCounterNumber(int counter) {
        return ((char)('i' + counter));
    }

    public int getNumber() {
        return number;
    }

    private static String trimArgList(String arglist) {
        return arglist.substring(0, arglist.length() - 2);
    }

    public List<? extends Field> getFields() {
        return Arrays.asList(fields);
    }
}
