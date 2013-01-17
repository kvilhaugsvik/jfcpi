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
import org.freeciv.packetgen.javaGenerator.formating.TokensToStringStyle;
import org.freeciv.packetgen.javaGenerator.representation.CodeAtoms;
import org.freeciv.packetgen.javaGenerator.representation.HasAtoms;
import org.freeciv.packetgen.javaGenerator.representation.IR;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
            to.hintStart(TokensToStringStyle.GROUP);

            Import previous = imported.first();
            for (Import anImport : imported) {
                if (!previous.sameFirstComponent(anImport)) {
                    to.hintEnd(TokensToStringStyle.GROUP);
                    to.hintStart(TokensToStringStyle.GROUP);
                }
                anImport.writeAtoms(to);
                previous = anImport;
            }

            to.hintEnd(TokensToStringStyle.GROUP);
        }
    }

    void add(Import<?> toImport) {
        imported.add(toImport);
    }

    class ScopeDataForJavaFile implements HasAtoms {
        private final TargetClass on;
        private final HashSet<String> alone;
        private final HashSet<String> allIn;

        private ScopeDataForJavaFile(TargetClass self) {
            this.on = self;
            this.alone = new HashSet<String>();
            this.allIn = new HashSet<String>();

            allIn.add("java.lang"); // always in scope in Java
            allIn.add(self.getPackage().getFullAddress());

            for (Import<?> elem : imported)
                if (elem.isAllIn())
                    allIn.add(elem.getTarget().getFullAddress());
                else
                    alone.add(elem.getTarget().getFullAddress());
        }

        public boolean isInScope(IR[] components) {
            final List<String> lastHints = components[components.length - 1].getHintsEnd();
            if (lastHints.contains(TargetClass.class.getCanonicalName()))
                return isClassInScope(components);

            throw new IllegalArgumentException("No understood end hint");
        }

        private boolean isClassInScope(IR[] components) {
            validateStart(components, TargetClass.class);

            return alone.contains(IR.joinSqueeze(components)) ||
                    (hasSubAddress(components, TargetPackage.class) &&
                            allIn.contains(IR.joinSqueeze(getSubAddress(components, TargetPackage.class))));
        }

        private void validateStart(IR[] components, Class<? extends Address> addressKind) {
            if (!hasSubAddress(components, addressKind))
                throw new IllegalArgumentException("No beginning of " + addressKind.getCanonicalName());
        }

        private boolean hasSubAddress(IR[] components, Class<? extends Address> addressKind) {
            return components[0].getHintsBegin().contains(addressKind.getCanonicalName());
        }

        private IR[] getSubAddress(IR[] components, Class<? extends Address> addressKind) {
            return IR.cutByHint(components, 0, addressKind.getCanonicalName());
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
