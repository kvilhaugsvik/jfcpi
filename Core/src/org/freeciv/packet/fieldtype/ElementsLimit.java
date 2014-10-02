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
 * A limit on the number of elements a field has.
 */
public class ElementsLimit {
    private static ElementsLimit end;

    private final ElementsLimit child;

    /**
     * The absolute maximum number of elements this field ever can have.
     */
    public final int full_array_size;

    /**
     * The maximum number of elements the field can have this time.
     */
    public final int elements_to_transfer;

    private ElementsLimit(int full_array_size, int elements_to_transfer, ElementsLimit subLimit) {
        this.full_array_size = full_array_size;
        this.elements_to_transfer = elements_to_transfer;
        this.child = subLimit;
    }

    /**
     * Get the limit on the next array dimension.
     * @return the limit on the next array dimension.
     * @throws NoSuchElementException if no more array dimensions exist.
     */
    public ElementsLimit next() {
        if (noLimit().equals(this))
            throw new NoSuchElementException("Limit has no sub limit");

        return child;
    }

    /**
     * Get the limit indicating that no more array dimensions exists.
     * @return the limit indicating that no more array dimensions exists.
     */
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

    /**
     * Create a new limit on the number of elements.
     * @param max the maximum number of elements this field can have.
     * @return the new limit on the number of elements.
     */
    public static ElementsLimit limit(int max) {
        return limit(max, max);
    }

    /**
     * Create a new limit on the number of elements.
     * @param max the maximum number of elements this field can have.
     * @param subLimit the limit for the next array dimension.
     * @return the new limit on the number of elements.
     */
    public static ElementsLimit limit(int max, ElementsLimit subLimit) {
        return limit(max, max, subLimit);
    }

    /**
     * Create a new limit on the number of elements.
     * @param full_array_size the maximum number of elements the field ever can have.
     * @param elements_to_transfer the maximum number of elements the field can have this time.
     * @return the new limit on the number of elements.
     */
    public static ElementsLimit limit(int full_array_size, int elements_to_transfer) {
        return limit(full_array_size, elements_to_transfer, noLimit());
    }

    /**
     * Create a new limit on the number of elements.
     * @param full_array_size the maximum number of elements the field ever can have.
     * @param elements_to_transfer the maximum number of elements the field can have this time.
     * @param subLimit the limit for the next array dimension.
     * @return the new limit on the number of elements.
     */
    public static ElementsLimit limit(int full_array_size, int elements_to_transfer, ElementsLimit subLimit) {
        if (null == subLimit)
            throw new NullPointerException("Sub limit can't be null. Did you mean noLimit()");
        if (full_array_size < 0)
            throw new IllegalArgumentException("The absolute largest possible number of elements can't be below 0");
        if (elements_to_transfer < 0)
            throw new IllegalArgumentException("How can less than 0 elements be transferred");
        if (full_array_size < elements_to_transfer)
            throw new IllegalLimitSizeException("The relative limit on the number of elements is " +
                    "to large compared to the absolute limit");

        return new ElementsLimit(full_array_size, elements_to_transfer, subLimit);
    }
}
