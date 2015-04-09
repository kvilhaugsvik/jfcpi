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

import org.freeciv.connection.HeaderData;
import org.freeciv.connection.InterpretWhenPossible;
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
        PACKET_NO_FIELDS p = PACKET_NO_FIELDS.fromValues(new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());

        assertEquals("Wrong kind", 1001, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4, p.getHeader().getTotalSize());
    }

    @Test
    public void simple_packet_oneField() throws NoSuchMethodException {
        PACKET_ONE_FIELD p = PACKET_ONE_FIELD.fromValues(5, new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());

        assertEquals("Wrong kind", 1002, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 4, p.getHeader().getTotalSize());

        assertEquals("Wrong field value", 5, p.getOneValue().intValue());
    }

    @Test
    public void simple_packet_twoFields() throws NoSuchMethodException {
        PACKET_TWO_FIELDS p = PACKET_TWO_FIELDS.fromValues(5000, 77,
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());

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
    private static PACKET_CAP_ADD.PACKET_CAP_ADD_variant capabilities_packetCapAdd_noCapabilities_fromFields() throws NoSuchMethodException {
        return PACKET_CAP_ADD.fromValues(1260,
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
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
        PACKET_CAP_ADD.PACKET_CAP_ADD_variant p = capabilities_packetCapAdd_noCapabilities_fromFields();

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
                InterpretWhenPossible.newDeltaStore()
        );

        assertEquals("Wrong kind", 1010, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 4, p.getHeader().getTotalSize());

        assertEquals("Wrong field value", 1260, p.getAlwaysThereValue().intValue());
    }

    /******************************************************************************************************************/

    private static PACKET_CAP_ADD.PACKET_CAP_ADD_variant_isAdded capabilities_packetCapAdd_isAdded_fromFields() throws NoSuchMethodException {
        return PACKET_CAP_ADD.fromValues_isAdded(1260, 2,
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    private static byte[] capabilities_packetCapAdd_isAdded_serialized() {
        return new byte[]{
                0, 0xA, 0x03, (byte)0xf2,
                3,
                0, 0, 0x4, (byte)0xec,
                2
        };
    }

    @Test
    public void capabilities_packetCapAdd_isAdded_fromFields_isCreated() throws NoSuchMethodException {
        capabilities_packetCapAdd_isAdded_fromFields();
    }

    @Test
    public void capabilities_packetCapAdd_isAdded_fromFields_headerData() throws NoSuchMethodException {
        PACKET_CAP_ADD.PACKET_CAP_ADD_variant_isAdded p = capabilities_packetCapAdd_isAdded_fromFields();

        assertEquals("Wrong kind", 1010, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 4 + 1, p.getHeader().getTotalSize());
    }

    @Test
    public void capabilities_packetCapAdd_isAdded_fromFields_bodyData() throws NoSuchMethodException {
        PACKET_CAP_ADD.PACKET_CAP_ADD_variant_isAdded p = capabilities_packetCapAdd_isAdded_fromFields();

        assertEquals("Wrong field value", 1260, p.getAlwaysThereValue().intValue());
        assertEquals("Wrong field value", 2, p.getThereWhenAddedValue().intValue());
    }

    @Test
    public void capabilities_packetCapAdd_isAdded_fromFields_serialize() throws NoSuchMethodException, IOException {
        PACKET_CAP_ADD.PACKET_CAP_ADD_variant_isAdded p = capabilities_packetCapAdd_isAdded_fromFields();

        // encoding
        String message = "Serialization not as expected";
        byte[] expected = capabilities_packetCapAdd_isAdded_serialized();
        assertSerializesTo(message, expected, p);
    }

    @Test
    public void capabilities_packetCapAdd_isAdded_deserialize() {
        PACKET_CAP_ADD.PACKET_CAP_ADD_variant_isAdded p = PACKET_CAP_ADD.fromHeaderAndStream_isAdded(
                bytesToDataInput(capabilities_packetCapAdd_isAdded_serialized(), 4),
                new Header_2_2(4 + 1 + 4 + 1, 1010),
                InterpretWhenPossible.newDeltaStore()
        );

        assertEquals("Wrong kind", 1010, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 4 + 1, p.getHeader().getTotalSize());

        assertEquals("Wrong field value", 1260, p.getAlwaysThereValue().intValue());
        assertEquals("Wrong field value", 2, p.getThereWhenAddedValue().intValue());
    }

    /******************************************************************************************************************/

    private static PACKET_CAP_REMOVE.PACKET_CAP_REMOVE_variant capabilities_packetCapRemove_noCapabilities_fromFields() throws NoSuchMethodException {
        return PACKET_CAP_REMOVE.fromValues(1260, 7,
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
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
        PACKET_CAP_REMOVE.PACKET_CAP_REMOVE_variant p = capabilities_packetCapRemove_noCapabilities_fromFields();

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
        PACKET_CAP_REMOVE.PACKET_CAP_REMOVE_variant p = PACKET_CAP_REMOVE.fromHeaderAndStream(
                bytesToDataInput(capabilities_packetCapRemove_noCapabilities_serialized(), 4),
                new Header_2_2(4 + 1 + 4 + 1, 1011),
                InterpretWhenPossible.newDeltaStore()
        );

        assertEquals("Wrong kind", 1011, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 4 + 1, p.getHeader().getTotalSize());

        assertEquals("Wrong field value", 1260, p.getAlwaysThereValue().intValue());
        assertEquals("Wrong field value", 7, p.getThereUnlessRemovedValue().intValue());
    }

    /******************************************************************************************************************/

    private static PACKET_CAP_REMOVE.PACKET_CAP_REMOVE_variant_isRemoved capabilities_packetCapRemove_isRemoved_fromFields() throws NoSuchMethodException {
        return PACKET_CAP_REMOVE.fromValues_isRemoved(1260,
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    private static byte[] capabilities_packetCapRemove_isRemoved_serialized() {
        return new byte[]{
                0, 0x9, 0x03, (byte)0xf3,
                1,
                0, 0, 0x4, (byte)0xec
        };
    }

    @Test
    public void capabilities_packetCapRemove_isRemoved_fromFields_isCreated() throws NoSuchMethodException, IOException {
        capabilities_packetCapRemove_isRemoved_fromFields();
    }

    @Test
    public void capabilities_packetCapRemove_isRemoved_fromFields_headerData() throws NoSuchMethodException, IOException {
        PACKET_CAP_REMOVE p = capabilities_packetCapRemove_isRemoved_fromFields();

        assertEquals("Wrong kind", 1011, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 4, p.getHeader().getTotalSize());
    }

    @Test
    public void capabilities_packetCapRemove_isRemoved_fromFields_bodyData() throws NoSuchMethodException, IOException {
        PACKET_CAP_REMOVE.PACKET_CAP_REMOVE_variant_isRemoved p = capabilities_packetCapRemove_isRemoved_fromFields();

        assertEquals("Wrong field value", 1260, p.getAlwaysThereValue().intValue());
    }

    @Test
    public void capabilities_packetCapRemove_isRemoved_fromFields_serialize() throws NoSuchMethodException, IOException {
        PACKET_CAP_REMOVE p = capabilities_packetCapRemove_isRemoved_fromFields();

        // encoding
        String message = "Serialization not as expected";
        byte[] expected = capabilities_packetCapRemove_isRemoved_serialized();
        assertSerializesTo(message, expected, p);
    }

    @Test
    public void capabilities_packetCapRemove_isRemoved_deserialize() {
        PACKET_CAP_REMOVE.PACKET_CAP_REMOVE_variant_isRemoved p = PACKET_CAP_REMOVE.fromHeaderAndStream_isRemoved(
                bytesToDataInput(capabilities_packetCapRemove_isRemoved_serialized(), 4),
                new Header_2_2(4 + 1 + 4, 1011),
                InterpretWhenPossible.newDeltaStore()
        );

        assertEquals("Wrong kind", 1011, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 4, p.getHeader().getTotalSize());

        assertEquals("Wrong field value", 1260, p.getAlwaysThereValue().intValue());
    }

    /******************************************************************************************************************/

    private static PACKET_CAP_ADD_AND_REMOVE.PACKET_CAP_ADD_AND_REMOVE_variant capabilities_packetCapAddAndRemove_noCapabilities_fromFields() throws NoSuchMethodException {
        return PACKET_CAP_ADD_AND_REMOVE.fromValues(1260, "days",
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
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
        PACKET_CAP_ADD_AND_REMOVE.PACKET_CAP_ADD_AND_REMOVE_variant p = capabilities_packetCapAddAndRemove_noCapabilities_fromFields();

        assertEquals("Wrong field value", 1260, p.getAlwaysThereValue().intValue());
        assertEquals("Wrong field value", "days", p.getOld_fieldValue());
    }

    @Test
    public void capabilities_packetCapAddAndRemove_noCapabilities_fromFields_serialize() throws NoSuchMethodException, IOException {
        PACKET_CAP_ADD_AND_REMOVE.PACKET_CAP_ADD_AND_REMOVE_variant p = capabilities_packetCapAddAndRemove_noCapabilities_fromFields();

        // encoding
        String message = "Serialization not as expected";
        byte[] expected = capabilities_packetCapAddAndRemove_noCapabilities_serialized();
        assertSerializesTo(message, expected, p);
    }

    @Test
    public void capabilities_packetCapAddAndRemove_noCapabilities_deserialize() {
        PACKET_CAP_ADD_AND_REMOVE.PACKET_CAP_ADD_AND_REMOVE_variant p = PACKET_CAP_ADD_AND_REMOVE.fromHeaderAndStream(
                bytesToDataInput(capabilities_packetCapAddAndRemove_noCapabilities_serialized(), 4),
                new Header_2_2(4 + 1 + 4 + 5, 1012),
                InterpretWhenPossible.newDeltaStore()
        );

        assertEquals("Wrong kind", 1012, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 4 + 5, p.getHeader().getTotalSize());

        assertEquals("Wrong field value", 1260, p.getAlwaysThereValue().intValue());
        assertEquals("Wrong field value", "days", p.getOld_fieldValue());
    }

    /******************************************************************************************************************/

    private static PACKET_CAP_ADD_AND_REMOVE.PACKET_CAP_ADD_AND_REMOVE_variant_updated capabilities_packetCapAddAndRemove_updated_fromFields() throws NoSuchMethodException {
        return PACKET_CAP_ADD_AND_REMOVE.fromValues_updated(1260, 70,
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    private static byte[] capabilities_packetCapAddAndRemove_updated_serialized() {
        return new byte[]{
                0, 13, 0x03, (byte)0xf4,
                3,
                0, 0, 0x4, (byte)0xec,
                0, 0, 0, 70
        };
    }

    @Test
    public void capabilities_packetCapAddAndRemove_updated_fromFields_isCreated() throws NoSuchMethodException {
        capabilities_packetCapAddAndRemove_updated_fromFields();
    }

    @Test
    public void capabilities_packetCapAddAndRemove_updated_fromFields_headerData() throws NoSuchMethodException {
        PACKET_CAP_ADD_AND_REMOVE p = capabilities_packetCapAddAndRemove_updated_fromFields();

        assertEquals("Wrong kind", 1012, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 4 + 4, p.getHeader().getTotalSize());
    }

    @Test
    public void capabilities_packetCapAddAndRemove_updated_fromFields_bodyData() throws NoSuchMethodException {
        PACKET_CAP_ADD_AND_REMOVE.PACKET_CAP_ADD_AND_REMOVE_variant_updated p = capabilities_packetCapAddAndRemove_updated_fromFields();

        assertEquals("Wrong field value", 1260, p.getAlwaysThereValue().intValue());
        assertEquals("Wrong field value", 70, p.getNew_fieldValue().intValue());
    }

    @Test
    public void capabilities_packetCapAddAndRemove_updated_fromFields_serialize() throws NoSuchMethodException, IOException {
        PACKET_CAP_ADD_AND_REMOVE.PACKET_CAP_ADD_AND_REMOVE_variant_updated p = capabilities_packetCapAddAndRemove_updated_fromFields();

        // encoding
        String message = "Serialization not as expected";
        byte[] expected = capabilities_packetCapAddAndRemove_updated_serialized();
        assertSerializesTo(message, expected, p);
    }

    @Test
    public void capabilities_packetCapAddAndRemove_updated_deserialize() {
        PACKET_CAP_ADD_AND_REMOVE.PACKET_CAP_ADD_AND_REMOVE_variant_updated p = PACKET_CAP_ADD_AND_REMOVE.fromHeaderAndStream_updated(
                bytesToDataInput(capabilities_packetCapAddAndRemove_updated_serialized(), 4),
                new Header_2_2(4 + 1 + 4 + 4, 1012),
                InterpretWhenPossible.newDeltaStore()
        );

        assertEquals("Wrong kind", 1012, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 4 + 4, p.getHeader().getTotalSize());

        assertEquals("Wrong field value", 1260, p.getAlwaysThereValue().intValue());
        assertEquals("Wrong field value", 70, p.getNew_fieldValue().intValue());
    }

    /******************************************************************************************************************/

    private static PACKET_CAP_USES_TWO_CAPS.PACKET_CAP_USES_TWO_CAPS_variant capabilities_packetCapUsesTwoCaps_noCapabilities_fromFields() throws NoSuchMethodException {
        return PACKET_CAP_USES_TWO_CAPS.fromValues(1260,
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
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
        PACKET_CAP_USES_TWO_CAPS.PACKET_CAP_USES_TWO_CAPS_variant p = capabilities_packetCapUsesTwoCaps_noCapabilities_fromFields();

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
                InterpretWhenPossible.newDeltaStore()
        );

        assertEquals("Wrong kind", 1013, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 4, p.getHeader().getTotalSize());

        assertEquals("Wrong field value", 1260, p.getAlwaysThereValue().intValue());
    }

    /******************************************************************************************************************/

    private static PACKET_CAP_USES_TWO_CAPS.PACKET_CAP_USES_TWO_CAPS_variant_cap1 capabilities_packetCapUsesTwoCaps_cap1_fromFields() throws NoSuchMethodException {
        return PACKET_CAP_USES_TWO_CAPS.fromValues_cap1(1260, 24,
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    private static byte[] capabilities_packetCapUsesTwoCaps_cap1_serialized() {
        return new byte[]{
                0, 10, 0x03, (byte)0xf5,
                3,
                0, 0, 0x4, (byte)0xec,
                24
        };
    }

    @Test
    public void capabilities_packetCapUsesTwoCaps_cap1_fromFields_isCreated() throws NoSuchMethodException {
        capabilities_packetCapUsesTwoCaps_cap1_fromFields();
    }

    @Test
    public void capabilities_packetCapUsesTwoCaps_cap1_fromFields_headerData() throws NoSuchMethodException {
        PACKET_CAP_USES_TWO_CAPS p = capabilities_packetCapUsesTwoCaps_cap1_fromFields();

        assertEquals("Wrong kind", 1013, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 4 + 1, p.getHeader().getTotalSize());
    }

    @Test
    public void capabilities_packetCapUsesTwoCaps_cap1_fromFields_bodyData() throws NoSuchMethodException {
        PACKET_CAP_USES_TWO_CAPS.PACKET_CAP_USES_TWO_CAPS_variant_cap1 p = capabilities_packetCapUsesTwoCaps_cap1_fromFields();

        assertEquals("Wrong field value", 1260, p.getAlwaysThereValue().intValue());
        assertEquals("Wrong field value", 24, p.getAddedByCap1Value().intValue());
    }

    @Test
    public void capabilities_packetCapUsesTwoCaps_cap1_fromFields_serialize() throws NoSuchMethodException, IOException {
        PACKET_CAP_USES_TWO_CAPS.PACKET_CAP_USES_TWO_CAPS_variant_cap1 p = capabilities_packetCapUsesTwoCaps_cap1_fromFields();

        // encoding
        String message = "Serialization not as expected";
        byte[] expected = capabilities_packetCapUsesTwoCaps_cap1_serialized();
        assertSerializesTo(message, expected, p);
    }

    @Test
    public void capabilities_packetCapUsesTwoCaps_cap1_deserialize() {
        PACKET_CAP_USES_TWO_CAPS.PACKET_CAP_USES_TWO_CAPS_variant_cap1 p = PACKET_CAP_USES_TWO_CAPS.fromHeaderAndStream_cap1(
                bytesToDataInput(capabilities_packetCapUsesTwoCaps_cap1_serialized(), 4),
                new Header_2_2(4 + 1 + 4 + 1, 1013),
                InterpretWhenPossible.newDeltaStore()
        );

        assertEquals("Wrong kind", 1013, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 4 + 1, p.getHeader().getTotalSize());

        assertEquals("Wrong field value", 1260, p.getAlwaysThereValue().intValue());
        assertEquals("Wrong field value", 24, p.getAddedByCap1Value().intValue());
    }

    /******************************************************************************************************************/

    private static PACKET_CAP_USES_TWO_CAPS.PACKET_CAP_USES_TWO_CAPS_variant_cap1_cap2 capabilities_packetCapUsesTwoCaps_cap1_cap2_fromFields() throws NoSuchMethodException {
        return PACKET_CAP_USES_TWO_CAPS.fromValues_cap1_cap2(1260, 24, "troner",
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());
    }

    private static byte[] capabilities_packetCapUsesTwoCaps_cap1_cap2_serialized() {
        return new byte[]{
                0, 17, 0x03, (byte)0xf5,
                7,
                0, 0, 0x4, (byte)0xec,
                24,
                't', 'r', 'o', 'n', 'e', 'r', 0
        };
    }

    @Test
    public void capabilities_packetCapUsesTwoCaps_cap1_cap2_fromFields_isCreated() throws NoSuchMethodException {
        capabilities_packetCapUsesTwoCaps_cap1_cap2_fromFields();
    }

    @Test
    public void capabilities_packetCapUsesTwoCaps_cap1_cap2_fromFields_headerData() throws NoSuchMethodException {
        PACKET_CAP_USES_TWO_CAPS p = capabilities_packetCapUsesTwoCaps_cap1_cap2_fromFields();

        assertEquals("Wrong kind", 1013, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 4 + 1 + 7, p.getHeader().getTotalSize());
    }

    @Test
    public void capabilities_packetCapUsesTwoCaps_cap1_cap2_fromFields_bodyData() throws NoSuchMethodException {
        PACKET_CAP_USES_TWO_CAPS.PACKET_CAP_USES_TWO_CAPS_variant_cap1_cap2 p = capabilities_packetCapUsesTwoCaps_cap1_cap2_fromFields();

        assertEquals("Wrong field value", 1260, p.getAlwaysThereValue().intValue());
        assertEquals("Wrong field value", 24, p.getAddedByCap1Value().intValue());
        assertEquals("Wrong field value", "troner", p.getAddedByCap2Value());
    }

    @Test
    public void capabilities_packetCapUsesTwoCaps_cap1_cap2_fromFields_serialize() throws NoSuchMethodException, IOException {
        PACKET_CAP_USES_TWO_CAPS.PACKET_CAP_USES_TWO_CAPS_variant_cap1_cap2 p = capabilities_packetCapUsesTwoCaps_cap1_cap2_fromFields();

        // encoding
        String message = "Serialization not as expected";
        byte[] expected = capabilities_packetCapUsesTwoCaps_cap1_cap2_serialized();
        assertSerializesTo(message, expected, p);
    }

    @Test
    public void capabilities_packetCapUsesTwoCaps_cap1_cap2_deserialize() {
        PACKET_CAP_USES_TWO_CAPS.PACKET_CAP_USES_TWO_CAPS_variant_cap1_cap2 p = PACKET_CAP_USES_TWO_CAPS.fromHeaderAndStream_cap1_cap2(
                bytesToDataInput(capabilities_packetCapUsesTwoCaps_cap1_cap2_serialized(), 4),
                new Header_2_2(4 + 1 + 4 + 1 + 7, 1013),
                InterpretWhenPossible.newDeltaStore()
        );

        assertEquals("Wrong kind", 1013, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 4 + 1 + 7, p.getHeader().getTotalSize());

        assertEquals("Wrong field value", 1260, p.getAlwaysThereValue().intValue());
        assertEquals("Wrong field value", 24, p.getAddedByCap1Value().intValue());
        assertEquals("Wrong field value", "troner", p.getAddedByCap2Value());
    }

    /*------------------------------------------------------------------------------------------------------------------
    Field arrays
    ------------------------------------------------------------------------------------------------------------------*/

    /**
     * Create a packet with a regular field array from values.
     * @throws NoSuchMethodException if the header type don't have the wanted constructor.
     */
    @Test
    public void fieldArray_regular_fromValues() throws NoSuchMethodException {
        final Integer[] count = {0, 1, 2, 3, 4};

        final PACKET_FIELD_ARRAY p = PACKET_FIELD_ARRAY.fromValues(count,
                new HeaderData(Header_2_2.class),
                InterpretWhenPossible.newDeltaStore());

        /* Check header. */
        assertEquals("Wrong kind", 1020, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 5, p.getHeader().getTotalSize());

        /* Check body. Assumed to work since it comes from the test it self. */
        assertArrayEquals("Wrong field value", count, p.getArrayValue());
    }

    /**
     * Create a packet with a regular field array from encoded data.
     */
    @Test
    public void fieldArray_regular_deSerialize() {
        final PACKET_FIELD_ARRAY p = PACKET_FIELD_ARRAY.fromHeaderAndStream(
                bytesToDataInput(
                        new byte[]{
                                /* Header (skipped) */
                                0, 4 + 1 + 5, (byte) 0x03, (byte) 0xfc,
                                /* The field has changed. */
                                1,
                                /* The values of the array (each take one byte) */
                                0, 1, 2, 3, 4
                        },
                        /* Skip the header bytes. */
                        4),
                new Header_2_2(10, 1020),
                /* This should be safe since old isn't used here. */
                InterpretWhenPossible.newDeltaStore());

        /* Check header. Assumed to work since it comes from the test it self. */
        assertEquals("Wrong kind", 1020, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 5, p.getHeader().getTotalSize());

        /* Check body. */
        assertArrayEquals("Wrong field value",
                new Integer[]{0, 1, 2, 3, 4},
                p.getArrayValue());
    }

    /**
     * Create a packet with a diff field array from encoded data.
     * Two of five elements are updated.
     */
    public static PACKET_FIELD_ARRAY_DIFF fieldArray_diffArray_deSerialize_updates_2_of_5_create() {
        return PACKET_FIELD_ARRAY_DIFF.fromHeaderAndStream(
                bytesToDataInput(
                        new byte[]{
                                /* Delta header. The field has changed. */
                                1,
                                /* The array it self. */
                                /* Array element 0 is unchanged (therefore zero). */
                                /* Array element 1 is 1. */
                                1, 1,
                                /* Array element 2 is unchanged (therefore zero). */
                                /* Array element 3 is 5. */
                                3, 5,
                                /* Array element 4 is unchanged (therefore zero). */
                                /* No more changes. */
                                (byte) 0xFF
                        }, 0),
                new Header_2_2(10, 1021),
                /* No need to keep old. This diff array test will only read a single packet. */
                InterpretWhenPossible.newDeltaStore());
    }

    /**
     * Create a packet with a diff field array from encoded data.
     * Two of five elements are updated.
     * Test that deserialization worked without throwing any exceptions.
     */
    @Test public void fieldArray_diffArray_deSerialize_updates_2_of_5_noExceptions() {
        final PACKET_FIELD_ARRAY_DIFF p = fieldArray_diffArray_deSerialize_updates_2_of_5_create();
    }

    /**
     * Create a packet with a diff field array from encoded data.
     * Two of five elements are updated.
     * Test that its element values are correct
     */
    @Test public void fieldArray_diffArray_deSerialize_updates_2_of_5_elementValues() {
        final PACKET_FIELD_ARRAY_DIFF p = fieldArray_diffArray_deSerialize_updates_2_of_5_create();

        /* Check header. Assumed to work since it comes from the test it self. */
        assertEquals("Wrong kind", 1021, p.getHeader().getPacketKind());
        assertEquals("Wrong size", 4 + 1 + 5, p.getHeader().getTotalSize());

        /* Check body with diff array. This is what is tested here. */
        /* FIXME: Access diff array elements like normal array elements. */

        /* Array element 0 is taken from the previous packet. There is no
         * previous packet, It should therefore have the default zero
         * value. */
        assertEquals("Value of byte taken from old wrong", 0, p.getDiffArrayValue()[0].getNewValue().byteValue());

        /* Array element 1 is updated. It should be 1. */
        assertEquals("Value of byte taken from old wrong", 1, p.getDiffArrayValue()[1].getNewValue().byteValue());

        /* Array element 2 is taken from the previous packet. There is no
         * previous packet, It should therefore have the default zero
         * value. */
        assertEquals("Value of byte taken from old wrong", 0, p.getDiffArrayValue()[2].getNewValue().byteValue());

        /* Array element 3 is updated. It should be 1. */
        assertEquals("Value of byte taken from old wrong", 5, p.getDiffArrayValue()[3].getNewValue().byteValue());

        /* Array element 4 is taken from the previous packet. There is no
         * previous packet, It should therefore have the default zero
         * value. */
        assertEquals("Value of byte taken from old wrong", 0, p.getDiffArrayValue()[4].getNewValue().byteValue());
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