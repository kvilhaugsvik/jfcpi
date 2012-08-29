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

import org.freeciv.packetgen.javaGenerator.IR.CodeAtom;
import org.freeciv.packetgen.javaGenerator.CodeAtoms;
import org.freeciv.packetgen.javaGenerator.HasAtoms;
import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.*;

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

    public void writeAtoms(CodeAtoms to) {
        to.add(new CodeAtom(javaCode));
    }

    static class WrappedAny extends WrapCodeString implements Typed<AValue> {
        protected WrappedAny(String javaCode) {
            super(javaCode);
        }
    }

    static class WrappedLong extends WrapCodeString implements Typed<ALong> {
        public WrappedLong(String javaCode) {
            super(javaCode);
        }
    }

    static class WrappedInt extends WrapCodeString implements Typed<AnInt> {
        public WrappedInt(String javaCode) {
            super(javaCode);
        }
    }

    static class WrappedString extends WrapCodeString implements Typed<AString> {
        public WrappedString(String javaCode) {
            super(javaCode);
        }
    }

    static class WrappedVoid extends WrapCodeString implements Typed<NoValue> {
        public WrappedVoid(String javaCode) {
            super(javaCode);
        }
    }

    static class WrappedBool extends WrapCodeString implements Typed<ABool> {
        public WrappedBool(String javaCode) {
            super(javaCode);
        }
    }
}
