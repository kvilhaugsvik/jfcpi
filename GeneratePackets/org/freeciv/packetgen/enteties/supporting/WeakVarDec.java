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

import org.freeciv.utility.Strings;

public class WeakVarDec {
    protected final String name;
    protected final String type;
    protected final ArrayDeclaration[] declarations;
    protected final int eatenDeclartions;

    @Deprecated
    public WeakVarDec(String kind, String name, ArrayDeclaration... declarations) {
        this(kind, name, 0, declarations);
    }

    public WeakVarDec(String kind, String name, int eatenDeclartions, ArrayDeclaration... declarations) {
        this.type = kind;
        this.declarations = declarations;
        this.eatenDeclartions = eatenDeclartions;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getTypeIncludingArrayBraces() {
        return type + Strings.repeat("[]", declarations.length - eatenDeclartions);
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
