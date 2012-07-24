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
import org.freeciv.packetgen.javaGenerator.expression.util.Formatted;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.Returnable;

public class VariableDeclaration extends Formatted implements Returnable {
    private final Visibility visibility;
    private final Scope scope;
    private final Modifiable modifiable;
    private final String type;
    private final String name;
    private final String value;

    public VariableDeclaration(Visibility visibility, Scope scope, Modifiable modifiable,
                               String type, String name, String value) {
        this.visibility = visibility;
        this.scope = scope;
        this.modifiable = modifiable;
        this.type = type;
        this.name = name;
        this.value = value;
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
        return value;
    }

    public String toString() {
        return (new Statement(this)).getJavaCode();
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        visibility.writeAtoms(to);
        scope.writeAtoms(to);
        modifiable.writeAtoms(to);
        to.add(new CodeAtom(type));
        to.add(new CodeAtom(name));
        if (null != value) {
            to.add(ASSIGN);
            to.add(new CodeAtom(value));
        }
    }
}
