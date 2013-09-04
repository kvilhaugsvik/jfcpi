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
import org.freeciv.packet.fieldtype.*;
import org.freeciv.packetgen.Hardcoded;
import com.kvilhaugsvik.dependency.UndefinedException;
import com.kvilhaugsvik.dependency.Dependency;
import com.kvilhaugsvik.dependency.ReqKind;
import com.kvilhaugsvik.dependency.Requirement;
import org.freeciv.packetgen.enteties.supporting.FactoryCapabilityCombination;
import org.freeciv.packetgen.enteties.supporting.Field;
import com.kvilhaugsvik.javaGenerator.*;
import com.kvilhaugsvik.javaGenerator.Block;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.*;
import org.freeciv.packetgen.enteties.supporting.PacketCapabilities;
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
    private final HashSet<Requirement> requirements;

    public Packet(String name, int number, String logger,
                  List<Annotate> packetFlags,
                  boolean deltaIsOn, final boolean enableDeltaBoolFolding, FieldType bv_delta_fields,
                  List<Field> packetFields,
                  SortedSet<String> caps) throws UndefinedException {
        super(ClassKind.ABSTRACT_CLASS, TargetPackage.from(org.freeciv.packet.Packet.class.getPackage()),
                Imports.are(),
                "Freeciv's protocol definition", packetFlags, name,
                      DEFAULT_PARENT, Arrays.asList(TargetClass.from(org.freeciv.packet.Packet.class)));

        this.number = number;
        this.fields = packetFields;

        this.logger = logger;
        this.delta = deltaIsOn && hasDelta(packetFlags) && hasAtLeastOneDeltaField(packetFields);

        for (Field field : packetFields) {
            field.introduceNeighbours(packetFields);
        }

        this.requirements = extractRequirements(bv_delta_fields, packetFields, delta);

        iFulfill = new Requirement(getName(), Packet.class);

        annotateMe(new PacketCapabilities(caps));

        addClassConstant(Visibility.PUBLIC, int.class, "number", literal(number));

        final Var<AValue> headerVar = Var.field(Collections.<Annotate>emptyList(),
                Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO, TargetClass.from(PacketHeader.class), "header", null);

        final LinkedList<Var<AValue>> sharedFields = extractSharedFields(bv_delta_fields, packetFields, headerVar, delta);

        for (Var field : sharedFields) {
            if (field instanceof Field) {
                this.addField(field);
                this.addMethod(createJavaGetter((Field)field));
            } else {
                this.addObjectConstantAndGetter(field);
            }
        }

        TargetMethod addExceptionLocation = addExceptionLocationAdder();

        this.addConstructorFields();

        final LinkedHashMap<String, List<Typed<AString>>> capabilityCombinations = allPossibleCombinations(caps);
        for (String capCombName : capabilityCombinations.keySet()) {
            final LinkedList<Field> implFields = filterForCapabilities(packetFields, capabilityCombinations.get(capCombName));

            // FIXME: Clean up so implFields and implFieldsAll can be the same
            final LinkedList<Var<AValue>> implFieldsAll = new LinkedList<Var<AValue>>();
            implFieldsAll.add(sharedFields.get(0));
            if (delta) implFieldsAll.add(sharedFields.get(1));
            for (Field<AValue> packetField : implFields)
                implFieldsAll.add(packetField);

            final int deltaFields = resetDeltaFields(implFields);

            ClassWriter inner = newInnerClass(ClassKind.CLASS, name + "_variant" + capCombName, getAddress(), Collections.<TargetClass>emptyList());

            for (Field field : implFields) {
                if (sharedFields.contains(field))
                    continue;

                inner.addField(field);
                inner.addMethod(createJavaGetter(field));
            }

            final Value<AValue> deltaOnSuper = delta ? inner.getInternalReferenceSuper().callV("delta") : null;

            inner.addMethod(createEncoder(implFields, enableDeltaBoolFolding, delta, inner.getInternalReferenceSuper().callV("header"), deltaOnSuper));
            inner.addMethod(createdCalcBodyLen(implFields, enableDeltaBoolFolding, getField("delta"), delta));
            inner.addMethod(createGetDeltaKeyPrivate(number, implFields));
            inner.addMethod(createGetDeltaKeyPublic(implFields, inner.getAddress()));
            inner.addMethod(createToString(name, implFields, delta, getField("number").ref(), deltaOnSuper));

            if (delta)
                Packet.addZeroField(capCombName, bv_delta_fields, deltaFields, this, inner);

            final Typed<AValue> zeroVal = delta ? inner.getAddress().<AValue>call("zero") : null;

            inner.addMethod(createBasicConstructor(implFieldsAll, sharedFields));

            addConstructorFromJavaTypes(implFields, capCombName, capabilityCombinations.get(capCombName), addExceptionLocation, deltaFields, bv_delta_fields, enableDeltaBoolFolding, inner.getAddress());
            addConstructorFromDataInput(name, implFields, capCombName, capabilityCombinations.get(capCombName), addExceptionLocation, deltaFields, enableDeltaBoolFolding, inner.getAddress(), zeroVal);
        }
    }

    private static LinkedList<Var<AValue>> extractSharedFields(FieldType bv_delta_fields, List<Field> packetFields, Var<AValue> headerVar, boolean delta) {
        LinkedList<Var<AValue>> sharedFields = new LinkedList<Var<AValue>>();
        sharedFields.add(headerVar);

        if (delta)
            sharedFields.add(Var.field(Collections.<Annotate>emptyList(),
                    Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO, bv_delta_fields.getAddress(), "delta", null));

        for (Field field : packetFields)
            if (!(field.isAnnotatedUsing(CapAdd.class) || field.isAnnotatedUsing(CapRemove.class)))
                sharedFields.add(field);

        return sharedFields;
    }

    private static HashSet<Requirement> extractRequirements(FieldType bv_delta_fields, List<Field> fields, boolean delta) {
        final HashSet<Requirement> requirements = new HashSet<Requirement>();

        for (Field field : fields)
            requirements.addAll(field.getReqs());

        if (delta)
            requirements.add(bv_delta_fields.getIFulfillReq());

        return requirements;
    }

    private int resetDeltaFields(List<Field> fields) {
        int deltaFields = 0;
        if (delta) {
            for (Field field : fields) {
                if (!field.isAnnotatedUsing(keyFlagg)) {
                    field.resetDelta();
                    field.setDelta(deltaFields);
                    deltaFields++;
                }
            }
        }
        return deltaFields;
    }

    private static void addZeroField(String capCombName, FieldType bv_delta_fields, int deltaFields, ClassWriter outer, ClassWriter inner) {
        // TODO: This registers "fromHeaderAndStream" + capCombName before its created since it uses zero.
        // TODO: Consider if a more elegant solution can be found.
        outer.getAddress().register(new TargetMethod(outer.getAddress(),
                "fromHeaderAndStream" + capCombName, outer.getAddress(),
                TargetMethod.Called.STATIC));

        inner.addClassConstant(Visibility.PUBLIC, inner.getAddress(), "zero", outer.getAddress().callV(
                "fromHeaderAndStream" + capCombName,
                TargetClass.from(DataInputStream.class).newInstance(
                        TargetClass.from(EndsInEternalZero.class).newInstance(
                                TargetClass.from(EndsInEternalZero.class).callV("allOneBytes",
                                        BuiltIn.sum(BuiltIn.divide(BuiltIn.subtract(literal(deltaFields), literal(1)),
                                                literal(8)), literal(1))))),
                TargetClass.from(org.freeciv.packet.Header_NA.class).newInstance(outer.getField("number").ref()),
                TargetClass.from(java.util.HashMap.class).addGenericTypeArguments(Arrays.asList(TargetClass.from(org.freeciv.packet.DeltaKey.class), TargetClass.from(org.freeciv.packet.Packet.class))).newInstance()));
    }

    private static LinkedList<Field> filterForCapabilities(List<Field> fieldList, Collection<Typed<AString>> usingCaps) {
        LinkedList<Field> packetFieldList = new LinkedList<Field>();
        for (Field candidate : fieldList)
            if (belongHere(candidate, usingCaps))
                packetFieldList.add(candidate);
        return packetFieldList;
    }

    private static boolean belongHere(Field candidate, Collection<Typed<AString>> usingCaps) {
        if (candidate.isAnnotatedUsing(CapAdd.class))
            return hasCap(candidate, usingCaps, CapAdd.class);
        if (candidate.isAnnotatedUsing(CapRemove.class))
            return !hasCap(candidate, usingCaps, CapRemove.class);
        return true;
    }

    private static boolean hasCap(Field field, Collection<Typed<AString>> usingCaps, Class<?> addOrRemove) {
        return usingCaps.contains(field.getAnnotation(addOrRemove).getValueOf("value"));
    }

    private static LinkedHashMap<String, List<Typed<AString>>> allPossibleCombinations(Set<String> caps) {
        ArrayList<String> capsList = new ArrayList<String>();
        ArrayList<Typed<AString>> capsListTyped = new ArrayList<Typed<AString>>();
        for (String cap : caps) {
            capsList.add(cap);
            capsListTyped.add(BuiltIn.literal(cap));
        }

        final LinkedHashMap<String, List<Typed<AString>>> allPossbile = new LinkedHashMap<String, List<Typed<AString>>>();
        final double combinations = Math.pow(2, caps.size());
        for (int combination = 0; combination < combinations; combination++) {
            StringBuilder capsName = new StringBuilder();
            List<Typed<AString>> capsSet = new ArrayList<Typed<AString>>();
            for (int i = 0; i < capsList.size(); i++)
                if ((combination & (1 << i)) != 0) {
                    capsName.append("_").append(capsList.get(i));
                    capsSet.add(capsListTyped.get(i));
                }
            allPossbile.put(capsName.toString(), capsSet);
        }
        return allPossbile;
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

    private Typed<NoValue> labelExceptionsWithPacketAndField(Var field, Block operation, TargetMethod addExceptionLocation) {
        Var<AValue> e = Var.param(Throwable.class, "e");
        return BuiltIn.tryCatch(
                operation,
                e,
                new Block(THROW(addExceptionLocation.<AValue>call(e.ref(), literal(field.getName())))));
    }

    private Typed<NoValue> generateHeader(Value<? extends AValue> headerKind, TargetMethod addExceptionLocation, List<? extends Typed<? extends AValue>> fields, Var headerDst, TargetClass inner) {
        return labelExceptionsWithPacketAndField(headerDst, new Block(headerDst.assign(BuiltIn.cast(
                PacketHeader.class,
                headerKind.callV("newInstance",
                        sum(inner.callV("calcBodyLen", fields.toArray(new Typed[fields.size()])),
                                headerKind.callV("getDeclaringClass").callV("getField", literal("HEADER_SIZE"))
                                        .callV("getInt", NULL)),
                        getField("number").ref())))), addExceptionLocation);
    }

    private void addDeltaField(TargetMethod addExceptionLocation, int deltaFields, Block body, FieldType bv_delta_fields, Var deltaVar) {
        body.addStatement(labelExceptionsWithPacketAndField(
                deltaVar,
                new Block(deltaVar.assign(bv_delta_fields.getAddress().newInstance(
                        bv_delta_fields.getUnderType()
                                .newInstance(TRUE, literal(deltaFields)),
                        TargetClass.from(ElementsLimit.class).callV("limit", literal(deltaFields))))),
                addExceptionLocation));
    }

    private void addConstructorFromJavaTypes(List<Field> fields, String capsName, List<Typed<AString>> caps, TargetMethod addExceptionLocation, int deltaFields, FieldType bv_delta_fields, boolean enableDeltaBoolFolding, TargetClass impl) throws UndefinedException {
        LinkedList<Var<? extends AValue>> params = new LinkedList<Var<? extends AValue>>();
        LinkedList<Reference<? extends AValue>> localVars = new LinkedList<Reference<? extends AValue>>();
        LinkedList<Reference<? extends AValue>> sizeArgs = new LinkedList<Reference<? extends AValue>>();
        Block body = new Block();

        for (Field field : fields) {
            Var<AValue> asParam = Var.param(field.getUnderType(), field.getName());
            params.add(asParam);
            body.addStatement(validation.call("validateNotNull", asParam.ref(), literal(asParam.getName())));

            Var<AValue> asLocal = field.getTmpLocalVar(null);
            localVars.add(asLocal.ref());
            if (!isBoolFolded(enableDeltaBoolFolding, field))
                sizeArgs.add(asLocal.ref());
            body.addStatement(asLocal);

            Block readAndValidate = new Block();
            field.validateLimitInsideInt(readAndValidate);
            readAndValidate.addStatement(asLocal.assign(field.getTType().newInstance(asParam.ref(), field.getSuperLimit(0, true))));
            final Typed<NoValue> readLabeled = labelExceptionsWithPacketAndField(field, readAndValidate, addExceptionLocation);
            body.addStatement(readLabeled);
        }

        if (delta) {
            final Var<? extends AValue> delta_tmp;
            delta_tmp = Var.param(getField("delta").getTType(), "delta" + "_tmp");
            body.addStatement(delta_tmp);
            addDeltaField(addExceptionLocation, deltaFields, body, bv_delta_fields, delta_tmp);
            localVars.addFirst(delta_tmp.ref());
            sizeArgs.addFirst(delta_tmp.ref());
        }

        final Var<AValue> headerKind = Var.param(TargetClass.from(Constructor.class), "headerKind");
        params.add(headerKind);
        body.addStatement(validation.call("validateNotNull", headerKind.ref(), literal(headerKind.getName())));

        final Var<? extends AValue> header_tmp = Var.param(PacketHeader.class, "header" + "_tmp");
        body.addStatement(header_tmp);
        body.addStatement(generateHeader(headerKind.ref(), addExceptionLocation, sizeArgs, header_tmp, impl));
        localVars.addFirst(header_tmp.ref());

        body.addStatement(BuiltIn.RETURN(impl.newInstance(localVars.toArray(new Reference[localVars.size()]))));

        Method result = Method.custom(Comment.no(), Visibility.PUBLIC, Scope.CLASS, impl, "fromValues" + capsName, params, Collections.<TargetClass>emptyList(), body);
        result.annotateMe(new FactoryCapabilityCombination(caps));
        addMethod(result);
    }

    private static Method createBasicConstructor(List<? extends Var<AValue>> fields, List<Var<AValue>> shared) throws UndefinedException {
        Block body = new Block();

        LinkedList<Typed<AValue>> superArgs = new LinkedList<Typed<AValue>>();
        LinkedList<Statement> setLocals = new LinkedList<Statement>();
        LinkedList<Var<AValue>> params = new LinkedList<Var<AValue>>();
        for (Var field : fields) {
            final Var<AValue> asParam = Var.param(
                    field.getTType(),
                    field.getName());

            params.add(asParam);

            if (shared.contains(field))
                superArgs.add(asParam.ref());
            else
                setLocals.add(new Statement(field.ref().assign(asParam.ref())));
        }

        body.addStatement(BuiltIn.superConstr(superArgs.toArray(new Typed[superArgs.size()])));
        for (Statement fieldSet : setLocals)
            body.addStatement(fieldSet);

        return Method.newConstructor(Comment.no(), Visibility.PRIVATE, params, Collections.<TargetClass>emptyList(), body);
    }

    private void addConstructorFromDataInput(String name, List<Field> fields, String capsName, List<Typed<AString>> caps, TargetMethod addExceptionLocation, int deltaFields, boolean enableDeltaBoolFolding, TargetClass impl, Typed<? extends AValue> zero) throws UndefinedException {
        Var<AnObject> argHeader = Var.param(TargetClass.from(PacketHeader.class), "header");
        final Var<AnObject> streamName = Var.param(TargetClass.from(DataInput.class), "from");
        final Var<AnObject> old =
                Var.param(TargetClass.from(java.util.Map.class).addGenericTypeArguments(Arrays.asList(TargetClass.from(org.freeciv.packet.DeltaKey.class), TargetClass.from(org.freeciv.packet.Packet.class))), "old");

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
            chosenOld = Var.local(impl, "chosenOld", R_IF(
                    isSame(NULL, old.ref().callV("get", impl.callV("getKeyPrivate", keyArgs.toArray(new Typed[keyArgs.size()])))),
                    zero,
                    cast(impl, old.ref().callV("get", impl.callV("getKeyPrivate", keyArgs.toArray(new Typed[keyArgs.size()]))))));
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
            if (isBoolFolded(enableDeltaBoolFolding, field)) {
                body.addStatement(asLocal.assign(field.getTType().newInstance(deltaHas(field, delta_tmp.ref()), Hardcoded.noLimit)));
            } else {
                final Typed<? extends AValue> fromOld;
                final Reference deltaTmpRef;
                if (delta) {
                    fromOld = asLocal.assign(chosenOld.ref().callV(field.getName()));
                    deltaTmpRef = delta_tmp.ref();
                } else {
                    fromOld = literal("Never run");
                    deltaTmpRef = null;
                }
                body.addStatement(ifDeltaElse(field, readLabeled, fromOld, delta, deltaTmpRef));
            }
        }

        body.groupBoundary();

        if (delta)
            constructorParams.addFirst(delta_tmp.ref());
        constructorParams.addFirst(argHeader.ref());

        Var me = Var.local(impl, "me", impl.newInstance(constructorParams.toArray(new Typed[constructorParams.size()])));
        body.addStatement(me);

        body.groupBoundary();

        Typed<AnInt> calcBodyLenCall = impl.<AnInt>callV("calcBodyLen", deltaAndFields.toArray(new Typed[deltaAndFields.size()]));

        body.addStatement(IF(isNotSame(getField("number").ref(), argHeader.ref().<AnInt>call("getPacketKind")),
                new Block(THROW(addExceptionLocation.callV(
                        TargetClass.from(FieldTypeException.class).newInstance(sum(
                                literal("Wrong packet number. "),
                                literal("Packet is " + name + " (" + number + ") but header is for packet number "),
                                argHeader.ref().<AnInt>call("getPacketKind"))),
                        literal("header"))))));

        body.addStatement(IF(
                argHeader.ref().<ABool>call("isWrongBodySize", calcBodyLenCall),
                new Block(THROW(addExceptionLocation.callV(
                        TargetClass.from(FieldTypeException.class).newInstance(sum(
                                literal("interpreted packet body size ("),
                                calcBodyLenCall,
                                literal(") don't match header packet body size ("),
                                argHeader.ref().<AnInt>call("getBodySize"),
                                literal(") for "), impl.callV("toString", me.ref()))),
                        literal("header"))))));

        body.groupBoundary();

        if (delta)
            body.addStatement(old.ref().callV("put",
                    impl.callV("getKey", me.ref()),
                    me.ref()));

        body.addStatement(RETURN(me.ref()));

        Method result = Method.custom(
                Comment.doc(
                        "Construct an object from a DataInput", new String(),
                        Comment.param(streamName, "data stream that is at the start of the package body"),
                        Comment.param(argHeader, "header data. Must contain size and number"),
                        Comment.param(old, "where the Delta protocol should look for older packets"),
                        Comment.docThrows(TargetClass.from(FieldTypeException.class), "if there is a problem")),
                Visibility.PUBLIC,
                Scope.CLASS,
                impl,
                "fromHeaderAndStream" + capsName,
                Arrays.asList(streamName, argHeader, old),
                Arrays.asList(TargetClass.from(FieldTypeException.class)),
                body);
        result.annotateMe(new FactoryCapabilityCombination(caps));
        addMethod(result);
    }

    private static boolean isBoolFolded(boolean boolFoldEnabled, Field field) {
        return boolFoldEnabled && field.isDelta() && "Boolean".equals(field.getUnderType().getSimpleName());
    }

    private static Typed<?> ifDeltaElse(Field field, Typed<?> defaultAction, Typed<?> deltaDisabledAction, boolean delta, Value<AValue> deltaVal) {
        return deltaApplies(field, delta) ?
                (null == deltaDisabledAction ?
                        IF(deltaHas(field, deltaVal),
                                new Block(defaultAction)) :
                        IF(deltaHas(field, deltaVal),
                                new Block(defaultAction),
                                new Block(deltaDisabledAction))) :
                defaultAction;
    }

    private static Value<ABool> deltaHas(Field field, Value<AValue> deltaVal) {
        return deltaVal.callV("getValue").<ABool>callV("get", literal(field.getDeltaFieldNumber()));
    }

    private static boolean deltaApplies(Field field, boolean delta) {
        return (delta && field.isDelta());
    }

    private static Method createEncoder(List<Field> fields, boolean enableDeltaBoolFolding, boolean delta, Value<AValue> headerVal, Value<AValue> deltaVal) {
        Var<AnObject> pTo = Var.<AnObject>param(TargetClass.from(DataOutput.class), "to");
        Block body = new Block();
        body.addStatement(headerVal.<NoValue>call("encodeTo", pTo.ref()));
        if (0 < fields.size()) {
            if (delta)
                body.addStatement(deltaVal.call("encodeTo", pTo.ref()));
            for (Field field : fields)
                if (!isBoolFolded(enableDeltaBoolFolding, field))
                    body.addStatement(ifDeltaElse(field, field.ref().<Returnable>call("encodeTo", pTo.ref()), null, delta, deltaVal));
        }
        return Method.newPublicDynamicMethod(Comment.no(),
                TargetClass.from(void.class), "encodeTo", Arrays.asList(pTo),
                Arrays.asList(TargetClass.from(IOException.class)), body);
    }

    private static Method createdCalcBodyLen(List<Field> fields, boolean enableDeltaBoolFolding, Var deltaVar, boolean delta) {
        Block body = new Block();
        LinkedList<Var<? extends AValue>> params = new LinkedList<Var<? extends AValue>>();

        Typed<? extends AValue> summing = literal(0);

        Var<? extends AValue> deltaParam = null;
        if (delta) {
            deltaParam = Var.param(deltaVar.getTType(), deltaVar.getName());
            params.add(deltaParam);
            summing = sum(summing, deltaParam.ref().callV("encodedLength"));
        }

        for (Field field : fields)
            if (!isBoolFolded(enableDeltaBoolFolding, field)) {
                Var<AValue> asParam = Var.param(field.getTType(), field.getName());
                params.add(asParam);
                summing = sum(summing, calcBodyLen(field, asParam, deltaParam, delta));
            }

        body.addStatement(RETURN(summing));

        return Method.custom(Comment.no(),
                Visibility.PRIVATE, Scope.CLASS,
                TargetClass.from(int.class), "calcBodyLen", params,
                Collections.<TargetClass>emptyList(),
                body);
    }

    private static Typed<? extends AValue> calcBodyLen(Field field, Var<AValue> asParam, Var deltaVar, boolean delta) {
        if (deltaApplies(field, delta))
            return R_IF(deltaHas(field, deltaVar.ref()), asParam.ref().<AnInt>call("encodedLength"), literal(0));
        else
            return asParam.ref().<AnInt>call("encodedLength");
    }

    private static Method createToString(String name, List<Field> fields, boolean delta, Reference numberRef, Value<AValue> deltaOnSuper) {
        Var buildOutput = Var.local(String.class, "out",
                sum(literal(name), literal("("), numberRef, literal(")")));
        Block body = new Block(buildOutput);
        if (delta)
            body.addStatement(BuiltIn.inc(buildOutput, sum(literal(" delta header = "),
                    deltaOnSuper.callV("toString"))));
        for (Field field : fields)
            body.addStatement(BuiltIn.inc(buildOutput, sum(
                    literal("\\n\\t" + field.getName() + " = "),
                    field.ref())));
        body.addStatement(RETURN(buildOutput.ref()));
        return Method.newPublicReadObjectState(Comment.no(), TargetClass.from(String.class), "toString", body);
    }

    private Method createGetDeltaKeyPublic(List<Field> fields, TargetClass address) {
        List<Typed<? extends AValue>> fromDynamicArgs = new LinkedList<Typed<? extends AValue>>();

        for (Field<?> field : getKeyFields(fields)) {
            fromDynamicArgs.add(field.ref());
        }

        return Method.newPublicReadObjectState(
                Comment.doc("Get a delta key for this packet",
                        "A DeltaKey used in a HashMap makes it easy to find the previous packet of " +
                                "the same packet kind where all key fields are the same.",
                        Comment.docReturns("a delta key matching the packet.")),
                TargetClass.from(DeltaKey.class), "getKey",
                new Block(RETURN(address.callV("getKeyPrivate",
                        fromDynamicArgs.toArray(new Typed[fromDynamicArgs.size()])))));
    }

    private static Method createGetDeltaKeyPrivate(int number, List<Field> fields) {
        List<Var<? extends AValue>> params = new LinkedList<Var<? extends AValue>>();
        List<Typed<? extends AValue>> deltaKeyArgs = new LinkedList<Typed<? extends AValue>>();

        deltaKeyArgs.add(literal(number));

        for (Field<?> field : getKeyFields(fields)) {
            final Var<AValue> asParam = Var.param(field.getTType(), field.getName());
            params.add(asParam);
            deltaKeyArgs.add(asParam.ref());
        }

        return Method.custom(
                Comment.doc("Get a delta key for this packet",
                        "A DeltaKey used in a HashMap makes it easy to find the previous packet of " +
                                "the same packet kind where all key fields are the same.",
                        Comment.docReturns("a delta key matching the packet.")),
                Visibility.PRIVATE, Scope.CLASS, TargetClass.from(DeltaKey.class), "getKeyPrivate",
                params,
                Collections.<TargetClass>emptyList(),
                new Block(RETURN(TargetClass.from(DeltaKey.class)
                        .newInstance(deltaKeyArgs.toArray(new Typed[deltaKeyArgs.size()])))));
    }

    private static List<Field<?>> getKeyFields(List<Field> fields) {
        List<Field<?>> out = new LinkedList<Field<?>>();
        for (Field field : fields)
            if (field.isAnnotatedUsing(keyFlagg))
                out.add(field);
        return out;
    }

    private static Method createJavaGetter(Field field) throws UndefinedException {
        Block body;

        body = new Block(RETURN(field.ref().<Returnable>call("getValue")));

        return Method.newPublicReadObjectState(Comment.no(),
                field.getUnderType(),
                getterNameJavaish(field) + "Value",
                body);
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
