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

package com.kvilhaugsvik.javaGenerator;

import org.freeciv.utility.Util;
import com.kvilhaugsvik.javaGenerator.representation.IR.CodeAtom;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import com.kvilhaugsvik.javaGenerator.util.Formatted;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.NoValue;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.Returnable;
import com.kvilhaugsvik.javaGenerator.representation.CodeAtoms;
import com.kvilhaugsvik.javaGenerator.representation.HasAtoms;
import com.kvilhaugsvik.javaGenerator.formating.TokensToStringStyle;

import java.util.BitSet;
import java.util.LinkedList;

public class Block extends Formatted {
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

    /**
     * Get the number of statements in the block. Statements inside statements don't count.
     * @return the number of statements in the block
     */
    public int numberOfStatements() {
        return statements.size();
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        to.add(LSC);
        if (0 < statements.size()) {
            to.hintStart(TokensToStringStyle.GROUP);
            for (int i = 0; i < statements.size(); i++) {
                if (!differentGroupsAt.isEmpty() && differentGroupsAt.get(i)) {
                    // Before the first and after the last line are already grouped
                    if (0 < i) {
                        to.hintEnd(TokensToStringStyle.GROUP);
                        to.hintStart(TokensToStringStyle.GROUP);
                    }
                }
                statements.get(i).writeAtoms(to);
            }
            to.hintEnd(TokensToStringStyle.GROUP);
        }
        to.add(RSC);
        to.refuseNextIf(eolKiller);
    }
}
