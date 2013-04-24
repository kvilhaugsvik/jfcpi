/*
 * Copyright (c) 2012, Sveinung Kvilhaugsvik
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

package com.kvilhaugsvik.javaGenerator;

import com.kvilhaugsvik.javaGenerator.expression.MethodCall;
import com.kvilhaugsvik.javaGenerator.representation.CodeAtoms;
import com.kvilhaugsvik.javaGenerator.representation.HasAtoms;
import com.kvilhaugsvik.javaGenerator.representation.IR.CodeAtom;
import com.kvilhaugsvik.javaGenerator.typeBridge.Value;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AValue;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.Returnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class TargetClass extends Address<TargetPackage> implements AValue {
    public static final TargetClass SELF_TYPED = new TargetClass();
    public static final TargetClass TYPE_NOT_KNOWN = new TargetClass();

    private final CodeAtom name;
    private final HashMap<String, TargetMethod> methods;

    TargetClass parent = null;
    boolean shallow = true;
    Class represents = null;

    public TargetClass(String inPackage, String className) {
        super(TargetPackage.from(inPackage), addressString2Components(className));
        final CodeAtom name = super.components[super.components.length - 1];
        final HashMap<String, TargetMethod> methods = new HashMap<String, TargetMethod>();

        this.name = name;
        this.methods = methods;

        registerBuiltIn();
    }

    protected TargetClass(Class wrapped) {
        this(TargetPackage.from(wrapped.getPackage()), new CodeAtom(wrapped.getSimpleName()));

        setRepresents(wrapped);
    }

    private static void convertMethods(TargetClass target, Class wrapped) {
        for (Method has : wrapped.getMethods())
            target.register(new TargetMethod(has));

        for (Field has : wrapped.getFields())
            target.register(new TargetMethod(has));

        if (null != wrapped.getSuperclass())
            if (null == target.parent)
                target.setParent(TargetClass.from(wrapped.getSuperclass()));
            else if (null == target.parent.represents)
                target.parent.setRepresents(wrapped.getSuperclass());

        target.shallow = false;
    }

    public TargetClass(TargetPackage where, CodeAtom name) {
        this(where, name, new HashMap<String, TargetMethod>());
    }

    private TargetClass(TargetPackage where, CodeAtom name, HashMap<String, TargetMethod> methods) {
        super(where, name);
        this.name = name;
        this.methods = methods;

        registerBuiltIn();
    }

    private TargetClass() {
        super(TargetPackage.TOP_LEVEL);
        this.name = HasAtoms.SELF;
        this.methods = new HashMap<String, TargetMethod>();

        registerBuiltIn();
    }

    private void registerBuiltIn() {
        register(new TargetMethod(this, "class", TargetClass.from(Class.class), TargetMethod.Called.STATIC_FIELD));
    }

    public TargetPackage getPackage() {
        return where;
    }

    public CodeAtom getCName() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    protected Class<?> getRepresents() {
        return this.represents;
    }

    protected void setRepresents(Class<?> rep) {
        /*if (null != this.represents) // TODO: track down double writes and uncomment
            throw new IllegalStateException("Class already set");*/

        this.represents = rep;
    }

    public <Kind extends AValue> Value<Kind> read(final String field) {
        return callV(field);
    }

    public void register(TargetMethod has) {
        // TODO: Fix underlying issue. For now work around by sparing dynamic non void methods from over writing
        if (methods.containsKey(has.getName())
                && methods.get(has.getName()).isDynamic()
                && methods.get(has.getName()).returnsAValue())
            return;

        methods.put(has.getName(), has);
    }

    public void setParent(TargetClass parent) {
        if (!(null == this.parent || this.parent.equals(parent)))
            throw new IllegalStateException("Parent already set");
        this.parent = parent;
    }

    public <Ret extends Returnable> Typed<Ret> call(String method, Typed<? extends AValue>... parameters) {
        methodExists(method);
        if (methods.containsKey(method))
            return methods.get(method).<Ret>call(parameters);
        else
            return parent.<Ret>call(method, parameters);
    }

    private void methodExists(String method) {
        if (!hasMethod(method))
            throw new IllegalArgumentException("No method named " + method + " on " + name.get());
    }

    private void initIfPossibleAndNotDone() {
        if (shallow && null != represents)
            convertMethods(this, represents);
    }

    private boolean hasMethod(String method) {
        initIfPossibleAndNotDone();
        return methods.containsKey(method) || (null != parent && parent.hasMethod(method));
    }

    public <Ret extends AValue> Value<Ret> callV(String method, Typed<? extends AValue>... parameters) {
        methodExists(method); // exception if method don't exist here or on parent
        if (methods.containsKey(method)) // method exists here
            return methods.get(method).<Ret>callV(parameters);
        else // method exists at parent
            return parent.<Ret>callV(method, parameters);
    }

    public Value<AValue> newInstance(Typed<? extends AValue>... parameterList) {
        return new MethodCall.HasResult<AValue>(TargetMethod.Called.STATIC, this, getNewMethod(this), parameterList);
    }

    protected static HasAtoms getNewMethod(final TargetClass type) {
        return new HasAtoms() {
            @Override public void writeAtoms(CodeAtoms to) {
                to.add(newInst);
                type.writeAtoms(to);
            }
        };
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        if (!SELF_TYPED.equals(this))
            to.hintStart(TargetClass.class.getName());
        if (SELF_TYPED.equals(this))
            name.writeAtoms(to);
        else
            super.writeAtoms(to);
        if (!SELF_TYPED.equals(this))
            to.hintEnd(TargetClass.class.getName());
    }

    private static TargetClass getExisting(String name) {
        return getExisting(name, TargetClass.class);
    }

    public static TargetClass from(String inPackage, String className) {
        try {
            return getExisting((TargetPackage.TOP_LEVEL_AS_STRING.equals(inPackage) ? "" : inPackage + ".") + className);
        } catch (NoSuchElementException e) {
            return new TargetClass(inPackage, className);
        }
    }

    public static TargetClass from(Class cl) {
        String name = cl.getCanonicalName();

        try {
            TargetClass targetClass = getExisting(name);

            if (null == targetClass.represents)
                targetClass.setRepresents(cl);

            return targetClass;
        } catch (NoSuchElementException e) {
            return new TargetClass(cl);
        }
    }
}
