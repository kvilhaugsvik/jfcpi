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

package org.freeciv.utility;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class TestEternalZero {
    @Test
    public void noBeginningIsZero() throws IOException {
        EndsInEternalZero stream = new EndsInEternalZero(new byte[0]);

        assertEquals("1st byte not 0", 0, stream.read());
        assertEquals("2nd byte not 0", 0, stream.read());
        assertEquals("3rd byte not 0", 0, stream.read());
    }

    @Test
    public void oneByteBeforeTheRest() throws IOException {
        EndsInEternalZero stream = new EndsInEternalZero(new byte[]{1});

        assertEquals("Beginning missing", 1, stream.read());
        assertEquals("2nd byte not 0", 0, stream.read());
    }

    @Test
    public void twoBytesBeforeTheRest() throws IOException {
        EndsInEternalZero stream = new EndsInEternalZero(new byte[]{1, 2});

        assertEquals("Beginning missing", 1, stream.read());
        assertEquals("Beginning missing", 2, stream.read());
        assertEquals("3rd byte not 0", 0, stream.read());
    }
}
