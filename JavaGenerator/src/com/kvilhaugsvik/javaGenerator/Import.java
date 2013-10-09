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

import com.kvilhaugsvik.javaGenerator.util.Formatted;
import com.kvilhaugsvik.javaGenerator.representation.CodeAtoms;
import com.kvilhaugsvik.javaGenerator.representation.HasAtoms;

public class Import<Of extends Address> extends Formatted implements HasAtoms, Comparable<Import<?>> {
    private final Of target;
    private final boolean allIn;

    private Import(Of target, boolean allIn) {
        this.target = target;
        this.allIn = allIn;
    }

    public boolean sameFirstComponent(Import other) {
        return target.getFirstComponent().toString().equals(other.target.getFirstComponent().toString());
    }

    public Of getTarget() {
        return target;
    }

    public boolean isAllIn() {
        return allIn;
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

    @Override
    public int compareTo(Import<?> other) {
        int compared = target.getFullAddress().compareTo(other.target.getFullAddress());

        if (0 == compared && allIn != other.allIn)
            throw new UnsupportedOperationException("Comparing the import of an address and the import of all in it");

        return compared;
    }

    public static Import<TargetPackage> allIn(Package target) {
        return allIn(TargetPackage.from(target));
    }

    public static Import<TargetPackage> allIn(TargetPackage target) {
        return new Import<TargetPackage>(target, true);
    }

    public static Import<TargetClass> classIn(Class target) {
        return classIn(TargetClass.from(target));
    }

    public static Import<TargetClass> classIn(TargetClass target) {
        return new Import<TargetClass>(target, false);
    }
}
