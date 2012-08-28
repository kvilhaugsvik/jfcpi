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

import org.freeciv.Util;
import org.freeciv.packetgen.javaGenerator.expression.util.Formatted;
import org.freeciv.packetgen.javaGenerator.IR.CodeAtom;

import java.util.*;

public class Address extends Formatted implements HasAtoms {
    protected final CodeAtom[] components;

    public Address(String... parts) {
        ArrayList<CodeAtom> build = new ArrayList<CodeAtom>(parts.length);
        for (String part : parts)
            build.add(new CodeAtom(part));
        components = build.toArray(new CodeAtom[build.size()]);
    }

    public Address(Address start, CodeAtom... parts) {
        this.components = new CodeAtom[start.components.length + parts.length];
        System.arraycopy(start.components, 0, this.components, 0, start.components.length);
        System.arraycopy(parts, 0, this.components, start.components.length, parts.length);
    }

    public String getFullAddress() {
        return Util.joinStringArray(components, ".", "", "");
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        to.joinSep(HAS, components);
    }
}
