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
import org.freeciv.packet.fieldtype.ElementsLimit;
import org.freeciv.packet.fieldtype.FieldTypeException;
import org.freeciv.packet.fieldtype.Key;
import org.freeciv.packetgen.Hardcoded;
import com.kvilhaugsvik.dependency.UndefinedException;
import com.kvilhaugsvik.dependency.Dependency;
import com.kvilhaugsvik.dependency.ReqKind;
import com.kvilhaugsvik.dependency.Requirement;
import org.freeciv.packetgen.enteties.supporting.Field;
import com.kvilhaugsvik.javaGenerator.*;
import com.kvilhaugsvik.javaGenerator.Block;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.*;
import org.freeciv.utility.EndsInEternalZero;
import org.freeciv.utility.Validation;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.logging.Level;

import static com.kvilhaugsvik.javaGenerator.util.BuiltIn.*;

public class Packet extends ClassWriter implements Dependency.Item, ReqKind {
    private static final TargetClass validation = TargetClass.from(Validation.class);
    private static final TargetClass noDeltaFlag = TargetClass.from(NoDelta.class);
    public static final TargetClass keyFlagg = TargetClass.from(Key.class);

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

    public Packet(String name, int number, TargetClass headerKind, String logger,
                  List<Annotate> packetFlags,
                  boolean deltaIsOn, final boolean enableDeltaBoolFolding, FieldType bv_delta_fields,
                  List<Field> fields) throws UndefinedException {
        super(ClassKind.CLASS, TargetPackage.from(org.freeciv.packet.Packet.class.getPackage()),
                Imports.are(),
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
                if (!field.isAnnotatedUsing(keyFlagg)) {
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
                addConstructorZero(fields, headerKind, addExceptionLocation, deltaFields, bv_delta_fields, enableDeltaBoolFolding);
                addClassConstant(Visibility.PUBLIC, getAddress(), "zero", getAddress().newInstance());
            }
        }

        addBasicConstructor(fields);
        addConstructorFromFields(fields, addExceptionLocation, deltaFields, bv_delta_fields, enableDeltaBoolFolding);
        addConstructorFromJavaTypes(fields, addExceptionLocation, deltaFields, bv_delta_fields, enableDeltaBoolFolding);
        addConstructorFromDataInput(name, fields, addExceptionLocation, deltaFields, enableDeltaBoolFolding);
    }

    private static boolean hasAtLeastOneDeltaField(List<Field> fields) {
        for (Field field : fields)
            if (!field.isAnnotatedUsing(keyFlagg))
                return true;
        return false;
    }

