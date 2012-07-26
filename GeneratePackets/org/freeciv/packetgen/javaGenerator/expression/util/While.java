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
import org.freeciv.packetgen.javaGenerator.CodeAtom;
import org.freeciv.packetgen.javaGenerator.CodeAtoms;
import org.freeciv.packetgen.javaGenerator.HasAtoms;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.creators.ExprFrom2;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.ABool;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.NoValue;

class While implements ExprFrom2<NoValue, ABool, Block> {
    @Override
    public NoValue x(final ABool cond, final Block rep) {
        return new Formatted.FormattedVoid() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                to.add(WHILE);
                to.add(LPR);
                cond.writeAtoms(to);
                to.add(RPR);
                rep.writeAtoms(to);
            }
        };
    }
}
