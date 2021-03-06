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

package com.kvilhaugsvik.javaGenerator.util;

import com.kvilhaugsvik.javaGenerator.DefaultStyle;
import com.kvilhaugsvik.javaGenerator.formating.TokensToStringStyle;
import com.kvilhaugsvik.javaGenerator.representation.HasAtoms;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.*;
import com.kvilhaugsvik.javaGenerator.representation.CodeAtoms;

import java.util.List;

public abstract class Formatted implements HasAtoms {
    public String getJavaCodeIndented(String start, TokensToStringStyle style) {
        List<String> lines = style.asFormattedLines(new CodeAtoms(this));
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

    public String toString(TokensToStringStyle style) {
        return getJavaCodeIndented("", style);
    }

    public String toString() {
        return toString(DefaultStyle.DEFAULT_STYLE_INDENT);
    }

    public static abstract class Type<Return extends Returnable> extends Formatted implements Typed<Return> {}
}
