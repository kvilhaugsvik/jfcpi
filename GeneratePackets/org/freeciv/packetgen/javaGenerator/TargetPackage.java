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

import java.util.NoSuchElementException;

public class TargetPackage extends Address<TargetPackage> {
    public static final TargetPackage TOP_LEVEL = new TargetPackage();
    public static final String TOP_LEVEL_AS_STRING = "";

    private TargetPackage() {
        super();
    }

    private TargetPackage(String parts) {
        super(TOP_LEVEL, addressString2Components(parts));
    }

    public static TargetPackage from(Package wrapped) {
        if (null == wrapped) // java.lang.Class gives null as the package for primitive Java types like int
            return TOP_LEVEL;

        return from(wrapped.getName());
    }

    public static TargetPackage from(String parts) {
        if (TOP_LEVEL_AS_STRING.equals(parts))
            return TOP_LEVEL;

        try {
            return getExisting(parts, TargetPackage.class);
        } catch (NoSuchElementException e) {
            return new TargetPackage(parts);
        }
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        to.hintStart(TargetPackage.class.getCanonicalName());
        super.writeAtoms(to);
        to.hintEnd(TargetPackage.class.getCanonicalName());
    }
}
