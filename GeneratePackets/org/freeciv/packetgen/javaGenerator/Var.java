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
import org.freeciv.packetgen.javaGenerator.expression.util.Formatted;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.Returnable;

import java.util.Collections;
import java.util.List;

public class Var extends Formatted implements Returnable {
    private final List<Annotate> annotations;
    private final Visibility visibility;
    private final Scope scope;
    private final Modifiable modifiable;
    private final TargetClass type;
    private final String name;
    private final AValue value;

    private final CodeAtom referName;

    @Deprecated
    protected Var(List<Annotate> annotations, Visibility visibility, Scope scope, Modifiable modifiable,
                String type, String name, AValue value) {
        this(annotations, visibility, scope, modifiable, new TargetClass(type), name, value);
    }

    protected Var(List<Annotate> annotations, Visibility visibility, Scope scope, Modifiable modifiable,
                TargetClass type, String name, AValue value) {
        this.annotations = annotations;
        this.visibility = visibility;
        this.scope = scope;
        this.modifiable = modifiable;
        this.type = type;
        this.name = name;
        this.value = value;

        this.referName = new CodeAtom((Scope.CODE_BLOCK.equals(scope) ? "" : "this.") + name);
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
        return type.getJavaCode();
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value.getJavaCode();
    }

    public String toString() {
        return (new Statement(this)).getJavaCode();
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
    public AValue ref() {
        return new FormattedAValue() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                to.add(referName);
            }
        };
    }

    // TODO: When fixing type system permit returning AValue
    public MethodCall.RetAValue call(String method, AValue... params) {
        final Var onVar = this;
        final MethodCall.RetAValue toCall = type.call(method, params);
        return new MethodCall.RetAValue(null, method, params) {
            @Override
            public void writeAtoms(CodeAtoms to) {
                onVar.ref().writeAtoms(to);
                to.add(HAS);
                toCall.writeAtoms(to);
            }
        };
    }

    public SetTo assign(final AValue value) {
        return new SetTo(referName, value);
    }


    public static Var local(Class type, String name, AValue value) {
        return new Var(Collections.<Annotate>emptyList(), null, Scope.CODE_BLOCK, Modifiable.YES,
                new TargetClass(type), name, value);
    }

    public static Var local(String type, String name, AValue value) {
        return new Var(Collections.<Annotate>emptyList(), null, Scope.CODE_BLOCK, Modifiable.YES, type, name, value);
    }

    public static Var field(Visibility visibility, Scope scope, Modifiable modifiable,
                                            String type, String name, AValue value) {
        return field(Collections.<Annotate>emptyList(), visibility, scope, modifiable, type, name, value);
    }

    public static Var field(List<Annotate> annotations, Visibility visibility, Scope scope, Modifiable modifiable,
                            String type, String name, AValue value) {
        return new Var(annotations, visibility, scope, modifiable, type, name, value);
    }


    public static class SetTo extends FormattedAValue {
        private final CodeAtom referName;
        private final AValue value;

        private SetTo(CodeAtom referName, AValue value) {
            this.referName = referName;
            this.value = value;
        }

        @Override
        public void writeAtoms(CodeAtoms to) {
            to.add(referName);
            to.add(ASSIGN);
            value.writeAtoms(to);
        }

        public static SetTo strToVal(String variable, AValue value) {
            return new SetTo(new CodeAtom(variable), value);
        }
    }
}
