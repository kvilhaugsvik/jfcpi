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

import org.freeciv.packetgen.javaGenerator.IR.CodeAtom;

public interface HasAtoms {
    public static final CodeAtom CCommentStart = new CodeAtom("/*");
    public static final CodeAtom CCommentEnd = new CodeAtom("*/");
    public static final CodeAtom EOL = new CodeAtom(";");
    public static final CodeAtom SEP = new CodeAtom(",");
    public static final CodeAtom LSC = new CodeAtom("{");
    public static final CodeAtom RSC = new CodeAtom("}");
    public static final CodeAtom LPR = new CodeAtom("(");
    public static final CodeAtom RPR = new CodeAtom(")");
    public static final CodeAtom ARRAY_ACCESS_START = new CodeAtom("[");
    public static final CodeAtom ARRAY_ACCESS_END = new CodeAtom("]");
    public static final CodeAtom HAS = new CodeAtom(".");
    public static final CodeAtom ALS = new CodeAtom("{");
    public static final CodeAtom ALE = new CodeAtom("}");
    public static final CodeAtom ASSIGN = new CodeAtom("=");
    public static final CodeAtom IS_SMALLER = new CodeAtom("<");
    public static final CodeAtom ADD = new CodeAtom("+");
    public static final CodeAtom MUL = new CodeAtom("*");
    public static final CodeAtom DIV = new CodeAtom("/");
    public static final CodeAtom INC = new CodeAtom("++");

    public static final CodeAtom IF = new CodeAtom("if");
    public static final CodeAtom ELSE = new CodeAtom("else");
    public static final CodeAtom RET = new CodeAtom("return");
    public static final CodeAtom RIF_THEN = new CodeAtom("?");
    public static final CodeAtom ELSE2 = new CodeAtom(":");
    public static final CodeAtom ASSRT = new IR.CodeAtom("assert");
    public static final CodeAtom THR = new CodeAtom("throw");
    public static final CodeAtom WHILE = new CodeAtom("while");
    public static final CodeAtom FOR = new CodeAtom("for");
    public static final CodeAtom FORSEP = new CodeAtom(";");
    public static final CodeAtom FOR_EACH_SEP = new CodeAtom(":");

    public void writeAtoms(CodeAtoms to);
}
