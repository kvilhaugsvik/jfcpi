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

import org.freeciv.packet.CONN_PONG;
import org.freeciv.packet.SERVER_JOIN_REQ;
import org.freeciv.packet.STRING;
import org.freeciv.packet.UINT32;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;

import static junit.framework.Assert.assertEquals;

public class PacketTest {
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
                        new STRING("FreecivJava"),
                        new STRING("+Freeciv.Devel-2.4-2011.Aug.02 "),
                        new STRING("-dev"),
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
                        new STRING("FreecivJava"),
                        new STRING("+Freeciv.Devel-2.4-2011.Aug.02 "),
                        new STRING("-dev"),
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
        STRING username = new STRING("FreecivJava");
        STRING capability = new STRING("+Freeciv.Devel-2.4-2011.Aug.02 ");
        STRING version_label = new STRING("-dev");
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
}
