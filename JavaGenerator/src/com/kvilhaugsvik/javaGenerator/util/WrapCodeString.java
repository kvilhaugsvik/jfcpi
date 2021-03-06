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

package com.kvilhaugsvik.javaGenerator.util;

import com.kvilhaugsvik.javaGenerator.representation.IR.CodeAtom;
import com.kvilhaugsvik.javaGenerator.representation.CodeAtoms;
import com.kvilhaugsvik.javaGenerator.representation.HasAtoms;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.*;

/**
 * Wrap a string of source code by declaring what it is
 */
public class WrapCodeString<Returns extends Returnable> implements HasAtoms, Typed<Returns> {
    private final String javaCode;

    /**
     * Constructor that forces the expression to be ready at initialization
     * @param javaCode
     */
    protected WrapCodeString(String javaCode) {
        this.javaCode = javaCode;
    }

    public void writeAtoms(CodeAtoms to) {
        to.add(new CodeAtom(javaCode));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WrapCodeString && javaCode.equals(((WrapCodeString)obj).javaCode);
    }

    @Override
    public int hashCode() {
        return javaCode.hashCode();
    }

    @Override
    public String toString() {
        return javaCode;
    }
}