    private static boolean hasDelta(List<Annotate> packetFlags) {
        for (Annotate flag : packetFlags)
            if (flag.sameClass(noDeltaFlag))
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
                        TargetClass.from(java.util.logging.Logger.class)
                                .callV("getLogger", BuiltIn.<AValue>toCode(logger))
                                .call("log",
                                        TargetClass.from(Level.class).callV("WARNING"),
                                        sum(literal("Misinterpretation. "), fte.ref().callV("getMessage")),
                                        fte.ref()),
                        RETURN(fte.ref())));
        addMethod(addExceptionLocation);

        return addExceptionLocation.getAddressOn(this.getAddress());
    }

    private void addConstructorZero(List<Field> fields, TargetClass headerKind, TargetMethod addExceptionLocation,
                                    int deltaFields, FieldType bv_delta_fields, boolean enableDeltaBoolFolding) throws UndefinedException {
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
                            .newInstance(zeroes.ref(), field.getSuperLimit(0, false)))),
                    addExceptionLocation));

        final LinkedList<Reference<? extends AValue>> deltaAndFields = getBodyFields(fields, enableDeltaBoolFolding);
        Var header = getField("header");
        body.addStatement(labelExceptionsWithPacketAndField(header, new Block(
                header.assign(headerKind.newInstance(
                        sum(getAddress().callV("calcBodyLen", deltaAndFields.toArray(new Typed[deltaAndFields.size()])), headerKind.callV("HEADER_SIZE")),
                        getField("number").ref()))), addExceptionLocation));

        addMethod(Method.newConstructor(Comment.no(), Visibility.PRIVATE,
                Collections.<Var<?>>emptyList(), Collections.<TargetClass>emptyList(), body));
    }

    private void addConstructorFromFields(List<Field> fields, TargetMethod addExceptionLocation, int deltaFields, FieldType bv_delta_fields, boolean enableDeltaBoolFolding) throws UndefinedException {
        Block body = new Block();

        if (delta)
            addDeltaField(addExceptionLocation, deltaFields, body, bv_delta_fields);


        LinkedList<Var<? extends AValue>> params = new LinkedList<Var<? extends AValue>>();
        for (Field field : fields) {
            final Var<AValue> asParam = Var.param(
                    field.getTType(),
                    field.getName());
            params.add(asParam);
            body.addStatement(validation.call("validateNotNull", asParam.ref(), literal(asParam.getName())));

            field.validateLimitInsideInt(body);
            body.addStatement(field.ref().assign(asParam.ref()));
            Block validate = new Block();
            field.appendArrayEaterValidationTo(validate);
            if (0 < validate.numberOfStatements())
                body.addStatement(labelExceptionsWithPacketAndField(field, validate, addExceptionLocation));
        }

        final LinkedList<Reference<? extends AValue>> deltaAndFields = getBodyFields(fields, enableDeltaBoolFolding);
        final Var<AValue> pHeaderKind = Var.param(TargetClass.from(Constructor.class), "headerKind");
        body.addStatement(generateHeader(pHeaderKind.ref(), addExceptionLocation, deltaAndFields));

        params.add(pHeaderKind);
        addMethod(Method.newPublicConstructor(Comment.no(), params, body));
    }

    private Typed<NoValue> labelExceptionsWithPacketAndField(Var field, Block operation, TargetMethod addExceptionLocation) {
        Var<AValue> e = Var.param(Throwable.class, "e");
        return BuiltIn.tryCatch(
                operation,
                e,
                new Block(THROW(addExceptionLocation.<AValue>call(e.ref(), literal(field.getName())))));
    }

    private Typed<NoValue> generateHeader(Value<? extends AValue> headerKind, TargetMethod addExceptionLocation, List<? extends Typed<? extends AValue>> fields) {
        Var header = getField("header");
        return labelExceptionsWithPacketAndField(header, new Block(header.assign(BuiltIn.cast(
                PacketHeader.class,
                headerKind.callV("newInstance",
                        sum(getAddress().callV("calcBodyLen", fields.toArray(new Typed[fields.size()])),
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
                        TargetClass.from(ElementsLimit.class).callV("limit", literal(deltaFields))))),
                addExceptionLocation));
    }

    private void addConstructorFromJavaTypes(List<Field> fields, TargetMethod addExceptionLocation, int deltaFields, FieldType bv_delta_fields, boolean enableDeltaBoolFolding) throws UndefinedException {
        LinkedList<Var<? extends AValue>> params = new LinkedList<Var<? extends AValue>>();
        LinkedList<Reference<? extends AValue>> localVars = new LinkedList<Reference<? extends AValue>>();
        Block body = new Block();

        for (Field field : fields) {
            Var<AValue> asParam = Var.param(field.getUnderType(), field.getName());
            params.add(asParam);
            body.addStatement(validation.call("validateNotNull", asParam.ref(), literal(asParam.getName())));

            Var<AValue> asLocal = field.getTmpLocalVar(null);
            localVars.add(asLocal.ref());
            body.addStatement(asLocal);

            Block readAndValidate = new Block();
            field.validateLimitInsideInt(readAndValidate);
            readAndValidate.addStatement(asLocal.assign(field.getTType().newInstance(asParam.ref(), field.getSuperLimit(0, true))));
            final Typed<NoValue> readLabeled = labelExceptionsWithPacketAndField(field, readAndValidate, addExceptionLocation);
            body.addStatement(readLabeled);
        }

        final Var<AValue> headerKind = Var.param(TargetClass.from(Constructor.class), "headerKind");
        params.add(headerKind);
        localVars.add(headerKind.ref());
        body.addStatement(validation.call("validateNotNull", headerKind.ref(), literal(headerKind.getName())));

        body.addStatement(BuiltIn.RETURN(getAddress().newInstance(localVars.toArray(new Reference[localVars.size()]))));

        addMethod(Method.custom(Comment.no(), Visibility.PUBLIC, Scope.CLASS, getAddress(), "fromValues", params, Collections.<TargetClass>emptyList(), body));
    }

    private void addBasicConstructor(List<Field> someFields) throws UndefinedException {
        Block body = new Block();

        LinkedList<Var> fields = new LinkedList<Var>(someFields);

        if (delta) fields.add(getField("delta"));

        fields.add(getField("header"));

        LinkedList<Var<? extends AValue>> params = new LinkedList<Var<? extends AValue>>();
        for (Var field : fields) {
            final Var<AValue> asParam = Var.param(
                    field.getTType(),
                    field.getName());
            params.add(asParam);
            body.addStatement(field.ref().assign(asParam.ref()));
        }

        addMethod(Method.newConstructor(Comment.no(), Visibility.PRIVATE, params, Collections.<TargetClass>emptyList(), body));
    }

    private void addConstructorFromDataInput(String name, List<Field> fields, TargetMethod addExceptionLocation, int deltaFields, boolean enableDeltaBoolFolding) throws UndefinedException {
        Var<TargetClass> argHeader = Var.param(TargetClass.from(PacketHeader.class), "header");
        final Var<TargetClass> streamName = Var.param(TargetClass.from(DataInput.class), "from");
        final Var<TargetClass> old =
                Var.param(TargetClass.from("java.util", "Map<DeltaKey, Packet>"), "old");

        final LinkedList<Reference<? extends AValue>> deltaAndFields = new LinkedList<Reference<? extends AValue>>();

        Block body = new Block();
        body.addStatement(validation.call("validateNotNull", argHeader.ref(), literal(argHeader.getName())));
        body.addStatement(validation.call("validateNotNull", streamName.ref(), literal(streamName.getName())));
        body.addStatement(validation.call("validateNotNull", old.ref(), literal(old.getName())));
        body.groupBoundary();

        final Var delta_tmp = delta ? Var.param(getField("delta").getTType(), "delta") : null;
        if (delta) {
            deltaAndFields.add(delta_tmp.ref());
            body.addStatement(delta_tmp);
            Block operation = new Block();
            operation.addStatement(delta_tmp.assign(delta_tmp.getTType().newInstance(
                    streamName.ref(),
                    TargetClass.from(ElementsLimit.class).callV("limit", literal(deltaFields)))));
            body.addStatement(labelExceptionsWithPacketAndField(
                    delta_tmp,
                    operation,
                    addExceptionLocation));
        }

        boolean oldNeeded = true;
        final Var<? extends AValue> chosenOld;
        if (delta) {
            LinkedList<Value<? extends AValue>> keyArgs = new LinkedList<Value<? extends AValue>>();
            for (Field keyField : getKeyFields(fields))
                keyArgs.add(keyField.getTmpLocalVar(null).ref());
            chosenOld = Var.local(getAddress(), "chosenOld", R_IF(
                    isSame(NULL, old.ref().callV("get", getAddress().callV("getKeyPrivate", keyArgs.toArray(new Typed[keyArgs.size()])))),
                    getField("zero").ref(),
                    cast(getAddress(), old.ref().callV("get", getAddress().callV("getKeyPrivate", keyArgs.toArray(new Typed[keyArgs.size()]))))));
        } else {
            chosenOld = null;
        }

        LinkedList<Reference<? extends AValue>> constructorParams = new LinkedList<Reference<? extends AValue>>();
        for (Field field : fields) {
            Var<AValue> asLocal = field.getTmpLocalVar(null);
            constructorParams.add(asLocal.ref());
            body.addStatement(asLocal);

            if (!isBoolFolded(enableDeltaBoolFolding, field))
                deltaAndFields.add(asLocal.ref());

            if (delta && oldNeeded) {
                if (!field.isAnnotatedUsing(keyFlagg)) {
                    oldNeeded = false;
                    body.addStatement(chosenOld);
                }
            }
            Block readAndValidate = new Block();
            field.validateLimitInsideInt(readAndValidate);
            readAndValidate.addStatement(asLocal.assign(asLocal.getTType().newInstance(streamName.ref(),
                    field.getSuperLimit(0, true))));
            final Typed<NoValue> readLabeled = labelExceptionsWithPacketAndField(field, readAndValidate, addExceptionLocation);
            body.addStatement(isBoolFolded(enableDeltaBoolFolding, field) ?
                    asLocal.assign(field.getTType().newInstance(deltaHas(field, delta_tmp), Hardcoded.noLimit)) :
                    ifDeltaElse(field, readLabeled, delta ?
                            asLocal.assign(chosenOld.ref().callV(getterNameJavaish(field))) :
                            literal("Never run"), delta_tmp));
        }

        body.groupBoundary();

        if (delta)
            constructorParams.add(delta_tmp.ref());
        constructorParams.add(argHeader.ref());
        Var me = Var.local(getAddress(), "me", getAddress().newInstance(constructorParams.toArray(new Typed[constructorParams.size()])));
        body.addStatement(me);

        body.groupBoundary();

        Typed<AnInt> calcBodyLenCall = getAddress().<AnInt>callV("calcBodyLen", deltaAndFields.toArray(new Typed[deltaAndFields.size()]));

        body.addStatement(IF(isNotSame(getField("number").ref(), argHeader.ref().<AnInt>call("getPacketKind")),
                new Block(THROW(addExceptionLocation.callV(
                        TargetClass.from(FieldTypeException.class).newInstance(sum(
                                literal("Wrong packet number. "),
                                literal("Packet is " + name + " (" + number + ") but header is for packet number "),
                                argHeader.ref().<AnInt>call("getPacketKind"))),
                        literal("header"))))));

        body.addStatement(IF(isNotSame(sum(argHeader.ref().<AnInt>call("getHeaderSize"),
                calcBodyLenCall), argHeader.ref().<AnInt>call("getTotalSize")),
                new Block(THROW(addExceptionLocation.callV(
                        TargetClass.from(FieldTypeException.class).newInstance(sum(
                                literal("interpreted packet size ("),
                                GROUP(sum(argHeader.ref().<AnInt>call("getHeaderSize"), calcBodyLenCall)),
                                literal(") don't match header packet size ("),
                                argHeader.ref().<AnInt>call("getTotalSize"),
                                literal(") for "), getAddress().callV("toString", me.ref()))),
                        literal("header"))))));

        body.groupBoundary();

        if (delta)
            body.addStatement(old.ref().callV("put",
                    getAddress().callV("getKey", me.ref()),
                    me.ref()));

        body.addStatement(RETURN(me.ref()));

        addMethod(Method.custom(
                Comment.doc(
                        "Construct an object from a DataInput", new String(),
                        Comment.param(streamName, "data stream that is at the start of the package body"),
                        Comment.param(argHeader, "header data. Must contain size and number"),
                        Comment.param(old, "where the Delta protocol should look for older packets"),
                        Comment.docThrows(TargetClass.from(FieldTypeException.class), "if there is a problem")),
                Visibility.PUBLIC,
                Scope.CLASS,
                getAddress(),
                "fromHeaderAndStream",
                Arrays.asList(streamName, argHeader, old),
                Arrays.asList(TargetClass.from(FieldTypeException.class)),
                body));
    }

    private LinkedList<Reference<? extends AValue>> getBodyFields(List<Field> fields, boolean enableDeltaBoolFolding) {
        final LinkedList<Reference<? extends AValue>> deltaAndFields = new LinkedList<Reference<? extends AValue>>();
        if (delta)
            deltaAndFields.add(getField("delta").ref());
        for (Field field : fields)
            if (!isBoolFolded(enableDeltaBoolFolding, field))
                deltaAndFields.add(field.ref());
        return deltaAndFields;
    }

    private boolean isBoolFolded(boolean boolFoldEnabled, Field field) {
        return boolFoldEnabled && field.isDelta() && "Boolean".equals(field.getUnderType().getName());
    }

    private Typed<?> ifDeltaElse(Field field, Typed<?> defaultAction, Typed<?> deltaDisabledAction, Var deltaVar) {
        return deltaApplies(field) ?
                (null == deltaDisabledAction ?
                        IF(deltaHas(field, deltaVar),
                                new Block(defaultAction)) :
                        IF(deltaHas(field, deltaVar),
                                new Block(defaultAction),
                                new Block(deltaDisabledAction))) :
                defaultAction;
    }

    private Value<ABool> deltaHas(Field field, Var deltaVar) {
        return deltaVar.ref().callV("getValue").<ABool>callV("get", literal(field.getDeltaFieldNumber()));
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
                    body.addStatement(ifDeltaElse(field, field.ref().<Returnable>call("encodeTo", pTo.ref()), null, getField("delta")));
        }
        addMethod(Method.newPublicDynamicMethod(Comment.no(),
                TargetClass.from(void.class), "encodeTo", Arrays.asList(pTo),
                Arrays.asList(TargetClass.from(IOException.class)), body));
    }

    private void addCalcBodyLen(List<Field> fields, boolean enableDeltaBoolFolding) {
        Block body = new Block();
        LinkedList<Var<? extends AValue>> params = new LinkedList<Var<? extends AValue>>();

        Typed<? extends AValue> summing = literal(0);

        Var<? extends AValue> deltaParam = null;
        if (delta) {
            deltaParam = Var.param(getField("delta").getTType(), getField("delta").getName());
            params.add(deltaParam);
            summing = sum(summing, deltaParam.ref().callV("encodedLength"));
        }

        for (Field field : fields)
            if (!isBoolFolded(enableDeltaBoolFolding, field)) {
                Var<AValue> asParam = Var.param(field.getTType(), field.getName());
                params.add(asParam);
                summing = sum(summing, calcBodyLen(field, asParam, deltaParam));
            }

        body.addStatement(RETURN(summing));

        addMethod(Method.custom(Comment.no(),
                Visibility.PRIVATE, Scope.CLASS,
                TargetClass.from(int.class), "calcBodyLen", params,
                Collections.<TargetClass>emptyList(),
                body));
    }

    private Typed<? extends AValue> calcBodyLen(Field field, Var<AValue> asParam, Var deltaVar) {
        if (deltaApplies(field))
            return R_IF(deltaHas(field, deltaVar), asParam.ref().<AnInt>call("encodedLength"), literal(0));
        else
            return asParam.ref().<AnInt>call("encodedLength");
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
                    literal("\\n\\t" + field.getName() + " = "),
                    field.ref())));
        body.addStatement(RETURN(buildOutput.ref()));
        addMethod(Method.newPublicReadObjectState(Comment.no(), TargetClass.from(String.class), "toString", body));
    }

    private void addGetDeltaKey(int number, List<Field> fields) {
        List<Var<? extends AValue>> params = new LinkedList<Var<? extends AValue>>();
        List<Typed<? extends AValue>> fromDynamicArgs = new LinkedList<Typed<? extends AValue>>();
        List<Typed<? extends AValue>> deltaKeyArgs = new LinkedList<Typed<? extends AValue>>();

        deltaKeyArgs.add(literal(number));

        for (Field<?> field : getKeyFields(fields)) {
            fromDynamicArgs.add(field.ref());
            final Var<AValue> asParam = Var.param(field.getTType(), field.getName());
            params.add(asParam);
            deltaKeyArgs.add(asParam.ref());
        }

        addMethod(Method.custom(
                Comment.doc("Get a delta key for this packet",
                        "A DeltaKey used in a HashMap makes it easy to find the previous packet of " +
                                "the same packet kind where all key fields are the same.",
                        Comment.docReturns("a delta key matching the packet.")),
                Visibility.PRIVATE, Scope.CLASS, TargetClass.from(DeltaKey.class), "getKeyPrivate",
                params,
                Collections.<TargetClass>emptyList(),
                new Block(RETURN(TargetClass.from(DeltaKey.class)
                        .newInstance(deltaKeyArgs.toArray(new Typed[deltaKeyArgs.size()]))))));

        addMethod(Method.newPublicReadObjectState(
                Comment.doc("Get a delta key for this packet",
                        "A DeltaKey used in a HashMap makes it easy to find the previous packet of " +
                                "the same packet kind where all key fields are the same.",
                        Comment.docReturns("a delta key matching the packet.")),
                TargetClass.from(DeltaKey.class), "getKey",
                new Block(RETURN(getAddress().callV("getKeyPrivate", fromDynamicArgs.toArray(new Typed[fromDynamicArgs.size()]))))));
    }

    private static List<Field<?>> getKeyFields(List<Field> fields) {
        List<Field<?>> out = new LinkedList<Field<?>>();
        for (Field field : fields)
            if (field.isAnnotatedUsing(keyFlagg))
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
