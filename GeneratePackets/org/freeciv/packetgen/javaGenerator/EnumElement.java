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

import org.freeciv.Util;
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.expression.util.Formatted;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;

public class EnumElement extends Formatted implements HasAtoms {
    private final String comment;
    private final String elementName;
    private final AValue[] paramlist;

    protected EnumElement(String comment, String elementName, String... params) {
        if (null == elementName)
            throw new IllegalArgumentException("All elements of enums must have names");

        this.comment = comment;
        this.elementName = elementName;
        this.paramlist = new AValue[params.length];
        for (int i = 0; i < params.length; i++)
            this.paramlist[i] = BuiltIn.asAValue(params[i]);
    }

    public String getEnumValueName() {
        return elementName;
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        to.add(new CodeAtom(elementName));
        to.add(LPR);
        to.joinSep(SEP, paramlist);
        to.add(RPR);
        if (null != comment) {
            to.add(CCommentStart);
            to.add(new CodeAtom(comment));
            to.add(CCommentEnd);
        }
    }

    public String toString() {
        return this.getJavaCode();
    }

    public static EnumElement newEnumValue(String enumValueName) {
        return new EnumElement(null, enumValueName);
    }

    public static EnumElement newEnumValue(String comment, String enumValueName, String... params) {
        return new EnumElement(comment, enumValueName, params);
    }
}
