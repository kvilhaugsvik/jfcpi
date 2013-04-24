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

import com.kvilhaugsvik.javaGenerator.expression.Reference;
import com.kvilhaugsvik.javaGenerator.typeBridge.Value;
import org.freeciv.packet.DeltaKey;
import org.freeciv.packet.NoDelta;
import org.freeciv.packet.PacketHeader;
import org.freeciv.packet.fieldtype.FieldTypeException;
import org.freeciv.packet.fieldtype.Key;
import org.freeciv.packetgen.Hardcoded;
import org.freeciv.packetgen.UndefinedException;
import org.freeciv.packetgen.dependency.Dependency;
import org.freeciv.packetgen.dependency.ReqKind;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.supporting.Field;
import com.kvilhaugsvik.javaGenerator.*;
import com.kvilhaugsvik.javaGenerator.Block;
import com.kvilhaugsvik.javaGenerator.Import;
import com.kvilhaugsvik.javaGenerator.expression.MethodCall;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.*;
import org.freeciv.types.*;
import org.freeciv.utility.EndsInEternalZero;
import org.freeciv.utility.Validation;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.logging.Level;

import static com.kvilhaugsvik.javaGenerator.util.BuiltIn.*;

public class Packet extends ClassWriter implements Dependency.Item, ReqKind {
    private static final TargetClass validation = TargetClass.from(Validation.class);

    private final int number;
    private final List<Field> fields;

    private final String logger;
    private final boolean delta;

    private final Requirement iFulfill;
    private final HashSet<Requirement> requirements = new HashSet<Requirement>();

    static { // TODO: Make target class support generics and remove this work arround
        try {
            TargetClass.from("java.util", "Map<DeltaKey, Packet>")
                    .register(new TargetMethod(Map.class.getMethod("get", Object.class)));
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Is this Java? Map is supposed to have get(Object)...", e);
        }
        try {
            TargetClass.from("java.util", "Map<DeltaKey, Packet>")
                    .register(new TargetMethod(Map.class.getMethod("put", Object.class, Object.class)));
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Is this Java? Map is supposed to have put...", e);
        }
    }

    @Deprecated
    public Packet(String name, int number, TargetClass headerKind, String logger,
                  List<Annotate> packetFlags, boolean deltaIsOn, final boolean enableDeltaBoolFolding,
                  Field... fields) throws UndefinedException {
        this(name, number, headerKind, logger, packetFlags,
                deltaIsOn, enableDeltaBoolFolding, deltaIsOn ? Hardcoded.deltaField : null,
                Arrays.asList(fields));
    }

