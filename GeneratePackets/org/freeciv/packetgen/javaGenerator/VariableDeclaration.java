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

public class VariableDeclaration {
    private final ClassWriter.Visibility visibility;
    private final ClassWriter.Scope scope;
    private final ClassWriter.Modifiable modifiable;
    private final String type;
    private final String name;
    private final String value;

    public VariableDeclaration(ClassWriter.Visibility visibility, ClassWriter.Scope scope, ClassWriter.Modifiable modifiable,
                               String type, String name, String value) {
        this.visibility = visibility;
        this.scope = scope;
        this.modifiable = modifiable;
        this.type = type;
        this.name = name;
        this.value = value;
    }

    public ClassWriter.Visibility getVisibility() {
        return visibility;
    }

    public ClassWriter.Scope getScope() {
        return scope;
    }

    public ClassWriter.Modifiable getModifiable() {
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
        return ClassWriter.ifIs(visibility.toString(), " ") +
                ClassWriter.ifIs(scope.toString(), " ") +
                ClassWriter.ifIs(modifiable.toString(), " ") +
                type + " " + name + ClassWriter.ifIs(" = ", value, "") + ";";
    }
}
