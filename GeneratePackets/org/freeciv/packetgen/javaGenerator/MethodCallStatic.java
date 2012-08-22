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
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.Returnable;

public class MethodCallStatic extends Formatted implements HasAtoms {
    private final String comment;
    protected final String method;
    private final AValue[] parameters;

    public MethodCallStatic(String comment, String name, String... params) {
        if (null == name)
            throw new IllegalArgumentException("No method name given to method call");

        this.comment = comment;
        this.method = name;
        this.parameters = new AValue[params.length];
        for (int i = 0; i < params.length; i++)
            this.parameters[i] = BuiltIn.asAValue(params[i]);
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
}
