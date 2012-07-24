/*
 * Copyright (c) 2011, 2012. Sveinung Kvilhaugsvik
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

import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.creators.*;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.*;
import org.freeciv.packetgen.javaGenerator.CodeAtoms;

import static org.freeciv.packetgen.javaGenerator.expression.util.WrapCodeString.*;

public class BuiltIn {
    public static final ABool TRUE = asBool("true");
    public static final ABool FALSE = asBool("false");

    public static final ExprFrom1<NoValue, AValue> RETURN = new ExprFrom1<NoValue, AValue>() {
        @Override
        public NoValue x(final AValue arg1) {
            return new Formatted.FormattedVoid() {
                @Override
                public void writeAtoms(CodeAtoms to) {
                    to.add(RET);
                    arg1.writeAtoms(to);
                }
            };
        }
    };

    public static final From2or3<NoValue, ABool, Block, Block> IF = new If();


    public static AString asAString(String javaCode) {
        return new WrappedString(javaCode);
    }

    public static ABool asBool(String javaCode) {
        return new WrappedBool(javaCode);
    }

    public static ALong asALong(String javaCode) {
        return new WrappedLong(javaCode);
    }

    public static AValue asAValue(String javaCode) {
        return new WrappedAny(javaCode);
    }

    public static NoValue asVoid(String javaCode) {
        return new WrappedVoid(javaCode);
    }
}
