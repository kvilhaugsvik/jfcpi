/*
 * Copyright (c) 2011, 2012. Sveinung Kvilhaugsvik
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

import org.freeciv.packetgen.javaGenerator.representation.CodeAtoms;
import org.freeciv.packetgen.javaGenerator.representation.HasAtoms;
import org.freeciv.packetgen.javaGenerator.representation.IR.CodeAtom;

public enum Scope implements HasAtoms {
    CLASS("static"),
    OBJECT(null),
    CODE_BLOCK(null);

    private final CodeAtom code;

    Scope(String code) {
        this.code = (null == code ? null : new CodeAtom(code));
    }

    @Override
    public String toString() {
        return (null == code ? null : code.get());
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        if (null != code)
            to.add(code);
    }
}
