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

import org.freeciv.Util;
import org.freeciv.packet.PacketHeader;
import org.freeciv.packetgen.GeneratorDefaults;
import org.freeciv.packetgen.UndefinedException;
import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.supporting.Field;
import org.freeciv.packetgen.javaGenerator.*;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.Import;
import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.*;

import java.io.DataInput;
import java.io.IOException;
import java.util.*;

import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.*;

public class Packet extends ClassWriter implements IDependency {
    private final int number;
    private final Field[] fields;

    private final String logger;

    private final Requirement iFulfill;
    private final HashSet<Requirement> requirements = new HashSet<Requirement>();

    @Deprecated public Packet(String name, int number, String headerKind, Field... fields) throws UndefinedException {
        this(name, number, new TargetClass(headerKind), GeneratorDefaults.LOG_TO, Collections.<Annotate>emptyList(), fields);
    }

    public Packet(String name, int number, TargetClass headerKind, String logger,
                  List<Annotate> packetFlags, Field... fields) throws UndefinedException {
        super(ClassKind.CLASS, new TargetPackage(org.freeciv.packet.Packet.class.getPackage()), new Import[]{
                              Import.allIn(new TargetPackage(org.freeciv.packet.fieldtype.FieldType.class.getPackage())),
                              Import.allIn(new TargetPackage(org.freeciv.types.FCEnum.class.getPackage())),
                              null,
                              Import.classIn(java.io.DataInput.class),
                              Import.classIn(java.io.DataOutput.class),
                              Import.classIn(java.util.logging.Logger.class),
                              Import.classIn(java.io.IOException.class)
                      }, "Freeciv's protocol definition", packetFlags, name, null, "Packet");

        this.number = number;
        this.fields = fields;

        this.logger = logger;

        for (Field field : fields) {
            field.introduceNeighbours(fields);
        }

        for (Field field : fields) {
            requirements.addAll(field.getReqs());
        }

        iFulfill = new Requirement(getName(), Requirement.Kind.PACKET);

        addClassConstant("int", "number", number + "");

        addObjectConstantAndGetter(Var.field(Collections.<Annotate>emptyList(),
                Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO, new TargetClass(PacketHeader.class), "header", null));

        for (Field field : fields) {
            addObjectConstantAndGetter(field);
            addJavaGetter(field);
        }

        addEncoder(fields);
        addCalcBodyLen(fields);

        addToString(name, fields);

        addConstructorFromFields(fields, headerKind);

        addConstructorFromJavaTypes(fields, headerKind);

        addConstructorFromDataInput(name, fields, headerKind);
    }

    private void addConstructorFromFields(Field[] fields, TargetClass headerKind) throws UndefinedException {
        Block constructorBody = new Block();
        LinkedList<Map.Entry<String, String>> params = new LinkedList<Map.Entry<String, String>>();
        for (Field field : fields) {
            params.add(new AbstractMap
                    .SimpleImmutableEntry<String, String>(field.getFType() + field.getArrayDeclaration(),
                                                          field.getFieldName()));
            field.appendValidationTo(true, constructorBody);
            constructorBody.addStatement(setFieldToVariableSameName(field.getFieldName()));
        }
        constructorBody.addStatement(generateHeader(headerKind));
        addConstructorPublic(null, createParameterList(params), constructorBody);
    }

    private Typed<AValue> generateHeader(TargetClass headerKind) {
        return getField("header").assign(headerKind.newInstance(
                sum(asAValue("calcBodyLen()"), headerKind.read("HEADER_SIZE")),
                asAValue("number")));
    }

    private void addConstructorFromJavaTypes(Field[] fields, TargetClass headerKind) throws UndefinedException {
        if (0 < fields.length) {
            LinkedList<Map.Entry<String, String>> params = new LinkedList<Map.Entry<String, String>>();
            Block constructorBodyJ = new Block();
            for (Field field : fields) {
                params.add(new AbstractMap.SimpleImmutableEntry<String, String>(
                        field.getJType() + field.getArrayDeclaration(),
                        field.getFieldName()));
                field.appendValidationTo(true, constructorBodyJ);
                if (field.hasDeclarations())
                    constructorBodyJ.addStatement(
                            field.assign(asAValue("new " + field.getFType() + field.getNewCreation())));
                field.forElementsInField("this." + field.getFieldName() + "[i] = " +
                        field.getNewFromJavaType(), constructorBodyJ);
            }
            constructorBodyJ.addStatement(generateHeader(headerKind));
            addConstructorPublic(null, createParameterList(params), constructorBodyJ);
        }
    }

