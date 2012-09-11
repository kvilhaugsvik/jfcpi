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

import org.freeciv.packetgen.javaGenerator.IR.CodeAtom;
import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.expression.util.Formatted;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AString;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.Returnable;
import org.freeciv.packetgen.javaGenerator.formating.CodeStyle;

public class MethodCall<Returns extends Returnable> extends Formatted implements HasAtoms, Typed<Returns> {
    private final Comment comment;
    protected final String method;
    protected final Typed<? extends AValue>[] parameters;

    public MethodCall(String name, String... params) {
        this(Comment.no(), name, params);
    }

    public MethodCall(Comment comment, String name, String... params) {
        this(comment, name, paramListIsAValue(params));
    }

    public MethodCall(String name, Typed<? extends AValue>... params) {
        this(Comment.no(), name, params);
    }

    public MethodCall(Comment comment, String name, Typed<? extends AValue>... params) {
        if (null == name)
            throw new IllegalArgumentException("No method name given to method call");

        this.comment = comment;
        this.method = name;
        this.parameters = params;
    }

    private static Typed<AValue>[] paramListIsAValue(String[] parameterList) {
        Typed<AValue>[] parameters = new Typed[parameterList.length];
        for (int i = 0; i < parameterList.length; i++)
            parameters[i] = BuiltIn.asAValue(parameterList[i]);
        return parameters;
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        to.hintStart(MethodCall.class.getCanonicalName());
        comment.writeAtoms(to);
        to.add(new CodeAtom(method));
        to.add(LPR);
        if (0 < parameters.length) {
            to.hintStart(CodeStyle.ARGUMENTS);
            to.joinSep(SEP, parameters);
            to.hintEnd(CodeStyle.ARGUMENTS);
        }
        to.add(RPR);
        to.hintEnd(MethodCall.class.getCanonicalName());
    }
}
