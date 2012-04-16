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

import org.freeciv.packetgen.javaGenerator.expression.TypedValueCode;

public class TargetPackage extends TypedValueCode {
    public TargetPackage(String wrapped) {
        super(wrapped);
    }

    @Override public String toString() {
        return getJavaCode();
    }

    public TargetPackage(Package wrapped) {
        super(wrapped.getName());
    }
}
