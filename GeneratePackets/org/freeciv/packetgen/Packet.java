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

import java.util.*;

public class Packet extends ClassWriter implements IDependency {
    private final int number;
    private final Field[] fields;

    private final Requirement iFulfill;
    private final HashSet<Requirement> requirements = new HashSet<Requirement>();

    public Packet(String name, int number, boolean hasTwoBytePacketNumber, Field... fields) {
        super(org.freeciv.packet.Packet.class.getPackage(),
                new String[]{
                        allInPackageOf(org.freeciv.packet.fieldtype.FieldType.class),
                        allInPackageOf(org.freeciv.types.FCEnum.class),
                        null,
                        java.io.DataInput.class.getCanonicalName(),
                        java.io.DataOutput.class.getCanonicalName(),
                        java.io.IOException.class.getCanonicalName()
                },
                "Freeciv's protocol definition",
                name,
                "Packet");

        this.number = number;
        this.fields = fields;

        addClassConstant("int", "number", number + "");
        addClassConstant("boolean", "hasTwoBytePacketNumber", hasTwoBytePacketNumber + "");

        for (Field field: fields) {
            addObjectConstant(field.getType() + field.getArrayDeclaration(), field.getVariableName());
        }

        addConstructorFromFields(name, fields);

        addConstructorFromJavaTypes(name, fields);

        addConstructorFromDataInput(name, fields);

        addMethodPublicReadObjectState(null, "int", "getNumber", "return number;");
        addMethodPublicReadObjectState(null, "boolean", "hasTwoBytePacketNumber", "return hasTwoBytePacketNumber;");

        addEncoder(hasTwoBytePacketNumber, fields);
        addEncodedSize(hasTwoBytePacketNumber, fields);

        addToString(name, fields);

        for (Field field: fields) {
            addGetAsField(field);
        }

        for (Field field: fields) {
            addJavaGetter(field);
        }

        for (Field field: fields) {
            requirements.addAll(field.getReqs());
        }

        iFulfill = new Requirement(getName(), Requirement.Kind.PACKET);
    }

    private void addConstructorFromFields(String name, Field[] fields) {
        LinkedList<String> constructorBody = new LinkedList<String>();
        String arglist = "";
        if (0 < fields.length) {
            for (Field field: fields) {
                arglist += buildArgumentList(field.getType(), field);
                if (field.hasDeclarations()) constructorBody.addAll(Arrays.asList(field.validate(".getValue()", this.getName())));
                constructorBody.add("this." + field.getVariableName() + " = " + field.getVariableName() + ";");
            }
            arglist = trimArgList(arglist);
        }
        addConstructorPublic(null, name, arglist, constructorBody.toArray(new String[0]));
    }

    private void addConstructorFromJavaTypes(String name, Field[] fields) {
        String javatypearglist = "";
        LinkedList<String> constructorBodyJ = new LinkedList<String>();
        if (0 < fields.length) {
            for (Field field: fields) {
                javatypearglist += buildArgumentList(field.getJType(), field);
                if (field.hasDeclarations())
                    constructorBodyJ.addAll(Arrays.asList(field.validate("", this.getName())));
                constructorBodyJ.addAll(
                        Arrays.asList(forElementsInField(field,
                                "this." + field.getVariableName() + " = new " + field.getType() + field.getNewCreation("") + ";",
                                "this." + field.getVariableName() + "[i] = " + field.getNewFromJavaType(),
                                "")));
            }
            javatypearglist = trimArgList(javatypearglist);

            addConstructorPublic(null, name, javatypearglist, constructorBodyJ.toArray(new String[0]));
        }
    }

