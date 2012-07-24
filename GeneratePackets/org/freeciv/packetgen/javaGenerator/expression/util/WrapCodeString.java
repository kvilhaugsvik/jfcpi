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

package org.freeciv.packetgen.javaGenerator.expression.util;

import org.freeciv.packetgen.javaGenerator.CodeAtom;
import org.freeciv.packetgen.javaGenerator.CodeAtoms;
import org.freeciv.packetgen.javaGenerator.HasAtoms;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;

/**
 * Wrap a string of source code by declaring what it is
 */
public abstract class WrapCodeString implements HasAtoms {
    private final String javaCode;

    /**
     * Constructor that forces the expression to be ready at initialization
     * @param javaCode
     */
    protected WrapCodeString(String javaCode) {
        this.javaCode = javaCode;
    }

    /**
     * Get source code for an expression that returns a value of the type of the class
     * @return the source code
     */
    public String getJavaCode() {
        return javaCode;
    }

    public void writeAtoms(CodeAtoms to) {
        to.add(new CodeAtom(javaCode));
    }

    public final String toString() {
        return getJavaCode();
    }

    static class WrappedAny extends WrapCodeString implements AValue {
        /**
         * Constructor that forces the expression to be ready at initialization
         *
         * @param javaCode
         */
        protected WrappedAny(String javaCode) {
            super(javaCode);
        }
    }
}
