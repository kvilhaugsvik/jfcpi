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

import org.freeciv.Util;
import org.freeciv.packetgen.javaGenerator.ClassWriter;
import org.freeciv.packetgen.javaGenerator.HasAtoms;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.*;
import org.freeciv.packetgen.javaGenerator.CodeAtoms;

public abstract class Formatted implements HasAtoms {
    public String getJavaCodeIndented(String start) {
        return ClassWriter.indent(basicFormatBlock(), start);
    }

    public String getJavaCode() {
        return Util.joinStringArray(basicFormatBlock(), "\n", "", "");
    }

    protected String[] basicFormatBlock() {
        CodeAtoms out = new CodeAtoms();
        writeAtoms(out);
        return ClassWriter.DEFAULT_STYLE.asFormattedLines(out).toArray(new String[0]);
    }

    public static abstract class FormattedReturnable extends Formatted implements Returnable {}
    static abstract class FormattedVoid extends Formatted implements NoValue {}
    public static abstract class FormattedAValue extends Formatted implements AValue {}
    static abstract class FormattedString extends Formatted implements AString {}
}