    private void addConstructorFromDataInput(String name, Field[] fields) {
        LinkedList<String> constructorBodyStream = new LinkedList<String>();
        final String streamName = "from";
        for (Field field: fields) {
            constructorBodyStream.addAll(
                    Arrays.asList(forElementsInField(field, "this." + field.getVariableName() +
                            " = new " + field.getType() +
                            field.getNewCreation(".getValue()") + ";",
                            "this." + field.getVariableName() + "[i] = " + field.getNewFromDataStream(streamName),
                            "")));
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
        addConstructorPublicWithExceptions("/***\n" +
                " * Construct an object from a DataInput\n" +
                " * @param " + streamName + " data stream that is at the start of the package body  \n" +
                " * @param headerLen length from header package\n" +
                " * @param packet the number of the packet specified in the header\n" +
                " * @throws IOException if the DataInput has a problem\n" +
                " */",
                name,
                "DataInput " + streamName + ", int headerLen, int packet",
                "IOException",
                constructorBodyStream.toArray(new String[0]));
    }

    private void addEncoder(boolean hasTwoBytePacketNumber, Field[] fields) {
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
                        "this." + field.getVariableName() + "[i].encodeTo(to);", "")));
        }
        addMethodPublicDynamic(null, "void", "encodeTo", "DataOutput to", "IOException", encodeFields.toArray(new String[0]));
    }

    private void addEncodedSize(boolean hasTwoBytePacketNumber, Field[] fields) {
        LinkedList<String> encodeFieldsLen = new LinkedList<String>();
        for (Field field: fields)
            if (field.hasDeclarations())
                encodeFieldsLen.addAll(Arrays.asList(forElementsInField(field,
                        "int " + field.getVariableName() + "Len" + " = " + "0;",
                        field.getVariableName() + "Len" + "+=" + "this." + field.getVariableName() +
                                "[i].encodedLength();", "")));
        encodeFieldsLen.add("return " + (hasTwoBytePacketNumber ? "4" : "3"));
        if (0 < fields.length) {
            for (Field field: fields)
                encodeFieldsLen.add("\t" + "+ " + ((field.hasDeclarations())?
                        field.getVariableName() + "Len":
                        "this." + field.getVariableName() + ".encodedLength()"));
        }
        encodeFieldsLen.add(encodeFieldsLen.removeLast() + ";");
        addMethodPublicReadObjectState(null, "int", "getEncodedSize", encodeFieldsLen.toArray(new String[0]));
    }

    private void addToString(String name, Field[] fields) {
        LinkedList<String> getToString = new LinkedList<String>();
        getToString.add("String out = \"" + name + "\" + \"(\" + number + \")\";");
        for (Field field: fields)
            if (field.hasDeclarations())
                getToString.addAll(Arrays.asList(forElementsInField(field, "out += \"\\n\\t" + field.getVariableName() +
                        " += (\";", "out += " + "this." + field.getVariableName() + "[i].getValue();", "out += \")\";")));
            else
                getToString.add("out += \"\\n\\t" + field.getVariableName() +
                        " = \" + " + "this." + field.getVariableName() + ".getValue();");
        getToString.add("");
        getToString.add("return out + \"\\n\";");
        addMethodPublicReadObjectState(null, "String", "toString", getToString.toArray(new String[0]));
    }

    private void addGetAsField(Field field) {
        addMethodPublicReadObjectState(null, field.getType() + field.getArrayDeclaration(), "get"
                + field.getVariableName().substring(0, 1).toUpperCase() + field.getVariableName().substring(1),
                "return " + "this." + field.getVariableName() + ";");
    }

    private void addJavaGetter(Field field) {
        addMethodPublicReadObjectState(null, field.getJType() + field.getArrayDeclaration(), "get"
                + field.getVariableName().substring(0, 1).toUpperCase() + field.getVariableName().substring(1)
                + "Value",
                (field.hasDeclarations()) ?
                        forElementsInField(field,
                                field.getJType() + field.getArrayDeclaration() + " out = new " +
                                        field.getJType() + field.getNewCreation(".getValue()") + ";",
                                "out[i] = " + "this." + field.getVariableName() + "[i].getValue();",
                                "return out;") :
                        new String[]{"return " + "this." + field.getVariableName() + ".getValue();"}
        );
    }

    private String[] forElementsInField(Field field, String before, String in, String after) {
        assert(null != in && !in.isEmpty());

        LinkedList<String> out = new LinkedList<String>();
        final int level = field.getNumberOfDeclarations();

        if (0 < level && null != before && !before.isEmpty()) {
            out.add(before);
        }

        String[] wrappedInFor = new String[1 + level * 2];
        String replaceWith = "";
        for (int counter = 0; counter < level; counter++) {
            wrappedInFor[counter] = "for(int " + getCounterNumber(counter) + " = 0; " +
                    getCounterNumber(counter) + " < " + "this." + field.getVariableName() + replaceWith + ".length; " +
                    getCounterNumber(counter) + "++) {";
            wrappedInFor[1 + counter + level] = "}";
            replaceWith += "[" + getCounterNumber(counter) + "]";
        }
        wrappedInFor[level] = in.replaceAll("\\[i\\]", replaceWith);

        out.addAll(Arrays.asList(wrappedInFor));

        if (0 < level && null != after && !after.isEmpty()) {
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

    private static String buildArgumentList(String kind, Field field) {
        return kind + field.getArrayDeclaration() + " " + field.getVariableName() + ", ";
    }

    private static String trimArgList(String arglist) {
        return arglist.substring(0, arglist.length() - 2);
    }

    public List<? extends Field> getFields() {
        return Arrays.asList(fields);
    }

    @Override
    public Collection<Requirement> getReqs() {
        return Collections.unmodifiableSet(requirements);
    }

    @Override
    public Requirement getIFulfillReq() {
        return iFulfill;
    }
}
