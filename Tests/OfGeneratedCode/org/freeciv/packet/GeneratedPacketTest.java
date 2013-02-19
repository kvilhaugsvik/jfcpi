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

import org.freeciv.packet.fieldtype.*;
import org.junit.Test;

import java.io.*;
import java.util.HashMap;

import static org.junit.Assert.*;

public class GeneratedPacketTest {
    @Test
    public void testPacketWithoutFields() throws IOException {
        CONN_PONG packet = new CONN_PONG();

        assertEquals(3, packet.getHeader().getTotalSize());
        assertEquals(89, packet.getHeader().getPacketKind());
    }

    @Test
    public void testPacketWithFieldsFromJavaTypes() throws IOException {
        SERVER_JOIN_REQ packet =
                new SERVER_JOIN_REQ("FreecivJava", "+Freeciv.Devel-2.4-2011.Aug.02 ", "-dev", 2L, 3L, 99L);

        assertEquals(64, packet.getHeader().getTotalSize());
        assertEquals(4, packet.getHeader().getPacketKind());
    }

    @Test
    public void testPacketWithFieldValuesFromJavaTypes() throws IOException {
        SERVER_JOIN_REQ packet =
                new SERVER_JOIN_REQ("FreecivJava", "+Freeciv.Devel-2.4-2011.Aug.02 ", "-dev", 2L, 3L, 99L);

        assertEquals("FreecivJava", packet.getUsernameValue());
        assertEquals("+Freeciv.Devel-2.4-2011.Aug.02 ", packet.getCapabilityValue());
        assertEquals("-dev", packet.getVersion_labelValue());
        assertEquals(2L, packet.getMajor_versionValue().longValue());
        assertEquals(3L, packet.getMinor_versionValue().longValue());
        assertEquals(99L, packet.getPatch_versionValue().longValue());
    }

    @Test
    public void testPacketWithFieldsFromFields() throws IOException {
        SERVER_JOIN_REQ packet =
                new SERVER_JOIN_REQ(
                        new STRING("FreecivJava", ElementsLimit.limit(100)),
                        new STRING("+Freeciv.Devel-2.4-2011.Aug.02 ", ElementsLimit.limit(100)),
                        new STRING("-dev", ElementsLimit.limit(100)),
                        new UINT32(2L, ElementsLimit.noLimit()),
                        new UINT32(3L, ElementsLimit.noLimit()),
                        new UINT32(99L, ElementsLimit.noLimit()));

        assertEquals(64, packet.getHeader().getTotalSize());
        assertEquals(4, packet.getHeader().getPacketKind());
    }

    @Test
    public void testPacketWithFieldValuesFromFields() throws IOException {
        SERVER_JOIN_REQ packet =
                new SERVER_JOIN_REQ(
                        new STRING("FreecivJava", ElementsLimit.limit(100)),
                        new STRING("+Freeciv.Devel-2.4-2011.Aug.02 ", ElementsLimit.limit(100)),
                        new STRING("-dev", ElementsLimit.limit(100)),
                        new UINT32(2L, ElementsLimit.noLimit()),
                        new UINT32(3L, ElementsLimit.noLimit()),
                        new UINT32(99L, ElementsLimit.noLimit()));

        assertEquals("FreecivJava", packet.getUsernameValue());
        assertEquals("+Freeciv.Devel-2.4-2011.Aug.02 ", packet.getCapabilityValue());
        assertEquals("-dev", packet.getVersion_labelValue());
        assertEquals(2L, packet.getMajor_versionValue().longValue());
        assertEquals(3L, packet.getMinor_versionValue().longValue());
        assertEquals(99L, packet.getPatch_versionValue().longValue());
    }

    @Test
    public void testPacketWithoutFieldsFromStream() throws IOException {
        DataInput inputStream = new DataInputStream(new ByteArrayInputStream(new byte[]{/*0, 3, 89*/}));
        CONN_PONG packet = new CONN_PONG(inputStream, new Header_2_1(3, 89));
        assertEquals(3, packet.getHeader().getTotalSize());
        assertEquals(89, packet.getHeader().getPacketKind());
    }

