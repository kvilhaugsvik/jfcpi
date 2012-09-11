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

package org.freeciv.packetgen.javaGenerator.expression;

import org.freeciv.packetgen.javaGenerator.IR.CodeAtom;
import org.freeciv.packetgen.javaGenerator.CodeAtoms;
import org.freeciv.packetgen.javaGenerator.HasAtoms;
import org.freeciv.packetgen.javaGenerator.TargetPackage;
import org.freeciv.packetgen.javaGenerator.expression.util.Formatted;

public class Import extends Formatted implements HasAtoms {
    private final HasAtoms target;

    private Import(HasAtoms target) {
        this.target = target;
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        to.add(IMPORT);
        target.writeAtoms(to);
        to.add(EOL);
    }

    public static Import allIn(TargetPackage target) {
        return new Import(target.has("*"));
    }

    public static Import classIn(Class target) {
        TargetPackage pack = new TargetPackage(target.getPackage());
        return new Import(pack.has(target.getSimpleName()));
    }
}
