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
import java.util.regex.Pattern;

public class TargetClass extends Address implements AValue {
    public static final TargetClass SELF_TYPED = null; // todo: Don't use null to signal the class using the code

    private final boolean isInScope;
    private final Common shared;

    public TargetClass(String fullPath, boolean isInScope) {
        super(fullPath.split("\\."));
        final CodeAtom name = super.components[super.components.length - 1];
        final HashMap<String, TargetMethod> methods = new HashMap<String, TargetMethod>();
        this.isInScope = isInScope;

        final TargetPackage where;
        if (1 < super.components.length)
            where = TargetPackage.from(fullPath.substring(0, fullPath.lastIndexOf(".")));
        else
            where = TargetPackage.TOP_LEVEL;

        this.shared = new Common(name, where, methods, this);

        // While all classes have a toString this isn't true for all types.
        // As all types are assumed to be classes this may cause trouble
        methods.put("toString", new TargetMethod(this, "toString", new TargetClass(String.class), TargetMethod.Called.DYNAMIC));
    }

    private static final Pattern inJavaLang = Pattern.compile("java\\.lang\\.\\w");
    public TargetClass(String fullPath) {
        this(fullPath, inJavaLang.matcher(fullPath).matches());
    }

    public TargetClass(Class wrapped) {
        this(wrapped, Package.getPackage("java.lang").equals(wrapped.getPackage()));
    }

    public TargetClass(Class wrapped, boolean isInScope) {
        this(TargetPackage.from(wrapped.getPackage()), new CodeAtom(wrapped.getSimpleName()), isInScope);

        shared.represents = wrapped;
    }

    private static void registerMethodsOf(TargetClass target, Class wrapped) {
        for (Method has : wrapped.getMethods())
            target.shared.methods.put(has.getName(), new TargetMethod(has));

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

    public <Ret extends Returnable> MethodCall<Ret> call(String method, Typed<? extends AValue>... parameters) {
        methodExists(method);
        return shared.methods.get(method).call(parameters);
    }

    private void methodExists(String method) {
        if (shared.shallow && null != shared.represents)
            registerMethodsOf(this, shared.represents);
        if (!shared.methods.containsKey(method))
            throw new IllegalArgumentException("No method named " + method + " on " + shared.name.get());
    }

    public <Ret extends AValue> Value<Ret> callV(String method, Typed<? extends AValue>... parameters) {
        methodExists(method);
        return shared.methods.get(method).callV(parameters);
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

    public static TargetClass fromName(String name) {
        if (cached.containsKey(name))
            return (TargetClass)(cached.get(name));
        else
            return new TargetClass(name);
    }

    public static TargetClass fromClass(Class cl) {
        String name = cl.getCanonicalName();

        if (cached.containsKey(name)) {
            TargetClass targetClass = (TargetClass) (cached.get(name));

            if (null == targetClass.shared.represents)
                targetClass.shared.represents = cl;

            return targetClass;
        }

        return new TargetClass(cl);
    }
}
