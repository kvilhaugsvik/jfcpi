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

import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.FieldTypeBasic;

import java.util.List;

public class WeakField extends WeakVarDec {
    private final List<WeakFlag> flags;

    public WeakField(String name, String kind, List<WeakFlag> flags, ArrayDeclaration... declarations) {
        super(new Requirement(kind, FieldTypeBasic.FieldTypeAlias.class), "org.freeciv.packet.fieldtype", kind, name, 0, declarations);
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
