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

import org.freeciv.Util;

import java.util.LinkedList;

public class CodeAtoms {
    private final LinkedList<CodeAtom> atoms = new LinkedList<CodeAtom>();
    private Util.OneCondition<CodeAtom> reason = null;

    public void add(CodeAtom atom) {
        if (null == reason || !reason.isTrueFor(atom))
            atoms.add(atom);
        reason = null;
    }

    public void refuseNextIf(Util.OneCondition<CodeAtom> reason) {
        this.reason = reason;
    }

    public CodeAtom[] getAtoms() {
        return atoms.toArray(new CodeAtom[0]);
    }
}
