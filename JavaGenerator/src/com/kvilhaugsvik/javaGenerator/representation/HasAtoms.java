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

package com.kvilhaugsvik.javaGenerator.representation;

import com.kvilhaugsvik.javaGenerator.representation.IR.CodeAtom;

public interface HasAtoms {
    CodeAtom JDocStart = new CodeAtom("/**");
    CodeAtom CCommentStart = new CodeAtom("/*");
    CodeAtom CCommentEnd = new CodeAtom("*/");
    CodeAtom EOL = new CodeAtom(";");
    CodeAtom SEP = new CodeAtom(",");
    CodeAtom LSC = new CodeAtom("{");
    CodeAtom RSC = new CodeAtom("}");
    CodeAtom LPR = new CodeAtom("(");
    CodeAtom RPR = new CodeAtom(")");
    CodeAtom ARRAY_ACCESS_START = new CodeAtom("[");
    CodeAtom ARRAY_ACCESS_END = new CodeAtom("]");
    CodeAtom GENERIC_START = new CodeAtom("<");
    CodeAtom GENERIC_END = new CodeAtom(">");
    CodeAtom HAS = new CodeAtom(".");
    CodeAtom EVERYTHING = new CodeAtom("*");
    CodeAtom ALS = new CodeAtom("{");
    CodeAtom ALE = new CodeAtom("}");
    CodeAtom ASSIGN = new CodeAtom("=");
    CodeAtom IS_SMALLER = new CodeAtom("<");
    CodeAtom IS_SMALLER_OR_EQUAL = new CodeAtom("<=");
    CodeAtom IS_BIGGER = new CodeAtom(">");
    CodeAtom IS_SAME = new CodeAtom("==");
    CodeAtom IS_NOT_SAME = new CodeAtom("!=");
    CodeAtom IS_INSTANCE_OF = new CodeAtom("instanceof");
    CodeAtom AND = new CodeAtom("&&");
    CodeAtom OR = new CodeAtom("||");
    CodeAtom NOT = new CodeAtom("!");
    CodeAtom ADD = new CodeAtom("+");
    CodeAtom SUB = new CodeAtom("-");
    CodeAtom MUL = new CodeAtom("*");
    CodeAtom DIV = new CodeAtom("/");
    CodeAtom REM = new CodeAtom("%");
    CodeAtom DEC = new CodeAtom("--");
    CodeAtom INC = new CodeAtom("++");
    CodeAtom INC_USING = new CodeAtom("+=");
    CodeAtom newInst = new CodeAtom("new");

    CodeAtom IF = new CodeAtom("if");
    CodeAtom ELSE = new CodeAtom("else");
    CodeAtom RET = new CodeAtom("return");
    CodeAtom BRE = new CodeAtom("break");
    CodeAtom RIF_THEN = new CodeAtom("?");
    CodeAtom ELSE2 = new CodeAtom(":");
    CodeAtom ASSRT = new IR.CodeAtom("assert");
    CodeAtom THR = new CodeAtom("throw");
    CodeAtom TRY = new CodeAtom("try");
    CodeAtom CATCH = new CodeAtom("catch");
    CodeAtom WHILE = new CodeAtom("while");
    CodeAtom FOR = new CodeAtom("for");
    CodeAtom FORSEP = new CodeAtom(";");
    CodeAtom FOR_EACH_SEP = new CodeAtom(":");

    CodeAtom PACKAGE = new IR.CodeAtom("package");
    CodeAtom IMPORT = new CodeAtom("import");
    CodeAtom EXTENDS = new IR.CodeAtom("extends");
    CodeAtom IMPLEMENTS = new IR.CodeAtom("implements");
    CodeAtom THROWS = new IR.CodeAtom("throws");

    CodeAtom SELF = new CodeAtom("SELF (self typed)");

    HasAtoms BLANK = new HasAtoms() {
        @Override
        public void writeAtoms(CodeAtoms to) {
        }
    };

    void writeAtoms(CodeAtoms to);
}
