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

import org.freeciv.packet.PacketHeader;
import org.freeciv.packet.fieldtype.FieldTypeException;
import org.freeciv.packetgen.UndefinedException;
import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.ReqKind;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.supporting.Field;
import org.freeciv.packetgen.javaGenerator.*;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.Import;
import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.*;

public class Packet extends ClassWriter implements IDependency, ReqKind {
    private final int number;
    private final Field[] fields;

    private final String logger;

    private final Requirement iFulfill;
    private final HashSet<Requirement> requirements = new HashSet<Requirement>();

    private final TargetClass ioexception = new TargetClass(IOException.class, true);

    public Packet(String name, int number, TargetClass headerKind, String logger,
                  List<Annotate> packetFlags, Field... fields) throws UndefinedException {
        super(ClassKind.CLASS, new TargetPackage(org.freeciv.packet.Packet.class.getPackage()), new Import[]{
                              Import.allIn(new TargetPackage(org.freeciv.packet.fieldtype.FieldType.class.getPackage())),
                              Import.allIn(new TargetPackage(org.freeciv.types.FCEnum.class.getPackage())),
                              Import.classIn(org.freeciv.Util.class),
                              null,
                              Import.classIn(java.io.DataInput.class),
                              Import.classIn(java.io.DataOutput.class),
                              Import.classIn(java.util.logging.Logger.class),
                              Import.classIn(java.io.IOException.class)
                      }, "Freeciv's protocol definition", packetFlags, name,
                      DEFAULT_PARENT, Arrays.asList(new TargetClass(org.freeciv.packet.Packet.class, true)));

        this.number = number;
        this.fields = fields;

        this.logger = logger;

        for (Field field : fields) {
            field.introduceNeighbours(fields);
        }

        for (Field field : fields) {
            requirements.addAll(field.getReqs());
        }

        iFulfill = new Requirement(getName(), Packet.class);

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

        TargetMethod addExceptionLocation = addExceptionLocationAdder();
        addConstructorFromFields(fields, headerKind, addExceptionLocation);
        addConstructorFromJavaTypes(fields, headerKind, addExceptionLocation);
        addConstructorFromDataInput(name, fields, headerKind, addExceptionLocation);
    }

    private TargetMethod addExceptionLocationAdder() {
        Var<AValue> e = Var.param(RuntimeException.class, "e");
        TargetClass ft = new TargetClass(FieldTypeException.class, true);
        Var<AValue> fte = Var.local(ft, "fte", cast(ft, e.ref()));
        Var<AString> pName = Var.param(String.class, "field");
        Method.Helper addExceptionLocation = Method.newHelper(
                Comment.no(),
                e.getTType(),
                "addExceptionLocation",
                Arrays.asList(e, pName),
                new Block(
                        IF(
                                isInstanceOf(e.ref(), ft),
                                new Block(
                                        fte,
                                        fte.call("setInPacket", literal(getName())),
                                        fte.call("setField", pName.ref()))),
                        RETURN(e.ref())));
        addMethod(addExceptionLocation);

        return addExceptionLocation.getAddressOn(this.getAddress());
    }

    private void addConstructorFromFields(Field[] fields, TargetClass headerKind, TargetMethod addExceptionLocation) throws UndefinedException {
        Block constructorBody = new Block();
        LinkedList<Var<? extends AValue>> params = new LinkedList<Var<? extends AValue>>();
        for (Field field : fields) {
            params.add(Var.param(
                    new TargetClass(field.getFType() + field.getArrayDeclaration(), true),
                    field.getFieldName()));
            field.appendValidationTo(true, constructorBody);
            constructorBody.addStatement(setFieldToVariableSameName(field.getFieldName()));

            Block validate = new Block();
            field.appendArrayEaterValidationTo(validate);
            if (0 < validate.numberOfStatements())
                constructorBody.addStatement(labelExceptionsWithPacketAndField(field, validate, addExceptionLocation));
        }
        constructorBody.addStatement(generateHeader(headerKind, addExceptionLocation));
        addMethod(Method.newPublicConstructor(Comment.no(), getName(), params, constructorBody));
    }

    private Typed<NoValue> labelExceptionsWithPacketAndField(Var field, Block operation, TargetMethod addExceptionLocation) {
        // TODO: When packet arrays are replaced by array as field type wrap all exceptions that isn't a
        // FieldTypeException in a FieldTypeException and set packet and field
        Var<AValue> e = Var.param(RuntimeException.class, "e");
        return BuiltIn.tryCatch(
                operation,
                e,
                new Block(THROW(addExceptionLocation.<AValue>call(e.ref(), literal(field.getName())))));
    }

