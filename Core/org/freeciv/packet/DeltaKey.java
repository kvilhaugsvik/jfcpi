/*
 * Copyright (c) 2013. Sveinung Kvilhaugsvik
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

package org.freeciv.packet;

import org.freeciv.packet.fieldtype.FieldType;

/**
 * DeltaKey - use as key in a HashMap to find the previous packet of the same kind where all key fields are equal.
 */
public class DeltaKey {
    private final int packetType;
    private final FieldType<?>[] keys;

    private final int hashCode;

    public DeltaKey(int packetType, FieldType<?>... keys) {
        this.packetType = packetType;
        this.keys = keys;

        this.hashCode = hashCodeCalc();
    }

    @Override
    public boolean equals(Object to) {
        if (!(to instanceof DeltaKey))
            return false;

        DeltaKey other = (DeltaKey) to;

        if (other.packetType != this.packetType || other.keys.length != this.keys.length)
            return false;

        for (int i = 0; i < keys.length; i++)
            if (!keys[i].equals(other.keys[i]))
                return false;

        return true;
    }

    public int hashCodeCalc() {
        int out = packetType << 16;

        for (FieldType<?> key : keys)
            out += key.getValue().hashCode();

        return out;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
