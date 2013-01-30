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
import org.freeciv.packet.fieldtype.FieldType;
import org.freeciv.packet.fieldtype.FieldTypeException;
import org.freeciv.packetgen.UndefinedException;
import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.ReqKind;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.supporting.Field;
import org.freeciv.packetgen.javaGenerator.*;
import org.freeciv.packetgen.javaGenerator.Block;
import org.freeciv.packetgen.javaGenerator.Import;
import org.freeciv.packetgen.javaGenerator.expression.MethodCall;
import org.freeciv.packetgen.javaGenerator.typeBridge.Typed;
import org.freeciv.packetgen.javaGenerator.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.typeBridge.willReturn.*;
import org.freeciv.types.FCEnum;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import static org.freeciv.packetgen.javaGenerator.util.BuiltIn.*;

public class Packet extends ClassWriter implements IDependency, ReqKind {
    private final int number;
    private final Field[] fields;

    private final String logger;

    private final Requirement iFulfill;
    private final HashSet<Requirement> requirements = new HashSet<Requirement>();

    private final TargetClass ioexception = TargetClass.newKnown(IOException.class);

    public Packet(String name, int number, TargetClass headerKind, String logger,
                  List<Annotate> packetFlags, Field... fields) throws UndefinedException {
        super(ClassKind.CLASS, TargetPackage.from(org.freeciv.packet.Packet.class.getPackage()),
                Imports.are(Import.allIn(FieldType.class.getPackage()),
                        Import.allIn(FCEnum.class.getPackage()),
                        Import.classIn(org.freeciv.Util.class),
                        Import.classIn(DataInput.class),
                        Import.classIn(DataOutput.class),
                        Import.classIn(java.util.logging.Logger.class),
                        Import.classIn(IOException.class)),
                "Freeciv's protocol definition", packetFlags, name,
                      DEFAULT_PARENT, Arrays.asList(TargetClass.newKnown(org.freeciv.packet.Packet.class)));

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

        addClassConstant(Visibility.PUBLIC, int.class, "number", literal(number));

        addObjectConstantAndGetter(Var.field(Collections.<Annotate>emptyList(),
                Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO, TargetClass.fromClass(PacketHeader.class), "header", null));

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
        TargetClass ft = TargetClass.newKnown(FieldTypeException.class);
        Var<AValue> fte = Var.local(ft, "fte",
                R_IF(isInstanceOf(e.ref(), ft), cast(ft, e.ref()), ft.newInstance(literal("Run time exception"), e.ref())));
        Var<AString> pName = Var.param(String.class, "field");
        Method.Helper addExceptionLocation = Method.newHelper(
                Comment.no(),
                ft,
                "addExceptionLocation",
                Arrays.asList(e, pName),
                new Block(
                        fte,
                        fte.ref().<Returnable>call("setInPacket", literal(getName())),
                        fte.ref().<Returnable>call("setField", pName.ref()),
                        new MethodCall<NoValue>("Logger.getLogger(" + logger + ").log",
                                TargetClass.fromClass(Level.class).callV("WARNING"),
                                sum(literal("Misinterpretation. "), fte.ref().callV("getMessage")),
                                fte.ref()),
                        RETURN(fte.ref())));
        addMethod(addExceptionLocation);

        return addExceptionLocation.getAddressOn(this.getAddress());
    }

    private void addConstructorFromFields(Field[] fields, TargetClass headerKind, TargetMethod addExceptionLocation) throws UndefinedException {
        Block constructorBody = new Block();
        LinkedList<Var<? extends AValue>> params = new LinkedList<Var<? extends AValue>>();
        for (Field field : fields) {
            params.add(Var.param(
                    field.getTType().scopeKnown(),
                    field.getFieldName()));
            field.appendValidationTo(constructorBody);
            constructorBody.addStatement(setFieldToVariableSameName(field.getFieldName()));

            Block validate = new Block();
            field.appendArrayEaterValidationTo(validate);
            if (0 < validate.numberOfStatements())
                constructorBody.addStatement(labelExceptionsWithPacketAndField(field, validate, addExceptionLocation));
        }
        constructorBody.addStatement(generateHeader(headerKind, addExceptionLocation));
        addMethod(Method.newPublicConstructor(Comment.no(), params, constructorBody));
    }

