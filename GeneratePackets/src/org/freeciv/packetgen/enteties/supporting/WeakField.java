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

import com.kvilhaugsvik.dependency.Requirement;
import org.freeciv.packetgen.enteties.FieldType;

import java.util.List;

public class WeakField extends WeakVarDec {
    private final List<WeakFlag> flags;

    public WeakField(String name, String kind, List<WeakFlag> flags, ArrayDeclaration... declarations) {
        super(new Requirement(kind, FieldType.class), name, declarations);
        this.flags = flags;
    }

    public List<WeakFlag> getFlags() {
        return flags;
    }

    public ArrayDeclaration[] getDeclarations() {
        return (ArrayDeclaration[])super.getDeclarations();
    }

    public static class ArrayDeclaration extends WeakVarDec.ArrayDeclaration {
        public final String elementsToTransfer;

        public ArrayDeclaration(IntExpression maxSize, String elementsToTransfer) {
            super(maxSize);
            this.elementsToTransfer = elementsToTransfer;
        }
    }
}
