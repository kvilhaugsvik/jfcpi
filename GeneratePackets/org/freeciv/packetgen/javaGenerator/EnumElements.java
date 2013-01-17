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

import java.util.*;

public class EnumElements extends Formatted implements HasAtoms {
    private final LinkedHashMap<String, EnumElement> enumerations = new LinkedHashMap<String, EnumElement>();

    public void add(EnumElement element) {
        enumerations.put(element.getEnumValueName(), element);
    }

    public EnumElement getElement(String name) {
        return enumerations.get(name);
    }

    public Set<String> getElementNames() {
        return Collections.unmodifiableSet(enumerations.keySet());
    }

    public boolean isEmpty() {
        return enumerations.isEmpty();
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        if (!enumerations.isEmpty()) {
            to.hintStart(EnumElements.class.getName());
            to.joinSep(SEP, enumerations.values());
            to.add(EOL);
            to.hintEnd(EnumElements.class.getName());
        }
    }
}
