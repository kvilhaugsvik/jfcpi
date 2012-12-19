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

package org.freeciv.packet.fieldtype;

import java.util.NoSuchElementException;

/**
 * Limit the number of elements an array of fields has
 */
public class ElementsLimit {
    private static ElementsLimit end;

    private final ElementsLimit child;

    public final int full_array_size;
    public final int elements_to_transfer;

    private ElementsLimit(int full_array_size, int elements_to_transfer, ElementsLimit subLimit) {
        this.full_array_size = full_array_size;
        this.elements_to_transfer = elements_to_transfer;
        this.child = subLimit;
    }

    public ElementsLimit next() {
        if (noLimit().equals(child))
            throw new NoSuchElementException("Limit has no sub limit");

        return child;
    }

    public static ElementsLimit noLimit() {
        if (null == end)
            end = new ElementsLimit(-1, -1, null) {
                @Override
                public ElementsLimit next() {
                    throw new NoSuchElementException("Limit has no sub limit");
                }
            };

        return end;
    }

    public static ElementsLimit limit(int absolute_max) {
        return limit(absolute_max, absolute_max);
    }

    public static ElementsLimit limit(int absolute_max, ElementsLimit subLimit) {
        return limit(absolute_max, absolute_max, subLimit);
    }

    public static ElementsLimit limit(int full_array_size, int elements_to_transfer) {
        return limit(full_array_size, elements_to_transfer, noLimit());
    }

    public static ElementsLimit limit(int full_array_size, int elements_to_transfer, ElementsLimit subLimit) {
        if (null == subLimit)
            throw new NullPointerException("Sub limit can't be null. Did you mean noLimit()");
        if (full_array_size < 0)
            throw new IllegalArgumentException("The absolute largest possible number of elements can't be below 0");
        if (elements_to_transfer < 0)
            throw new IllegalArgumentException("How can less than 0 elements be transferred");

        return new ElementsLimit(full_array_size, elements_to_transfer, subLimit);
    }
}
