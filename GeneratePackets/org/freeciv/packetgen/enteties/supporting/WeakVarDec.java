/*
 * Copyright (c) 2012. Sveinung Kvilhaugsvik
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

package org.freeciv.packetgen.enteties.supporting;

import org.freeciv.packetgen.javaGenerator.TargetArray;
import org.freeciv.packetgen.javaGenerator.TargetClass;

public class WeakVarDec {
    protected final String name;
    protected final String packageOfType; // TODO: This information don't belong here. Query the storage in users
    protected final String type;
    protected final ArrayDeclaration[] declarations;
    protected final int eatenDeclartions; // TODO: This information don't belong here. Query the storage in users

    @Deprecated
    public WeakVarDec(String packageOfType, String kind, String name, ArrayDeclaration... declarations) {
        this(packageOfType, kind, name, 0, declarations);
    }

    public WeakVarDec(String packageOfType, String kind, String name, int eatenDeclartions, ArrayDeclaration... declarations) {
        this.type = kind;
        this.packageOfType = packageOfType;
        this.declarations = declarations;
        this.eatenDeclartions = eatenDeclartions;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public TargetClass getJavaType() { // TODO: This method don't belong here. Query the storage in users
        int arrayLevels = declarations.length - eatenDeclartions;
        if (0 == arrayLevels)
            return TargetClass.fromName(packageOfType + "." + type);
        else
            return new TargetArray(packageOfType + "." + type, arrayLevels, true);
    }

    public String getName() {
        return name;
    }

    public ArrayDeclaration[] getDeclarations() {
        return declarations;
    }

    public static class ArrayDeclaration {
        public final IntExpression maxSize;

        public ArrayDeclaration(IntExpression maxSize) {
            this.maxSize = maxSize;
        }
    }
}
