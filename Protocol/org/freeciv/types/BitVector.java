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
    protected final int sizeInBits;

    private BitVector(int size) {
        this.sizeInBits = size;
        this.vec = new boolean[size];
    }

    protected BitVector(int sizeInBits, byte[] src) {
        this(sizeInBits);
        for (int pos = 0; pos < sizeInBits; sizeInBits++)
            vec[pos] = 0 != (src[isInByteNumber(pos)] & (1 << isBitNumberInAByte(pos)));
    }

    protected BitVector(int size, boolean setAllTo) {
        this(size);
        for (int toSet = 0; toSet < size; toSet++) {
            vec[toSet] = setAllTo;
        }
    }

    protected BitVector(boolean[] normal) {
        vec = normal.clone();
        sizeInBits = normal.length;
    }

    private int isBitNumberInAByte(int pos) {
        return pos % 8;
    }

    private int isInByteNumber(int pos) {
        return pos / 8;
    }

    public boolean get(int bitNumber) {
        return vec[bitNumber];
    }

    // TODO: Move to subclass so this can be made imputable
    public void set(int bitNumber, boolean value) {
        vec[bitNumber] = value;
    }

    public byte[] getAsByteArray() {
        byte[] out = new byte[1 + isInByteNumber((sizeInBits - 1))];
        Arrays.fill(out, (byte)0);
        for (int bit = 0; bit < sizeInBits; bit++) {
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
        String[] vecAsText = new String[vec.length];
        for (int index = 0; index < vec.length; index++) {
            vecAsText[index] = boolToStr(vec[index]);
        }

        return joinStringArray(vecAsText, ", ", "(", ")");
    }

    private static String boolToStr(boolean toConvert) {
        return (toConvert ? "1" : "0");
    }
}
