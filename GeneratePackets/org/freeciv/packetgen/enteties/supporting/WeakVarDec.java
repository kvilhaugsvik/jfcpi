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

import com.kvilhaugsvik.javaGenerator.TargetArray;
import com.kvilhaugsvik.javaGenerator.TargetClass;
import org.freeciv.packetgen.dependency.Requirement;

public class WeakVarDec {
    protected final String name;
    protected final Requirement reqKind;
    protected final String type;
    protected final ArrayDeclaration[] declarations;

    public WeakVarDec(Requirement reqKind, String name, ArrayDeclaration... declarations) {
        if (null == reqKind)
            throw new IllegalArgumentException("Must have a type");

        this.reqKind = reqKind;
        this.type = reqKind.getName();
        this.declarations = declarations;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public ArrayDeclaration[] getDeclarations() {
        return declarations;
    }

    public Requirement getTypeRequirement() {
        return reqKind;
    }

    public static class ArrayDeclaration {
        public final IntExpression maxSize;

        public ArrayDeclaration(IntExpression maxSize) {
            this.maxSize = maxSize;
        }
    }
}
