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

package org.freeciv.packetgen.enteties;

import org.freeciv.packetgen.UndefinedException;
import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.supporting.Field;
import org.freeciv.packetgen.javaGenerator.ClassWriter;
import org.freeciv.packetgen.javaGenerator.TargetPackage;

import java.util.*;

public class Packet extends ClassWriter implements IDependency {
    private final int number;
    private final Field[] fields;

    private final Requirement iFulfill;
    private final HashSet<Requirement> requirements = new HashSet<Requirement>();

    public Packet(String name, int number, boolean hasTwoBytePacketNumber, Field... fields) throws UndefinedException {
        super(ClassKind.CLASS, new TargetPackage(org.freeciv.packet.Packet.class.getPackage()), new String[]{
                              allInPackageOf(org.freeciv.packet.fieldtype.FieldType.class),
                              allInPackageOf(org.freeciv.types.FCEnum.class),
                              null,
                              java.io.DataInput.class.getCanonicalName(),
                              java.io.DataOutput.class.getCanonicalName(),
                              java.io.IOException.class.getCanonicalName()
                      }, "Freeciv's protocol definition", name, null, "Packet");

        this.number = number;
        this.fields = fields;

        for (Field field : fields) {
            field.introduceNeighbours(fields);
        }

        for (Field field : fields) {
            requirements.addAll(field.getReqs());
        }

        iFulfill = new Requirement(getName(), Requirement.Kind.PACKET);

        addClassConstant("int", "number", number + "");

        for (Field field : fields) {
            addObjectConstant(field.getType() + field.getArrayDeclaration(), field.getFieldName());
        }

        addConstructorFromFields(fields);

        addConstructorFromJavaTypes(fields);

        addConstructorFromDataInput(name, fields);

        addMethodPublicReadObjectState(null, "int", "getNumber", "return number;");

        addEncoder(hasTwoBytePacketNumber, fields);
        addEncodedSize(hasTwoBytePacketNumber, fields);

        addToString(name, fields);

        for (Field field : fields) {
            addGetAsField(field);
        }

        for (Field field : fields) {
            addJavaGetter(field);
        }
    }

    private void addConstructorFromFields(Field[] fields) throws UndefinedException {
        LinkedList<String> constructorBody = new LinkedList<String>();
        LinkedList<Map.Entry<String, String>> params = new LinkedList<Map.Entry<String, String>>();
        for (Field field : fields) {
            params.add(new AbstractMap
                    .SimpleImmutableEntry<String, String>(field.getType() + field.getArrayDeclaration(),
                                                          field.getFieldName()));
            constructorBody.addAll(Arrays.asList(field.validate(true)));
            constructorBody.add(setFieldToVariableSameName(field.getFieldName()));
        }
        addConstructorPublic(null, createParameterList(params), constructorBody.toArray(new String[0]));
    }

    private void addConstructorFromJavaTypes(Field[] fields) throws UndefinedException {
        if (0 < fields.length) {
            LinkedList<Map.Entry<String, String>> params = new LinkedList<Map.Entry<String, String>>();
            LinkedList<String> constructorBodyJ = new LinkedList<String>();
            for (Field field : fields) {
                params.add(new AbstractMap
                        .SimpleImmutableEntry<String, String>(field.getJType() + field.getArrayDeclaration(),
                                                              field.getFieldName()));
                constructorBodyJ.addAll(Arrays.asList(field.validate(true)));
                constructorBodyJ.addAll(
                        Arrays.asList(field.forElementsInField("this." + field.getFieldName() + " = new " + field
                                                                 .getType() + field.getNewCreation() + ";",
                                                         "this." + field.getFieldName() + "[i] = " + field
                                                                 .getNewFromJavaType(),
                                                         "")));
            }
            addConstructorPublic(null, createParameterList(params), constructorBodyJ.toArray(new String[0]));
        }
    }

