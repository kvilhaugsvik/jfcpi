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

import static org.freeciv.utility.Util.*;

import java.util.Arrays;

/**
 * A BitVector is a list of binary values.
 */
public class BitVector {
    protected final boolean[] vec;

    /**
     * Create a new zeroed BitVector of the wanted size.
     * @param size the size of the BitVector to create.
     */
    public BitVector(final int size) {
        this.vec = new boolean[size];
    }

    /**
     * Interpret the given data and create a new BitVector of the wanted size from them. See {@see #getAsByteArray()}.
     * @param sizeInBits the wanted size of the new BitVector.
     * @param src the data to fill the new BitVector.
     */
    public BitVector(final int sizeInBits, final byte[] src) {
        this(sizeInBits);

        if (src.length < bytesNeededToHoldBits(vec.length))
            throw new IllegalArgumentException("Not enough data to get " + sizeInBits + " bits");

        for (int pos = 0; pos < sizeInBits; pos++)
            vec[pos] = 0 != (src[isInByteNumber(pos)] & (1 << isBitNumberInAByte(pos)));
    }

    /**
     * Create a new BitVector with the same length and data as the given boolean array.
     * @param normal the boolean array to copy.
     */
    protected BitVector(final boolean[] normal) {
        vec = normal.clone();
    }

    private int isBitNumberInAByte(final int pos) {
        return pos % 8;
    }

    private static int isInByteNumber(final int pos) {
        return pos / 8;
    }

    /**
     * Get the value of a specific bit.
     * @param bitNumber the position of the bit to get.
     * @return true if the bit is set, false if it isn't set.
     */
    public boolean get(final int bitNumber) {
        return vec[bitNumber];
    }

    /**
     * Set the value of a specific bit.
     * @param bitNumber the position of the bit to set.
     * @param value the value to set it to.
     */
    // TODO: Move to subclass so this can be made immutable
    public void set(final int bitNumber, final boolean value) {
        vec[bitNumber] = value;
    }

    /**
     * Get this BitVector as an array of bytes. Useful when sending it over the network.
     * @return this BitVector as an array of bytes.
     */
    public byte[] getAsByteArray() {
        byte[] out = new byte[bytesNeededToHoldBits(vec.length)];
        Arrays.fill(out, (byte)0);
        for (int bit = 0; bit < vec.length; bit++) {
            if (vec[bit])
                out[isInByteNumber(bit)] += 1 << isBitNumberInAByte(bit);
        }
        return out;
    }

    /**
     * Get this BitVector as an array of bits.
     * @return this BitVector as an array of bits.
     */
    public boolean[] getBits() {
        return vec.clone();
    }

    @Override
    public String toString() {
        return joinStringArray(vec, ", ");
    }

    private static int bytesNeededToHoldBits(int numberOfBits) {
        return 1 + isInByteNumber(numberOfBits - 1);
    }
}
