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

package org.freeciv.packetgen.javaGenerator;

import org.freeciv.packetgen.javaGenerator.IR.CodeAtom;
import org.freeciv.packetgen.javaGenerator.expression.Value;
import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.util.Formatted;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.Returnable;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

public class TargetClass extends Address implements AValue {
    public static final TargetClass SELF_TYPED = new TargetClass();

    private final boolean isInScope;
    private final Common shared;

    public TargetClass(String fullPath, boolean isInScope) {
        super(fullPath);
        final CodeAtom name = super.components[super.components.length - 1];
        final HashMap<String, TargetMethod> methods = new HashMap<String, TargetMethod>();
        this.isInScope = isInScope;

        final TargetPackage where;
        if (fullPath.endsWith("...") && 4 < super.components.length) // Chewing gum and baling wire
            where = TargetPackage.from(fullPath.substring(0, fullPath.substring(0, fullPath.length() - 3).lastIndexOf(".")));
        else if (!fullPath.endsWith("...") && 1 < super.components.length)
            where = TargetPackage.from(fullPath.substring(0, fullPath.lastIndexOf(".")));
        else
            where = TargetPackage.TOP_LEVEL;

        this.shared = new Common(name, where, methods, this);
    }

    protected TargetClass(Class wrapped, boolean isInScope) {
        this(TargetPackage.from(wrapped.getPackage()), new CodeAtom(wrapped.getSimpleName()), isInScope);

        shared.represents = wrapped;
    }

    private static void convertMethods(TargetClass target, Class wrapped) {
        for (Method has : wrapped.getMethods())
            target.shared.methods.put(has.getName(), new TargetMethod(has));

        if (null != wrapped.getSuperclass())
            if (null == target.shared.parent)
                target.setParent(TargetClass.fromClass(wrapped.getSuperclass()));
            else if (null == target.shared.parent.shared.represents)
                target.shared.parent.shared.represents = wrapped.getSuperclass();

        target.shared.shallow = false;
    }

    public TargetClass(TargetPackage where, CodeAtom name, boolean isInScope) {
        this(where, name, isInScope, new HashMap<String, TargetMethod>());
    }

    private TargetClass(TargetPackage where, CodeAtom name, boolean isInScope, HashMap<String, TargetMethod> methods) {
        super(where, name);
        this.isInScope = isInScope;
        this.shared = new Common(name, where, methods, this);
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

    public TargetClass scopeKnown() {
        return shared.variants.scopeKnown();
    }

    public TargetClass scopeUnknown() {
        return shared.variants.scopeUnknown();
    }

    public <Kind extends AValue> Typed<Kind> read(final String field) {
        final TargetClass parent = this;
        return new Typed<Kind>() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                if (isInScope)
                    to.add(shared.name);
                else
                    parent.writeAtoms(to);
                to.add(HAS);
                to.add(new CodeAtom(field));
            }
        };
    }

    public void register(TargetMethod has) {
        shared.methods.put(has.getName(), has);
    }

    public void setParent(TargetClass parent) {
        if (!(null == shared.parent || shared.parent.equals(parent)))
            throw new IllegalStateException("Parent already set");
        shared.parent = parent;
    }

    public <Ret extends Returnable> MethodCall<Ret> call(String method, Typed<? extends AValue>... parameters) {
        methodExists(method);
        if (shared.methods.containsKey(method))
            return shared.methods.get(method).call(parameters);
        else
            return shared.parent.call(method, parameters);
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
            return shared.methods.get(method).callV(parameters);
        else // method exists at parent
            return shared.parent.callV(method, parameters);
    }

    // TODO: Should this be seen as a function called on the type?
    private final static CodeAtom typeClassField = new CodeAtom("class");
    public Typed<AValue> classVal() {
        final TargetClass parent = this;
        return new Formatted.Type<AValue>() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                if (isInScope)
                    to.add(shared.name);
                else
                    parent.writeAtoms(to);
                to.add(HAS);
                to.add(typeClassField);
            }
        };
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
        if (isInScope)
            shared.name.writeAtoms(to);
        else
            super.writeAtoms(to);
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

    private static final Pattern inJavaLang = Pattern.compile("java\\.lang\\.\\w+");
    public static TargetClass fromName(String inPackage, String className) {
        String name = inPackage + "." + className;
        boolean inScope = inJavaLang.matcher(name).matches();
        try {
            return getExisting(name, inScope);
        } catch (NoSuchElementException e) {
            return new TargetClass(name, inScope);
        }
    }

    public static TargetClass fromClass(Class cl) {
        String name = cl.getCanonicalName();
        boolean inScope = Package.getPackage("java.lang").equals(cl.getPackage());

        try {
            TargetClass targetClass = getExisting(name, inScope);

            if (null == targetClass.shared.represents)
                targetClass.shared.represents = cl;

            return targetClass;
        } catch (NoSuchElementException e) {
            return new TargetClass(cl, inScope);
        }
    }

    public static TargetClass newKnown(Class cl) {
        return fromClass(cl).scopeKnown();
    }

    public static TargetClass newKnown(String inPackage, String className) {
        return fromName(inPackage, className).scopeKnown();
    }
}
