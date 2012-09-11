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
import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.util.Formatted;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.Returnable;

import java.util.Collections;
import java.util.List;

public class Var extends Formatted implements Typed<Returnable> {
    private final List<Annotate> annotations;
    private final Visibility visibility;
    private final Scope scope;
    private final Modifiable modifiable;
    private final TargetClass type;
    private final String name;
    private final Typed<? extends AValue> value;

    private final Address referName;

    protected Var(List<Annotate> annotations, Visibility visibility, Scope scope, Modifiable modifiable,
                TargetClass type, String name, Typed<? extends AValue> value) {
        this.annotations = annotations;
        this.visibility = visibility;
        this.scope = scope;
        this.modifiable = modifiable;
        this.type = type;
        this.name = name;
        this.value = value;

        this.referName = new Address((Scope.CODE_BLOCK.equals(scope) ? "" : "this.") + name);
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
    public Reference<AValue> ref() {
        return new Reference<AValue>(this);
    }

    public <Ret extends Returnable> MethodCall<Ret> call(String method, Typed<? extends AValue>... params) {
        return ref().<Ret>call(method, params);
    }

    public SetTo assign(final Typed<? extends AValue> value) {
        return ref().assign(value);
    }


    public static Var local(Class type, String name, Typed<? extends AValue> value) {
        return new Var(Collections.<Annotate>emptyList(), null, Scope.CODE_BLOCK, Modifiable.YES,
                new TargetClass(type), name, value);
    }

    public static Var local(String type, String name, Typed<? extends AValue> value) {
        return new Var(Collections.<Annotate>emptyList(), null, Scope.CODE_BLOCK, Modifiable.YES, new TargetClass(type), name, value);
    }

    public static Var param(TargetClass kind, String name) {
        return new Var(Collections.<Annotate>emptyList(), null, Scope.CODE_BLOCK, Modifiable.YES, kind, name, null);
    }

    public static Var param(Class kind, String name) {
        return Var.param(new TargetClass(kind), name);
    }

    public static Var param(String kind, String name) {
        return Var.param(new TargetClass(kind), name);
    }

    public static Var field(Visibility visibility, Scope scope, Modifiable modifiable,
                                            String type, String name, Typed<? extends AValue> value) {
        return field(Collections.<Annotate>emptyList(), visibility, scope, modifiable, type, name, value);
    }

    public static Var field(List<Annotate> annotations, Visibility visibility, Scope scope, Modifiable modifiable,
                            String type, String name, Typed<? extends AValue> value) {
        return field(annotations, visibility, scope, modifiable, new TargetClass(type), name, value);
    }

    public static Var field(List<Annotate> annotations, Visibility visibility, Scope scope, Modifiable modifiable,
                            TargetClass type, String name, Typed<? extends AValue> value) {
        return new Var(annotations, visibility, scope, modifiable, type, name, value);
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


    public static class SetTo extends Formatted.Type<AValue> {
        private final Address referName;
        private final Typed<? extends AValue> value;

        private SetTo(Address referName, Typed<? extends AValue> value) {
            this.referName = referName;
            this.value = value;
        }

        @Override
        public void writeAtoms(CodeAtoms to) {
            referName.writeAtoms(to);
            to.add(ASSIGN);
            value.writeAtoms(to);
        }

        public static SetTo strToVal(String variable, Typed<? extends AValue> value) {
            return new SetTo(new Address(variable), value);
        }
    }

    public static class Reference<Contains extends AValue> extends Address implements Typed<Contains> {
        private final Var of;

        public Reference(Var of, CodeAtom... followedBy) {
            super(of.referName, followedBy);
            this.of = of;
        }

        public <Ret extends Returnable> MethodCall<Ret> call(String method, Typed<? extends AValue>... params) {
            final MethodCall<AValue> toCall = of.type.call(method, params);
            return new MethodCall<Ret>(method, params) {
                @Override
                public void writeAtoms(CodeAtoms to) {
                    of.ref().writeAtoms(to);
                    to.add(HAS);
                    toCall.writeAtoms(to);
                }
            };
        }

        public SetTo assign(final Typed<? extends AValue> value) {
            return new SetTo(of.referName, value);
        }
    }
}
