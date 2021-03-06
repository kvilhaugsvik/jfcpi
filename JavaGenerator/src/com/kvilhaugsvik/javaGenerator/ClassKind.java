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

package com.kvilhaugsvik.javaGenerator;

import com.kvilhaugsvik.javaGenerator.representation.CodeAtoms;
import com.kvilhaugsvik.javaGenerator.representation.HasAtoms;
import com.kvilhaugsvik.javaGenerator.representation.IR.CodeAtom;

/**
 * A Java class kind
 */
public enum ClassKind implements HasAtoms {
    CLASS,
    ABSTRACT_CLASS,
    ENUM,
    INTERFACE;

    private final CodeAtom code;

    ClassKind() {
        this.code = new ClassKind.Atom(name().toLowerCase().replace('_', ' '));
    }

    @Override
    public String toString() {
        return code.get();
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        to.add(code);
    }

    public static class Atom extends CodeAtom {
        public Atom(String atom) {
            super(atom);
        }
    }
}
