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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class IR {
    private final CodeAtom atom;
    private final List<Hint> hintsBefore;
    private final List<Hint> hintsAfter;

    public IR(CodeAtom atom) {
        this.atom = atom;
        this.hintsBefore = new LinkedList<Hint>();
        this.hintsAfter = new LinkedList<Hint>();
    }

    public CodeAtom getAtom() {
        return atom;
    }

    public List<Hint> getHintsBefore() {
        return Collections.unmodifiableList(hintsBefore);
    }

    public List<Hint> getHintsAfter() {
        return Collections.unmodifiableList(hintsAfter);
    }

    public void addHint(Hint hint) {
        if (hint.isStart)
            hintsBefore.add(hint);
        else
            hintsAfter.add(hint);
    }

    public String toString() {
        StringBuilder out = new StringBuilder("IR[");
        for (Hint hint : hintsBefore) {
            out.append(hint.get());
            out.append(" ");
        }
        out.append(":");
        out.append(atom.get());
        out.append(":");
        for (Hint hint : hintsAfter) {
            out.append(" ");
            out.append(hint.get());
        }
        out.append("]");
        return out.toString();
    }


    public static class Hint {
        private final boolean isStart;
        private final String hint;

        private Hint(String hint, boolean isStart) {
            this.isStart = isStart;
            this.hint = hint;
        }


        public static Hint begin(String hint) {
            return new Hint(hint, true);
        }

        public static Hint end(String hint) {
            return new Hint(hint, false);
        }

        public String get() {
            return hint;
        }
    }

    public static class CodeAtom implements HasAtoms {
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

        public String toString() {
            return atom;
        }
    }
}
