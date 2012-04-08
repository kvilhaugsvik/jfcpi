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

import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class PacketTest {
    @Test public void testRawPacketFromData() throws IOException {
        DataInput inputStream = new DataInputStream(new ByteArrayInputStream(
                new byte[]{/*0, 64, 4, */70, 114, 101, 101, 99, 105, 118, 74, 97, 118, 97, 0, 43, 70, 114, 101, 101, 99,
                        105, 118, 46, 68, 101, 118, 101, 108, 45, 50, 46, 52, 45, 50, 48, 49, 49, 46, 65, 117, 103, 46,
                        48, 50, 32, 0, 45, 100, 101, 118, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 99}));
        RawPacket packet = new RawPacket(inputStream, 64, 4, false);

        assertEquals(64, packet.getEncodedSize());
        assertEquals(4, packet.getNumber());
    }

    @Test public void testRawPacketSerializesCorrectly() throws IOException {
        DataInput inputStream = new DataInputStream(new ByteArrayInputStream(
                new byte[]{/*0, 64, 4, */70, 114, 101, 101, 99, 105, 118, 74, 97, 118, 97, 0, 43, 70, 114, 101, 101, 99,
                        105, 118, 46, 68, 101, 118, 101, 108, 45, 50, 46, 52, 45, 50, 48, 49, 49, 46, 65, 117, 103, 46,
                        48, 50, 32, 0, 45, 100, 101, 118, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 99}));
        RawPacket packet = new RawPacket(inputStream, 64, 4, false);

        ByteArrayOutputStream serialized = new ByteArrayOutputStream();
        packet.encodeTo(new DataOutputStream(serialized));

        assertArrayEquals("Packet don't serialize correctly (missing header?)",
            new byte[]{0, 64, 4, 70, 114, 101, 101, 99, 105, 118, 74, 97, 118, 97, 0, 43, 70, 114, 101, 101, 99,
                105, 118, 46, 68, 101, 118, 101, 108, 45, 50, 46, 52, 45, 50, 48, 49, 49, 46, 65, 117, 103, 46,
                48, 50, 32, 0, 45, 100, 101, 118, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 99},
            serialized.toByteArray());
    }

    @Test public void testRawPacketSerializesCorrectly2ByteKind() throws IOException {
        DataInput inputStream = new DataInputStream(new ByteArrayInputStream(
                new byte[]{/*0, 65, 00, 4, */70, 114, 101, 101, 99, 105, 118, 74, 97, 118, 97, 0, 43, 70, 114, 101, 101, 99,
                        105, 118, 46, 68, 101, 118, 101, 108, 45, 50, 46, 52, 45, 50, 48, 49, 49, 46, 65, 117, 103, 46,
                        48, 50, 32, 0, 45, 100, 101, 118, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 99}));
        RawPacket packet = new RawPacket(inputStream, 65, 4, true);

        ByteArrayOutputStream serialized = new ByteArrayOutputStream();
        packet.encodeTo(new DataOutputStream(serialized));

        assertArrayEquals("Packet don't serialize correctly (missing header?)",
                new byte[]{0, 65, 00, 4, 70, 114, 101, 101, 99, 105, 118, 74, 97, 118, 97, 0, 43, 70, 114, 101, 101, 99,
                        105, 118, 46, 68, 101, 118, 101, 108, 45, 50, 46, 52, 45, 50, 48, 49, 49, 46, 65, 117, 103, 46,
                        48, 50, 32, 0, 45, 100, 101, 118, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 99},
                serialized.toByteArray());
    }
}
