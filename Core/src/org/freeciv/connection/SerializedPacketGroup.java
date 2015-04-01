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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * One or more packets
 */
public interface SerializedPacketGroup {
    /**
     * The serialized data
     * @return all data in the group
     */
    byte[] getAsData();

    /**
     * Put the packets in this group in the given list
     * @param in the list to append the packets to
     * @return the number of packets added
     */
    int putPackets(List<Packet> in);

    interface SeparateSerializedPacketGroups {
        SerializedPacketGroup fromInputStream(InputStream in) throws IOException;
    }
}
