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

public enum Visibility implements HasAtoms {
    PUBLIC(false),
    PACKAGE(true),
    PROTECTED(false),
    PRIVATE(false);

    private final CodeAtom code;

    Visibility(boolean silent) {
        this.code = (silent ? null : new Visibility.Atom(name().toLowerCase()));
    }

    @Override
    public String toString() {
        return (null == code ? null : code.get());
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        if (null != code)
            to.add(code);
    }

    public static class Atom extends CodeAtom {
        public Atom(String atom) {
            super(atom);
        }
    }
}
