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

package org.freeciv.packetgen.javaGenerator.expression;

import org.freeciv.packetgen.javaGenerator.*;
import org.freeciv.packetgen.javaGenerator.expression.util.Formatted;

public class Import<Of extends Address> extends Formatted implements HasAtoms {
    private final Of target;
    private final boolean allIn;

    private Import(Of target, boolean allIn) {
        this.target = target;
        this.allIn = allIn;
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        to.add(IMPORT);
        target.writeAtoms(to);
        if (allIn) {
            to.add(HAS);
            to.add(EVERYTHING);
        }
        to.add(EOL);
    }

    public static Import<TargetPackage> allIn(Package target) {
        return allIn(TargetPackage.from(target));
    }

    public static Import<TargetPackage> allIn(TargetPackage target) {
        return new Import<TargetPackage>(target, true);
    }

    public static Import<TargetClass> classIn(Class target) {
        return classIn(new TargetClass(target));
    }

    public static Import<TargetClass> classIn(TargetClass target) {
        return new Import<TargetClass>(target, false);
    }
}