    private void addConstructorFromDataInput(String name, Field[] fields, TargetClass headerKind) throws UndefinedException {
        Var argHeader = Var.local(PacketHeader.class, "header", null);
        final Var streamName = Var.local(DataInput.class, "from", null);

        Block constructorBodyStream = new Block(getField("header").assign(argHeader.ref()));
        for (Field field : fields) {
            field.appendValidationTo(false, constructorBodyStream);
            if (field.hasDeclarations())
                constructorBodyStream.addStatement(
                        field.assign(asAValue("new " + field.getFType() + field.getNewCreation())));
            field.forElementsInField(
                    "this." + field.getFieldName() + "[i] = " + field.getNewFromDataStream(streamName.getName()),
                    constructorBodyStream);
        }

        constructorBodyStream.groupBoundary();

        constructorBodyStream.addStatement(IF(asBool("number != header.getPacketKind()"),
                Block.fromStrings("throw new IOException(\"Tried to create package " +
                                          name + " but packet number was \" + header.getPacketKind())")));

        constructorBodyStream.addStatement(ASSERT(asBool("header instanceof " + headerKind.getName()),
                literalString("Packet not generated for this kind of header")));

        Block wrongSize = new Block();
        constructorBodyStream.addStatement(IF(asBool("header.getHeaderSize() + calcBodyLen() != header.getTotalSize()"),
                wrongSize));
        wrongSize.addStatement(asVoid("Logger.getLogger(" + logger + ").warning(" +
                "\"Probable misinterpretation: \" + " +
                "\"interpreted packet size (\" + (header.getHeaderSize() + calcBodyLen()) + \")" +
                " don't match header packet size (\" + header.getTotalSize() + \")" +
                " for \" + this.toString())"));
        wrongSize.addStatement(THROW((new TargetClass(IOException.class)).newInstance(sum(
                literalString("Packet size in header and Java packet not the same."),
                literalString(" Header packet size: "), argHeader.<AnInt>call("getTotalSize"),
                literalString(" Header size: "), argHeader.<AnInt>call("getHeaderSize"),
                literalString(" Packet body size: "), asAValue("calcBodyLen()")))));
        addConstructorPublicWithExceptions("/***\n" +
                                                   " * Construct an object from a DataInput\n" +
                                                   " * @param " + streamName.getName() + " data stream that is at the start of " +
                                                   "the package body  \n" +
                                                   " * @param header header data. Must contain size and number\n" +
                                                   " * @throws IOException if the DataInput has a problem\n" +
                                                   " */",
                                           "DataInput " + streamName.getName() + ", PacketHeader header",
                                           "IOException",
                                           constructorBodyStream);
    }

    private void addEncoder(Field[] fields) {
        Block body = new Block();
        body.addStatement(getField("header").call("encodeTo", asAValue("to")));
        if (0 < fields.length) {
            for (Field field : fields)
                field.forElementsInField("this." + field.getFieldName() + "[i].encodeTo(to)", body);
        }
        addMethodPublicDynamic(null, "void", "encodeTo", "DataOutput to", "IOException", body);
    }

    private void addCalcBodyLen(Field[] fields) {
        LinkedList<String> encodeFieldsLen = new LinkedList<String>();
        for (Field field : fields)
            if (field.hasDeclarations())
                encodeFieldsLen.addAll(Arrays.asList(field.forElementsInField(
                        "int " + field.getFieldName() + "Len" + " = " + "0;",
                        field.getFieldName() + "Len" + "+=" +
                                "this." + field.getFieldName() + "[i].encodedLength();", "")));
        if (1 < fields.length) {
            StringBuilder build = new StringBuilder("return (");
            build.append(calcBodyLen(fields[0]));
            boolean broken = false;
            for (int i = 1; i < fields.length; i++) {
                if (true) {
                    encodeFieldsLen.add(build.toString());
                    build = new StringBuilder();
                }
                build.append("+ ");
                build.append(calcBodyLen(fields[i]));
            }
            build.append(");");
            encodeFieldsLen.add(build.toString());
        } else if (1 == fields.length) {
            encodeFieldsLen.add("return " + calcBodyLen(fields[0]) + ";");

        } else {
            encodeFieldsLen.add("return 0;");
        }
        addMethod(null,
                Visibility.PRIVATE, Scope.OBJECT,
                "int", "calcBodyLen", null, null,
                encodeFieldsLen.toArray(new String[0]));
    }

    private static String calcBodyLen(Field field) {
        return (field.hasDeclarations() ?
                field.getFieldName() + "Len" :
                "this." + field.getFieldName() + ".encodedLength()");
    }

    private void addToString(String name, Field[] fields) {
        Var buildOutput = Var.local("String", "out",
                asAString("\"" + name + "\" + \"(\" + number + \")\""));
        Block body = new Block(buildOutput);
        for (Field field : fields)
            body.addStatement(asVoid("out += \"\\n\\t" + field.getFieldName() + " = \" + " + (field.hasDeclarations() ?
                    "org.freeciv.Util.joinStringArray(" + "this." + field.getFieldName() + ", " +
                            "\", \"" +
                            ", \"(\", \")\"" + ")" :
                    "this." + field.getFieldName() + ".toString()")));
        body.addStatement(RETURN(buildOutput.ref()));
        addMethodPublicReadObjectState(null, "String", "toString", body);
    }

    private void addJavaGetter(Field field) throws UndefinedException {
        Block body;

        if (field.hasDeclarations()) {
            Var out = Var.local(field.getJType() + field.getArrayDeclaration(), "out",
                    asAValue("new " + field.getJType() + field.getNewCreation()));
            body = new Block(out);
            field.forElementsInField("out[i] = " + "this." + field.getFieldName() + "[i].getValue()", body);
            body.addStatement(RETURN(out.ref()));
        } else {
            body = new Block(RETURN(asAValue("this." + field.getFieldName() + ".getValue()")));
        }

        addMethodPublicReadObjectState(null, field.getJType() + field.getArrayDeclaration(),
                "get" + field.getFieldName().substring(0, 1).toUpperCase() + field.getFieldName().substring(1) + "Value",
                body);
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
