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

package org.freeciv.packetgen.javaGenerator;

import org.freeciv.packetgen.javaGenerator.IR.CodeAtom;
import org.freeciv.packetgen.javaGenerator.expression.Statement;
import org.freeciv.packetgen.javaGenerator.expression.Value;
import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.util.Formatted;
import org.freeciv.packetgen.javaGenerator.expression.util.ValueHelper;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.Returnable;

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

    private final Address referName;

    protected Var(List<Annotate> annotations, Visibility visibility, Scope scope, Modifiable modifiable,
                TargetClass type, String name, Typed<Kind> value) {
        this.annotations = annotations;
        this.visibility = visibility;
        this.scope = scope;
        this.modifiable = modifiable;
        this.type = type;
        this.name = name;
        this.value = value;

        switch (scope) {
            case CLASS:
                this.referName = new Address(TargetClass.SELF_TYPED, new CodeAtom(name));
                break;
            case OBJECT:
                this.referName = new Address("this", name);
                break;
            case CODE_BLOCK:
            default:
                this.referName = new Address(name);
                break;
        }
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
    public String getJavaCodeIndented(String start) {
        return new Statement(this).getJavaCodeIndented(start);
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
        return new Reference<Kind>(this);
    }

    public <Ret extends Returnable> MethodCall<Ret> call(String method, Typed<? extends AValue>... params) {
        return ref().<Ret>call(method, params);
    }

    public <Ret extends AValue> SetTo<Ret> assign(final Typed<Ret> value) {
        return ref().assign(value);
    }


    public static <Kind extends AValue> Var<Kind> local(Class type, String name, Typed<Kind> value) {
        return Var.<Kind>local(new TargetClass(type), name, value);
    }

    public static <Kind extends AValue> Var<Kind> local(String type, String name, Typed<Kind> value) {
        return Var.<Kind>local(new TargetClass(type), name, value);
    }

    public static <Kind extends AValue> Var<Kind> local(TargetClass type, String name, Typed<Kind> value) {
        return new Var<Kind>(Collections.<Annotate>emptyList(), null, Scope.CODE_BLOCK, Modifiable.YES, type, name, value);
    }

    public static <Kind extends AValue> Var<Kind> param(TargetClass kind, String name) {
        return new Var<Kind>(Collections.<Annotate>emptyList(), null, Scope.CODE_BLOCK, Modifiable.YES, kind, name, null);
    }

    public static <Kind extends AValue> Var<Kind> param(Class kind, String name) {
        return Var.<Kind>param(new TargetClass(kind), name);
    }

    public static <Kind extends AValue> Var<Kind> param(String kind, String name) {
        return Var.<Kind>param(new TargetClass(kind), name);
    }

    public static <Kind extends AValue> Var<Kind> field(Visibility visibility, Scope scope, Modifiable modifiable,
                                            String type, String name, Typed<Kind> value) {
        return Var.<Kind>field(Collections.<Annotate>emptyList(), visibility, scope, modifiable, type, name, value);
    }

    public static <Kind extends AValue> Var<Kind> field(List<Annotate> annotations, Visibility visibility, Scope scope, Modifiable modifiable,
                            String type, String name, Typed<Kind> value) {
        return Var.<Kind>field(annotations, visibility, scope, modifiable, new TargetClass(type), name, value);
    }

    public static <Kind extends AValue> Var<Kind> field(List<Annotate> annotations, Visibility visibility, Scope scope, Modifiable modifiable,
                            TargetClass type, String name, Typed<Kind> value) {
        return new Var<Kind>(annotations, visibility, scope, modifiable, type, name, value);
    }

    public <Kind extends AValue> Typed<Kind> read(final String field) {
        return new Typed<Kind>() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                referName.writeAtoms(to);
                to.add(HAS);
                to.add(new CodeAtom(field));
            }
        };
    }


    public static class SetTo<Ret extends AValue> extends Formatted.Type<Ret> {
        private final Address referName;
        private final Typed<Ret> value;

        private SetTo(Address referName, Typed<Ret> value) {
            this.referName = referName;
            this.value = value;
        }

        @Override
        public void writeAtoms(CodeAtoms to) {
            referName.writeAtoms(to);
            to.add(ASSIGN);
            value.writeAtoms(to);
        }

        public static <Ret extends AValue> SetTo<Ret> strToVal(String variable, Typed<Ret> value) {
            return new SetTo<Ret>(new Address(variable), value);
        }
    }

    public static class Reference<Contains extends AValue> extends Address implements Value<Contains> {
        private final Var of;
        private final ValueHelper valueHelper;

        public Reference(Var of, CodeAtom... followedBy) {
            super(of.referName, followedBy);
            this.of = of;
            this.valueHelper = new ValueHelper(of.type, this);
        }

        public <Ret extends Returnable> MethodCall<Ret> call(String method, Typed<? extends AValue>... params) {
            return valueHelper.call(method, params);
        }

        @Override
        public <Ret extends AValue> Value<Ret> callV(String method, Typed<? extends AValue>... params) {
            return valueHelper.callV(method, params);
        }

        public <Ret extends AValue> SetTo<Ret> assign(final Typed<Ret> value) {
            return new SetTo<Ret>(of.referName, value);
        }
    }
}
