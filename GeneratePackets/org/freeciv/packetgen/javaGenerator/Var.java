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

import org.freeciv.packetgen.javaGenerator.expression.Statement;
import org.freeciv.packetgen.javaGenerator.expression.creators.ExprFrom1;
import org.freeciv.packetgen.javaGenerator.expression.util.Formatted;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.Returnable;

public class Var extends Formatted implements Returnable {
    private final Visibility visibility;
    private final Scope scope;
    private final Modifiable modifiable;
    private final String type;
    private final String name;
    private final AValue value;

    private final CodeAtom referName;

    private Var(Visibility visibility, Scope scope, Modifiable modifiable,
                String type, String name, AValue value) {
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
        return type;
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
    public void writeAtoms(CodeAtoms to) {
        if (null != visibility)
            visibility.writeAtoms(to);
        scope.writeAtoms(to);
        modifiable.writeAtoms(to);
        to.add(new CodeAtom(type));
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

    public AValue assign(final AValue arg1) {
        return new FormattedAValue() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                to.add(referName);
                to.add(ASSIGN);
                arg1.writeAtoms(to);
            }
        };
    }


    public static Var local(String type, String name, AValue value) {
        return new Var(null, Scope.CODE_BLOCK, Modifiable.YES, type, name, value);
    }

    public static Var field(Visibility visibility, Scope scope, Modifiable modifiable,
                                            String type, String name, AValue value) {
        return new Var(visibility, scope, modifiable, type, name, value);
    }
}
