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

    public static NoValue RETURN(final AValue arg1) {
            return new Formatted.FormattedVoid() {
                @Override
                public void writeAtoms(CodeAtoms to) {
                    to.add(RET);
                    arg1.writeAtoms(to);
                }
            };
    }

    private static final From2or3<NoValue, ABool, Block, Block> ifImpl = new If();
    public static NoValue IF (ABool cond, Block then) {
        return ifImpl.x(cond, then);
    }
    public static NoValue IF (ABool cond, Block then, Block ifNot) {
        return ifImpl.x(cond, then, ifNot);
    }

    public static NoValue WHILE(final ABool cond, final Block rep) {
        return new Formatted.FormattedVoid() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                to.add(WHILE);
                to.add(LPR);
                cond.writeAtoms(to);
                to.add(RPR);
                rep.writeAtoms(to);
            }
        };
    }

    public static final ExprFrom1<AString, AValue> TO_STRING_OBJECT =
            new ExprFrom1<AString, AValue>() {
                @Override
                public AString x(AValue arg1) {
                    return asAString(arg1.getJavaCode() + ".toString()");
                }
            };


    public static AString asAString(String javaCode) {
        return new WrappedString(javaCode);
    }

    public static ABool asBool(String javaCode) {
        return new WrappedBool(javaCode);
    }

    public static AnInt asAnInt(String javaCode) {
        return new WrappedInt(javaCode);
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
