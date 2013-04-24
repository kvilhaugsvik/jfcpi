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

package com.kvilhaugsvik.javaGenerator;

import com.kvilhaugsvik.javaGenerator.expression.Reference;
import com.kvilhaugsvik.javaGenerator.formating.TokensToStringStyle;
import com.kvilhaugsvik.javaGenerator.representation.CodeAtoms;
import com.kvilhaugsvik.javaGenerator.representation.IR.CodeAtom;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.util.Formatted;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AValue;

import java.util.Collections;
import java.util.List;

public class Var<Kind extends AValue> extends Formatted implements Typed<Kind> {
    private final List<Annotate> annotations;
    private final Visibility visibility;
    private final Scope scope;
    private final Modifiable modifiable;
    private final TargetClass type;
    private final String name;
    private final Typed<? extends AValue> value;

    private final Reference reference;

    protected Var(List<Annotate> annotations, Visibility visibility, Scope scope, Modifiable modifiable,
                TargetClass type, String name, Typed<Kind> value, TargetClass locatedOn) {
        this.annotations = annotations;
        this.visibility = visibility;
        this.scope = scope;
        this.modifiable = modifiable;
        this.type = type;
        this.name = name;
        this.value = value;

        this.reference = Reference.refOn(locatedOn, this);
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public Scope getScope() {
        return scope;
    }

    public Modifiable getModifiable() {
        return modifiable;
    }

    public TargetClass getTType() {
        return type;
    }

    public String getType() {
        return type.getName();
    }

    public String getName() {
        return name;
    }

    public Typed<? extends AValue> getValue() {
        return value;
    }

    @Override
    public String getJavaCodeIndented(String start, TokensToStringStyle style) {
        return new Statement(this).getJavaCodeIndented(start, style);
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        for (Annotate annotation : annotations)
            annotation.writeAtoms(to);
        if (null != visibility)
            visibility.writeAtoms(to);
        scope.writeAtoms(to);
        modifiable.writeAtoms(to);
        type.writeAtoms(to);
        to.add(new CodeAtom(name));
        if (null != value) {
            to.add(ASSIGN);
            value.writeAtoms(to);
        }
    }

    /**
     * Get the name read from a local scope
     * @return variable name access
     */
    public Reference<Kind> ref() {
        return reference;
    }

    public <Ret extends AValue> Reference.SetTo<Ret> assign(final Typed<Ret> value) {
        return ref().assign(value);
    }


    public static <Kind extends AValue> Var<Kind> local(Class type, String name, Typed<Kind> value) {
        return Var.<Kind>local(TargetClass.from(type), name, value);
    }

    public static <Kind extends AValue> Var<Kind> local(TargetClass type, String name, Typed<Kind> value) {
        return new Var<Kind>(Collections.<Annotate>emptyList(), null, Scope.CODE_BLOCK, Modifiable.YES, type, name, value, TargetClass.SELF_TYPED);
    }

    public static <Kind extends AValue> Var<Kind> param(TargetClass kind, String name) {
        return new Var<Kind>(Collections.<Annotate>emptyList(), null, Scope.CODE_BLOCK, Modifiable.YES, kind, name, null, TargetClass.SELF_TYPED);
    }

    public static <Kind extends AValue> Var<Kind> param(Class kind, String name) {
        return Var.<Kind>param(TargetClass.from(kind), name);
    }

    public static <Kind extends AValue> Var<Kind> field(List<Annotate> annotations,
                                                        Visibility visibility, Scope scope, Modifiable modifiable,
                                                        TargetClass type, String name, Typed<Kind> value) {
        return Var.<Kind>field(annotations, visibility, scope, modifiable, type, name, value, TargetClass.SELF_TYPED);
    }

    public static <Kind extends AValue> Var<Kind> field(List<Annotate> annotations,
                                                        Visibility visibility, Scope scope, Modifiable modifiable,
                                                        TargetClass type, String name, Typed<Kind> value,
                                                        TargetClass locatedOn) {
        return new Var<Kind>(annotations, visibility, scope, modifiable, type, name, value, locatedOn);
    }

    public static <Kind extends AValue> Var<Kind> field(List<Annotate> annotations,
                                                        Visibility visibility, Scope scope, Modifiable modifiable,
                                                        Class type, String name, Typed<Kind> value) {
        return Var.<Kind>field(annotations, visibility, scope, modifiable, TargetClass.from(type), name, value);
    }

    public boolean isAnnotatedUsing(String name) {
        for (Annotate annotation : annotations)
            if (annotation.getName().equals(name))
                return true;
        return false;
    }
}
