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

import org.freeciv.packet.DeltaKey;
import org.freeciv.packet.Packet;
import org.freeciv.packet.PacketHeader;
import org.freeciv.packet.RawPacket;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InterpretWhenPossible implements ToPacket {
    private final PacketsMapping map;
    private final Map<DeltaKey, Packet> old;

    InterpretWhenPossible(PacketsMapping map) {
        this.map = map;
        this.old = new HashMap<DeltaKey, Packet>();
    }

    @Override
    public Packet convert(PacketHeader head, byte[] packet) {
        try {
            DataInputStream body = new DataInputStream(new ByteArrayInputStream(packet));

            int toSkip = head.getHeaderSize();
            while (0 < toSkip)
                toSkip = toSkip - body.skipBytes(toSkip);

            return map.interpret(head, body, old);
        } catch (IOException e) {
            return new RawPacket(packet, head);
        }
    }
}
