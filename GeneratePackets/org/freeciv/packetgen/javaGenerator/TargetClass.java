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

import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;

public class TargetClass extends Address {
    private final CodeAtom name;

    public TargetClass(String fullPath) {
        super(fullPath.split("\\."));
        name = super.components[super.components.length - 1];
    }

    public TargetClass(Class wrapped) {
        this(wrapped.getCanonicalName());
    }

    // TODO: Should this be seen as a function called on the type?
    private final static CodeAtom typeClassField = new CodeAtom("class");
    public AValue classVal() {
        final TargetClass parent = this;
        return new FormattedAValue() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                parent.writeAtoms(to);
                to.add(HAS);
                to.add(typeClassField);
            }
        };
    }

    private final static CodeAtom newInst = new CodeAtom("new");
    public MethodCall.RetAValue newInstance(AValue... parameterList) {
        return new MethodCall.RetAValue(null, "new " + name.get(), parameterList) {
            @Override
            public void writeAtoms(CodeAtoms to) {
                to.add(newInst);
                to.add(name);
                to.add(LPR);
                to.joinSep(SEP, parameters);
                to.add(RPR);
            }
        };
    }
}
