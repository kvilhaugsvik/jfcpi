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
    private final TreeSet<Import<?>> imported;
    private final Imports imports = this;

    Imports(Import<?>... first) {
        this.imported = new TreeSet<Import<?>>(Arrays.asList(first));
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        if (!imported.isEmpty()) {
            to.hintStart(CodeStyle.GROUP);

            Import previous = imported.first();
            for (Import anImport : imported) {
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
        imported.add(toImport);
    }

    class ScopeDataForJavaFile implements HasAtoms {
        private final TargetClass on;

        private ScopeDataForJavaFile(TargetClass self) {
            this.on = self;
        }

        @Override
        public void writeAtoms(CodeAtoms to) {
            if (!TargetPackage.TOP_LEVEL.equals(on.getPackage())) {
                to.add(HasAtoms.PACKAGE);
                on.getPackage().writeAtoms(to);
                to.add(HasAtoms.EOL);
            }

            imports.writeAtoms(to);
        }
    }

    public ScopeDataForJavaFile getScopeData(TargetClass on) {
        return new ScopeDataForJavaFile(on);
    }

    public static Imports are(Import<?>... first) {
        return new Imports(first);
    }
}
