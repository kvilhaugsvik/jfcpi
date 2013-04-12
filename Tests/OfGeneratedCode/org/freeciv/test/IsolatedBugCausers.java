/*
 * Copyright (c) 2013 Sveinung Kvilhaugsvik.
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
import org.junit.Test;

import java.io.*;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

public class IsolatedBugCausers {
    @Test public void fromNetwork_PacketHasArrayOfSize0() throws IOException {
        DataInput inputStream = new DataInputStream(new ByteArrayInputStream(
                new byte[]{0} // 0 is the size. The array isn't there as it ends in 0.
        ));
        TestArrayTransfer fromData = new TestArrayTransfer(inputStream, new Header_2_2(5, 927), new java.util.HashMap<DeltaKey, Packet>());

        assertEquals("Wrong size", 0, fromData.getToTransferValue().intValue());
        assertArrayEquals("Wrong data", new Long[0], fromData.getTheArrayValue());
    }

    @Test public void fromNetwork_PacketHasBitString_InstanceLimitSmallerThanAbsoluteLimit() throws IOException {
        DataInput inputStream = new DataInputStream(new ByteArrayInputStream(
                new byte[]{0, 8, -1} // size is 8 bits. Data is 11111111
        ));
        TestBitString fromData = new TestBitString(inputStream, new Header_2_2(7, 931), new java.util.HashMap<DeltaKey, Packet>());
    }
}