    @Test
    public void testPacketWithFieldsFromStream() throws IOException {
        DataInput inputStream = new DataInputStream(new ByteArrayInputStream(
                new byte[]{/*0, 64, 4, */70, 114, 101, 101, 99, 105, 118, 74, 97, 118, 97, 0, 43, 70, 114, 101, 101, 99,
                        105, 118, 46, 68, 101, 118, 101, 108, 45, 50, 46, 52, 45, 50, 48, 49, 49, 46, 65, 117, 103, 46,
                        48, 50, 32, 0, 45, 100, 101, 118, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 99}));
        SERVER_JOIN_REQ packet = new SERVER_JOIN_REQ(inputStream, new Header_2_1(64, 4));
        assertEquals(64, packet.getHeader().getTotalSize());
        assertEquals(4, packet.getHeader().getPacketKind());
    }

    @Test
    public void testPacketFieldValuesFromStream() throws IOException {
        DataInput inputStream = new DataInputStream(new ByteArrayInputStream(
                new byte[]{/*0, 64, 4, */70, 114, 101, 101, 99, 105, 118, 74, 97, 118, 97, 0, 43, 70, 114, 101, 101, 99,
                        105, 118, 46, 68, 101, 118, 101, 108, 45, 50, 46, 52, 45, 50, 48, 49, 49, 46, 65, 117, 103, 46,
                        48, 50, 32, 0, 45, 100, 101, 118, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 99}));
        SERVER_JOIN_REQ packet = new SERVER_JOIN_REQ(inputStream, new Header_2_1(64, 4));
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
        SERVER_JOIN_REQ packet = new SERVER_JOIN_REQ(inputStream, new Header_2_1(64, 5));
   }

    @Test(expected = FieldTypeException.class)
    public void testPacketWithFieldsFromStreamFailsOnWrongSize() throws IOException {
        DataInput inputStream = new DataInputStream(new ByteArrayInputStream(
                new byte[]{/*0, 64, 4, */70, 114, 101, 101, 99, 105, 118, 74, 97, 118, 97, 0, 43, 70, 114, 101, 101, 99,
                        105, 118, 46, 68, 101, 118, 101, 108, 45, 50, 46, 52, 45, 50, 48, 49, 49, 46, 65, 117, 103, 46,
                        48, 50, 32, 0, 45, 100, 101, 118, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 99}));
        SERVER_JOIN_REQ packet = new SERVER_JOIN_REQ(inputStream, new Header_2_1(62, 4));
    }

    @Test public void testPacketGetFields() {
        STRING username = new STRING("FreecivJava", ElementsLimit.limit(100));
        STRING capability = new STRING("+Freeciv.Devel-2.4-2011.Aug.02 ", ElementsLimit.limit(100));
        STRING version_label = new STRING("-dev", ElementsLimit.limit(100));
        UINT32 major_version = new UINT32(2L, ElementsLimit.noLimit());
        UINT32 minor_version = new UINT32(3L, ElementsLimit.noLimit());
        UINT32 patch_version = new UINT32(99L, ElementsLimit.noLimit());
        SERVER_JOIN_REQ packet =
                new SERVER_JOIN_REQ(
                        username,
                        capability,
                        version_label,
                        major_version,
                        minor_version,
                        patch_version);

        assertEquals(username.getValue(), packet.getUsername().getValue());
        assertEquals(capability.getValue(), packet.getCapability().getValue());
        assertEquals(version_label.getValue(), packet.getVersion_label().getValue());
        assertEquals(major_version.getValue(), packet.getMajor_version().getValue());
        assertEquals(minor_version.getValue(), packet.getMinor_version().getValue());
        assertEquals(patch_version.getValue(), packet.getPatch_version().getValue());
    }

