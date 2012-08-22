/*
 * Copyright (c) 2011, 2012, Sveinung Kvilhaugsvik
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

package org.freeciv.packetgen.javaGenerator;

import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.expression.util.Formatted;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AString;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.Returnable;

public class MethodCallStatic extends Formatted implements HasAtoms {
    private final String comment;
    protected final String method;
    private final AValue[] parameters;

    public MethodCallStatic(String comment, String name, String... params) {
        this(comment, name, paramListIsAValue(params));
    }

    public MethodCallStatic(String comment, String name, AValue... params) {
        if (null == name)
            throw new IllegalArgumentException("No method name given to method call");

        this.comment = comment;
        this.method = name;
        this.parameters = params;
    }

    private static AValue[] paramListIsAValue(String[] parameterList) {
        AValue[] parameters = new AValue[parameterList.length];
        for (int i = 0; i < parameterList.length; i++)
            parameters[i] = BuiltIn.asAValue(parameterList[i]);
        return parameters;
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        to.add(new CodeAtom(method));
        to.add(LPR);
        to.joinSep(SEP, parameters);
        to.add(RPR);
        if (null != comment) {
            to.add(CCommentStart);
            to.add(new CodeAtom(comment));
            to.add(CCommentEnd);
        }
    }

    public static class AReturnable extends MethodCallStatic implements Returnable {
        public AReturnable(String comment, String name, String... params) {
            super(comment, name, params);
        }
    }

    public static class RetAValue extends MethodCallStatic implements AValue {
        public RetAValue(String comment, String name, AValue... params) {
            super(comment, name, params);
        }
    }

    public static class RetAString extends MethodCallStatic implements AString {
        public RetAString(String comment, String name, AValue... params) {
            super(comment, name, params);
        }
    }
}
