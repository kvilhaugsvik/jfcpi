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

package com.kvilhaugsvik.javaGenerator.expression;

import com.kvilhaugsvik.javaGenerator.representation.CodeAtoms;
import com.kvilhaugsvik.javaGenerator.representation.HasAtoms;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.util.Formatted;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AValue;

public class ArrayLiteral extends Formatted implements Typed<AValue> {
    private final Typed<? extends AValue>[] elements;

    public ArrayLiteral(Typed<? extends AValue>... elements) {
        this.elements = elements;
    }

    @Override public void writeAtoms(CodeAtoms to) {
        to.add(HasAtoms.ALS);
        to.joinSep(SEP, elements);
        to.add(HasAtoms.ALE);
    }
}
