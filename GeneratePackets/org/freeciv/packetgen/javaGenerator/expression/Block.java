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

package org.freeciv.packetgen.javaGenerator.expression;

import org.freeciv.Util;
import org.freeciv.packetgen.javaGenerator.IR;
import org.freeciv.packetgen.javaGenerator.IR.CodeAtom;
import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.util.Formatted;
import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.*;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.NoValue;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.Returnable;
import org.freeciv.packetgen.javaGenerator.CodeAtoms;
import org.freeciv.packetgen.javaGenerator.HasAtoms;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedList;

public class Block extends Formatted implements Typed<NoValue> {
    private static final Util.OneCondition<CodeAtom> eolKiller = new Util.OneCondition<CodeAtom>() {
        @Override
        public boolean isTrueFor(CodeAtom argument) {
            return HasAtoms.EOL.equals(argument);
        }
    };

    private final LinkedList<Statement> statements = new LinkedList<Statement>();
    private final BitSet differentGroupsAt = new BitSet();

    public Block(Typed<? extends Returnable>... firstStatements) {
        for (Typed<? extends Returnable> statement : firstStatements)
            statements.add(new Statement(statement));
    }

    public void addStatement(Statement statement) {
        statements.add(statement);
    }

    public void addStatement(Typed<? extends Returnable> statement) {
        statements.add(new Statement(statement));
    }

    public void groupBoundary() {
        differentGroupsAt.set(statements.size());
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        to.add(LSC);
        to.hintStart("Group");
        for (int i = 0; i < statements.size(); i++) {
            if (!differentGroupsAt.isEmpty() && differentGroupsAt.get(i)) {
                // Before the first and after the last line are already grouped
                if (0 < i) {
                    to.hintEnd("Group");
                    to.hintStart("Group");
                }
            }
            statements.get(i).writeAtoms(to);
        }
        to.hintEnd("Group");
        to.add(RSC);
        to.refuseNextIf(eolKiller);
    }

    @Deprecated public static Block fromStrings(String... firstStatements) {
        Block out = new Block();
        for (String statement : firstStatements)
            out.addStatement(asVoid(statement));
        return out;
    }
}
