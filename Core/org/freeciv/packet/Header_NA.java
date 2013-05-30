/*
 * Copyright (c) 2013, Sveinung Kvilhaugsvik
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

import java.io.DataOutput;
import java.io.IOException;

public class Header_NA extends PacketHeader {
    public Header_NA(int packetKind) {
        super(packetKind, 0, 0);
    }

    @Override
    public int getHeaderSize() {
        return 0;
    }

    @Override
    public void encodeTo(DataOutput to) throws IOException {
        throw new UnsupportedOperationException("Tried to encode a packet without a real header");
    }

    @Override
    public boolean isWrongBodySize(int candidate) {
        return false;
    }
}
