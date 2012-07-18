/*
 * Copyright (c) 2012, Sveinung Kvilhaugsvik
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

public abstract class PacketHeader {
    protected final int packetKind;
    protected final int bodySize;
    protected final int totalSize;

    protected PacketHeader(int packetKind, int bodySize, int totalSize) {
        assert 0 <= packetKind : "A packet kind number should be positive";
        assert 0 <= bodySize : "Body size can't be negative";
        assert 0 <= totalSize : "Total size can't be negative";
        assert bodySize <= totalSize : "Total size includes body size and header size so it's bigger";

        this.packetKind = packetKind;
        this.bodySize = bodySize;
        this.totalSize = totalSize;
    }

    /**
     * Get the packet kind
     * @return the packet number of the kind
     */
    public int getPacketKind() {
        return packetKind;
    }

    /**
     * Get the predicted size of the body without the header
     * @return the size of the body
     */
    public int getHeaderSize() {
        return totalSize - bodySize;
    }

    /**
     * Get the predicted size of the body without the header
     * @return the size of the body
     */
    public int getBodySize() {
        return bodySize;
    }

    /**
     * Get the predicted size of the entire packet
     * @return the size of the packet
     */
    public int getTotalSize() {
        return totalSize;
    }

    /***
     * serialize the packet to the format on the line
     * @param to The output to write the packet to
     * @throws java.io.IOException when problem writing
     */
    public abstract void encodeTo(DataOutput to) throws IOException;
}
