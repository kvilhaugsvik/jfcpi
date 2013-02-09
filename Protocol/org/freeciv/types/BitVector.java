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

package org.freeciv.types;

import static org.freeciv.Util.*;

import java.util.Arrays;

public abstract class BitVector {
    protected final boolean[] vec;

    private BitVector(final int size) {
        this.vec = new boolean[size];
    }

    protected BitVector(final int sizeInBits, final byte[] src) {
        this(sizeInBits);
        for (int pos = 0; pos < sizeInBits; pos++)
            vec[pos] = 0 != (src[isInByteNumber(pos)] & (1 << isBitNumberInAByte(pos)));
    }

    protected BitVector(final int size, final boolean setAllTo) {
        this(size);
        for (int toSet = 0; toSet < size; toSet++) {
            vec[toSet] = setAllTo;
        }
    }

    protected BitVector(final boolean[] normal) {
        vec = normal.clone();
    }

    private int isBitNumberInAByte(final int pos) {
        return pos % 8;
    }

    private int isInByteNumber(final int pos) {
        return pos / 8;
    }

    public boolean get(final int bitNumber) {
        return vec[bitNumber];
    }

    // TODO: Move to subclass so this can be made imputable
    public void set(final int bitNumber, final boolean value) {
        vec[bitNumber] = value;
    }

    public byte[] getAsByteArray() {
        byte[] out = new byte[1 + isInByteNumber((vec.length - 1))];
        Arrays.fill(out, (byte)0);
        for (int bit = 0; bit < vec.length; bit++) {
            if (vec[bit])
                out[isInByteNumber(bit)] += 1 << isBitNumberInAByte(bit);
        }
        return out;
    }

    public boolean[] getBits() {
        return vec.clone();
    }

    @Override
    public String toString() {
        return joinStringArray(vec, ", ");
    }
}
