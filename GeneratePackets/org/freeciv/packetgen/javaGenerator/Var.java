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

import org.freeciv.packetgen.javaGenerator.representation.CodeAtoms;
import org.freeciv.packetgen.javaGenerator.representation.IR.CodeAtom;
import org.freeciv.packetgen.javaGenerator.expression.Statement;
import org.freeciv.packetgen.javaGenerator.typeBridge.Value;
import org.freeciv.packetgen.javaGenerator.typeBridge.Typed;
import org.freeciv.packetgen.javaGenerator.util.Formatted;
import org.freeciv.packetgen.javaGenerator.util.ValueHelper;
import org.freeciv.packetgen.javaGenerator.typeBridge.willReturn.AValue;
import org.freeciv.packetgen.javaGenerator.typeBridge.willReturn.Returnable;

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
                TargetClass type, String name, Typed<Kind> value) {
        this.annotations = annotations;
        this.visibility = visibility;
        this.scope = scope;
        this.modifiable = modifiable;
        this.type = type;
        this.name = name;
        this.value = value;

        this.reference = Reference.refOn(this);
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
        return reference;
    }

    public <Ret extends AValue> SetTo<Ret> assign(final Typed<Ret> value) {
        return ref().assign(value);
    }


    public static <Kind extends AValue> Var<Kind> local(Class type, String name, Typed<Kind> value) {
        return Var.<Kind>local(TargetClass.fromClass(type), name, value);
    }

    public static <Kind extends AValue> Var<Kind> local(TargetClass type, String name, Typed<Kind> value) {
        return new Var<Kind>(Collections.<Annotate>emptyList(), null, Scope.CODE_BLOCK, Modifiable.YES, type, name, value);
    }

    public static <Kind extends AValue> Var<Kind> param(TargetClass kind, String name) {
        return new Var<Kind>(Collections.<Annotate>emptyList(), null, Scope.CODE_BLOCK, Modifiable.YES, kind, name, null);
    }

    public static <Kind extends AValue> Var<Kind> param(Class kind, String name) {
        return Var.<Kind>param(TargetClass.fromClass(kind), name);
    }

    public static <Kind extends AValue> Var<Kind> field(List<Annotate> annotations,
                                                        Visibility visibility, Scope scope, Modifiable modifiable,
                                                        TargetClass type, String name, Typed<Kind> value) {
        return new Var<Kind>(annotations, visibility, scope, modifiable, type, name, value);
    }

    public static <Kind extends AValue> Var<Kind> field(List<Annotate> annotations,
                                                        Visibility visibility, Scope scope, Modifiable modifiable,
                                                        Class type, String name, Typed<Kind> value) {
        return Var.<Kind>field(annotations, visibility, scope, modifiable, TargetClass.fromClass(type), name, value);
    }

    public <Kind extends AValue> Typed<Kind> read(final String field) {
        return new Typed<Kind>() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                reference.writeAtoms(to);
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
            return new SetTo<Ret>(new Address(Address.LOCAL_CODE_BLOCK, Address.addressString2Components(variable)), value);
        }
    }

    public static class Reference<Contains extends AValue> extends Address implements Value<Contains> {
        public static final Reference<AValue> THIS = Var.<AValue>param(TargetClass.SELF_TYPED, "this").ref();

        private final ValueHelper valueHelper;

        private Reference(Var of, Address where, CodeAtom name) {
            super(where, name);
            this.valueHelper = new ValueHelper(of.type, this);
        }

        public static Reference refOn(Var of) {
            CodeAtom name = new CodeAtom(of.name);
            switch (of.scope) {
                case CLASS:
                    return new Reference(of, TargetClass.SELF_TYPED, name);
                case OBJECT:
                    return new Reference(of, THIS, name);
                case CODE_BLOCK:
                default:
                    return new Reference(of, Address.LOCAL_CODE_BLOCK, name);
            }
        }

        public <Ret extends Returnable> Typed<Ret> call(String method, Typed<? extends AValue>... params) {
            return valueHelper.<Ret>call(method, params);
        }

        @Override
        public <Ret extends AValue> Value<Ret> callV(String method, Typed<? extends AValue>... params) {
            return valueHelper.<Ret>callV(method, params);
        }

        public <Ret extends AValue> SetTo<Ret> assign(final Typed<Ret> value) {
            return new SetTo<Ret>(this, value);
        }

        @Override
        public void writeAtoms(CodeAtoms to) {
            to.hintStart(Reference.class.getCanonicalName());
            super.writeAtoms(to);
            to.hintEnd(Reference.class.getCanonicalName());
        }
    }
}
