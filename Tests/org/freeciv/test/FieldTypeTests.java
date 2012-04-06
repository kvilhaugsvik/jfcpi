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

package org.freeciv.test;

import org.freeciv.packet.fieldtype.UINT32;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;

public class FieldTypeTests {
    private static long roundTripUINT32(long number) throws IOException {
        ByteArrayOutputStream storeTo = new ByteArrayOutputStream();
        UINT32 theInt = new UINT32(number);
        theInt.encodeTo(new DataOutputStream(storeTo));
        return (new UINT32(new DataInputStream(new ByteArrayInputStream(storeTo.toByteArray())))).getValue();
    }

    private static void assertRoundTripOkUINT32(long number) throws IOException {
        assertEquals("Value round trip not successful", number, roundTripUINT32(number));
    }

    private static long fromJavaUINT32(long number) {
        return new UINT32(number).getValue();
    }

    private static void assertFromJavaOkUINT32(long number) {
        assertEquals("Construction from Java long failed", number, fromJavaUINT32(number));
    }

    @Test public void UINT32_0Value() {
        assertFromJavaOkUINT32(0L);
    }
    @Test public void UINT32_0fromJavaUINT32() throws IOException {
        assertRoundTripOkUINT32(0L);
    }

    @Test public void UINT32_1Value() {
        assertFromJavaOkUINT32(1L);
    }
    @Test public void UINT32_1RoundTrip() throws IOException {
        assertRoundTripOkUINT32(1L);
    }

    @Test public void UINT32_MaxSub1Value() {
        assertFromJavaOkUINT32(((long)Integer.MAX_VALUE) - 1L);
    }
    @Test public void UINT32_MaxSub1RoundTrip() throws IOException {
        assertRoundTripOkUINT32(((long)Integer.MAX_VALUE) - 1L);
    }

    @Test public void UINT32_MaxValue() {
        assertFromJavaOkUINT32((long)Integer.MAX_VALUE);
    }
    @Test public void UINT32_MaxRoundTrip() throws IOException {
        assertRoundTripOkUINT32((long)Integer.MAX_VALUE);
    }

    @Test public void UINT32_MaxAdd1Value() {
        assertFromJavaOkUINT32(((long)Integer.MAX_VALUE) + 1L);
    }
    @Test public void UINT32_MaxAdd1RoundTrip() throws IOException {
        assertRoundTripOkUINT32(((long)Integer.MAX_VALUE) + 1L);
    }

    @Test public void UINT32_MaxAdd5Value() {
        assertFromJavaOkUINT32(((long)Integer.MAX_VALUE) + 5L);
    }
    @Test public void UINT32_MaxAdd5RoundTrip() throws IOException {
        assertRoundTripOkUINT32(((long)Integer.MAX_VALUE) + 5L);
    }

    @Test public void UINT32_MaxAdd7Value() {
        assertFromJavaOkUINT32(((long)Integer.MAX_VALUE) + 7L);
    }
    @Test public void UINT32_MaxAdd7RoundTrip() throws IOException {
        assertRoundTripOkUINT32(((long)Integer.MAX_VALUE) + 7L);
    }
}