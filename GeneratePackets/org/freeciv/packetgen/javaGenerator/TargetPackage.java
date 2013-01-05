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

public class TargetPackage extends Address {
    public static final TargetPackage TOP_LEVEL = new TargetPackage();

    private TargetPackage() {
        super();
    }

    private TargetPackage(String parts) {
        super(parts);
    }

    public static TargetPackage from(Package wrapped) {
        if (null == wrapped) // java.lang.Class gives null as the package for primitive Java types like int
            return TOP_LEVEL;

        return from(wrapped.getName());
    }

    public static TargetPackage from(String parts) {
        String name = parts;
        if (cached.containsKey(name))
            return (TargetPackage)(cached.get(name));

        return new TargetPackage(parts);
    }
}