    private Typed<NoValue> generateHeader(TargetClass headerKind, TargetMethod addExceptionLocation) {
        Var header = getField("header");
        return labelExceptionsWithPacketAndField(header, new Block(
                header.assign(headerKind.newInstance(
                        sum(BuiltIn.<AValue>toCode("calcBodyLen()"), headerKind.read("HEADER_SIZE")),
                        BuiltIn.<AValue>toCode("number")))), addExceptionLocation);
    }

    private void addConstructorFromJavaTypes(Field[] fields, TargetClass headerKind, TargetMethod addExceptionLocation) throws UndefinedException {
        if (0 < fields.length) {
            LinkedList<Var<? extends AValue>> params = new LinkedList<Var<? extends AValue>>();
            Block constructorBodyJ = new Block();
            for (Field field : fields) {
                params.add(Var.param(
                        new TargetClass(field.getJType() + field.getArrayDeclaration(), true),
                        field.getFieldName()));

                Block readAndValidate = new Block();
                field.appendValidationTo(true, readAndValidate);
                if (field.hasDeclarations())
                    readAndValidate.addStatement(
                            field.assign(BuiltIn.<AValue>toCode("new " + field.getFType() + field.getNewCreation())));
                field.forElementsInField("this." + field.getFieldName() + "[i] = " +
                        field.getNewFromJavaType(), readAndValidate);
                constructorBodyJ.addStatement(labelExceptionsWithPacketAndField(field, readAndValidate, addExceptionLocation));
            }
            constructorBodyJ.addStatement(generateHeader(headerKind, addExceptionLocation));
            addMethod(Method.newPublicConstructor(Comment.no(), getName(), params, constructorBodyJ));
        }
    }

    private void addConstructorFromDataInput(String name, Field[] fields, TargetClass headerKind, TargetMethod addExceptionLocation) throws UndefinedException {
        Var<TargetClass> argHeader = Var.param(new TargetClass(PacketHeader.class, true), "header");
        final Var<TargetClass> streamName = Var.param(new TargetClass(DataInput.class, true), "from");

        Block constructorBodyStream = new Block(getField("header").assign(argHeader.ref()));
        for (Field field : fields) {
            Block readAndValidate = new Block();
            field.appendValidationTo(false, readAndValidate);
            if (field.hasDeclarations())
                readAndValidate.addStatement(
                        field.assign(BuiltIn.<AValue>toCode("new " + field.getFType() + field.getNewCreation())));
            field.forElementsInField(
                    "this." + field.getFieldName() + "[i] = " + field.getNewFromDataStream(streamName.getName()),
                    readAndValidate);
            constructorBodyStream.addStatement(labelExceptionsWithPacketAndField(field, readAndValidate, addExceptionLocation));
        }

        constructorBodyStream.groupBoundary();

        constructorBodyStream.addStatement(IF(BuiltIn.<ABool>toCode("number != header.getPacketKind()"),
                new Block(THROW((ioexception).newInstance(sum(
                        literal("Tried to create package " + name + " but packet number was "),
                        argHeader.<AnInt>call("getPacketKind")))))));

        constructorBodyStream.addStatement(ASSERT(BuiltIn.<ABool>toCode("header instanceof " + headerKind.getName()),
                literal("Packet not generated for this kind of header")));

        Block wrongSize = new Block();
        constructorBodyStream.addStatement(IF(BuiltIn.<ABool>toCode("header.getHeaderSize() + calcBodyLen() != header.getTotalSize()"),
                wrongSize));
        wrongSize.addStatement(new MethodCall<NoValue>("Logger.getLogger(" + logger + ").warning", sum(
                literal("Probable misinterpretation: "),
                literal("interpreted packet size ("),
                GROUP(sum(argHeader.<AnInt>call("getHeaderSize"), BuiltIn.<AnInt>toCode("calcBodyLen()"))),
                literal(") don't match header packet size ("), argHeader.<AnInt>call("getTotalSize"),
                literal(") for "), BuiltIn.<AString>toCode("this.toString()"))));
        wrongSize.addStatement(THROW(ioexception.newInstance(sum(
                literal("Packet size in header and Java packet not the same."),
                literal(" Header packet size: "), argHeader.<AnInt>call("getTotalSize"),
                literal(" Header size: "), argHeader.<AnInt>call("getHeaderSize"),
                literal(" Packet body size: "), BuiltIn.<AValue>toCode("calcBodyLen()")))));
        addMethod(Method.newPublicConstructorWithException(Comment.doc(
                "Construct an object from a DataInput", new String(),
                Comment.param(streamName, "data stream that is at the start of the package body"),
                Comment.param(argHeader, "header data. Must contain size and number"),
                Comment.docThrows(ioexception, "if the DataInput has a problem")),
                getName(), Arrays.asList(streamName, argHeader),
                Arrays.asList(ioexception),
                constructorBodyStream));
    }

