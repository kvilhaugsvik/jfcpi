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

package org.freeciv.packetgen.javaGenerator;

public interface HasAtoms {
    public static final CodeAtom EOL = new CodeAtom(";");
    public static final CodeAtom LSC = new CodeAtom("{");
    public static final CodeAtom RSC = new CodeAtom("}");
    public static final CodeAtom LPR = new CodeAtom("(");
    public static final CodeAtom RPR = new CodeAtom(")");
    public static final CodeAtom ASSIGN = new CodeAtom("=");
    public static final CodeAtom ADD = new CodeAtom("+");

    public static final CodeAtom IF = new CodeAtom("if");
    public static final CodeAtom ELSE = new CodeAtom("else");
    public static final CodeAtom RET = new CodeAtom("return");

    public void writeAtoms(CodeAtoms to);
}
