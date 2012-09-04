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

import java.util.List;

public abstract class Formatted implements HasAtoms {
    public String getJavaCodeIndented(String start) {
        List<String> lines = ClassWriter.DEFAULT_STYLE_INDENT.asFormattedLines(new CodeAtoms(this));
        if (0 == lines.size())
            return "";
        StringBuilder out = new StringBuilder(start);
        out.append(lines.get(0));
        for (int i = 1; i < lines.size(); i++) {
            out.append("\n");
            String line = lines.get(i);
            if (!"".equals(line)) {
                out.append(start);
                out.append(line);
            }
        }
        return out.toString();
    }

    public String toString() {
        return getJavaCodeIndented("");
    }

    public static abstract class Type<Return extends Returnable> extends Formatted implements Typed<Return> {}
}
