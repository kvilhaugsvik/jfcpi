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

import static org.junit.Assert.*;

public class GeneratedUsingFullGenerator {
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
}