    private void addConstructorFromDataInput(String name, Field[] fields) throws UndefinedException {
        LinkedList<String> constructorBodyStream = new LinkedList<String>();
        final String streamName = "from";
        for (Field field : fields) {
            constructorBodyStream.addAll(Arrays.asList(field.validate(false)));
            constructorBodyStream.addAll(Arrays.asList(field.forElementsInField(
                            "this." + field.getFieldName() + " = new " + field.getType() +
                                    field.getNewCreation() + ";",
                            "this." + field.getFieldName() + "[i] = " + field.getNewFromDataStream(streamName),
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
                                                   " * @param " + streamName + " data stream that is at the start of " +
                                                   "the package body  \n" +
                                                   " * @param headerLen length from header package\n" +
                                                   " * @param packet the number of the packet specified in the " +
                                                   "header\n" +
                                                   " * @throws IOException if the DataInput has a problem\n" +
                                                   " */",
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
            for (Field field : fields)
                encodeFields.addAll(Arrays.asList(field.forElementsInField("",
                                                                     "this." + field
                                                                             .getFieldName() + "[i].encodeTo(to);",
                                                                     "")));
        }
        addMethodPublicDynamic(null, "void", "encodeTo", "DataOutput to", "IOException",
                               encodeFields.toArray(new String[0]));
    }

    private void addEncodedSize(boolean hasTwoBytePacketNumber, Field[] fields) {
        LinkedList<String> encodeFieldsLen = new LinkedList<String>();
        for (Field field : fields)
            if (field.hasDeclarations())
                encodeFieldsLen.addAll(Arrays.asList(field.forElementsInField("int " + field
                                                                                .getFieldName() + "Len" + " = " +
                                                                                "0;",
                                                                        field.getFieldName() + "Len" + "+=" +
                                                                                "this." + field
                                                                                .getFieldName() +
                                                                                "[i].encodedLength();", "")));
        encodeFieldsLen.add("return " + (hasTwoBytePacketNumber ? "4" : "3"));
        if (0 < fields.length) {
            for (Field field : fields)
                encodeFieldsLen.add("\t" + "+ " + ((field.hasDeclarations()) ?
                        field.getFieldName() + "Len" :
                        "this." + field.getFieldName() + ".encodedLength()"));
        }
        encodeFieldsLen.add(encodeFieldsLen.removeLast() + ";");
        addMethodPublicReadObjectState(null, "int", "getEncodedSize", encodeFieldsLen.toArray(new String[0]));
    }

    private void addToString(String name, Field[] fields) {
        LinkedList<String> getToString = new LinkedList<String>();
        getToString.add("String out = \"" + name + "\" + \"(\" + number + \")\";");
        for (Field field : fields)
            if (field.hasDeclarations())
                getToString.addAll(Arrays.asList(field.forElementsInField(
                        "out += \"\\n\\t" + field.getFieldName() + " = (\\n\";",
                        "out += " + "\"\\t\\t\" + " + "this." + field.getFieldName() + "[i].toString() + \"\\n\";",
                        "out += \"\\t)\";")));
            else
                getToString.add("out += \"\\n\\t" + field.getFieldName() +
                                        " = \" + " + "this." + field.getFieldName() + ".toString();");
        getToString.add("");
        getToString.add("return out + \"\\n\";");
        addMethodPublicReadObjectState(null, "String", "toString", getToString.toArray(new String[0]));
    }

    private void addGetAsField(Field field) {
        addMethodPublicReadObjectState(null, field.getType() + field.getArrayDeclaration(), "get"
                + field.getFieldName().substring(0, 1).toUpperCase() + field.getFieldName().substring(1),
                                       "return " + "this." + field.getFieldName() + ";");
    }

    private void addJavaGetter(Field field) throws UndefinedException {
        addMethodPublicReadObjectState(null, field.getJType() + field.getArrayDeclaration(), "get"
                + field.getFieldName().substring(0, 1).toUpperCase() + field.getFieldName().substring(1)
                + "Value",
                                       (field.hasDeclarations()) ?
                                               field.forElementsInField(field.getJType() + field
                                                                          .getArrayDeclaration() + " out = new " +
                                                                          field.getJType() + field
                                                                          .getNewCreation() + ";",
                                                                  "out[i] = " + "this." + field
                                                                          .getFieldName() + "[i].getValue();",
                                                                  "return out;") :
                                               new String[]{
                                                       "return " + "this." + field.getFieldName() + ".getValue();"}
        );
    }

    public int getNumber() {
        return number;
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