    private void addEncoder(Field[] fields) {
        Var<TargetClass> pTo = Var.<TargetClass>param(new TargetClass(DataOutput.class, true), "to");
        Block body = new Block();
        body.addStatement(getField("header").call("encodeTo", BuiltIn.<AValue>toCode("to")));
        if (0 < fields.length) {
            for (Field field : fields)
                field.forElementsInField("this." + field.getFieldName() + "[i].encodeTo(to)", body);
        }
        addMethod(Method.newPublicDynamicMethod(Comment.no(),
                TargetClass.fromName("void"), "encodeTo", Arrays.asList(pTo),
                Arrays.asList(ioexception), body));
    }

    private void addCalcBodyLen(Field[] fields) {
        Block encodeFieldsLen = new Block();
        for (Field field : fields)
            if (field.hasDeclarations()) {
                encodeFieldsLen.addStatement(BuiltIn.<NoValue>toCode("int " + field.getFieldName() + "Len" + " = " + "0"));
                field.forElementsInField(field.getFieldName() + "Len" + "+=" +
                        "this." + field.getFieldName() + "[i].encodedLength()", encodeFieldsLen);
            }
        if (0 < fields.length) {
            Typed<? extends AValue> summing = calcBodyLen(fields[0]);
            for (int i = 1; i < fields.length; i++)
                summing = sum(summing, calcBodyLen(fields[i]));
            encodeFieldsLen.addStatement(RETURN(summing));
        } else {
            encodeFieldsLen.addStatement(RETURN(literal(0)));
        }
        addMethod(Method.custom(Comment.no(),
                Visibility.PRIVATE, Scope.OBJECT,
                TargetClass.fromName("int"), "calcBodyLen", Collections.<Var<AValue>>emptyList(),
                Collections.<TargetClass>emptyList(),
                encodeFieldsLen));
    }

    private static Typed<? extends AValue> calcBodyLen(Field field) {
        return (field.hasDeclarations() ?
                BuiltIn.<AValue>toCode(field.getFieldName() + "Len") :
                BuiltIn.<AValue>toCode("this." + field.getFieldName() + ".encodedLength()"));
    }

    private void addToString(String name, Field[] fields) {
        Var buildOutput = Var.local(String.class, "out",
                sum(literal(name), literal("("), getField("number").ref(), literal(")")));
        Block body = new Block(buildOutput);
        for (Field field : fields)
            body.addStatement(BuiltIn.inc(buildOutput, sum(
                    literal("\\n\\t" + field.getFieldName() + " = "),
                    (field.hasDeclarations() ?
                            new MethodCall<AString>("Util.joinStringArray", field.ref(), literal(", "),
                                    literal("("), literal(")")) :
                            field.<AString>call("toString")))));
        body.addStatement(RETURN(buildOutput.ref()));
        addMethod(Method.newPublicReadObjectState(Comment.no(), new TargetClass(String.class), "toString", body));
    }

    private void addJavaGetter(Field field) throws UndefinedException {
        Block body;

        if (field.hasDeclarations()) {
            Var out = Var.local(field.getJType() + field.getArrayDeclaration(), "out",
                    BuiltIn.<AValue>toCode("new " + field.getJType() + field.getNewCreation()));
            body = new Block(out);
            field.forElementsInField("out[i] = " + "this." + field.getFieldName() + "[i].getValue()", body);
            body.addStatement(RETURN(out.ref()));
        } else {
            body = new Block(RETURN(BuiltIn.<AValue>toCode("this." + field.getFieldName() + ".getValue()")));
        }

        addMethod(Method.newPublicReadObjectState(Comment.no(),
                TargetClass.fromName(field.getJType() + field.getArrayDeclaration()),
                "get" + field.getFieldName().substring(0, 1).toUpperCase() + field.getFieldName().substring(1) + "Value",
                body));
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