    public Packet(String name, int number, TargetClass headerKind, String logger,
                  List<Annotate> packetFlags,
                  boolean deltaIsOn, final boolean enableDeltaBoolFolding, FieldType bv_delta_fields,
                  List<Field> fields) throws UndefinedException {
        super(ClassKind.CLASS, TargetPackage.from(org.freeciv.packet.Packet.class.getPackage()),
                Imports.are(Import.allIn(org.freeciv.packet.fieldtype.FieldType.class.getPackage()),
                        Import.allIn(FCEnum.class.getPackage()),
                        Import.classIn(org.freeciv.Util.class),
                        Import.classIn(DataInput.class),
                        Import.classIn(DataOutput.class),
                        Import.classIn(java.util.logging.Logger.class),
                        Import.classIn(IOException.class)),
                "Freeciv's protocol definition", packetFlags, name,
                      DEFAULT_PARENT, Arrays.asList(TargetClass.from(org.freeciv.packet.Packet.class)));

        this.number = number;
        this.fields = fields;

        this.logger = logger;
        this.delta = deltaIsOn && hasDelta(packetFlags) && hasAtLeastOneDeltaField(fields);

        for (Field field : fields) {
            field.introduceNeighbours(fields);
        }

        for (Field field : fields) {
            requirements.addAll(field.getReqs());
        }

        iFulfill = new Requirement(getName(), Packet.class);

        addClassConstant(Visibility.PUBLIC, int.class, "number", literal(number));

        addObjectConstantAndGetter(Var.field(Collections.<Annotate>emptyList(),
                Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO, TargetClass.from(PacketHeader.class), "header", null));

        int deltaFields = 0;
        if (delta) {
            requirements.add(bv_delta_fields.getIFulfillReq());

            addObjectConstant(bv_delta_fields.getAddress(), "delta");

            addMethod(Method.custom(Comment.no(), Visibility.PROTECTED, Scope.OBJECT,
                    TargetClass.from(boolean[].class), "getDeltaVector",
                    Collections.<Var<? extends AValue>>emptyList(),
                    Collections.<TargetClass>emptyList(),
                    new Block(RETURN(getField("delta").ref().callV("getValue").callV("getBits")))));

            for (Field field : fields) {
                if (!field.isAnnotatedUsing(Key.class.getSimpleName())) {
                    field.setDelta(deltaFields);
                    deltaFields++;
                }
            }
        }

        for (Field field : fields) {
            addObjectConstantAndGetter(field);
            addJavaGetter(field);
        }

        addEncoder(fields, enableDeltaBoolFolding);
        addCalcBodyLen(fields, enableDeltaBoolFolding);
        addGetDeltaKey(number, fields);

        addToString(name, fields);

        TargetMethod addExceptionLocation = addExceptionLocationAdder();

        if (delta) {
            if (0 < fields.size()) {
                addConstructorZero(fields, headerKind, addExceptionLocation, deltaFields, bv_delta_fields);
                addClassConstant(Visibility.PUBLIC, getAddress(), "zero", getAddress().newInstance());
            }
        }

        addConstructorFromFields(fields, headerKind, addExceptionLocation, deltaFields, bv_delta_fields);
        addConstructorFromJavaTypes(fields, headerKind, addExceptionLocation, deltaFields, bv_delta_fields);
        addConstructorFromDataInput(name, fields, headerKind, addExceptionLocation, deltaFields, enableDeltaBoolFolding, bv_delta_fields);
    }

    private static boolean hasAtLeastOneDeltaField(List<Field> fields) {
        for (Field field : fields)
            if (!field.isAnnotatedUsing(Key.class.getSimpleName()))
                return true;
        return false;
    }

    private static boolean hasDelta(List<Annotate> packetFlags) {
        for (Annotate flag : packetFlags)
            if (NoDelta.class.getSimpleName().equals(flag.getName()))
                return false;
        return true;
    }

