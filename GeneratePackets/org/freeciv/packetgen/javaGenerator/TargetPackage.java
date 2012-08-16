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

import org.freeciv.packetgen.javaGenerator.expression.util.Formatted;

public class TargetPackage extends Formatted implements HasAtoms {
    private final CodeAtom[] components;

    public TargetPackage(String... parts) {
        components = new CodeAtom[parts.length];
        for (int i = 0; i < parts.length; i++) {
            components[i] = new CodeAtom(parts[i]);
        }
    }

    public TargetPackage(Package wrapped) {
        this(wrapped.getName().split("\\."));
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        to.joinSep(HAS, components);
    }
}
