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
import com.kvilhaugsvik.javaGenerator.util.AddressScopeHelper;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AValue;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.Returnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class TargetClass extends Address<TargetPackage> implements AValue {
    public static final TargetClass SELF_TYPED = new TargetClass();
    public static final TargetClass TYPE_NOT_KNOWN = new TargetClass();

    private final boolean isInScope;
    private final Common shared;

    public TargetClass(String inPackage, String className, boolean isInScope) {
        super(TargetPackage.from(inPackage), addressString2Components(className));
        final CodeAtom name = super.components[super.components.length - 1];
        final HashMap<String, TargetMethod> methods = new HashMap<String, TargetMethod>();
        this.isInScope = isInScope;

        this.shared = new Common(name, where, methods, this);

        registerBuiltIn();
    }

    protected TargetClass(Class wrapped, boolean isInScope) {
        this(TargetPackage.from(wrapped.getPackage()), new CodeAtom(wrapped.getSimpleName()), isInScope);

        setRepresents(wrapped);
    }

    private static void convertMethods(TargetClass target, Class wrapped) {
        for (Method has : wrapped.getMethods())
            target.register(new TargetMethod(has));

        for (Field has : wrapped.getFields())
            target.register(new TargetMethod(has));

        if (null != wrapped.getSuperclass())
            if (null == target.shared.parent)
                target.setParent(TargetClass.from(wrapped.getSuperclass()));
            else if (null == target.shared.parent.shared.represents)
                target.shared.parent.setRepresents(wrapped.getSuperclass());

        target.shared.shallow = false;
    }

    public TargetClass(TargetPackage where, CodeAtom name, boolean isInScope) {
        this(where, name, isInScope, new HashMap<String, TargetMethod>());
    }

    private TargetClass(TargetPackage where, CodeAtom name, boolean isInScope, HashMap<String, TargetMethod> methods) {
        super(where, name);
        this.isInScope = isInScope;
        this.shared = new Common(name, where, methods, this);

        registerBuiltIn();
    }

    private TargetClass(Common common, boolean isInScope) {
        super(common.where, common.name);
        this.isInScope = isInScope;
        this.shared = common;
    }

    private TargetClass() {
        super(TargetPackage.TOP_LEVEL);
        this.isInScope = true;
        this.shared = new Common(HasAtoms.SELF, TargetPackage.TOP_LEVEL, new HashMap<String, TargetMethod>(), this);

        registerBuiltIn();
    }

    private void registerBuiltIn() {
        register(new TargetMethod(this, "class", TargetClass.from(Class.class), TargetMethod.Called.STATIC_FIELD));
    }

    public TargetPackage getPackage() {
        return shared.where;
    }

    public CodeAtom getCName() {
        return shared.name;
    }

    public String getName() {
        return shared.name.get();
    }

    protected Class<?> getRepresents() {
        return this.shared.represents;
    }

    protected void setRepresents(Class<?> rep) {
        /*if (null != this.shared.represents) // TODO: track down double writes and uncomment
            throw new IllegalStateException("Class already set");*/

        this.shared.represents = rep;
    }

    public TargetClass scopeKnown() {
        return shared.variants.scopeKnown();
    }

    public TargetClass scopeUnknown() {
        return shared.variants.scopeUnknown();
    }

    public <Kind extends AValue> Value<Kind> read(final String field) {
        return callV(field);
    }

    public void register(TargetMethod has) {
        // TODO: Fix underlying issue. For now work around by sparing dynamic non void methods from over writing
        if (shared.methods.containsKey(has.getName())
                && shared.methods.get(has.getName()).isDynamic()
                && shared.methods.get(has.getName()).returnsAValue())
            return;

        shared.methods.put(has.getName(), has);
    }

    public void setParent(TargetClass parent) {
        if (!(null == shared.parent || shared.parent.equals(parent)))
            throw new IllegalStateException("Parent already set");
        shared.parent = parent;
    }

    public <Ret extends Returnable> Typed<Ret> call(String method, Typed<? extends AValue>... parameters) {
        methodExists(method);
        if (shared.methods.containsKey(method))
            return shared.methods.get(method).<Ret>call(parameters);
        else
            return shared.parent.<Ret>call(method, parameters);
    }

    private void methodExists(String method) {
        if (!hasMethod(method))
            throw new IllegalArgumentException("No method named " + method + " on " + shared.name.get());
    }

    private void initIfPossibleAndNotDone() {
        if (shared.shallow && null != shared.represents)
            convertMethods(this, shared.represents);
    }

    private boolean hasMethod(String method) {
        initIfPossibleAndNotDone();
        return shared.methods.containsKey(method) || (null != shared.parent && shared.parent.hasMethod(method));
    }

    public <Ret extends AValue> Value<Ret> callV(String method, Typed<? extends AValue>... parameters) {
        methodExists(method); // exception if method don't exist here or on parent
        if (shared.methods.containsKey(method)) // method exists here
            return shared.methods.get(method).<Ret>callV(parameters);
        else // method exists at parent
            return shared.parent.<Ret>callV(method, parameters);
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
            shared.name.writeAtoms(to);
        else
            super.writeAtoms(to);
        if (!SELF_TYPED.equals(this))
            to.hintEnd(TargetClass.class.getName());
    }

    private static class Common {
        final AddressScopeHelper<TargetClass> variants;

        final CodeAtom name;
        final TargetPackage where;
        final HashMap<String, TargetMethod> methods;


        TargetClass parent;
        boolean shallow = true;
        Class represents = null;

        private Common(CodeAtom name, TargetPackage where, HashMap<String, TargetMethod> methods, TargetClass existing) {
            this.name = name;
            this.where = where;
            this.methods = methods;

            TargetClass inScope = existing.isInScope ? existing : new TargetClass(this, true);
            TargetClass notInScope = existing.isInScope ? new TargetClass(this, false) : existing;
            this.variants = new AddressScopeHelper<TargetClass>(inScope, notInScope);
        }
    }

    private static TargetClass getExisting(String name, boolean inScope) {
        final TargetClass found = getExisting(name, TargetClass.class);

        return inScope ? found.scopeKnown() : found.scopeUnknown();
    }

    public static TargetClass from(String inPackage, String className) {
        boolean inScope = "java.lang".equals(inPackage);
        try {
            return getExisting((TargetPackage.TOP_LEVEL_AS_STRING.equals(inPackage) ? "" : inPackage + ".") + className,
                    inScope);
        } catch (NoSuchElementException e) {
            return new TargetClass(inPackage, className, inScope);
        }
    }

    public static TargetClass from(Class cl) {
        String name = cl.getCanonicalName();
        boolean inScope = Package.getPackage("java.lang").equals(cl.getPackage());

        try {
            TargetClass targetClass = getExisting(name, inScope);

            if (null == targetClass.shared.represents)
                targetClass.setRepresents(cl);

            return targetClass;
        } catch (NoSuchElementException e) {
            return new TargetClass(cl, inScope);
        }
    }

    public static TargetClass newKnown(Class cl) {
        return from(cl).scopeKnown();
    }

    public static TargetClass newKnown(String inPackage, String className) {
        return from(inPackage, className).scopeKnown();
    }
}
