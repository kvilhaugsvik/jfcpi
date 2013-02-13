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

import org.freeciv.packet.fieldtype.*;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

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
        UINT32S theArray = new UINT32S(new Long[]{5L, 1000L}, ElementsLimit.limit(2, 2));
        theArray.encodeTo(new DataOutputStream(storeTo));

        // read it back
        UINT32S theReturnedArray = new UINT32S(new DataInputStream(new ByteArrayInputStream(storeTo.toByteArray())), ElementsLimit.limit(2, 2));

        // compare the values
        assertEquals("1 dimensional field type array didn't survive encoding followed by decoding",
                theArray.getValue()[0], theReturnedArray.getValue()[0]);
        assertEquals("1 dimensional field type array didn't survive encoding followed by decoding",
                theArray.getValue()[1], theReturnedArray.getValue()[1]);
    }

    @Test public void STRING_1_dimensionalArray_round_trip() throws IOException {
        ByteArrayOutputStream storeTo = new ByteArrayOutputStream();

        // create and write
        STRINGS theArray = new STRINGS(new String[]{"1", "win"}, ElementsLimit.limit(2, 2, ElementsLimit.limit(5, 4)));
        theArray.encodeTo(new DataOutputStream(storeTo));

        // check the encoding
        assertEquals("1d + eater field type array encoded wrong", "1", theArray.getValue()[0]);
        assertEquals("1d + eater  field type array encoded wrong", "win", theArray.getValue()[1]);

        // read it back
        STRINGS theReturnedArray = new STRINGS(new DataInputStream(new ByteArrayInputStream(storeTo.toByteArray())),
                ElementsLimit.limit(2, 2, ElementsLimit.limit(5, 4)));

        // compare the values
        assertEquals("1d + eater field type array didn't survive encoding followed by decoding",
                theArray.getValue()[0], theReturnedArray.getValue()[0]);
        assertEquals("1d + eater  field type array didn't survive encoding followed by decoding",
                theArray.getValue()[1], theReturnedArray.getValue()[1]);
    }

    @Test public void UINT8_2D_understand() throws IOException {
        ByteArrayOutputStream storeTo = new ByteArrayOutputStream();
        storeTo.write(new byte[]{1, 3, 7, 12});

        UINT8S2D result = new UINT8S2D(new DataInputStream(new ByteArrayInputStream(storeTo.toByteArray())),
                ElementsLimit.limit(2, 2, ElementsLimit.limit(2, 2)));

        assertEquals(1, result.getValue()[0][0].intValue());
        assertEquals(3, result.getValue()[0][1].intValue());
        assertEquals(7, result.getValue()[1][0].intValue());
        assertEquals(12, result.getValue()[1][1].intValue());
    }

    private void testString(String text, int maxLen) throws IOException {
        STRING fromJava = new STRING(text, ElementsLimit.limit(maxLen));
        checkString(text, fromJava, maxLen);

        ByteArrayOutputStream storeTo = new ByteArrayOutputStream();
        fromJava.encodeTo(new DataOutputStream(storeTo));
        STRING fromData = new STRING(new DataInputStream(new ByteArrayInputStream(storeTo.toByteArray())),
                ElementsLimit.limit(maxLen));
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

    @Test public void STRING_fewer_max_this_time() throws IOException {
        STRING smallButOk = new STRING("Hello", ElementsLimit.limit(20, 7));
        assertEquals("Hello", smallButOk.getValue());
    }

    @Test(expected = IllegalLimitSizeException.class)
    public void STRING_more_this_time_than_max() throws IOException {
        STRING notOk = new STRING("Hello", ElementsLimit.limit(7, 20));
        fail(notOk.getValue() + " is smaller than 20 but 20 is smaller than 7");
    }

    @Test public void verifyInsideLimitsSmallerAndOK() throws IOException {
        STRING smallButOk = new STRING("Hello", ElementsLimit.limit(20, 7));
        smallButOk.verifyInsideLimits(ElementsLimit.limit(20, 7));
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void verifyInsideLimitsSmallerNotOK() throws IOException {
        UINT8S toSmall = new UINT8S(new Integer[]{2, 5}, ElementsLimit.limit(3, 2));
        toSmall.verifyInsideLimits(ElementsLimit.limit(20, 7));
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void verifyInsideLimitsBigger() throws IOException {
        STRING toBigForStricterLimits = new STRING("Hello", ElementsLimit.limit(20, 7));
        toBigForStricterLimits.verifyInsideLimits(ElementsLimit.limit(4, 3));
    }

    @Test(expected = IllegalLimitSizeException.class)
    public void verifyInsideLimitsRelativeLimitToBig() throws IOException {
        STRING field = new STRING("Hello", ElementsLimit.limit(20, 7));
        field.verifyInsideLimits(ElementsLimit.limit(20, 21));
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void verifyInsideLimitsWrongLenInDimension1() throws IOException {
        ByteArrayOutputStream storeTo = new ByteArrayOutputStream();
        storeTo.write(new byte[]{1, 3, 7, 12, 44, 77});

        UINT8S2D array = new UINT8S2D(new DataInputStream(new ByteArrayInputStream(storeTo.toByteArray())),
                ElementsLimit.limit(2, 2, ElementsLimit.limit(3, 3)));

        array.verifyInsideLimits(ElementsLimit.limit(2, 2, ElementsLimit.limit(2, 2)));
    }

    @Test
    public void BVGeneral_construct_fromData() throws IOException {
        ByteArrayOutputStream storeTo = new ByteArrayOutputStream();
        storeTo.write(new byte[]{1, 3});
        DataInputStream inn = new DataInputStream(new ByteArrayInputStream(storeTo.toByteArray()));

        BV_GENERAL bits = new BV_GENERAL(inn, ElementsLimit.limit(11));
        boolean[] result = bits.getValue().getBits();

        assertEquals(11, result.length);

        assertTrue(result[0]);

        assertFalse(result[1]);
        assertFalse(result[2]);
        assertFalse(result[3]);
        assertFalse(result[4]);
        assertFalse(result[5]);
        assertFalse(result[6]);
        assertFalse(result[7]);

        assertTrue(result[8]);
        assertTrue(result[9]);
        assertFalse(result[10]);
    }
}