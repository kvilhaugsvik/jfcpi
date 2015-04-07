/*
 * Copyright (c) 2015. Sveinung Kvilhaugsvik
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

package org.freeciv.connection;

import org.freeciv.packet.Header_2_2;

import org.freeciv.packet.PacketHeader;
import org.junit.Test;

import static org.junit.Assert.*;

public class HeaderDataTest {
    /**
     * Check that creation of a new header from its field values works.
     */
    @Test public void newHeader_fromValues_basic() {
        /* Set up */
        HeaderData headerData = new HeaderData(Header_2_2.class);

        /* Create the new header. */
        PacketHeader header = headerData.newHeader(4, 88);

        /* Check the resulting header */
        assertEquals(4, header.getTotalSize());
        assertEquals(88, header.getPacketKind());
    }
}
