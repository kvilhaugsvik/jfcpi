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

import org.freeciv.connection.BadProtocolData;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class PacketHeader {
    protected final int packetKind;
    protected final int bodySize;
    protected final int totalSize;

    protected PacketHeader(int packetKind, int bodySize, int totalSize) {
        if (packetKind < 0)
            throw new IllegalArgumentException("A packet kind number should be positive");

        if (bodySize < 0)
            throw new IllegalArgumentException("Body size can't be negative");

        if (totalSize < 0)
            throw new IllegalArgumentException("Total size can't be negative");

        if (totalSize < bodySize)
            throw new IllegalArgumentException("Total size includes body size and header size so it cant be smaller");

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
     * Get the predicted size of the header without the body
     * @return the size of the header
     */
    public abstract int getHeaderSize();

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

    /**
     * Check if the body size is approved.
     * This is used to see if encoding / decoding is OK. Don't override this unless the header is fake and the real
     * body size is unknown.
     * @return false if the total size is as expected.
     */
    public boolean isWrongBodySize(int candidate) {
        return candidate != getBodySize();
    }

    /***
     * serialize the packet to the format on the line
     * @param to The output to write the packet to
     * @throws java.io.IOException when problem writing
     */
    public abstract void encodeTo(DataOutput to) throws IOException;

    public boolean equals(PacketHeader other) {
        return this.getClass().equals(other.getClass()) &&
                other.getPacketKind() == this.getPacketKind() &&
                other.getTotalSize() == this.getTotalSize();
    }

    @Override
    public String toString() {
        return  this.getClass().getCanonicalName() +
                "(" + "kind = " + getPacketKind() + ", size = " + getTotalSize() + ")";
    }

    /**
     * Serialize the header to the packet format that is sent over the
     * network and return it as a byte array.
     * @return a byte array containing the serialized header.
     */
    public byte[] toBytes() {
        try {
            /* Reuse the header's encodeTo() method. */

            /* Create the stream that will create the byte array the
             * serialized header will end up in. */
            final ByteArrayOutputStream headerSerialized = new ByteArrayOutputStream(this.getHeaderSize());

            /* Wrap the above stream in a DataOutputStream so the header can
             * encode to it. */
            final DataOutputStream out = new DataOutputStream(headerSerialized);

            /* Write the encoded header. */

            this.encodeTo(out);

            /* Return the byte array that now have been written to inside the
             * ByteArrayOutputStream. */
            return headerSerialized.toByteArray();
        } catch (IOException e) {
            throw new BadProtocolData("Unable to encode header", e);
        }
    };
}
