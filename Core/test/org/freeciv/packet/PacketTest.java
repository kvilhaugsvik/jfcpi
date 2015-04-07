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
import org.junit.Test;

import java.io.*;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class PacketTest {
    private static final HeaderData TWO_ONE = new HeaderData(Header_2_1.class);
    private static final HeaderData TWO_TWO = new HeaderData(Header_2_2.class);

    @Test public void testRawPacketFromData() throws IOException, InvocationTargetException {
        RawPacket packet = new RawPacket(
                new byte[]{0, 64, 4, 70, 114, 101, 101, 99, 105, 118, 74, 97, 118, 97, 0, 43, 70, 114, 101, 101, 99,
                        105, 118, 46, 68, 101, 118, 101, 108, 45, 50, 46, 52, 45, 50, 48, 49, 49, 46, 65, 117, 103, 46,
                        48, 50, 32, 0, 45, 100, 101, 118, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 99},
                TWO_ONE);

        assertEquals(64, packet.getHeader().getTotalSize());
        assertEquals(4, packet.getHeader().getPacketKind());
    }

    /**
     * Test that it is possible to create a RawPacket from a header.
     * @throws IOException when toBytes(), not the thing tested here, had a problem.
     */
    @Test public void test_RawPacket_fromHeader_basic() throws IOException {
        /* The header to create the RawPacket from. */
        PacketHeader header = new Header_2_2(4, 88);

        /* Do the packet creation. */
        RawPacket packet = new RawPacket(header);

        /* Check that the encoded result is as expected. */
        assertArrayEquals(new byte[]{0, 4, 0, 88}, packet.toBytes());
    }

    /**
     * Test that RawPacket from header fails when the packet's size
     * indicates that it should have had a body.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void test_RawPacket_fromHeader_hasBody(){
        /* The header to create the RawPacket from header alone fails if a
         * body is expected. */
        PacketHeader header = new Header_2_2(5, 42);

        /* Try to create the packet. Should fail. */
        RawPacket packet = new RawPacket(header);
    }

    @Test public void testRawPacketSerializesCorrectly() throws IOException, InvocationTargetException {
        final byte[] data = {0, 64, 4, 70, 114, 101, 101, 99, 105, 118, 74, 97, 118, 97, 0, 43, 70, 114, 101, 101, 99,
                105, 118, 46, 68, 101, 118, 101, 108, 45, 50, 46, 52, 45, 50, 48, 49, 49, 46, 65, 117, 103, 46,
                48, 50, 32, 0, 45, 100, 101, 118, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 99};
        RawPacket packet = new RawPacket(data, TWO_ONE);

        ByteArrayOutputStream serialized = new ByteArrayOutputStream();
        packet.encodeTo(new DataOutputStream(serialized));

        assertArrayEquals("Packet don't serialize correctly (missing header?)",
                data,
            serialized.toByteArray());
    }

    @Test public void testRawPacketSerializesCorrectly2ByteKind() throws IOException, InvocationTargetException {
        final byte[] data = {0, 65, 00, 4, 70, 114, 101, 101, 99, 105, 118, 74, 97, 118, 97, 0, 43, 70, 114, 101, 101, 99,
                105, 118, 46, 68, 101, 118, 101, 108, 45, 50, 46, 52, 45, 50, 48, 49, 49, 46, 65, 117, 103, 46,
                48, 50, 32, 0, 45, 100, 101, 118, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 99};
        RawPacket packet = new RawPacket(data, TWO_TWO);

        ByteArrayOutputStream serialized = new ByteArrayOutputStream();
        packet.encodeTo(new DataOutputStream(serialized));

        assertArrayEquals("Packet don't serialize correctly (missing header?)",
                data,
                serialized.toByteArray());
    }
}
