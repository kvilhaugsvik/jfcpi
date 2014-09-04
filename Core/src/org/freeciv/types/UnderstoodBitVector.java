/*
 * Copyright (c) 2013 Sveinung Kvilhaugsvik.
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

import org.freeciv.utility.Util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * A list of boolean values were each value is understood to correspond to an enumerator of an enumeration. If the
 * corresponding bit vector position is true the enumerator is present.
 * @param <Of> The enum used to interpret the bit vector.
 */
public class UnderstoodBitVector<Of extends FCEnum> extends BitVector {
    private final Method numberToEnumConstant;

    /**
     * Interpret the given data and create a new UnderstoodBitVector of the wanted size from them.
     * See {@see #getAsByteArray()}.
     * @param sizeInBits the wanted size of the new UnderstoodBitVector.
     * @param src the data to fill the new UnderstoodBitVector.
     * @param of the enum to interpret the UnderstoodBitVector with.
     */
    public UnderstoodBitVector(int sizeInBits, byte[] src, Class<Of> of) {
        super(sizeInBits, src);
        this.numberToEnumConstant = setNumberToEnumConstant(of);
    }

    /**
     * Create a new zeroed UnderstoodBitVectorBitVector of the wanted size.
     * @param size the size of the UnderstoodBitVector to create.
     * @param of the enum to interpret the UnderstoodBitVector with.
     */
    public UnderstoodBitVector(int size, Class<Of> of) {
        super(size);
        this.numberToEnumConstant = setNumberToEnumConstant(of);
    }

    /**
     * Create a new UnderstoodBitVector with the same length and data as the given boolean array.
     * @param from the boolean array to copy.
     * @param of the enum to interpret the UnderstoodBitVector with.
     */
    public UnderstoodBitVector(boolean[] from, Class<Of> of) {
        super(from);
        this.numberToEnumConstant = setNumberToEnumConstant(of);
    }

    private Method setNumberToEnumConstant(Class<Of> of) {
        // TODO: validate numberToEnumConstant
        try {
            return of.getMethod("valueOf", int.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Should have had conversion method", e);
        }
    }

    /**
     * Set the given enumerator to present in the bit vector.
     * @param element the enumerator to be set to present.
     */
    public void set(final Of element) {
        set(element, true);
    }

    /**
     * Set the given enumerator to present or absent in the bit vector.
     * @param element the enumerator to set to present or absent.
     * @param value true if the enumerator is present, false if it is absent.
     */
    public void set(final Of element, final boolean value) {
        super.set(element.getNumber(), value);
    }

    /**
     * Get if the given enumerator is present in the bit vector.
     * @param element the enumerator to look for.
     * @return true if the enumerator is present.
     */
    public boolean get(final Of element) {
        return super.get(element.getNumber());
    }

    /**
     * Get a list of all enumerations that are set to true.
     * @return a list of all enumerations that are set to true.
     * @throws InvocationTargetException when conversion of a bit vector element to its enum constant failed.
     */
    public List<Of> getAll() throws InvocationTargetException {
        LinkedList<Of> out = new LinkedList<Of>();

        try {for (int i = 0; i < vec.length; i++)
            if (vec[i]) {
                out.add((Of) numberToEnumConstant.invoke(null, i));
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Not allowed to convert using " + numberToEnumConstant, e);
        }

        return out;
    }

    @Override
    public String toString() {
        try {
            return Util.joinStringArray(getAll().toArray(), ", ", "(", ")");
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }
}
