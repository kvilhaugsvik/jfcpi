/*
 * Copyright (c) 2012. Sveinung Kvilhaugsvik
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

package org.freeciv.packetgen;

public class Requirement {
    private final String name;
    private final Kind kind;

    private boolean fulfilled;

    public Requirement(String name, Kind kind) {
        this.name = name;
        this.kind = kind;
    }

    public void setFulfilled() {
        fulfilled = true;
    }

    public boolean isFulfilled() {
        return fulfilled;
    }

    public Kind getKind() {
        return kind;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object that) {
        if (that instanceof Requirement)
            return name.equals(((Requirement)that).getName()) &&
                    kind.equals(((Requirement)that).getKind());
        else
            return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + kind.ordinal();
    }

    public enum Kind {
        ENUM,
        FIELD_TYPE
    }
}
