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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class IR {
    private final CodeAtom atom;
    private final List<String> hintsBegin;
    private final List<String> hintsEnd;

    public IR(CodeAtom atom) {
        this.atom = atom;
        this.hintsBegin = new LinkedList<String>();
        this.hintsEnd = new LinkedList<String>();
    }

    public CodeAtom getAtom() {
        return atom;
    }

    public List<String> getHintsBegin() {
        return Collections.unmodifiableList(hintsBegin);
    }

    public List<String> getHintsEnd() {
        return Collections.unmodifiableList(hintsEnd);
    }

    public void hintBegin(String hint) {
        hintsBegin.add(hint);
    }

    public void hintEnd(String hint) {
        hintsEnd.add(hint);
    }

    public String toString() {
        StringBuilder out = new StringBuilder("IR[");
        for (String hint : hintsBegin) {
            out.append(hint);
            out.append(" ");
        }
        out.append(":");
        out.append(atom.get());
        out.append(":");
        for (String hint : hintsEnd) {
            out.append(" ");
            out.append(hint);
        }
        out.append("]");
        return out.toString();
    }

    /**
     * Joint the atoms of the elements without space between or other formatting
     * @param irs an array containing the IR
     * @return the atoms of the elements of irs without space between or other formatting
     */
    public static String joinSqueeze(IR[] irs) {
        StringBuilder out = new StringBuilder();

        for (IR a : irs)
            out.append(a.getAtom().get());

        return out.toString();
    }

    public static IR[] cutByHint(final IR[] components, final int at, final String hint) {
        if (!components[at].getHintsBegin().contains(hint))
            throw new IllegalArgumentException(hint + " didn't begin at " + at);

        int levels = 0;
        for (int i = at; i < components.length; i++) {
            for (String considered : components[i].getHintsBegin())
                if (considered.equals(hint))
                    levels++;
            for (String considered : components[i].getHintsEnd())
                if (considered.equals(hint))
                    levels--;
            if (levels < 1)
                return Arrays.copyOfRange(components, at, i + 1);
        }

        throw new IllegalArgumentException("No end");
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
