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

package com.kvilhaugsvik.javaGenerator;

import com.kvilhaugsvik.javaGenerator.expression.Reference;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.util.Formatted;
import com.kvilhaugsvik.javaGenerator.representation.CodeAtoms;
import com.kvilhaugsvik.javaGenerator.representation.HasAtoms;
import com.kvilhaugsvik.javaGenerator.representation.IR.CodeAtom;

import java.util.NoSuchElementException;

public class Annotate extends Formatted implements HasAtoms {
    private final TargetClass annotation;
    private final Reference.SetTo[] arguments;

    public Annotate(Class annotation, Reference.SetTo... arguments) {
        this(TargetClass.from(annotation), arguments);
    }

    public Annotate(TargetClass annotation, Reference.SetTo... arguments) {
        this.annotation = annotation;
        this.arguments = arguments;
    }

    public boolean sameClass(TargetClass other) {
        return annotation.equals(other);
    }

    public Typed<?> getValueOf(String name) {
        for (Reference.SetTo var : arguments)
            if (var.getReferName().toString().equals(name))
                return var.getValue();

        throw new NoSuchElementException(name + " not set in annotation " + this);
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        to.add(new Annotate.Atom(annotation));
        if (0 < arguments.length) {
            to.add(LPR);
            to.joinSep(SEP, arguments);
            to.add(RPR);
        }
    }

    public static class Atom extends CodeAtom {
        public Atom(TargetClass annotation) {
            // It is not a real atom. TODO: Find a better solution that still permits formatting after an atom.
            this(annotation.getFullAddress());
        }

        public Atom(String atom) {
            super("@" + atom);
        }
    }
}
