/*
 * Copyright (c) 2012, Sveinung Kvilhaugsvik
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

import java.util.Collections;
import java.util.List;

public class WeakField {
    private final String name, type;
    private final ArrayDeclaration[] declarations;

    @Deprecated public WeakField(String name, String kind, ArrayDeclaration... declarations) {
        this(name, kind, Collections.<WeakFlag>emptyList(), declarations);
    }

    public WeakField(String name, String kind, List<WeakFlag> flags, ArrayDeclaration... declarations) {
        this.name = name;
        this.type = kind;
        this.declarations = declarations;
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

    public static class ArrayDeclaration {
        public final IntExpression maxSize;
        public final String elementsToTransfer;

        public ArrayDeclaration(IntExpression maxSize, String elementsToTransfer) {
            this.maxSize = maxSize;
            this.elementsToTransfer = elementsToTransfer;
        }
    }
}
