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

import org.freeciv.NotReadyYetException;
import org.freeciv.packet.Packet;

import java.io.IOException;

public interface FreecivConnection {
    /**
     * Return true if a packet is ready to be read
     * @return true if a packet is ready to be read
     */
    public boolean packetReady();

    /**
     * Get the next packet
     * @return a packet
     * @throws IOException if there is an error reading it
     * @throws NotReadyYetException if no packet is ready
     */
    public Packet getPacket() throws IOException, NotReadyYetException;

    /**
     * Send a packet via this connection
     * @param toSend
     * @throws IOException
     */
    void toSend(Packet toSend) throws IOException;

    /**
     * Close this connection as soon as its data has been read
     */
    void setOver();

    /**
     * Will this connection be closed (unless it already is) as soon as its empty?
     * @return true if the connection is closed or soon will be
     */
    boolean isOver();

    /**
     * Is the underlying connection open
     * @return true if the underlying connection is open
     */
    public boolean isOpen();
}
