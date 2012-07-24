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
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.creators.*;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.*;
import org.freeciv.packetgen.javaGenerator.CodeAtom;
import org.freeciv.packetgen.javaGenerator.CodeAtoms;
import org.freeciv.packetgen.javaGenerator.HasAtoms;

class If implements From2or3<NoValue, ABool, Block, Block>, ExprFrom2<NoValue, ABool, Block> {
    private static final Util.OneCondition<CodeAtom> eolKiller = new Util.OneCondition<CodeAtom>() {
        @Override
        public boolean isTrueFor(CodeAtom argument) {
            return HasAtoms.EOL.equals(argument);
        }
    };

    @Override
    public NoValue getCodeFor(ABool cond, Block then) {
        return getCodeFor(cond, then, null);
    }

    @Override
    public NoValue getCodeFor(final ABool cond, final Block then, final Block ifNot) {
        return new Formatted() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                to.add(IF);
                to.add(LPR);
                cond.writeAtoms(to);
                to.add(RPR);
                to.add(LSC);
                then.writeAtoms(to);
                to.add(RSC);
                if (null != ifNot) {
                    to.add(ELSE);
                    to.add(LSC);
                    ifNot.writeAtoms(to);
                    to.add(RSC);
                }
                to.refuseNextIf(eolKiller);
            }
        };
    }
}
