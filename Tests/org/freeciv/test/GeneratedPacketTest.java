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

package org.freeciv.test;

import org.freeciv.packet.*;
import org.freeciv.packet.fieldtype.*;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class GeneratedPacketTest {
    @Test
    public void testPacketWithoutFields() throws IOException {
        CONN_PONG packet = new CONN_PONG();

        assertEquals(3, packet.getEncodedSize());
        assertEquals(89, packet.getNumber());
    }

    @Test
    public void testPacketWithFieldsFromJavaTypes() throws IOException {
        SERVER_JOIN_REQ packet =
                new SERVER_JOIN_REQ("FreecivJava", "+Freeciv.Devel-2.4-2011.Aug.02 ", "-dev", 2L, 3L, 99L);

        assertEquals(64, packet.getEncodedSize());
        assertEquals(4, packet.getNumber());
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
                        new STRING("FreecivJava", 100),
                        new STRING("+Freeciv.Devel-2.4-2011.Aug.02 ", 100),
                        new STRING("-dev", 100),
                        new UINT32(2L),
                        new UINT32(3L),
                        new UINT32(99L));

        assertEquals(64, packet.getEncodedSize());
        assertEquals(4, packet.getNumber());
    }

    @Test
    public void testPacketWithFieldValuesFromFields() throws IOException {
        SERVER_JOIN_REQ packet =
                new SERVER_JOIN_REQ(
                        new STRING("FreecivJava", 100),
                        new STRING("+Freeciv.Devel-2.4-2011.Aug.02 ", 100),
                        new STRING("-dev", 100),
                        new UINT32(2L),
                        new UINT32(3L),
                        new UINT32(99L));

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
        CONN_PONG packet = new CONN_PONG(inputStream, 3, 89);
        assertEquals(3, packet.getEncodedSize());
        assertEquals(89, packet.getNumber());
    }

    @Test
    public void testPacketWithFieldsFromStream() throws IOException {
        DataInput inputStream = new DataInputStream(new ByteArrayInputStream(
                new byte[]{/*0, 64, 4, */70, 114, 101, 101, 99, 105, 118, 74, 97, 118, 97, 0, 43, 70, 114, 101, 101, 99,
                        105, 118, 46, 68, 101, 118, 101, 108, 45, 50, 46, 52, 45, 50, 48, 49, 49, 46, 65, 117, 103, 46,
                        48, 50, 32, 0, 45, 100, 101, 118, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 99}));
        SERVER_JOIN_REQ packet = new SERVER_JOIN_REQ(inputStream, 64, 4);
        assertEquals(64, packet.getEncodedSize());
        assertEquals(4, packet.getNumber());
    }

    @Test
    public void testPacketFieldValuesFromStream() throws IOException {
        DataInput inputStream = new DataInputStream(new ByteArrayInputStream(
                new byte[]{/*0, 64, 4, */70, 114, 101, 101, 99, 105, 118, 74, 97, 118, 97, 0, 43, 70, 114, 101, 101, 99,
                        105, 118, 46, 68, 101, 118, 101, 108, 45, 50, 46, 52, 45, 50, 48, 49, 49, 46, 65, 117, 103, 46,
                        48, 50, 32, 0, 45, 100, 101, 118, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 99}));
        SERVER_JOIN_REQ packet = new SERVER_JOIN_REQ(inputStream, 64, 4);
        assertEquals("FreecivJava", packet.getUsernameValue());
        assertEquals("+Freeciv.Devel-2.4-2011.Aug.02 ", packet.getCapabilityValue());
        assertEquals("-dev", packet.getVersion_labelValue());
        assertEquals(2L, packet.getMajor_versionValue().longValue());
        assertEquals(3L, packet.getMinor_versionValue().longValue());
        assertEquals(99L, packet.getPatch_versionValue().longValue());
    }

    @Test(expected = IOException.class)
    public void testPacketWithFieldsFromStreamFailsOnWrongPackageNumber() throws IOException {
        DataInput inputStream = new DataInputStream(new ByteArrayInputStream(
                new byte[]{/*0, 64, 4, */70, 114, 101, 101, 99, 105, 118, 74, 97, 118, 97, 0, 43, 70, 114, 101, 101, 99,
                        105, 118, 46, 68, 101, 118, 101, 108, 45, 50, 46, 52, 45, 50, 48, 49, 49, 46, 65, 117, 103, 46,
                        48, 50, 32, 0, 45, 100, 101, 118, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 99}));
        SERVER_JOIN_REQ packet = new SERVER_JOIN_REQ(inputStream, 64, 5);
   }

    @Test(expected = IOException.class)
    public void testPacketWithFieldsFromStreamFailsOnWrongSize() throws IOException {
        DataInput inputStream = new DataInputStream(new ByteArrayInputStream(
                new byte[]{/*0, 64, 4, */70, 114, 101, 101, 99, 105, 118, 74, 97, 118, 97, 0, 43, 70, 114, 101, 101, 99,
                        105, 118, 46, 68, 101, 118, 101, 108, 45, 50, 46, 52, 45, 50, 48, 49, 49, 46, 65, 117, 103, 46,
                        48, 50, 32, 0, 45, 100, 101, 118, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 99}));
        SERVER_JOIN_REQ packet = new SERVER_JOIN_REQ(inputStream, 62, 4);
    }

    @Test public void testPacketGetFields() {
        STRING username = new STRING("FreecivJava", 100);
        STRING capability = new STRING("+Freeciv.Devel-2.4-2011.Aug.02 ", 100);
        STRING version_label = new STRING("-dev", 100);
        UINT32 major_version = new UINT32(2L);
        UINT32 minor_version = new UINT32(3L);
        UINT32 patch_version = new UINT32(99L);
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
        SERVER_JOIN_REQ packet = new SERVER_JOIN_REQ(inputStream, 64, 4);

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
        SERVER_JOIN_REQ2ByteKind packet = new SERVER_JOIN_REQ2ByteKind(inputStream, 65, 4);

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

    @Test(expected = IllegalArgumentException.class)
    public void generatedPacketWithArrayFieldsSimpleFromJavaToSmallArray() {
        TestArray packet = new TestArray(new Long[]{5L});
    }

    @Test(expected = IllegalArgumentException.class)
    public void generatedPacketWithArrayFieldsSimpleFromJavaToBigArray() {
        TestArray packet = new TestArray(new Long[]{5L, 6L, 7L});
    }

    @Test public void generatedPacketWithArrayFieldsSimpleFromFields() {
        UINT32[] uint32s = {new UINT32(5L), new UINT32(6L)};
        TestArray packet = new TestArray(uint32s);
        assertArrayEquals("Result not the same as constructor",
                uint32s,
                packet.getTheArray());
    }

    @Test(expected = IllegalArgumentException.class)
    public void generatedPacketWithArrayFieldsSimpleFromFieldsToSmallArray() {
        TestArray packet = new TestArray(new UINT32[]{new UINT32(5L)});
    }

    @Test(expected = IllegalArgumentException.class)
    public void generatedPacketWithArrayFieldsSimpleFromFieldsToBigArray() {
        TestArray packet = new TestArray(new UINT32[]{new UINT32(5L), new UINT32(6L), new UINT32(7L)});
    }

    @Test public void generatedPacketWithArrayFieldsTransferFromJava() {
        TestArrayTransfer packet = new TestArrayTransfer(2, new Long[]{5L, 6L});
        assertArrayEquals("Result not the same as constructor", new Long[]{5L, 6L}, packet.getTheArrayValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void generatedPacketWithArrayFieldsTransferFromJavaToSmallArray() {
        TestArrayTransfer packet = new TestArrayTransfer(2, new Long[]{5L});
    }

    @Test(expected = IllegalArgumentException.class)
    public void generatedPacketWithArrayFieldsTransferFromJavaToBigArray() {
        TestArrayTransfer packet = new TestArrayTransfer(2, new Long[]{5L, 6L, 7L});
    }

    @Test(expected = IllegalArgumentException.class)
    public void generatedPacketWithArrayFieldsTransferToBigArray() {
        TestArrayTransfer packet = new TestArrayTransfer(5, new Long[]{5L, 6L, 7L, 6L, 7L});
    }

    @Test public void generatedPacketWithArrayFieldsTransferFromFields() {
        UINT32[] uint32s = {new UINT32(5L), new UINT32(6L)};
        TestArrayTransfer packet = new TestArrayTransfer(new UINT8(2), uint32s);
        assertArrayEquals("Result not the same as constructor",
                uint32s,
                packet.getTheArray());
    }

    @Test(expected = IllegalArgumentException.class)
    public void generatedPacketWithArrayFieldsTransferFromFieldsToSmallArray() {
        TestArrayTransfer packet = new TestArrayTransfer(new UINT8(2),
                new UINT32[]{new UINT32(5L)});
    }

    @Test(expected = IllegalArgumentException.class)
    public void generatedPacketWithArrayFieldsTransferFromFieldsToBigArray() {
        TestArrayTransfer packet = new TestArrayTransfer(new UINT8(2),
                new UINT32[]{new UINT32(5L), new UINT32(6L), new UINT32(7L)});
    }

    @Test(expected = IllegalArgumentException.class)
    public void generatedPacketWithArrayFieldsTransferFromFieldsToBigTransfer() {
        UINT32[] uint32s = {new UINT32(5L), new UINT32(6L), new UINT32(5L), new UINT32(6L)};
        TestArrayTransfer packet = new TestArrayTransfer(new UINT8(4), uint32s);
    }

    @Test public void generatedPacketWithArrayFieldsDoubleFromJava() {
        Long[][] array = {{5L, 6L, 7L},
                          {8L, 9L, 10L}};
        TestArrayDouble packet = new TestArrayDouble(array);
        assertArrayEquals("Result not the same as constructor", array, packet.getTheArrayValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void generatedPacketWithArrayFieldsDoubleFromJavaToSmall1stArray() {
        Long[][] array = {{5L, 6L, 7L}};
        TestArrayDouble packet = new TestArrayDouble(array);
    }

    @Test(expected = IllegalArgumentException.class)
    public void generatedPacketWithArrayFieldsDoubleFromJavaToSmall2ndArray() {
        Long[][] array = {{5L, 6L},
                          {5L, 6L}};
        TestArrayDouble packet = new TestArrayDouble(array);
    }

    @Test(expected = IllegalArgumentException.class)
    public void generatedPacketWithArrayFieldsDoubleFromJavaToBig1stArray() {
        Long[][] array = {{5L, 6L, 7L},
                          {5L, 6L, 7L},
                          {5L, 6L, 7L}};
        TestArrayDouble packet = new TestArrayDouble(array);
    }

    @Test(expected = IllegalArgumentException.class)
    public void generatedPacketWithArrayFieldsDoubleFromJavaToBig2ndArray() {
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

    @Test(expected = IllegalArgumentException.class)
    public void generatedPacketWithArrayFieldsDoubleTransfer1stArrayToSmall() {
        Long[][] array = {{8L, 9L, 10L}};
        TestArrayDoubleTransfer packet = new TestArrayDoubleTransfer(2, 3, array);
    }

    @Test(expected = IllegalArgumentException.class)
    public void generatedPacketWithArrayFieldsDoubleTransfer2ndArrayToSmall() {
        Long[][] array = {
                {5L, 6L},
                {8L, 9L}};
        TestArrayDoubleTransfer packet = new TestArrayDoubleTransfer(2, 3, array);
    }

    @Test(expected = IllegalArgumentException.class)
    public void generatedPacketWithArrayFieldsDoubleTransfer1stArrayToBig() {
        Long[][] array = {
                {5L, 6L, 7L},
                {8L, 9L, 10L},
                {11L, 12L, 13L}};
        TestArrayDoubleTransfer packet = new TestArrayDoubleTransfer(2, 3, array);
    }

    @Test(expected = IllegalArgumentException.class)
    public void generatedPacketWithArrayFieldsDoubleTransfer2ndArrayToBig() {
        Long[][] array = {
                {5L, 6L, 7L, 1L},
                {8L, 9L, 10L, 2L}};
        TestArrayDoubleTransfer packet = new TestArrayDoubleTransfer(2, 3, array);
    }

    @Test(expected = IllegalArgumentException.class)
    public void generatedPacketWithArrayFieldsDoubleTransfer1stSizeOverMax() {
        Long[][] array = {
                {5L, 6L, 7L},
                {8L, 9L, 10L},
                {11L, 12L, 13L},
                {14L, 15L, 16L},
                {17L, 19L, 18L}};
        TestArrayDoubleTransfer packet = new TestArrayDoubleTransfer(5, 3, array);
    }

    @Test(expected = IllegalArgumentException.class)
    public void generatedPacketWithArrayFieldsDoubleTransfer2ndSizeOverMax() {
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

    @Test(expected = IllegalArgumentException.class)
    public void generatedPacketSimpleStringToBig() {
        StringArray packet = new StringArray("Not an ArrayNot an Array",
                new String[]{"Element 1", "Element 2", "Element 3"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void generatedPacketFistStringInArrayToBig() {
        StringArray packet = new StringArray("Not an Array",
                new String[]{"Element 1Element 1", "Element 2", "Element 3"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void generatedPacketMiddleStringInArrayToBig() {
        StringArray packet = new StringArray("Not an Array",
                new String[]{"Element 1", "Element 2Element 2", "Element 3"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void generatedPacketLastStringInArrayToBig() {
        StringArray packet = new StringArray("Not an Array",
                new String[]{"Element 1", "Element 2", "Element 3Element 3"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void generatedPacketArrayToBig() {
        StringArray packet = new StringArray("Not an Array",
                new String[]{"Element 1", "Element 2", "Element 3", "Element 4"});
    }
}
