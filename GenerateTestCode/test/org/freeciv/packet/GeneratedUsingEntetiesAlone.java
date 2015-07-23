/*
 * Copyright (c) 2011. Sveinung Kvilhaugsvik
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

import org.freeciv.connection.HeaderData;
import org.freeciv.connection.InterpretWhenPossible;
import org.freeciv.packet.fieldtype.*;
import org.junit.Test;

import java.io.*;
import java.util.HashMap;

import static org.junit.Assert.*;

public class GeneratedUsingEntetiesAlone {
    @Test
    public void testPacketWithoutFields() throws IOException, NoSuchMethodException {
        CONN_PONG packet = CONN_PONG.fromValues(new HeaderData(Header_2_1.class),
                InterpretWhenPossible.newDeltaStore());

        assertEquals(3, packet.getHeader().getTotalSize());
        assertEquals(89, packet.getHeader().getPacketKind());
    }

    @Test
    public void testPacketWithFieldsFromJavaTypes() throws IOException, NoSuchMethodException {
        SERVER_JOIN_REQ packet =
                SERVER_JOIN_REQ.fromValues("FreecivJava", "+Freeciv.Devel-2.4-2011.Aug.02 ", "-dev", 2L, 3L, 99L,
                        new HeaderData(Header_2_1.class),
                        InterpretWhenPossible.newDeltaStore());

        assertEquals(64, packet.getHeader().getTotalSize());
        assertEquals(4, packet.getHeader().getPacketKind());
    }

    @Test
    public void testPacketWithFieldValuesFromJavaTypes() throws IOException, NoSuchMethodException {
        SERVER_JOIN_REQ packet =
                SERVER_JOIN_REQ.fromValues("FreecivJava", "+Freeciv.Devel-2.4-2011.Aug.02 ", "-dev", 2L, 3L, 99L,
                        new HeaderData(Header_2_1.class),
                        InterpretWhenPossible.newDeltaStore());

        assertEquals("FreecivJava", packet.getUsernameValue());
        assertEquals("+Freeciv.Devel-2.4-2011.Aug.02 ", packet.getCapabilityValue());
        assertEquals("-dev", packet.getVersion_labelValue());
        assertEquals(2L, packet.getMajor_versionValue().longValue());
        assertEquals(3L, packet.getMinor_versionValue().longValue());
        assertEquals(99L, packet.getPatch_versionValue().longValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void packet_fromJava_notNull() throws IOException, NoSuchMethodException {
        DeltaVectorTest packet = DeltaVectorTest.fromValues(8, null, 1260L,
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    @Test
    public void testPacketWithoutFieldsFromStream() throws IOException {
        DataInput inputStream = new DataInputStream(new ByteArrayInputStream(new byte[]{/*0, 3, 89*/}));
        CONN_PONG packet = CONN_PONG.fromHeaderAndStream(inputStream, new Header_2_1(3, 89), InterpretWhenPossible.newDeltaStore());
        assertEquals(3, packet.getHeader().getTotalSize());
        assertEquals(89, packet.getHeader().getPacketKind());
    }

    @Test
    public void testPacketWithFieldsFromStream() throws IOException {
        DataInput inputStream = new DataInputStream(new ByteArrayInputStream(
                new byte[]{/*0, 64, 4, */70, 114, 101, 101, 99, 105, 118, 74, 97, 118, 97, 0, 43, 70, 114, 101, 101, 99,
                        105, 118, 46, 68, 101, 118, 101, 108, 45, 50, 46, 52, 45, 50, 48, 49, 49, 46, 65, 117, 103, 46,
                        48, 50, 32, 0, 45, 100, 101, 118, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 99}));
        SERVER_JOIN_REQ packet = SERVER_JOIN_REQ.fromHeaderAndStream(inputStream, new Header_2_1(64, 4), InterpretWhenPossible.newDeltaStore());
        assertEquals(64, packet.getHeader().getTotalSize());
        assertEquals(4, packet.getHeader().getPacketKind());
    }

    @Test
    public void testPacketFieldValuesFromStream() throws IOException {
        DataInput inputStream = new DataInputStream(new ByteArrayInputStream(
                new byte[]{/*0, 64, 4, */70, 114, 101, 101, 99, 105, 118, 74, 97, 118, 97, 0, 43, 70, 114, 101, 101, 99,
                        105, 118, 46, 68, 101, 118, 101, 108, 45, 50, 46, 52, 45, 50, 48, 49, 49, 46, 65, 117, 103, 46,
                        48, 50, 32, 0, 45, 100, 101, 118, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 99}));
        SERVER_JOIN_REQ packet = SERVER_JOIN_REQ.fromHeaderAndStream(inputStream, new Header_2_1(64, 4), InterpretWhenPossible.newDeltaStore());
        assertEquals("FreecivJava", packet.getUsernameValue());
        assertEquals("+Freeciv.Devel-2.4-2011.Aug.02 ", packet.getCapabilityValue());
        assertEquals("-dev", packet.getVersion_labelValue());
        assertEquals(2L, packet.getMajor_versionValue().longValue());
        assertEquals(3L, packet.getMinor_versionValue().longValue());
        assertEquals(99L, packet.getPatch_versionValue().longValue());
    }

    @Test(expected = FieldTypeException.class)
    public void testPacketWithFieldsFromStreamFailsOnWrongPackageNumber() throws IOException {
        DataInput inputStream = new DataInputStream(new ByteArrayInputStream(
                new byte[]{/*0, 64, 4, */70, 114, 101, 101, 99, 105, 118, 74, 97, 118, 97, 0, 43, 70, 114, 101, 101, 99,
                        105, 118, 46, 68, 101, 118, 101, 108, 45, 50, 46, 52, 45, 50, 48, 49, 49, 46, 65, 117, 103, 46,
                        48, 50, 32, 0, 45, 100, 101, 118, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 99}));
        SERVER_JOIN_REQ packet = SERVER_JOIN_REQ.fromHeaderAndStream(inputStream, new Header_2_1(64, 5), InterpretWhenPossible.newDeltaStore());
   }

    @Test(expected = FieldTypeException.class)
    public void testPacketWithFieldsFromStreamFailsOnWrongSize() throws IOException {
        DataInput inputStream = new DataInputStream(new ByteArrayInputStream(
                new byte[]{/*0, 64, 4, */70, 114, 101, 101, 99, 105, 118, 74, 97, 118, 97, 0, 43, 70, 114, 101, 101, 99,
                        105, 118, 46, 68, 101, 118, 101, 108, 45, 50, 46, 52, 45, 50, 48, 49, 49, 46, 65, 117, 103, 46,
                        48, 50, 32, 0, 45, 100, 101, 118, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 99}));
        SERVER_JOIN_REQ packet = SERVER_JOIN_REQ.fromHeaderAndStream(inputStream, new Header_2_1(62, 4), InterpretWhenPossible.newDeltaStore());
    }

    @Test public void testGeneratedPacketSerializesCorrectly() throws IOException {
        DataInput inputStream = new DataInputStream(new ByteArrayInputStream(
                new byte[]{/*0, 64, 4, */70, 114, 101, 101, 99, 105, 118, 74, 97, 118, 97, 0, 43, 70, 114, 101, 101, 99,
                        105, 118, 46, 68, 101, 118, 101, 108, 45, 50, 46, 52, 45, 50, 48, 49, 49, 46, 65, 117, 103, 46,
                        48, 50, 32, 0, 45, 100, 101, 118, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 99}));
        SERVER_JOIN_REQ packet = SERVER_JOIN_REQ.fromHeaderAndStream(inputStream, new Header_2_1(64, 4), InterpretWhenPossible.newDeltaStore());

        ByteArrayOutputStream serialized = new ByteArrayOutputStream();
        packet.encodeTo(new DataOutputStream(serialized));

        assertArrayEquals("Packet don't serialize  (missing header?)",
                new byte[]{0, 64, 4, 70, 114, 101, 101, 99, 105, 118, 74, 97, 118, 97, 0, 43, 70, 114, 101, 101, 99,
                        105, 118, 46, 68, 101, 118, 101, 108, 45, 50, 46, 52, 45, 50, 48, 49, 49, 46, 65, 117, 103, 46,
                        48, 50, 32, 0, 45, 100, 101, 118, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 99},
                serialized.toByteArray());
    }

    @Test public void testGeneratedPacketSerializesCorrectly2ByteKind() throws IOException {
        DataInput inputStream = new DataInputStream(new ByteArrayInputStream(
                new byte[]{/*0, 65, 00, 4, */70, 114, 101, 101, 99, 105, 118, 74, 97, 118, 97, 0, 43, 70, 114, 101, 101, 99,
                        105, 118, 46, 68, 101, 118, 101, 108, 45, 50, 46, 52, 45, 50, 48, 49, 49, 46, 65, 117, 103, 46,
                        48, 50, 32, 0, 45, 100, 101, 118, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 99}));
        SERVER_JOIN_REQ packet = SERVER_JOIN_REQ.fromHeaderAndStream(inputStream, new Header_2_2(65, 4), InterpretWhenPossible.newDeltaStore());

        ByteArrayOutputStream serialized = new ByteArrayOutputStream();
        packet.encodeTo(new DataOutputStream(serialized));

        assertArrayEquals("Packet don't serialize correctly (missing header?)",
                new byte[]{0, 65, 00, 4, 70, 114, 101, 101, 99, 105, 118, 74, 97, 118, 97, 0, 43, 70, 114, 101, 101, 99,
                        105, 118, 46, 68, 101, 118, 101, 108, 45, 50, 46, 52, 45, 50, 48, 49, 49, 46, 65, 117, 103, 46,
                        48, 50, 32, 0, 45, 100, 101, 118, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 99},
                serialized.toByteArray());
    }

    @Test public void generatedPacketWithArrayFieldsSimpleFromJava() throws NoSuchMethodException {
        TestArray packet = TestArray.fromValues(new Long[]{5L, 6L},
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
        assertArrayEquals("Result not the same as constructor", new Long[]{5L, 6L}, packet.getTheArrayValue());
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsSimpleFromJavaToSmallArray() throws NoSuchMethodException {
        TestArray packet = TestArray.fromValues(new Long[]{5L},
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsSimpleFromJavaToBigArray() throws NoSuchMethodException {
        TestArray packet = TestArray.fromValues(new Long[]{5L, 6L, 7L},
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    @Test public void generatedPacketWithArrayFieldsTransferFromJava() throws NoSuchMethodException {
        TestArrayTransfer packet = TestArrayTransfer.fromValues(2, new Long[]{5L, 6L},
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
        assertArrayEquals("Result not the same as constructor", new Long[]{5L, 6L}, packet.getTheArrayValue());
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsTransferFromJavaToSmallArray() throws NoSuchMethodException {
        TestArrayTransfer packet = TestArrayTransfer.fromValues(2, new Long[]{5L},
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsTransferFromJavaToBigArray() throws NoSuchMethodException {
        TestArrayTransfer packet = TestArrayTransfer.fromValues(2, new Long[]{5L, 6L, 7L},
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsTransfer_ArrayToBig() throws NoSuchMethodException {
        TestArrayTransfer packet = TestArrayTransfer.fromValues(3, new Long[]{5L, 6L, 7L, 6L, 7L},
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    @Test(expected = IllegalLimitSizeException.class)
    public void generatedPacketWithArrayFieldsTransfer_ToBigTransfer() throws NoSuchMethodException {
        TestArrayTransfer packet = TestArrayTransfer.fromValues(5, new Long[]{5L, 6L, 5L, 6L, 8L},
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    @Test public void generatedPacketWithArrayFieldsDoubleFromJava() throws NoSuchMethodException {
        Long[][] array = {{5L, 6L, 7L},
                          {8L, 9L, 10L}};
        TestArrayDouble packet = TestArrayDouble.fromValues(array,
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
        assertArrayEquals("Result not the same as constructor", array, packet.getTheArrayValue());
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsDoubleFromJava_1stArrayToSmall() throws NoSuchMethodException {
        Long[][] array = {{5L, 6L, 7L}};
        TestArrayDouble packet = TestArrayDouble.fromValues(array,
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsDoubleFromJava_2ndArrayToSmall() throws NoSuchMethodException {
        Long[][] array = {{5L, 6L},
                          {5L, 6L}};
        TestArrayDouble packet = TestArrayDouble.fromValues(array,
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsDoubleFromJava_1stArrayToBig() throws NoSuchMethodException {
        Long[][] array = {{5L, 6L, 7L},
                          {5L, 6L, 7L},
                          {5L, 6L, 7L}};
        TestArrayDouble packet = TestArrayDouble.fromValues(array,
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsDoubleFromJava_2ndArrayToBig() throws NoSuchMethodException {
        Long[][] array = {{5L, 6L, 7L, 8L},
                          {5L, 6L, 7L, 9L}};
        TestArrayDouble packet = TestArrayDouble.fromValues(array,
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    @Test public void generatedPacketWithArrayFieldsDoubleTransferFromJava() throws NoSuchMethodException {
        Long[][] array = {{5L, 6L, 7L},
                          {8L, 9L, 10L}};
        TestArrayDoubleTransfer packet = TestArrayDoubleTransfer.fromValues(2, 3, array,
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
        assertArrayEquals("Result not the same as constructor", array, packet.getTheArrayValue());
    }

    @Test public void generatedPacketWithArrayFieldsDoubleTransferFromJavaMax() throws NoSuchMethodException {
        Long[][] array = {
                {5L, 6L, 7L, 8L},
                {9L, 10L, 11L, 12L},
                {13L, 14L, 15L, 16L}};
        TestArrayDoubleTransfer packet = TestArrayDoubleTransfer.fromValues(3, 4, array,
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
        assertArrayEquals("Result not the same as constructor", array, packet.getTheArrayValue());
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsDoubleTransfer_1stArrayToSmall() throws NoSuchMethodException {
        Long[][] array = {{8L, 9L, 10L}};
        TestArrayDoubleTransfer packet = TestArrayDoubleTransfer.fromValues(2, 3, array,
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsDoubleTransfer_2ndArrayToSmall() throws NoSuchMethodException {
        Long[][] array = {
                {5L, 6L},
                {8L, 9L}};
        TestArrayDoubleTransfer packet = TestArrayDoubleTransfer.fromValues(2, 3, array,
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsDoubleTransfer_1stArrayToBig() throws NoSuchMethodException {
        Long[][] array = {
                {5L, 6L, 7L},
                {8L, 9L, 10L},
                {11L, 12L, 13L}};
        TestArrayDoubleTransfer packet = TestArrayDoubleTransfer.fromValues(2, 3, array,
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsDoubleTransfer_2ndArrayToBig() throws NoSuchMethodException {
        Long[][] array = {
                {5L, 6L, 7L, 1L},
                {8L, 9L, 10L, 2L}};
        TestArrayDoubleTransfer packet = TestArrayDoubleTransfer.fromValues(2, 3, array,
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    @Test(expected = IllegalLimitSizeException.class)
    public void generatedPacketWithArrayFieldsDoubleTransfer_1stSizeOverMax() throws NoSuchMethodException {
        Long[][] array = {
                {5L, 6L, 7L},
                {8L, 9L, 10L},
                {11L, 12L, 13L},
                {14L, 15L, 16L},
                {17L, 19L, 18L}};
        TestArrayDoubleTransfer packet = TestArrayDoubleTransfer.fromValues(5, 3, array,
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    @Test(expected = IllegalLimitSizeException.class)
    public void generatedPacketWithArrayFieldsDoubleTransfer_2ndSizeOverMax() throws NoSuchMethodException {
        Long[][] array = {
                {5L, 6L, 7L, 8L, 9L, 10L},
                {11L, 12L, 17L, 18L, 19L, 30L}};
        TestArrayDoubleTransfer packet = TestArrayDoubleTransfer.fromValues(2, 6, array,
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    @Test public void generatedPacketWithStringAndArrayOfString() throws NoSuchMethodException {
        StringArray packet = StringArray.fromValues("Not an Array",
                new String[]{"Element 1", "Element 2", "Element 3"},
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());

        assertEquals("Plain string different", "Not an Array", packet.getNotAnArrayValue());
        assertArrayEquals("Array different ",
                new String[]{"Element 1", "Element 2", "Element 3"},
                packet.getTheArrayValue());
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketSimpleStringToBig() throws NoSuchMethodException {
        StringArray packet = StringArray.fromValues("Not an ArrayNot an Array",
                new String[]{"Element 1", "Element 2", "Element 3"},
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketFistStringInArrayToBig() throws NoSuchMethodException {
        StringArray packet = StringArray.fromValues("Not an Array",
                new String[]{"Element 1Element 1", "Element 2", "Element 3"},
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketMiddleStringInArrayToBig() throws NoSuchMethodException {
        StringArray packet = StringArray.fromValues("Not an Array",
                new String[]{"Element 1", "Element 2Element 2", "Element 3"},
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketLastStringInArrayToBig() throws NoSuchMethodException {
        StringArray packet = StringArray.fromValues("Not an Array",
                new String[]{"Element 1", "Element 2", "Element 3Element 3"},
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacket_ArrayToBig() throws NoSuchMethodException {
        StringArray packet = StringArray.fromValues("Not an Array",
                new String[]{"Element 1", "Element 2", "Element 3", "Element 4"},
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketStringToBig() throws NoSuchMethodException {
        StringArray packet = StringArray.fromValues("Not an ArrayNot an Array",
                new String[]{
                        "Element 1",
                        "Element 2",
                        "Element 3"
                },
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    @Test
    public void generatedPacketExceptionTellsWhere() throws NoSuchMethodException {
        try {
            StringArray packet = StringArray.fromValues(
                    "Not an ArrayNot an Array",
                    new String[]{
                            "Element 1",
                            "Element 2",
                            "Element 3"
                    },
                    new HeaderData(Header_2_2.class),
                    InterpretWhenPossible.newDeltaStore());
            fail("No exception cast");
        } catch (FieldTypeException e) {
            assertEquals("StringArray", e.getInPacket());
            assertEquals("notAnArray", e.getField());
        }
    }

    @Test
    public void delta_fromData_allThere() throws IOException {
        ByteArrayOutputStream storeTo = new ByteArrayOutputStream();
        storeTo.write(new byte[]{3, 50, 'w', 'o', 'r', 'k', 's', 0, 0, 0, 1, 0});
        DataInputStream inn = new DataInputStream(new ByteArrayInputStream(storeTo.toByteArray()));

        DeltaVectorTest packet = DeltaVectorTest.fromHeaderAndStream(inn, new Header_2_2(16, 933), InterpretWhenPossible.newDeltaStore());

        assertEquals(50, packet.getIdValue().intValue());
        assertEquals("works", packet.getField1Value());
        assertEquals(256, packet.getField2Value().intValue());
    }

    @Test
    public void delta_fromData_nothingBefore_missingIsZero() throws IOException {
        ByteArrayOutputStream storeTo = new ByteArrayOutputStream();
        storeTo.write(new byte[]{2, 50, 0, 0, 1, 0});
        DataInputStream inn = new DataInputStream(new ByteArrayInputStream(storeTo.toByteArray()));

        DeltaVectorTest packet = DeltaVectorTest.fromHeaderAndStream(inn, new Header_2_2(10, 933), InterpretWhenPossible.newDeltaStore());

        assertEquals(50, packet.getIdValue().intValue());
        assertEquals("", packet.getField1Value());
        assertEquals(256, packet.getField2Value().intValue());
    }

    @Test
    public void delta_fromData_differentKeySameKindBefore_missingIsZero() throws IOException {
        ByteArrayOutputStream storeTo = new ByteArrayOutputStream();
        storeTo.write(new byte[]{3, 50, 'w', 'o', 'r', 'k', 's', 0, 0, 0, 1, 0}); // packet 1
        storeTo.write(new byte[]{2, 100, 0, 0, 1, 0}); // packet 2
        DataInputStream inn = new DataInputStream(new ByteArrayInputStream(storeTo.toByteArray()));

        HashMap<DeltaKey, Packet> old = InterpretWhenPossible.newDeltaStore();
        DeltaVectorTest.fromHeaderAndStream(inn, new Header_2_2(16, 933), old);
        DeltaVectorTest packet = DeltaVectorTest.fromHeaderAndStream(inn, new Header_2_2(10, 933), old);

        assertEquals(100, packet.getIdValue().intValue());
        assertEquals("", packet.getField1Value());
        assertEquals(256, packet.getField2Value().intValue());
    }

    @Test
    public void delta_fromData_sameKeySameKindBefore_missingIsPrevious() throws IOException {
        ByteArrayOutputStream storeTo = new ByteArrayOutputStream();

        /* Create the serialized packet 1 */
        storeTo.write(new byte[]{3, 50, 'w', 'o', 'r', 'k', 's', 0, 0, 0, 1, 0});

        /* Create the serialized packet 2 */
        storeTo.write(new byte[]{2, 50, 0, 0, 1, 0});

        /* Stream the packets. */
        DataInputStream inn = new DataInputStream(new ByteArrayInputStream(storeTo.toByteArray()));

        HashMap<DeltaKey, Packet> old = InterpretWhenPossible.newDeltaStore();

        /* Read packet 1. */
        DeltaVectorTest firstPacket = DeltaVectorTest.fromHeaderAndStream(inn, new Header_2_2(16, 933), old);

        /* Store packet 1 so packet 2 can reference it. */
        old.put(firstPacket.getKey(), firstPacket);

        /* Read packet 2. This should have its field1 from packet 1. */
        DeltaVectorTest packet = DeltaVectorTest.fromHeaderAndStream(inn, new Header_2_2(10, 933), old);

        /* Make sure that the key field is as expected. */
        assertEquals(50, packet.getIdValue().intValue());

        /* Make sure that packet 2 got the string "works" from packet 1. */
        assertEquals("works", packet.getField1Value());

        /* Make sure that the following field isn't corrupted. */
        assertEquals(256, packet.getField2Value().intValue());
    }

    @Test
    public void delta_roundTrip_fromData_encodeTo() throws IOException {
        ByteArrayOutputStream storeTo = new ByteArrayOutputStream();
        storeTo.write(new byte[]{2, 50, 0, 0, 1, 0});
        DataInputStream inn = new DataInputStream(new ByteArrayInputStream(storeTo.toByteArray()));

        DeltaVectorTest packet = DeltaVectorTest.fromHeaderAndStream(inn, new Header_2_2(10, 933), InterpretWhenPossible.newDeltaStore());

        final ByteArrayOutputStream reserialized = new ByteArrayOutputStream();
        DataOutputStream writeTo = new DataOutputStream(reserialized);
        packet.encodeTo(writeTo);

        assertArrayEquals(new byte[]{0, 10, 3, -91, 2, 50, 0, 0, 1, 0}, reserialized.toByteArray());
    }

    @Test
    public void delta_deltaVector_noPrevious() throws IOException, NoSuchMethodException {
        DeltaVectorTest packet = DeltaVectorTest.fromValues(8, "works", 1260L,
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());

        boolean[] dv = packet.getDelta().getValue().getBits();

        assertEquals("Wrong delta vector size", 2, dv.length);
        assertTrue("Should be sent", dv[0]);
        assertTrue("Should be sent", dv[1]);
    }

    @Test
    public void delta_deltaVector_fromData() throws IOException {
        ByteArrayOutputStream storeTo = new ByteArrayOutputStream();
        storeTo.write(new byte[]{2, 50, 0, 0, 1, 0});
        DataInputStream inn = new DataInputStream(new ByteArrayInputStream(storeTo.toByteArray()));

        DeltaVectorTest packet = DeltaVectorTest.fromHeaderAndStream(inn, new Header_2_2(10, 933), InterpretWhenPossible.newDeltaStore());

        boolean[] dv = packet.getDelta().getValue().getBits();

        assertEquals("Wrong delta vector size", 2, dv.length);
        assertFalse("Should not be sent", dv[0]);
        assertTrue("Should be sent", dv[1]);
    }

    /**
     * Check that a field that hasn't changed since the previous packet
     * isn't sent.
     */
    @Test
    public void delta_deltaVector_fromValues_previous() {
        /* The delta store. */
        final HashMap<DeltaKey, Packet> deltaStore = InterpretWhenPossible.newDeltaStore();

        /* Create the previous packet. Values sent in it shouldn't be
         * resent. */
        final DeltaVectorTest previousPacket =
                DeltaVectorTest.fromValues(8, "old", 1260L, new HeaderData(Header_2_2.class), deltaStore);

        /* Store the previous packet so it can be found. */
        deltaStore.put(previousPacket.getKey(), previousPacket);

        /* Create the test packet. It shouldn't resent field 2. */
        final DeltaVectorTest packet = DeltaVectorTest.fromValues(8, "current", 1260L,
                new HeaderData(Header_2_2.class), deltaStore);

        /* Get the bits of the delta header bit vector. */
        final boolean[] dv = packet.getDelta().getValue().getBits();

        /* Pre test sanity check. */
        assertEquals("Wrong delta vector size", 2, dv.length);

        /* field1 has changed. It should therefore be sent. */
        assertTrue("Should be sent", dv[0]);

        /* field2 hasn't changed. It therefore shouldn't be sent. */
        assertFalse("Should not be sent", dv[1]);
    }

    /**
     * Check that the delta key generated by a packet is equal to it self.
     */
    @Test public void deltaKey_deltaVector_correctKey() throws IOException, NoSuchMethodException {
        PacketInterpretedDelta packet = DeltaVectorTest.fromValues(8, "works", 1260L,
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());

        assertEquals(new DeltaKey(933, new UINT8(8, ElementsLimit.noLimit())), packet.getKey());
    }

    /**
     * Check that the delta key generated by a packet unequal to a
     * different key.
     */
    @Test public void deltaKey_deltaVector_inCorrectKey() throws IOException, NoSuchMethodException {
        PacketInterpretedDelta packet = DeltaVectorTest.fromValues(8, "works", 1260L,
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());

        assertNotEquals(new DeltaKey(933, new UINT8(3, ElementsLimit.noLimit())), packet.getKey());
    }
}
