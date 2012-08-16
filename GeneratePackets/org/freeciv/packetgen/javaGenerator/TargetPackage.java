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
    public TargetPackage(String... parts) {
        super(parts);
    }

    public TargetPackage(Package wrapped) {
        super(wrapped.getName().split("\\."));
    }

    public Address has(final String element) {
        return new Address(this, new CodeAtom(element));
    }
}
