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

public interface IR {

    public static class CodeAtom implements HasAtoms, IR {
        private final String atom;

        public CodeAtom(String atom) {
            this.atom = atom;
        }

        public String get() {
            return atom;
        }

        @Override
        public void writeAtoms(CodeAtoms to) {
            to.add(this);
        }
    }
}
