/*
 * Copyright (c) 2012. Sveinung Kvilhaugsvik
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

import java.io.DataOutput;
import java.io.IOException;

public class HeaderTest {
    @Test(expected = IllegalArgumentException.class)
    public void headerNegativePacketNumber() {
        new PacketHeader(-1, 4, 8) {
            @Override
            public void encodeTo(DataOutput to) throws IOException {
            }
        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void headerNegativeBodySize() {
        new PacketHeader(72, -4, 0) {
            @Override
            public void encodeTo(DataOutput to) throws IOException {
            }
        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void headerNegativeTotalSize() {
        new PacketHeader(72, 0, -1) {
            @Override
            public void encodeTo(DataOutput to) throws IOException {
            }
        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void headerTotalSmallerThanBody() {
        new PacketHeader(72, 5, 4) {
            @Override
            public void encodeTo(DataOutput to) throws IOException {
            }
        };
    }
}
