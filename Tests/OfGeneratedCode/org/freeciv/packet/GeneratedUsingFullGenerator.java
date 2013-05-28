/*
 * Copyright (c) 2013. Sveinung Kvilhaugsvik
 * Portions are data from Freeciv's common/packets.def. Copyright
 * of those (if copyrightable) belong to their respective copyright
 * holders.
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
import java.util.HashMap;

import static org.junit.Assert.*;

public class GeneratedUsingFullGenerator {
    /*------------------------------------------------------------------------------------------------------------------
    Simple
    ------------------------------------------------------------------------------------------------------------------*/
    @Test
    public void simple_packet_noFields() throws NoSuchMethodException {
        PACKET_NO_FIELDS p = PACKET_NO_FIELDS.fromValues(Header_2_2.class.getConstructor(int.class, int.class));

        assertEquals("Wrong kind", 1001, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4, p.getHeader().getTotalSize());
    }

    @Test
    public void simple_packet_oneField() throws NoSuchMethodException {
        PACKET_ONE_FIELD p = PACKET_ONE_FIELD.fromValues(5, Header_2_2.class.getConstructor(int.class, int.class));

        assertEquals("Wrong kind", 1002, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 4, p.getHeader().getTotalSize());

        assertEquals("Wrong field value", 5, p.getOneValue().intValue());
    }

    @Test
    public void simple_packet_twoFields() throws NoSuchMethodException {
        PACKET_TWO_FIELDS p = PACKET_TWO_FIELDS.fromValues(5000, 77, Header_2_2.class.getConstructor(int.class, int.class));

        assertEquals("Wrong kind", 1003, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 4 + 1, p.getHeader().getTotalSize());

        assertEquals("Wrong field value", 5000, p.getOneValue().intValue());
        assertEquals("Wrong field value", 77, p.getTwoValue().byteValue());
    }

    /*------------------------------------------------------------------------------------------------------------------
    Capabilities.
    Structure:
      Packet
        Capability variant
          from values helper
          serialized data helper
          from values test
          header data test
          body data test
          serialization test
          deserialization test
    ------------------------------------------------------------------------------------------------------------------*/
    private static PACKET_CAP_ADD capabilities_packetCapAdd_noCapabilities_fromFields() throws NoSuchMethodException {
        return PACKET_CAP_ADD.fromValues(1260, Header_2_2.class.getConstructor(int.class, int.class));
    }

    private static byte[] capabilities_packetCapAdd_noCapabilities_serialized() {
        return new byte[]{
                0, 0x9, 0x03, (byte)0xf2,
                1,
                0, 0, 0x4, (byte)0xec
        };
    }

    @Test
    public void capabilities_packetCapAdd_noCapabilities_fromFields_isCreated() throws NoSuchMethodException {
        capabilities_packetCapAdd_noCapabilities_fromFields();
    }

    @Test
    public void capabilities_packetCapAdd_noCapabilities_fromFields_headerData() throws NoSuchMethodException {
        PACKET_CAP_ADD p = capabilities_packetCapAdd_noCapabilities_fromFields();

        assertEquals("Wrong kind", 1010, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 4, p.getHeader().getTotalSize());
    }

    @Test
    public void capabilities_packetCapAdd_noCapabilities_fromFields_bodyData() throws NoSuchMethodException {
        PACKET_CAP_ADD p = capabilities_packetCapAdd_noCapabilities_fromFields();

        assertEquals("Wrong field value", 1260, p.getAlwaysThereValue().intValue());
    }

    @Test
    public void capabilities_packetCapAdd_noCapabilities_fromFields_serialize() throws NoSuchMethodException, IOException {
        PACKET_CAP_ADD p = capabilities_packetCapAdd_noCapabilities_fromFields();

        // encoding
        String message = "Serialization not as expected";
        byte[] expected = capabilities_packetCapAdd_noCapabilities_serialized();
        assertSerializesTo(message, expected, p);
    }

    @Test
    public void capabilities_packetCapAdd_noCapabilities_deserialize() {
        PACKET_CAP_ADD p = PACKET_CAP_ADD.fromHeaderAndStream(
                bytesToDataInput(capabilities_packetCapAdd_noCapabilities_serialized(), 4),
                new Header_2_2(4 + 1 + 4, 1010),
                new HashMap<DeltaKey, Packet>()
        );

        assertEquals("Wrong kind", 1010, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 4, p.getHeader().getTotalSize());

        assertEquals("Wrong field value", 1260, p.getAlwaysThereValue().intValue());
    }

    /******************************************************************************************************************/

    private static PACKET_CAP_REMOVE capabilities_packetCapRemove_noCapabilities_fromFields() throws NoSuchMethodException {
        return PACKET_CAP_REMOVE.fromValues(1260, 7, Header_2_2.class.getConstructor(int.class, int.class));
    }

    private static byte[] capabilities_packetCapRemove_noCapabilities_serialized() {
        return new byte[]{
                0, 0xA, 0x03, (byte)0xf3,
                3,
                0, 0, 0x4, (byte)0xec,
                7
        };
    }

    @Test
    public void capabilities_packetCapRemove_noCapabilities_fromFields_isCreated() throws NoSuchMethodException, IOException {
        capabilities_packetCapRemove_noCapabilities_fromFields();
    }

    @Test
    public void capabilities_packetCapRemove_noCapabilities_fromFields_headerData() throws NoSuchMethodException, IOException {
        PACKET_CAP_REMOVE p = capabilities_packetCapRemove_noCapabilities_fromFields();

        assertEquals("Wrong kind", 1011, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 4 + 1, p.getHeader().getTotalSize());
    }

    @Test
    public void capabilities_packetCapRemove_noCapabilities_fromFields_bodyData() throws NoSuchMethodException, IOException {
        PACKET_CAP_REMOVE p = capabilities_packetCapRemove_noCapabilities_fromFields();

        assertEquals("Wrong field value", 1260, p.getAlwaysThereValue().intValue());
        assertEquals("Wrong field value", 7, p.getThereUnlessRemovedValue().intValue());
    }

    @Test
    public void capabilities_packetCapRemove_noCapabilities_fromFields_serialize() throws NoSuchMethodException, IOException {
        PACKET_CAP_REMOVE p = capabilities_packetCapRemove_noCapabilities_fromFields();

        // encoding
        String message = "Serialization not as expected";
        byte[] expected = capabilities_packetCapRemove_noCapabilities_serialized();
        assertSerializesTo(message, expected, p);
    }

    @Test
    public void capabilities_packetCapRemove_noCapabilities_deserialize() {
        PACKET_CAP_REMOVE p = PACKET_CAP_REMOVE.fromHeaderAndStream(
                bytesToDataInput(capabilities_packetCapRemove_noCapabilities_serialized(), 4),
                new Header_2_2(4 + 1 + 4 + 1, 1011),
                new HashMap<DeltaKey, Packet>()
        );

        assertEquals("Wrong kind", 1011, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 4 + 1, p.getHeader().getTotalSize());

        assertEquals("Wrong field value", 1260, p.getAlwaysThereValue().intValue());
        assertEquals("Wrong field value", 7, p.getThereUnlessRemovedValue().intValue());
    }

    /******************************************************************************************************************/

    private static PACKET_CAP_ADD_AND_REMOVE capabilities_packetCapAddAndRemove_noCapabilities_fromFields() throws NoSuchMethodException {
        return PACKET_CAP_ADD_AND_REMOVE.fromValues(1260, "days", Header_2_2.class.getConstructor(int.class, int.class));
    }

    private static byte[] capabilities_packetCapAddAndRemove_noCapabilities_serialized() {
        return new byte[]{
                0, 14, 0x03, (byte)0xf4,
                3,
                0, 0, 0x4, (byte)0xec,
                'd', 'a', 'y', 's', 0
        };
    }

    @Test
    public void capabilities_packetCapAddAndRemove_noCapabilities_fromFields_isCreated() throws NoSuchMethodException {
        capabilities_packetCapAddAndRemove_noCapabilities_fromFields();
    }

    @Test
    public void capabilities_packetCapAddAndRemove_noCapabilities_fromFields_headerData() throws NoSuchMethodException {
        PACKET_CAP_ADD_AND_REMOVE p = capabilities_packetCapAddAndRemove_noCapabilities_fromFields();

        assertEquals("Wrong kind", 1012, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 4 + 5, p.getHeader().getTotalSize());
    }

    @Test
    public void capabilities_packetCapAddAndRemove_noCapabilities_fromFields_bodyData() throws NoSuchMethodException {
        PACKET_CAP_ADD_AND_REMOVE p = capabilities_packetCapAddAndRemove_noCapabilities_fromFields();

        assertEquals("Wrong field value", 1260, p.getAlwaysThereValue().intValue());
        assertEquals("Wrong field value", "days", p.getOld_fieldValue());
    }

    @Test
    public void capabilities_packetCapAddAndRemove_noCapabilities_fromFields_serialize() throws NoSuchMethodException, IOException {
        PACKET_CAP_ADD_AND_REMOVE p = capabilities_packetCapAddAndRemove_noCapabilities_fromFields();

        // encoding
        String message = "Serialization not as expected";
        byte[] expected = capabilities_packetCapAddAndRemove_noCapabilities_serialized();
        assertSerializesTo(message, expected, p);
    }

    @Test
    public void capabilities_packetCapAddAndRemove_noCapabilities_deserialize() {
        PACKET_CAP_ADD_AND_REMOVE p = PACKET_CAP_ADD_AND_REMOVE.fromHeaderAndStream(
                bytesToDataInput(capabilities_packetCapAddAndRemove_noCapabilities_serialized(), 4),
                new Header_2_2(4 + 1 + 4 + 5, 1012),
                new HashMap<DeltaKey, Packet>()
        );

        assertEquals("Wrong kind", 1012, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 4 + 5, p.getHeader().getTotalSize());

        assertEquals("Wrong field value", 1260, p.getAlwaysThereValue().intValue());
        assertEquals("Wrong field value", "days", p.getOld_fieldValue());
    }

    /******************************************************************************************************************/

    private static PACKET_CAP_USES_TWO_CAPS capabilities_packetCapUsesTwoCaps_noCapabilities_fromFields() throws NoSuchMethodException {
        return PACKET_CAP_USES_TWO_CAPS.fromValues(1260, Header_2_2.class.getConstructor(int.class, int.class));
    }

    private static byte[] capabilities_packetCapUsesTwoCaps_noCapabilities_serialized() {
        return new byte[]{
                0, 9, 0x03, (byte)0xf5,
                1,
                0, 0, 0x4, (byte)0xec
        };
    }

    @Test
    public void capabilities_packetCapUsesTwoCaps_noCapabilities_fromFields_isCreated() throws NoSuchMethodException {
        capabilities_packetCapUsesTwoCaps_noCapabilities_fromFields();
    }

    @Test
    public void capabilities_packetCapUsesTwoCaps_noCapabilities_fromFields_headerData() throws NoSuchMethodException {
        PACKET_CAP_USES_TWO_CAPS p = capabilities_packetCapUsesTwoCaps_noCapabilities_fromFields();

        assertEquals("Wrong kind", 1013, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 4, p.getHeader().getTotalSize());
    }

    @Test
    public void capabilities_packetCapUsesTwoCaps_noCapabilities_fromFields_bodyData() throws NoSuchMethodException {
        PACKET_CAP_USES_TWO_CAPS p = capabilities_packetCapUsesTwoCaps_noCapabilities_fromFields();

        assertEquals("Wrong field value", 1260, p.getAlwaysThereValue().intValue());
    }

    @Test
    public void capabilities_packetCapUsesTwoCaps_noCapabilities_fromFields_serialize() throws NoSuchMethodException, IOException {
        PACKET_CAP_USES_TWO_CAPS p = capabilities_packetCapUsesTwoCaps_noCapabilities_fromFields();

        // encoding
        String message = "Serialization not as expected";
        byte[] expected = capabilities_packetCapUsesTwoCaps_noCapabilities_serialized();
        assertSerializesTo(message, expected, p);
    }

    @Test
    public void capabilities_packetCapUsesTwoCaps_noCapabilities_deserialize() {
        PACKET_CAP_USES_TWO_CAPS p = PACKET_CAP_USES_TWO_CAPS.fromHeaderAndStream(
                bytesToDataInput(capabilities_packetCapUsesTwoCaps_noCapabilities_serialized(), 4),
                new Header_2_2(4 + 1 + 4, 1013),
                new HashMap<DeltaKey, Packet>()
        );

        assertEquals("Wrong kind", 1013, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 4, p.getHeader().getTotalSize());

        assertEquals("Wrong field value", 1260, p.getAlwaysThereValue().intValue());
    }

    /*------------------------------------------------------------------------------------------------------------------
    General helpers
    ------------------------------------------------------------------------------------------------------------------*/
    private static void assertSerializesTo(String message, byte[] expected, Packet packet) throws IOException {
        final ByteArrayOutputStream serialized = new ByteArrayOutputStream(packet.getHeader().getTotalSize());
        packet.encodeTo(new DataOutputStream(serialized));
        assertArrayEquals(message, expected, serialized.toByteArray());
    }

    private static DataInputStream bytesToDataInput(byte[] bytes, int skip) {
        final DataInputStream stream = new DataInputStream(new ByteArrayInputStream(bytes, skip, bytes.length - skip));
        return stream;
    }
}