    private Typed<NoValue> labelExceptionsWithPacketAndField(Var field, Block operation, TargetMethod addExceptionLocation) {
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
                        sum(new MethodCall<AnInt>("calcBodyLen"), headerKind.read("HEADER_SIZE")),
                        getField("number").ref()))), addExceptionLocation);
    }

    private void addConstructorFromJavaTypes(Field[] fields, TargetClass headerKind, TargetMethod addExceptionLocation) throws UndefinedException {
        if (0 < fields.length) {
            LinkedList<Var<? extends AValue>> params = new LinkedList<Var<? extends AValue>>();
            Block constructorBodyJ = new Block();
            for (Field field : fields) {
                Var<AValue> asParam = Var.param(field.getUnderType().scopeKnown(),
                        field.getFieldName());
                params.add(asParam);

                Block readAndValidate = new Block();
                field.appendValidationTo(readAndValidate);
                readAndValidate.addStatement(field.assign(field.getTType().scopeKnown().newInstance(
                        asParam.ref(), field.getSuperLimit(0))));
                constructorBodyJ.addStatement(labelExceptionsWithPacketAndField(field, readAndValidate, addExceptionLocation));
            }
            constructorBodyJ.addStatement(generateHeader(headerKind, addExceptionLocation));
            addMethod(Method.newPublicConstructor(Comment.no(), params, constructorBodyJ));
        }
    }

    private void addConstructorFromDataInput(String name, Field[] fields, TargetClass headerKind, TargetMethod addExceptionLocation) throws UndefinedException {
        Var<TargetClass> argHeader = Var.param(TargetClass.newKnown(PacketHeader.class), "header");
        final Var<TargetClass> streamName = Var.param(TargetClass.newKnown(DataInput.class), "from");
        MethodCall<AnInt> calcBodyLenCall = new MethodCall<AnInt>("calcBodyLen");

        Block constructorBodyStream = new Block(getField("header").assign(argHeader.ref()));
        for (Field field : fields) {
            Block readAndValidate = new Block();
            field.appendValidationTo(readAndValidate);
            readAndValidate.addStatement(field.assign(field.getTType().scopeKnown().newInstance(streamName.ref(),
                    field.getSuperLimit(0))));
            constructorBodyStream.addStatement(labelExceptionsWithPacketAndField(field, readAndValidate, addExceptionLocation));
        }

        constructorBodyStream.groupBoundary();

        constructorBodyStream.addStatement(IF(isNotSame(getField("number").ref(), argHeader.ref().<AnInt>call("getPacketKind")),
                new Block(THROW((ioexception).newInstance(sum(
                        literal("Tried to create package " + name + " but packet number was "),
                        argHeader.ref().<AnInt>call("getPacketKind")))))));

        constructorBodyStream.addStatement(ASSERT(isInstanceOf(argHeader.ref(), headerKind),
                literal("Packet not generated for this kind of header")));

        Block wrongSize = new Block();
        constructorBodyStream.addStatement(IF(isNotSame(sum(argHeader.ref().<AnInt>call("getHeaderSize"),
                calcBodyLenCall), argHeader.ref().<AnInt>call("getTotalSize")), wrongSize));
        wrongSize.addStatement(new MethodCall<NoValue>("Logger.getLogger(" + logger + ").warning", sum(
                literal("Probable misinterpretation: "),
                literal("interpreted packet size ("),
                GROUP(sum(argHeader.ref().<AnInt>call("getHeaderSize"), calcBodyLenCall)),
                literal(") don't match header packet size ("), argHeader.ref().<AnInt>call("getTotalSize"),
                literal(") for "), new MethodCall<AString>("this.toString"))));
        wrongSize.addStatement(THROW(ioexception.newInstance(sum(
                literal("Packet size in header and Java packet not the same."),
                literal(" Header packet size: "), argHeader.ref().<AnInt>call("getTotalSize"),
                literal(" Header size: "), argHeader.ref().<AnInt>call("getHeaderSize"),
                literal(" Packet body size: "), calcBodyLenCall))));
        addMethod(Method.newPublicConstructorWithException(Comment.doc(
                "Construct an object from a DataInput", new String(),
                Comment.param(streamName, "data stream that is at the start of the package body"),
                Comment.param(argHeader, "header data. Must contain size and number"),
                Comment.docThrows(ioexception, "if the DataInput has a problem")),
                Arrays.asList(streamName, argHeader),
                Arrays.asList(ioexception),
                constructorBodyStream));
    }

    private void addEncoder(Field[] fields) {
        Var<TargetClass> pTo = Var.<TargetClass>param(TargetClass.newKnown(DataOutput.class), "to");
        Block body = new Block();
        body.addStatement(getField("header").ref().<Returnable>call("encodeTo", pTo.ref()));
        if (0 < fields.length) {
            for (Field field : fields)
                body.addStatement(field.ref().<Returnable>call("encodeTo", pTo.ref()));
        }
        addMethod(Method.newPublicDynamicMethod(Comment.no(),
                TargetClass.fromClass(void.class), "encodeTo", Arrays.asList(pTo),
                Arrays.asList(ioexception), body));
    }

    private void addCalcBodyLen(Field[] fields) {
        Block encodeFieldsLen = new Block();
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
                TargetClass.fromClass(int.class), "calcBodyLen", Collections.<Var<AValue>>emptyList(),
                Collections.<TargetClass>emptyList(),
                encodeFieldsLen));
    }

    private static Typed<? extends AValue> calcBodyLen(Field field) {
        return field.ref().<Returnable>call("encodedLength");
    }

    private void addToString(String name, Field[] fields) {
        Var buildOutput = Var.local(String.class, "out",
                sum(literal(name), literal("("), getField("number").ref(), literal(")")));
        Block body = new Block(buildOutput);
        for (Field field : fields)
            body.addStatement(BuiltIn.inc(buildOutput, sum(
                    literal("\\n\\t" + field.getFieldName() + " = "),
                    field.ref().<AString>call("toString"))));
        body.addStatement(RETURN(buildOutput.ref()));
        addMethod(Method.newPublicReadObjectState(Comment.no(), TargetClass.fromClass(String.class), "toString", body));
    }

    private void addJavaGetter(Field field) throws UndefinedException {
        Block body;

        body = new Block(RETURN(field.ref().<Returnable>call("getValue")));

        addMethod(Method.newPublicReadObjectState(Comment.no(),
                field.getUnderType().scopeKnown(),
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
