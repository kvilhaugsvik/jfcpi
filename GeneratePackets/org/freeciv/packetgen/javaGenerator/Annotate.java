/*
 * Copyright (c) 2012, Sveinung Kvilhaugsvik
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

import org.freeciv.packetgen.javaGenerator.util.Formatted;
import org.freeciv.packetgen.javaGenerator.representation.CodeAtoms;
import org.freeciv.packetgen.javaGenerator.representation.HasAtoms;
import org.freeciv.packetgen.javaGenerator.representation.IR.CodeAtom;

public class Annotate extends Formatted implements HasAtoms {
    private final String annotation;
    private final Var.SetTo[] arguments;

    public Annotate(String annotation, Var.SetTo... arguments) {
        this.annotation = annotation;
        this.arguments = arguments;
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        to.add(new Annotate.Atom(annotation));
        if (0 < arguments.length) {
            to.add(LPR);
            to.joinSep(SEP, arguments);
            to.add(RPR);
        }
    }

    public static class Atom extends CodeAtom {
        public Atom(String atom) {
            super("@" + atom);
        }
    }
}
