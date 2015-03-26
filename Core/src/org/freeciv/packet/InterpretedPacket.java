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

package org.freeciv.packet;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * A packet that has been interpreted so the value of each field it has is
 * known.
 */
public abstract class InterpretedPacket implements Packet {
    @Override public byte[] toBytes() throws IOException {
        /* Reuse the generated packets' encodeTo() method. */

        /* Create the stream that will create the byte array the
         * serialized packet will end up in. */
        final ByteArrayOutputStream packetSerialized = new ByteArrayOutputStream(this.getHeader().getTotalSize());

        /* Wrap the above stream in a DataOutputStream so the packets can
         * encode to it. */
        final DataOutputStream out = new DataOutputStream(packetSerialized);

        /* Write the encoded packet. */
        this.encodeTo(out);

        /* Return the byte array that now have been written to inside the
         * ByteArrayOutputStream. */
        return packetSerialized.toByteArray();
    }
}
