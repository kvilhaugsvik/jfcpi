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
import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.util.Formatted;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.Returnable;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.regex.Pattern;

public class TargetClass extends Address implements AValue {
    private final boolean isInScope;
    private final CodeAtom name;
    private final HashMap<String, TargetMethod> methods = new HashMap<String, TargetMethod>();

    public TargetClass(String fullPath, boolean isInScope) {
        super(fullPath.split("\\."));
        name = super.components[super.components.length - 1];
        this.isInScope = isInScope;

        // While all classes have a toString this isn't true for all types.
        // As all types are assumed to be classes this may cause trouble
        methods.put("toString", new TargetMethod("toString"));
    }

    private static final Pattern inJavaLang = Pattern.compile("java\\.lang\\.\\w");
    public TargetClass(String fullPath) {
        this(fullPath, inJavaLang.matcher(fullPath).matches());
    }

    public TargetClass(Class wrapped) {
        this(wrapped, Package.getPackage("java.lang").equals(wrapped.getPackage()));
    }

    public TargetClass(Class wrapped, boolean isInScope) {
        super(wrapped.getCanonicalName().split("\\."));
        name = super.components[super.components.length - 1];
        this.isInScope = isInScope;

        for (Method has : wrapped.getMethods())
            methods.put(has.getName(), new TargetMethod(has));
    }

    public String getName() {
        return name.get();
    }

    public <Kind extends AValue> Typed<Kind> read(final String field) {
        final TargetClass parent = this;
        return new Typed<Kind>() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                if (isInScope)
                    to.add(name);
                else
                    parent.writeAtoms(to);
                to.add(HAS);
                to.add(new CodeAtom(field));
            }
        };
    }

    public <Ret extends Returnable> MethodCall<Ret> call(String method, Typed<? extends AValue>... parameters) {
        if (!methods.containsKey(method))
            throw new IllegalArgumentException("No method named " + method + " on " + name.get());

        return methods.get(method).call(parameters);
    }

    // TODO: Should this be seen as a function called on the type?
    private final static CodeAtom typeClassField = new CodeAtom("class");
    public Typed<AValue> classVal() {
        final TargetClass parent = this;
        return new Formatted.Type<AValue>() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                if (isInScope)
                    to.add(name);
                else
                    parent.writeAtoms(to);
                to.add(HAS);
                to.add(typeClassField);
            }
        };
    }

    private final static CodeAtom newInst = new CodeAtom("new");
    public MethodCall<AValue> newInstance(Typed<? extends AValue>... parameterList) {
        final TargetClass parent = this;
        return new MethodCall<AValue>("new " + name.get(), parameterList) {
            @Override
            public void writeAtoms(CodeAtoms to) {
                to.add(newInst);
                if (isInScope)
                    to.add(name);
                else
                    parent.writeAtoms(to);
                to.add(LPR);
                to.joinSep(SEP, parameters);
                to.add(RPR);
            }
        };
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        if (isInScope)
            name.writeAtoms(to);
        else
            super.writeAtoms(to);
    }

    public static TargetClass fromName(String name) {
        if (null == name)
            // TODO: Find a different way to mark constructors than giving them the return type null
            return null;
        else
            return new TargetClass(name);
    }
}
