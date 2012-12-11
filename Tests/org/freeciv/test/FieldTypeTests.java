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

import org.freeciv.packet.fieldtype.ElementsLimit;
import org.freeciv.packet.fieldtype.STRING;
import org.freeciv.packet.fieldtype.UINT32;
import org.freeciv.packet.fieldtype.UINT32S;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class FieldTypeTests {
    private static long roundTripUINT32(long number) throws IOException {
        ByteArrayOutputStream storeTo = new ByteArrayOutputStream();
        UINT32 theInt = new UINT32(number, ElementsLimit.noLimit());
        theInt.encodeTo(new DataOutputStream(storeTo));
        return (new UINT32(new DataInputStream(new ByteArrayInputStream(storeTo.toByteArray())), ElementsLimit.noLimit())).getValue();
    }

    private static void assertRoundTripOkUINT32(long number) throws IOException {
        assertEquals("Value round trip not successful", number, roundTripUINT32(number));
    }

    private static long fromJavaUINT32(long number) {
        return new UINT32(number, ElementsLimit.noLimit()).getValue();
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

    @Test public void UNINT32_1_dimensionalArray_round_trip() throws IOException {
        ByteArrayOutputStream storeTo = new ByteArrayOutputStream();

        // create and write
        UINT32S theArray = new UINT32S(new UINT32[]{
            new UINT32(5L, ElementsLimit.noLimit()),
            new UINT32(1000L, ElementsLimit.noLimit())
        }, ElementsLimit.limit(2, 2));
        theArray.encodeTo(new DataOutputStream(storeTo));

        // read it back
        UINT32S theReturnedArray = new UINT32S(new DataInputStream(new ByteArrayInputStream(storeTo.toByteArray())), ElementsLimit.limit(2, 2));

        // compare the values
        assertEquals("1 dimensional field type array didn't survive encoding followed by decoding",
                theArray.getValue()[0].getValue(), theReturnedArray.getValue()[0].getValue());
        assertEquals("1 dimensional field type array didn't survive encoding followed by decoding",
                theArray.getValue()[1].getValue(), theReturnedArray.getValue()[1].getValue());
    }

    private void testString(String text, int maxLen) throws IOException {
        STRING fromJava = new STRING(text, ElementsLimit.limit(maxLen, maxLen));
        checkString(text, fromJava, maxLen);

        ByteArrayOutputStream storeTo = new ByteArrayOutputStream();
        fromJava.encodeTo(new DataOutputStream(storeTo));
        STRING fromData = new STRING(new DataInputStream(new ByteArrayInputStream(storeTo.toByteArray())),
                ElementsLimit.limit(maxLen, maxLen));
        checkString(text, fromData, maxLen);
    }

    private static void checkString(String text, STRING fromJava, int maxLen) {
        assertEquals("Wrong content", text, fromJava.getValue());
        // TODO: figure out if String has the terminator when it is at its max size
        assertEquals("Wrong length", text.getBytes().length + 1, fromJava.encodedLength());
    }

    @Test public void STRING_ASCII() throws IOException {
        testString("da er det for sent", 100);
    }

    @Test public void STRING_non_ASCII() throws IOException {
        testString("Jeg råde vil alle i ungdummens dager", 100);
    }

    @Test public void STRING_non_ASCII_twice() throws IOException {
        testString("I dag når du hører", 100);
    }

    @Test public void STRING_empty_odd() throws IOException {
        testString("", 3);
    }

    @Test public void STRING_empty_even() throws IOException {
        testString("", 4);
    }
}