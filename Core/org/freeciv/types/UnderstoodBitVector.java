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

public class UnderstoodBitVector<Of extends FCEnum> extends BitVector {
    private final Method numberToEnumConstant;

    public UnderstoodBitVector(int sizeInBits, byte[] src, Class<Of> of) {
        super(sizeInBits, src);
        this.numberToEnumConstant = setNumberToEnumConstant(of);
    }

    public UnderstoodBitVector(int size, Class<Of> of) {
        super(size);
        this.numberToEnumConstant = setNumberToEnumConstant(of);
    }

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

    public void set(final Of element) {
        set(element, true);
    }

    public void set(final Of element, final boolean value) {
        super.set(element.getNumber(), value);
    }

    public boolean get(final Of element) {
        return super.get(element.getNumber());
    }

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
