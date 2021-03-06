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
import java.util.*;

public class TargetClass extends Address<TargetPackage> {
    public static final TargetClass SELF_TYPED = new TargetClass(HasAtoms.SELF);
    public static final TargetClass TYPE_NOT_KNOWN = new TargetClass(new CodeAtom(""));

    private final HashMap<String, TargetMethod> methods;
    private final ClassKind kind;

    TargetClass parent = null;
    boolean shallow = true;
    Class represents = null;

    protected TargetClass(String inPackage, String className, ClassKind kind) {
        super(TargetPackage.from(inPackage), addressString2Components(className), Collections.<CodeAtom>emptyList());
        final HashMap<String, TargetMethod> methods = new HashMap<String, TargetMethod>();

        this.methods = methods;
        this.kind = kind;

        registerBuiltIn();
    }

    protected TargetClass(Class wrapped) {
        this(TargetPackage.from(wrapped.getPackage()), getClassNames(wrapped),
                wrapped.isEnum() ? ClassKind.ENUM : ClassKind.CLASS, Collections.<HasAtoms>emptyList(), new HashMap<String, TargetMethod>(), null);

        setRepresents(wrapped);
    }

    private static LinkedList<CodeAtom> getClassNames(Class wrapped) {
        if (null == wrapped.getEnclosingClass()) {
            LinkedList<CodeAtom> parts = new LinkedList<CodeAtom>();
            parts.add(new CodeAtom(wrapped.getSimpleName()));
            return parts;
        } else {
            LinkedList<CodeAtom> parts = getClassNames(wrapped.getEnclosingClass());
            parts.add(new CodeAtom(wrapped.getSimpleName()));
            return parts;
        }
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

    public TargetClass(TargetPackage where, List<? extends CodeAtom> name, ClassKind kind, List<HasAtoms> afterDotPart, HashMap<String, TargetMethod> methods, TargetClass parent) {
        super(where, name, afterDotPart);
        this.kind = kind;

        this.methods = methods;
        this.parent = parent;

        registerBuiltIn();
    }

    private TargetClass(CodeAtom symbol) {
        super(TargetPackage.TOP_LEVEL, Arrays.asList(symbol), Collections.<CodeAtom>emptyList());
        this.methods = new HashMap<String, TargetMethod>();
        this.kind = ClassKind.CLASS;

        registerBuiltIn();
    }

    private static List<HasAtoms> addTypeArgs(List<? extends HasAtoms> afterDotPart, List<TargetClass> typeArgs) {
        List<HasAtoms> out = new LinkedList<HasAtoms>(afterDotPart);
        out.add(HasAtoms.GENERIC_START);
        for (int i = 0; i < typeArgs.size(); i++) {
            if (0 < i)
                out.add(HasAtoms.SEP);
            out.add(typeArgs.get(i));
        }
        out.add(HasAtoms.GENERIC_END);
        return out;
    }

    public TargetClass addGenericTypeArguments(List<TargetClass> typeArgs) {
        return new TargetClass(this.getPackage(), this.components, this.kind, addTypeArgs(this.afterDotPart, typeArgs), methods, this);
    }

    private void registerBuiltIn() {
        register(new TargetMethod(this, "class", TargetClass.from(Class.class), TargetMethod.Called.STATIC_FIELD));

        if (ClassKind.ENUM.equals(this.kind)) {
            register(new TargetMethod(this, "values", TargetArray.from(this, 1), TargetMethod.Called.STATIC));
            register(new TargetMethod(this, "valueOf", this, TargetMethod.Called.STATIC));
        }
    }

    public TargetPackage getPackage() {
        return where;
    }

    public Collection<? extends CodeAtom> getTypedClassName() {
        return components;
    }

    protected Class<?> getRepresents() {
        return this.represents;
    }

    protected void setRepresents(Class<?> rep) {
        /*if (null != this.represents) // TODO: track down double writes and uncomment
            throw new IllegalStateException("Class already set");*/

        this.represents = rep;
    }

    public void register(TargetMethod has) {
        // TODO: Fix underlying issue. For now work around by sparing dynamic non void methods from over writing
        if (methods.containsKey(has.getSimpleName())
                && methods.get(has.getSimpleName()).isDynamic()
                && methods.get(has.getSimpleName()).returnsAValue())
            return;

        methods.put(has.getSimpleName(), has);
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
            throw new IllegalArgumentException("No method named " + method + " on " + getSimpleName());
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
        /* TODO: consider to have a flag set in stead of checking for SELF_TYPED. That would allow
         * incomplete types to use it as well. */
        if (this == TargetClass.SELF_TYPED ) {
            /* This is a special case. Just assume that this is a regular dynamic method. */
            TargetMethod fake = new TargetMethod(TargetClass.SELF_TYPED, method, TargetClass.TYPE_NOT_KNOWN, TargetMethod.Called.DYNAMIC);

            return fake.callV(parameters);
        }

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
        if (SELF_TYPED.equals(this)) {
            super.writeAtoms(to);
        } else {
            to.hintStart(TargetClass.class.getName());
            super.writeAtoms(to);
            to.hintEnd(TargetClass.class.getName());
        }
    }

    private static TargetClass getExisting(String name) {
        return getExisting(name, TargetClass.class);
    }

    public static TargetClass from(String inPackage, String className) {
        final ClassKind kind = ClassKind.CLASS; // TODO: Make a parameter
        try {
            return getExisting((TargetPackage.TOP_LEVEL_AS_STRING.equals(inPackage) ? "" : inPackage + ".") + className);
        } catch (NoSuchElementException e) {
            return new TargetClass(inPackage, className, kind);
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
