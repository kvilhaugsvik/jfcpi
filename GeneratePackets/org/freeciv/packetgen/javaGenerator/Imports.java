/*
 * Copyright (c) 2013, Sveinung Kvilhaugsvik
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

import org.freeciv.packetgen.javaGenerator.expression.Import;
import org.freeciv.packetgen.javaGenerator.formating.CodeStyle;

import java.util.Arrays;
import java.util.TreeSet;

public class Imports implements HasAtoms {
    private final TreeSet<Import<?>> imports;

    Imports(Import<?>... first) {
        this.imports = new TreeSet<Import<?>>(Arrays.asList(first));
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        if (!imports.isEmpty()) {
            to.hintStart(CodeStyle.GROUP);

            Import previous = imports.first();
            for (Import anImport : imports) {
                if (!previous.sameFirstComponent(anImport)) {
                    to.hintEnd(CodeStyle.GROUP);
                    to.hintStart(CodeStyle.GROUP);
                }
                anImport.writeAtoms(to);
                previous = anImport;
            }

            to.hintEnd(CodeStyle.GROUP);
        }
    }

    void add(Import<?> toImport) {
        imports.add(toImport);
    }

    public static Imports are(Import<?>... first) {
        return new Imports(first);
    }
}