    @Test public void testGeneratedPacketSerializesCorrectly() throws IOException {
        DataInput inputStream = new DataInputStream(new ByteArrayInputStream(
                new byte[]{/*0, 64, 4, */70, 114, 101, 101, 99, 105, 118, 74, 97, 118, 97, 0, 43, 70, 114, 101, 101, 99,
                        105, 118, 46, 68, 101, 118, 101, 108, 45, 50, 46, 52, 45, 50, 48, 49, 49, 46, 65, 117, 103, 46,
                        48, 50, 32, 0, 45, 100, 101, 118, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 99}));
        SERVER_JOIN_REQ packet = new SERVER_JOIN_REQ(inputStream, new Header_2_1(64, 4));

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
        SERVER_JOIN_REQ2ByteKind packet = new SERVER_JOIN_REQ2ByteKind(inputStream, new Header_2_2(65, 4));

        ByteArrayOutputStream serialized = new ByteArrayOutputStream();
        packet.encodeTo(new DataOutputStream(serialized));

        assertArrayEquals("Packet don't serialize correctly (missing header?)",
                new byte[]{0, 65, 00, 4, 70, 114, 101, 101, 99, 105, 118, 74, 97, 118, 97, 0, 43, 70, 114, 101, 101, 99,
                        105, 118, 46, 68, 101, 118, 101, 108, 45, 50, 46, 52, 45, 50, 48, 49, 49, 46, 65, 117, 103, 46,
                        48, 50, 32, 0, 45, 100, 101, 118, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 99},
                serialized.toByteArray());
    }

