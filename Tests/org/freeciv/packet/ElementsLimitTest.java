/*
 * Copyright (c) 2012 Sveinung Kvilhaugsvik
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

import org.freeciv.packet.fieldtype.ElementsLimit;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class ElementsLimitTest {
    @Test
    public void noLimits() {
        assertNotNull(ElementsLimit.noLimit());
    }

    @Test(expected = NoSuchElementException.class)
    public void noLimitsHasNoNext() {
        ElementsLimit.noLimit().next();
    }

    @Test
    public void noLimitsIsSelf() {
        assertEquals(ElementsLimit.noLimit(), ElementsLimit.noLimit());
    }

    @Test
    public void simpleLimits() {
        ElementsLimit limit = ElementsLimit.limit(3, 2);
        assertEquals(3, limit.full_array_size);
        assertEquals(2, limit.elements_to_transfer);
    }

    @Test(expected = NoSuchElementException.class)
    public void simpleLimitsHasNoNext() {
        ElementsLimit.limit(3, 2).next();
    }

    @Test(expected = IllegalArgumentException.class)
    public void simpleLimitsNoNegative1() {
        ElementsLimit limit = ElementsLimit.limit(-3, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void simpleLimitsNoNegative2() {
        ElementsLimit limit = ElementsLimit.limit(3, -2);
    }

    @Test
    public void superLimitsHasNext() {
        ElementsLimit limit = ElementsLimit.limit(4, 1, ElementsLimit.limit(3, 2));

        assertEquals(4, limit.full_array_size);
        assertEquals(1, limit.elements_to_transfer);
        assertEquals(3, limit.next().full_array_size);
        assertEquals(2, limit.next().elements_to_transfer);
    }

    @Test(expected = NullPointerException.class)
    public void superLimitsSubNotNull() {
        ElementsLimit limit = ElementsLimit.limit(3, 2, null);
    }
}
