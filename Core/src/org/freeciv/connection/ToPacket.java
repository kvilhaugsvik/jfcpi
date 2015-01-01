/*
 * Copyright (c) 2013. Sveinung Kvilhaugsvik
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

import org.freeciv.packet.Packet;
import org.freeciv.packet.PacketHeader;

/**
 * Deserialize raw packet data.
 */
public interface ToPacket {
    /**
     * Deserialize a packet.
     * @param head the header of the packet.
     * @param remaining the packet data representing the packet body.
     * @return the deserialized packet.
     */
    @Deprecated
    public Packet convert(PacketHeader head, byte[] remaining);

    /**
     * Deserialize a packet.
     * @param packet the packet data to interpret as a packet.
     * @param headerData packet header interpreter.
     * @return the deserialized packet.
     */
    public Packet convert(byte[] packet, HeaderData headerData);
}
