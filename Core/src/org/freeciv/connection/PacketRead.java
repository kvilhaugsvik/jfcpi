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

package org.freeciv.connection;

import org.freeciv.packet.Packet;

import java.io.IOException;

public interface PacketRead extends PacketChangeHeader, Over {
    /**
     * Return true if a packet is ready to be read
     * @return true if a packet is ready to be read
     */
    boolean packetReady();

    /**
     * Get the next packet
     * @return a packet
     * @throws java.io.IOException if there is an error reading it
     * @throws org.freeciv.connection.NotReadyYetException if no packet is ready
     */
    Packet getPacket() throws IOException, NotReadyYetException;
}
