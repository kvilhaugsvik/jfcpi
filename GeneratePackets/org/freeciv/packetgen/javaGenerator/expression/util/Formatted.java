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
import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.*;
import org.freeciv.packetgen.javaGenerator.CodeAtoms;

public abstract class Formatted implements HasAtoms {
    public String getJavaCodeIndented(String start) {
        CodeAtoms out = new CodeAtoms(this);
        return Util.joinStringArray(ClassWriter.DEFAULT_STYLE_INDENT.asFormattedLines(out).toArray(new String[0]),
                "\n", "", "");
    }

    public String toString() {
        return getJavaCodeIndented("");
    }

    protected String[] basicFormatBlock() {
        CodeAtoms out = new CodeAtoms();
        writeAtoms(out);
        return ClassWriter.DEFAULT_STYLE.asFormattedLines(out).toArray(new String[0]);
    }

    public static abstract class FormattedReturnable extends Formatted implements Typed<Returnable> {}
    static abstract class FormattedVoid extends Formatted implements Typed<NoValue> {}
    public static abstract class FormattedAValue extends Formatted implements Typed<AValue> {}
    static abstract class FormattedString extends Formatted implements Typed<AString> {}
    public static abstract class FormattedBool extends Formatted implements Typed<ABool> {}
}
