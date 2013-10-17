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

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.Assert.*;

public class TestUnderstoodBitVector {
    @Test
    public void basic_constructor_works() {
        new UnderstoodBitVector<Understanding>(4, Understanding.class);
    }

    @Test
    public void basic_constructor_startAsAllFalse() {
        UnderstoodBitVector<Understanding> v = new UnderstoodBitVector<Understanding>(4, Understanding.class);

        assertFalse(v.get(Understanding.ZERO));
        assertFalse(v.get(Understanding.ONE));
        assertFalse(v.get(Understanding.TWO));
        assertFalse(v.get(Understanding.THREE));
    }

    @Test
    public void setToTrue_changeTheRightOneAndOnlyIt() {
        UnderstoodBitVector<Understanding> v = new UnderstoodBitVector<Understanding>(4, Understanding.class);
        v.set(Understanding.TWO);

        assertFalse(v.get(Understanding.ZERO));
        assertFalse(v.get(Understanding.ONE));
        assertTrue(v.get(Understanding.TWO));
        assertFalse(v.get(Understanding.THREE));
    }

    @Test
    public void serialize_correctFormat() {
        UnderstoodBitVector<Understanding> v1 = new UnderstoodBitVector<Understanding>(4, Understanding.class);
        v1.set(Understanding.TWO);

        // the current serialization has been tested by manually making generated bv types extend UnderstoodBitVector
        assertEquals("Wrong format. The old serialization was tested.", 1, v1.getAsByteArray().length);
        assertEquals("Wrong format. The old serialization was tested.", 4, v1.getAsByteArray()[0]);
    }

    @Test
    public void serializeRoundTrip_sameSerialized() {
        UnderstoodBitVector<Understanding> v1 = new UnderstoodBitVector<Understanding>(4, Understanding.class);
        v1.set(Understanding.TWO);
        BitVector v2 = new UnderstoodBitVector<Understanding>(4, v1.getAsByteArray(), Understanding.class);

        assertArrayEquals("Data didn't survive round trip", v1.getAsByteArray(), v2.getAsByteArray());
    }

    @Test
    public void serializeRoundTrip_sameMeaning() {
        UnderstoodBitVector<Understanding> v1 = new UnderstoodBitVector<Understanding>(4, Understanding.class);
        v1.set(Understanding.TWO);

        UnderstoodBitVector<Understanding> v2 = new UnderstoodBitVector<Understanding>(4, v1.getAsByteArray(), Understanding.class);

        assertFalse("Meaning didn't survive round trip", v2.get(Understanding.ZERO));
        assertFalse("Meaning didn't survive round trip", v2.get(Understanding.ONE));
        assertTrue("Meaning didn't survive round trip", v2.get(Understanding.TWO));
        assertFalse("Meaning didn't survive round trip", v2.get(Understanding.THREE));
    }

    @Test
    public void getAll() throws InvocationTargetException {
        UnderstoodBitVector<Understanding> v1 = new UnderstoodBitVector<Understanding>(4, Understanding.class);
        v1.set(Understanding.TWO);
        v1.set(Understanding.THREE);

        List<Understanding> all = v1.getAll();

        assertFalse(all.contains(Understanding.ZERO));
        assertFalse(all.contains(Understanding.ONE));
        assertTrue(all.contains(Understanding.TWO));
        assertTrue(all.contains(Understanding.THREE));
    }

    private enum Understanding implements FCEnum {
        ZERO(0), ONE(1), TWO(2), THREE(3);

        private final int number;

        private Understanding(int number) {
            this.number = number;
        }

        @Override
        public int getNumber() {
            return number;
        }

        public static Understanding valueOf(int number) {
            return Helper.valueOfUnknownIsIllegal(number, values());
        }
    }
}