    private TargetMethod addExceptionLocationAdder() {
        Var<AValue> e = Var.param(Throwable.class, "e");
        TargetClass ft = TargetClass.from(FieldTypeException.class);
        Var<AValue> fte = Var.local(ft, "fte",
                R_IF(isInstanceOf(e.ref(), ft), cast(ft, e.ref()),
                        ft.newInstance(sum(literal("threw "), e.ref().callV("getClass").callV("getName")), e.ref())));
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
                                TargetClass.from(Level.class).callV("WARNING"),
                                sum(literal("Misinterpretation. "), fte.ref().callV("getMessage")),
                                fte.ref()),
                        RETURN(fte.ref())));
        addMethod(addExceptionLocation);

        return addExceptionLocation.getAddressOn(this.getAddress());
    }

    private void addConstructorZero(List<Field> fields, TargetClass headerKind, TargetMethod addExceptionLocation,
                                    int deltaFields, FieldType bv_delta_fields) throws UndefinedException {
        Block body = new Block();

        if (delta)
            addDeltaField(addExceptionLocation, deltaFields, body, bv_delta_fields);

        Var<AValue> zeroes = Var.local(TargetClass.from(DataInputStream.class), "zeroStream",
                TargetClass.from(DataInputStream.class)
                        .newInstance(TargetClass.from(EndsInEternalZero.class).newInstance()));
        body.addStatement(zeroes);

        for (Field field : fields)
            body.addStatement(labelExceptionsWithPacketAndField(field,
                    new Block(field.ref().assign(field.getTType()
                            .newInstance(zeroes.ref(), field.getSuperLimit(0)))),
                    addExceptionLocation));

        Var header = getField("header");
        body.addStatement(labelExceptionsWithPacketAndField(header, new Block(
                header.assign(headerKind.newInstance(
                        sum(new MethodCall<AnInt>("calcBodyLen"), headerKind.read("HEADER_SIZE")),
                        getField("number").ref()))), addExceptionLocation));

        addMethod(Method.newConstructor(Comment.no(), Visibility.PRIVATE,
                Collections.<Var<?>>emptyList(), Collections.<TargetClass>emptyList(), body));
    }

    private void addConstructorFromFields(List<Field> fields, TargetClass headerKind, TargetMethod addExceptionLocation, int deltaFields, FieldType bv_delta_fields) throws UndefinedException {
        Block constructorBody = new Block();

        if (delta)
            addDeltaField(addExceptionLocation, deltaFields, constructorBody, bv_delta_fields);


        LinkedList<Var<? extends AValue>> params = new LinkedList<Var<? extends AValue>>();
        for (Field field : fields) {
            final Var<AValue> asParam = Var.param(
                    field.getTType(),
                    field.getFieldName());
            params.add(asParam);
            constructorBody.addStatement(validation.call("validateNotNull", asParam.ref(), literal(asParam.getName())));

            field.validateLimitInsideInt(constructorBody);
            constructorBody.addStatement(field.ref().assign(asParam.ref()));
            Block validate = new Block();
            field.appendArrayEaterValidationTo(validate);
            if (0 < validate.numberOfStatements())
                constructorBody.addStatement(labelExceptionsWithPacketAndField(field, validate, addExceptionLocation));
        }

        final Var<AValue> pHeaderKind = Var.param(TargetClass.from(Constructor.class), "headerKind");
        constructorBody.addStatement(generateHeader(pHeaderKind.ref(), addExceptionLocation));

        params.add(pHeaderKind);
        addMethod(Method.newPublicConstructor(Comment.no(), params, constructorBody));
    }

    private Typed<NoValue> labelExceptionsWithPacketAndField(Var field, Block operation, TargetMethod addExceptionLocation) {
        Var<AValue> e = Var.param(Throwable.class, "e");
        return BuiltIn.tryCatch(
                operation,
                e,
                new Block(THROW(addExceptionLocation.<AValue>call(e.ref(), literal(field.getName())))));
    }

    private Typed<NoValue> generateHeader(Value<? extends AValue> headerKind, TargetMethod addExceptionLocation) {
        Var header = getField("header");
        return labelExceptionsWithPacketAndField(header, new Block(header.assign(BuiltIn.cast(
                PacketHeader.class,
                headerKind.callV("newInstance",
                        sum(new MethodCall<AnInt>("calcBodyLen"),
                                headerKind.callV("getDeclaringClass").callV("getField", literal("HEADER_SIZE"))
                                        .callV("getInt", NULL)),
                        getField("number").ref())))), addExceptionLocation);
    }

    private void addDeltaField(TargetMethod addExceptionLocation, int deltaFields, Block body, FieldType bv_delta_fields) {
        body.addStatement(labelExceptionsWithPacketAndField(
                getField("delta"),
                new Block(getField("delta").assign(bv_delta_fields.getAddress().newInstance(
                        bv_delta_fields.getUnderType()
                                .newInstance(TRUE, literal(deltaFields)),
                        new MethodCall("ElementsLimit.limit", literal(deltaFields))))),
                addExceptionLocation));
    }

    private void addConstructorFromJavaTypes(List<Field> fields, TargetClass headerKind, TargetMethod addExceptionLocation, int deltaFields, FieldType bv_delta_fields) throws UndefinedException {
        if (0 < fields.size()) {
            LinkedList<Var<? extends AValue>> params = new LinkedList<Var<? extends AValue>>();
            Block constructorBodyJ = new Block();

            if (delta)
                addDeltaField(addExceptionLocation, deltaFields, constructorBodyJ, bv_delta_fields);

            for (Field field : fields) {
                Var<AValue> asParam = Var.param(field.getUnderType(),
                        field.getFieldName());
                params.add(asParam);
                constructorBodyJ.addStatement(validation.call("validateNotNull", asParam.ref(), literal(asParam.getName())));

                Block readAndValidate = new Block();
                field.validateLimitInsideInt(readAndValidate);
                readAndValidate.addStatement(field.assign(field.getTType().newInstance(
                        asParam.ref(), field.getSuperLimit(0))));
                final Typed<NoValue> readLabeled = labelExceptionsWithPacketAndField(field, readAndValidate, addExceptionLocation);
                constructorBodyJ.addStatement(readLabeled);
            }

            final Var<AValue> pHeaderKind = Var.param(TargetClass.from(Constructor.class), "headerKind");
            constructorBodyJ.addStatement(generateHeader(pHeaderKind.ref(), addExceptionLocation));

            params.add(pHeaderKind);
            addMethod(Method.newPublicConstructor(Comment.no(), params, constructorBodyJ));
        }
    }

    private void addConstructorFromDataInput(String name, List<Field> fields, TargetClass headerKind, TargetMethod addExceptionLocation, int deltaFields, boolean enableDeltaBoolFolding, FieldType bv_delta_fields) throws UndefinedException {
        Var<TargetClass> argHeader = Var.param(TargetClass.from(PacketHeader.class), "header");
        final Var<TargetClass> streamName = Var.param(TargetClass.from(DataInput.class), "from");
        final Var<TargetClass> old =
                Var.param(TargetClass.from("java.util", "Map<DeltaKey, Packet>"), "old");
        MethodCall<AnInt> calcBodyLenCall = new MethodCall<AnInt>("calcBodyLen");

        Block constructorBodyStream = new Block();
        constructorBodyStream.addStatement(validation.call("validateNotNull", argHeader.ref(), literal(argHeader.getName())));
        constructorBodyStream.addStatement(validation.call("validateNotNull", streamName.ref(), literal(streamName.getName())));
        constructorBodyStream.addStatement(validation.call("validateNotNull", old.ref(), literal(old.getName())));
        constructorBodyStream.groupBoundary();

        constructorBodyStream.addStatement(getField("header").assign(argHeader.ref()));
        if (delta) {
            Block operation = new Block();
            operation.addStatement(getField("delta").assign(getField("delta").getTType().newInstance(
                    streamName.ref(),
                    new MethodCall("ElementsLimit.limit", literal(deltaFields)))));
            constructorBodyStream.addStatement(labelExceptionsWithPacketAndField(
                    getField("delta"),
                    operation,
                    addExceptionLocation));
        }

        boolean oldNeeded = true;
        final Var<? extends AValue> chosenOld;
        if (delta)
            chosenOld = Var.local(getAddress(), "chosenOld", R_IF(
                    isSame(NULL, old.ref().callV("get", getAddress().callV("getKey", Reference.THIS))),
                    getField("zero").ref(),
                    cast(getAddress(), old.ref().callV("get", getAddress().callV("getKey", Reference.THIS)))));
        else
            chosenOld = null;
        for (Field field : fields) {
            if (delta && oldNeeded) {
                if (!field.isAnnotatedUsing(Key.class.getSimpleName())) {
                    oldNeeded = false;
                    constructorBodyStream.addStatement(chosenOld);
                }
            }
            Block readAndValidate = new Block();
            field.validateLimitInsideInt(readAndValidate);
            readAndValidate.addStatement(field.assign(field.getTType().newInstance(streamName.ref(),
                    field.getSuperLimit(0))));
            final Typed<NoValue> readLabeled = labelExceptionsWithPacketAndField(field, readAndValidate, addExceptionLocation);
            constructorBodyStream.addStatement(isBoolFolded(enableDeltaBoolFolding, field) ?
                    field.assign(field.getTType().newInstance(deltaHas(field), Hardcoded.noLimit)) :
                    ifDeltaElse(field, readLabeled, delta ?
                            field.assign(chosenOld.ref().callV(getterNameJavaish(field))) :
                            literal("Never run")));
        }

        constructorBodyStream.groupBoundary();

        constructorBodyStream.addStatement(IF(isNotSame(getField("number").ref(), argHeader.ref().<AnInt>call("getPacketKind")),
                new Block(THROW(addExceptionLocation.callV(
                        TargetClass.from(FieldTypeException.class).newInstance(sum(
                                literal("Wrong packet number. "),
                                literal("Packet is " + name + " (" + number + ") but header is for packet number "),
                                argHeader.ref().<AnInt>call("getPacketKind"))),
                        literal("header"))))));

        constructorBodyStream.addStatement(IF(isNotSame(sum(argHeader.ref().<AnInt>call("getHeaderSize"),
                calcBodyLenCall), argHeader.ref().<AnInt>call("getTotalSize")),
                new Block(THROW(addExceptionLocation.callV(
                        TargetClass.from(FieldTypeException.class).newInstance(sum(
                                literal("interpreted packet size ("),
                                GROUP(sum(argHeader.ref().<AnInt>call("getHeaderSize"), calcBodyLenCall)),
                                literal(") don't match header packet size ("),
                                argHeader.ref().<AnInt>call("getTotalSize"),
                                literal(") for "), new MethodCall<AString>("this.toString"))),
                        literal("header"))))));

        if (delta)
            constructorBodyStream.addStatement(old.ref().callV("put",
                    getAddress().callV("getKey", Reference.THIS),
                    Reference.THIS));

        addMethod(Method.newPublicConstructorWithException(Comment.doc(
                "Construct an object from a DataInput", new String(),
                Comment.param(streamName, "data stream that is at the start of the package body"),
                Comment.param(argHeader, "header data. Must contain size and number"),
                Comment.param(old, "where the Delta protocol should look for older packets"),
                Comment.docThrows(TargetClass.from(FieldTypeException.class), "if there is a problem")),
                Arrays.asList(streamName, argHeader, old),
                Arrays.asList(TargetClass.from(FieldTypeException.class)),
                constructorBodyStream));
    }

    private boolean isBoolFolded(boolean boolFoldEnabled, Field field) {
        return boolFoldEnabled && field.isDelta() && "Boolean".equals(field.getUnderType().getName());
    }

    private Typed<?> ifDeltaElse(Field field, Typed<?> defaultAction, Typed<?> deltaDisabledAction) {
        return deltaApplies(field) ?
                (null == deltaDisabledAction ?
                        IF(deltaHas(field),
                                new Block(defaultAction)) :
                        IF(deltaHas(field),
                                new Block(defaultAction),
                                new Block(deltaDisabledAction))) :
                defaultAction;
    }

    private Value<ABool> deltaHas(Field field) {
        return getField("delta").ref().callV("getValue").<ABool>callV("get", literal(field.getDeltaFieldNumber()));
    }

    private boolean deltaApplies(Field field) {
        return (delta && field.isDelta());
    }

    private void addEncoder(List<Field> fields, boolean enableDeltaBoolFolding) {
        Var<TargetClass> pTo = Var.<TargetClass>param(TargetClass.from(DataOutput.class), "to");
        Block body = new Block();
        body.addStatement(getField("header").ref().<Returnable>call("encodeTo", pTo.ref()));
        if (0 < fields.size()) {
            if (delta)
                body.addStatement(getField("delta").ref().call("encodeTo", pTo.ref()));
            for (Field field : fields)
                if (!isBoolFolded(enableDeltaBoolFolding, field))
                    body.addStatement(ifDeltaElse(field, field.ref().<Returnable>call("encodeTo", pTo.ref()), null));
        }
        addMethod(Method.newPublicDynamicMethod(Comment.no(),
                TargetClass.from(void.class), "encodeTo", Arrays.asList(pTo),
                Arrays.asList(TargetClass.from(IOException.class)), body));
    }

    private void addCalcBodyLen(List<Field> fields, boolean enableDeltaBoolFolding) {
        Block encodeFieldsLen = new Block();
        if (0 < fields.size()) {
            final Typed<? extends AValue> elem1 = calcBodyLen(fields.get(0));
            Typed<? extends AValue> summing;
            if (delta) {
                final Value deltaSize = getField("delta").ref().callV("encodedLength");
                if (isBoolFolded(enableDeltaBoolFolding, fields.get(0)))
                    summing = deltaSize;
                else
                    summing = sum(deltaSize, elem1);
            } else {
                summing = elem1;
            }
            for (int i = 1; i < fields.size(); i++)
                if (!isBoolFolded(enableDeltaBoolFolding, fields.get(i)))
                    summing = sum(summing, calcBodyLen(fields.get(i)));
            encodeFieldsLen.addStatement(RETURN(summing));
        } else {
            encodeFieldsLen.addStatement(RETURN(literal(0)));
        }
        addMethod(Method.custom(Comment.no(),
                Visibility.PRIVATE, Scope.OBJECT,
                TargetClass.from(int.class), "calcBodyLen", Collections.<Var<AValue>>emptyList(),
                Collections.<TargetClass>emptyList(),
                encodeFieldsLen));
    }

    private Typed<? extends AValue> calcBodyLen(Field field) {
        if (deltaApplies(field))
            return R_IF(deltaHas(field), field.ref().<AnInt>call("encodedLength"), literal(0));
        else
            return field.ref().<Returnable>call("encodedLength");
    }

    private void addToString(String name, List<Field> fields) {
        Var buildOutput = Var.local(String.class, "out",
                sum(literal(name), literal("("), getField("number").ref(), literal(")")));
        Block body = new Block(buildOutput);
        if (delta)
            body.addStatement(BuiltIn.inc(buildOutput, sum(literal(" delta header = "),
                    getField("delta").ref().callV("toString"))));
        for (Field field : fields)
            body.addStatement(BuiltIn.inc(buildOutput, sum(
                    literal("\\n\\t" + field.getFieldName() + " = "),
                    field.ref())));
        body.addStatement(RETURN(buildOutput.ref()));
        addMethod(Method.newPublicReadObjectState(Comment.no(), TargetClass.from(String.class), "toString", body));
    }

    private void addGetDeltaKey(int number, List<Field> fields) {
        List<Typed<? extends AValue>> params = new LinkedList<Typed<? extends AValue>>();
        params.add(literal(number));
        for (Field<?> field : getKeyFields(fields))
            params.add(field.ref());
        addMethod(Method.newPublicReadObjectState(
                Comment.doc("Get a delta key for this packet",
                        "A DeltaKey used in a HashMap makes it easy to find the previous packet of " +
                                "the same packet kind where all key fields are the same.",
                        Comment.docReturns("a delta key matching the packet.")),
                TargetClass.from(DeltaKey.class), "getKey",
                new Block(RETURN(TargetClass.from(DeltaKey.class)
                        .newInstance(params.toArray(new Typed[params.size()]))))));
    }

    private static List<Field<?>> getKeyFields(List<Field> fields) {
        List<Field<?>> out = new LinkedList<Field<?>>();
        for (Field field : fields)
            if (field.isAnnotatedUsing(Key.class.getSimpleName()))
                out.add(field);
        return out;
    }

    private void addJavaGetter(Field field) throws UndefinedException {
        Block body;

        body = new Block(RETURN(field.ref().<Returnable>call("getValue")));

        addMethod(Method.newPublicReadObjectState(Comment.no(),
                field.getUnderType(),
                getterNameJavaish(field) + "Value",
                body));
    }

    public int getNumber() {
        return number;
    }

    public List<? extends Field> getFields() {
        return Collections.unmodifiableList(fields);
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
