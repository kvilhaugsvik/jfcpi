/*
 * Copyright (c) 2011. Sveinung Kvilhaugsvik
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

/**
 * A single packet in the Freeciv network protocol.
 */
public interface Packet {
    /**
     * Get the packet header of this packet.
     * @return the packet's header.
     */
    PacketHeader getHeader();

    /**
     * Serialize the packet, including the header, to the packet format
     * that is sent over the network and write it to the specified
     * DataOutput.
     * @param to The DataOutput to write the packet to.
     * @throws IOException when a problem occurs while writing it.
     */
    void encodeTo(DataOutput to) throws IOException;

    /**
     * Serialize the packet, including the header, to the packet format
     * that is sent over the network and return it as a byte array.
     * @return a byte array containing the serialized packet.
     * @throws IOException when a problem occurs while writing it.
     */
    byte[] toBytes() throws IOException;
}
