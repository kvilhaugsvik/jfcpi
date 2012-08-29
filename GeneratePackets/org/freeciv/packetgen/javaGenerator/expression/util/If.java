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
import org.freeciv.packetgen.javaGenerator.IR.CodeAtom;
import org.freeciv.packetgen.javaGenerator.CodeAtoms;
import org.freeciv.packetgen.javaGenerator.HasAtoms;

class If implements From2or3<Typed<NoValue>, Typed<ABool>, Block, Block>, ExprFrom2<Typed<NoValue>, Typed<ABool>, Block> {
    @Override
    public Typed<NoValue> x(Typed<ABool> cond, Block then) {
        return x(cond, then, null);
    }

    @Override
    public Typed<NoValue> x(final Typed<ABool> cond, final Block then, final Block ifNot) {
        return new Formatted.Type<NoValue>() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                to.add(IF);
                to.add(LPR);
                cond.writeAtoms(to);
                to.add(RPR);
                then.writeAtoms(to);
                if (null != ifNot) {
                    to.add(ELSE);
                    ifNot.writeAtoms(to);
                }
            }
        };
    }
}