    @Test public void generatedPacketWithArrayFieldsSimpleFromJava() {
        TestArray packet = new TestArray(new Long[]{5L, 6L});
        assertArrayEquals("Result not the same as constructor", new Long[]{5L, 6L}, packet.getTheArrayValue());
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsSimpleFromJavaToSmallArray() {
        TestArray packet = new TestArray(new Long[]{5L});
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsSimpleFromJavaToBigArray() {
        TestArray packet = new TestArray(new Long[]{5L, 6L, 7L});
    }

    @Test public void generatedPacketWithArrayFieldsSimpleFromFields() {
        UINT32S uint32s = new UINT32S(new Long[]{5L, 6L}, ElementsLimit.limit(2));
        TestArray packet = new TestArray(uint32s);
        assertEquals("Result not the same as constructor",
                uint32s,
                packet.getTheArray());
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsSimpleFromFields_ArrayToSmall() {
        TestArray packet = new TestArray(new UINT32S(new Long[]{5L}, ElementsLimit.limit(1)));
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsSimpleFromFields_ArrayToBig() {
        TestArray packet = new TestArray(new UINT32S(new Long[]{5L, 6L, 7L}, ElementsLimit.limit(3)));
    }

    @Test public void generatedPacketWithArrayFieldsTransferFromJava() {
        TestArrayTransfer packet = new TestArrayTransfer(2, new Long[]{5L, 6L});
        assertArrayEquals("Result not the same as constructor", new Long[]{5L, 6L}, packet.getTheArrayValue());
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsTransferFromJavaToSmallArray() {
        TestArrayTransfer packet = new TestArrayTransfer(2, new Long[]{5L});
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsTransferFromJavaToBigArray() {
        TestArrayTransfer packet = new TestArrayTransfer(2, new Long[]{5L, 6L, 7L});
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsTransfer_ArrayToBig() {
        TestArrayTransfer packet = new TestArrayTransfer(3, new Long[]{5L, 6L, 7L, 6L, 7L});
    }

    @Test public void generatedPacketWithArrayFieldsTransferFromFields() {
        UINT32S uint32s = new UINT32S(new Long[]{5L, 6L}, ElementsLimit.limit(2));
        TestArrayTransfer packet = new TestArrayTransfer(new UINT8(2, ElementsLimit.noLimit()), uint32s);
        assertEquals("Result not the same as constructor",
                uint32s,
                packet.getTheArray());
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsTransferFromFields_ArrayToSmall() {
        TestArrayTransfer packet = new TestArrayTransfer(new UINT8(2, ElementsLimit.noLimit()),
                new UINT32S(new Long[]{5L}, ElementsLimit.limit(1)));
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsTransferFromFields_ArrayToBig() {
        TestArrayTransfer packet = new TestArrayTransfer(new UINT8(2, ElementsLimit.noLimit()),
                new UINT32S(new Long[]{5L, 6L, 7L}, ElementsLimit.limit(3)));
    }

    @Test(expected = IllegalLimitSizeException.class)
    public void generatedPacketWithArrayFieldsTransferFromFields_ToBigTransfer() {
        UINT32S uint32s = new UINT32S(new Long[]{5L, 6L, 5L, 6L, 8L}, ElementsLimit.limit(5));
        TestArrayTransfer packet = new TestArrayTransfer(new UINT8(5, ElementsLimit.noLimit()), uint32s);
    }

    @Test public void generatedPacketWithArrayFieldsDoubleFromJava() {
        Long[][] array = {{5L, 6L, 7L},
                          {8L, 9L, 10L}};
        TestArrayDouble packet = new TestArrayDouble(array);
        assertArrayEquals("Result not the same as constructor", array, packet.getTheArrayValue());
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsDoubleFromJava_1stArrayToSmall() {
        Long[][] array = {{5L, 6L, 7L}};
        TestArrayDouble packet = new TestArrayDouble(array);
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsDoubleFromJava_2ndArrayToSmall() {
        Long[][] array = {{5L, 6L},
                          {5L, 6L}};
        TestArrayDouble packet = new TestArrayDouble(array);
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsDoubleFromJava_1stArrayToBig() {
        Long[][] array = {{5L, 6L, 7L},
                          {5L, 6L, 7L},
                          {5L, 6L, 7L}};
        TestArrayDouble packet = new TestArrayDouble(array);
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsDoubleFromJava_2ndArrayToBig() {
        Long[][] array = {{5L, 6L, 7L, 8L},
                          {5L, 6L, 7L, 9L}};
        TestArrayDouble packet = new TestArrayDouble(array);
    }

    @Test public void generatedPacketWithArrayFieldsDoubleTransferFromJava() {
        Long[][] array = {{5L, 6L, 7L},
                          {8L, 9L, 10L}};
        TestArrayDoubleTransfer packet = new TestArrayDoubleTransfer(2, 3, array);
        assertArrayEquals("Result not the same as constructor", array, packet.getTheArrayValue());
    }

    @Test public void generatedPacketWithArrayFieldsDoubleTransferFromJavaMax() {
        Long[][] array = {
                {5L, 6L, 7L, 8L},
                {9L, 10L, 11L, 12L},
                {13L, 14L, 15L, 16L}};
        TestArrayDoubleTransfer packet = new TestArrayDoubleTransfer(3, 4, array);
        assertArrayEquals("Result not the same as constructor", array, packet.getTheArrayValue());
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsDoubleTransfer_1stArrayToSmall() {
        Long[][] array = {{8L, 9L, 10L}};
        TestArrayDoubleTransfer packet = new TestArrayDoubleTransfer(2, 3, array);
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsDoubleTransfer_2ndArrayToSmall() {
        Long[][] array = {
                {5L, 6L},
                {8L, 9L}};
        TestArrayDoubleTransfer packet = new TestArrayDoubleTransfer(2, 3, array);
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsDoubleTransfer_1stArrayToBig() {
        Long[][] array = {
                {5L, 6L, 7L},
                {8L, 9L, 10L},
                {11L, 12L, 13L}};
        TestArrayDoubleTransfer packet = new TestArrayDoubleTransfer(2, 3, array);
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketWithArrayFieldsDoubleTransfer_2ndArrayToBig() {
        Long[][] array = {
                {5L, 6L, 7L, 1L},
                {8L, 9L, 10L, 2L}};
        TestArrayDoubleTransfer packet = new TestArrayDoubleTransfer(2, 3, array);
    }

    @Test(expected = IllegalLimitSizeException.class)
    public void generatedPacketWithArrayFieldsDoubleTransfer_1stSizeOverMax() {
        Long[][] array = {
                {5L, 6L, 7L},
                {8L, 9L, 10L},
                {11L, 12L, 13L},
                {14L, 15L, 16L},
                {17L, 19L, 18L}};
        TestArrayDoubleTransfer packet = new TestArrayDoubleTransfer(5, 3, array);
    }

    @Test(expected = IllegalLimitSizeException.class)
    public void generatedPacketWithArrayFieldsDoubleTransfer_2ndSizeOverMax() {
        Long[][] array = {
                {5L, 6L, 7L, 8L, 9L, 10L},
                {11L, 12L, 17L, 18L, 19L, 30L}};
        TestArrayDoubleTransfer packet = new TestArrayDoubleTransfer(2, 6, array);
    }

    @Test public void generatedPacketWithStringAndArrayOfString() {
        StringArray packet = new StringArray("Not an Array",
                new String[]{"Element 1", "Element 2", "Element 3"});

        assertEquals("Plain string different", "Not an Array", packet.getNotAnArrayValue());
        assertArrayEquals("Array different ",
                new String[]{"Element 1", "Element 2", "Element 3"},
                packet.getTheArrayValue());
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketSimpleStringToBig() {
        StringArray packet = new StringArray("Not an ArrayNot an Array",
                new String[]{"Element 1", "Element 2", "Element 3"});
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketFistStringInArrayToBig() {
        StringArray packet = new StringArray("Not an Array",
                new String[]{"Element 1Element 1", "Element 2", "Element 3"});
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketMiddleStringInArrayToBig() {
        StringArray packet = new StringArray("Not an Array",
                new String[]{"Element 1", "Element 2Element 2", "Element 3"});
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketLastStringInArrayToBig() {
        StringArray packet = new StringArray("Not an Array",
                new String[]{"Element 1", "Element 2", "Element 3Element 3"});
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacket_ArrayToBig() {
        StringArray packet = new StringArray("Not an Array",
                new String[]{"Element 1", "Element 2", "Element 3", "Element 4"});
    }

    @Test(expected = IllegalNumberOfElementsException.class)
    public void generatedPacketStringToBig() {
        StringArray packet = new StringArray(new STRING("Not an ArrayNot an Array", ElementsLimit.limit(30)),
                new STRINGS(new String[]{
                        "Element 1",
                        "Element 2",
                        "Element 3"
                }, ElementsLimit.limit(3, ElementsLimit.limit(10))));
    }

    @Test
    public void generatedPacketExceptionTellsWhere() {
        try {
            StringArray packet = new StringArray(new STRING("Not an ArrayNot an Array", ElementsLimit.limit(30)),
                    new STRINGS(new String[]{
                            "Element 1",
                            "Element 2",
                            "Element 3"
                    }, ElementsLimit.limit(3, ElementsLimit.limit(10))));
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

        DeltaVectorTest packet = new DeltaVectorTest(inn, new Header_2_2(16, 933));

        assertEquals(50, packet.getId().getValue().intValue());
        assertEquals("works", packet.getField1().getValue());
        assertEquals(256, packet.getField2().getValue().intValue());
    }

    @Test
    public void delta_fromData_nothingBefore_missingIsZero() throws IOException {
        ByteArrayOutputStream storeTo = new ByteArrayOutputStream();
        storeTo.write(new byte[]{2, 50, 0, 0, 1, 0});
        DataInputStream inn = new DataInputStream(new ByteArrayInputStream(storeTo.toByteArray()));

        DeltaVectorTest packet = new DeltaVectorTest(inn, new Header_2_2(10, 933));

        assertEquals(50, packet.getId().getValue().intValue());
        assertEquals("", packet.getField1().getValue());
        assertEquals(256, packet.getField2().getValue().intValue());
    }

    @Test
    public void delta_fromData_differentKeySameKindBefore_missingIsZero() throws IOException {
        ByteArrayOutputStream storeTo = new ByteArrayOutputStream();
        storeTo.write(new byte[]{3, 50, 'w', 'o', 'r', 'k', 's', 0, 0, 0, 1, 0}); // packet 1
        storeTo.write(new byte[]{2, 100, 0, 0, 1, 0}); // packet 2
        DataInputStream inn = new DataInputStream(new ByteArrayInputStream(storeTo.toByteArray()));

        HashMap<DeltaKey, Packet> old = new HashMap<DeltaKey, Packet>();
        new DeltaVectorTest(inn, new Header_2_2(16, 933), old);
        DeltaVectorTest packet = new DeltaVectorTest(inn, new Header_2_2(10, 933), old);

        assertEquals(100, packet.getId().getValue().intValue());
        assertEquals("", packet.getField1().getValue());
        assertEquals(256, packet.getField2().getValue().intValue());
    }

    @Test
    public void delta_fromData_sameKeySameKindBefore_missingIsPrevious() throws IOException {
        ByteArrayOutputStream storeTo = new ByteArrayOutputStream();
        storeTo.write(new byte[]{3, 50, 'w', 'o', 'r', 'k', 's', 0, 0, 0, 1, 0}); // packet 1
        storeTo.write(new byte[]{2, 50, 0, 0, 1, 0}); // packet 2
        DataInputStream inn = new DataInputStream(new ByteArrayInputStream(storeTo.toByteArray()));

        HashMap<DeltaKey, Packet> old = new HashMap<DeltaKey, Packet>();
        new DeltaVectorTest(inn, new Header_2_2(16, 933), old);
        DeltaVectorTest packet = new DeltaVectorTest(inn, new Header_2_2(10, 933), old);

        assertEquals(50, packet.getId().getValue().intValue());
        assertEquals("works", packet.getField1().getValue());
        assertEquals(256, packet.getField2().getValue().intValue());
    }

    @Test
    public void delta_deltaVector_noPrevious() throws IOException {
        DeltaVectorTest packet = new DeltaVectorTest(8, "works", 1260L);

        boolean[] dv = packet.getDeltaVector();

        assertEquals("Wrong delta vector size", 2, dv.length);
        assertTrue("Should be sent", dv[0]);
        assertTrue("Should be sent", dv[1]);
    }

    @Test
    public void delta_fromJavaTypes_someThere_data() throws IOException {
        DeltaVectorTest packet = new DeltaVectorTest(8, "works", null);

        // the data
        assertEquals(8, packet.getId().getValue().intValue());
        assertEquals("works", packet.getField1().getValue());
        assertNull(packet.getField2());
    }

    @Test
    public void delta_fromJavaTypes_someThere_metaData() throws IOException {
        DeltaVectorTest packet = new DeltaVectorTest(8, "works", null);

        // meta data
        assertEquals(8, packet.getHeader().getBodySize());
    }

    @Test
    public void delta_fromJavaTypes_someThere_encode() throws IOException {
        DeltaVectorTest packet = new DeltaVectorTest(8, "works", null);

        ByteArrayOutputStream storeTo = new ByteArrayOutputStream();
        packet.encodeTo(new DataOutputStream(storeTo));

        // encode
        assertArrayEquals(new byte[]{0, 12, 3, -91, 1, 8, 'w', 'o', 'r', 'k', 's', 0}, storeTo.toByteArray());
    }

    @Test
    public void delta_fromFields_someThere_data() throws IOException {
        DeltaVectorTest packet = new DeltaVectorTest(
                new UINT8(8, ElementsLimit.noLimit()),
                new STRING("works", ElementsLimit.limit(100)),
                null);

        // the data
        assertEquals(8, packet.getId().getValue().intValue());
        assertEquals("works", packet.getField1().getValue());
        assertNull(packet.getField2());
    }

    @Test
    public void delta_fromFields_someThere_metadata() throws IOException {
        DeltaVectorTest packet = new DeltaVectorTest(
                new UINT8(8, ElementsLimit.noLimit()),
                new STRING("works", ElementsLimit.limit(100)),
                null);

        // meta data
        assertEquals(8, packet.getHeader().getBodySize());
    }

    @Test
    public void delta_fromFields_someThere_encode() throws IOException {
        DeltaVectorTest packet = new DeltaVectorTest(
                new UINT8(8, ElementsLimit.noLimit()),
                new STRING("works", ElementsLimit.limit(100)),
                null);

        ByteArrayOutputStream storeTo = new ByteArrayOutputStream();
        packet.encodeTo(new DataOutputStream(storeTo));

        // encode
        assertArrayEquals(new byte[]{0, 12, 3, -91, 1, 8, 'w', 'o', 'r', 'k', 's', 0}, storeTo.toByteArray());
    }
}
