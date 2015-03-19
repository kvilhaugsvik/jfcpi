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
import java.util.logging.Level;
import java.util.logging.Logger;

public class InterpretWhenPossible implements ToPacket {
    private final ProtocolVariant map;
    private final Map<DeltaKey, Packet> old;
    private final String loggerName;

    public InterpretWhenPossible(ProtocolVariant map, String loggerName) {
        this.map = map;
        this.old = newDeltaStore();
        this.loggerName = loggerName;
    }

    @Override
    public Packet convert(PacketHeader head, byte[] packet) {
        try {
            DataInputStream body = new DataInputStream(new ByteArrayInputStream(packet));

            int toSkip = head.getHeaderSize();
            while (0 < toSkip)
                toSkip = toSkip - body.skipBytes(toSkip);

            return map.interpret(head, body, old);
        } catch (IOException | IllegalAccessException e) {
            /* Log the misinterpretation. */
            log(e);

            return new RawPacket(packet, head);
        }
    }

    @Override
    public Packet convert(byte[] packet, HeaderData headerData) {
        try {
            DataInputStream entirePacket = new DataInputStream(new ByteArrayInputStream(packet));

            PacketHeader head = headerData.newHeaderFromStream(entirePacket);

            return map.interpret(head, entirePacket, old);
        } catch (IOException | IllegalAccessException e) {
            /* Log the misinterpretation. */
            log(e);

            return new RawPacket(packet, headerData);
        }
    }

    /**
     * Log a misinterpretation
     * @param e the exception indicating the misinterpretation.
     */
    private void log(Exception e) {
        Logger.getLogger(loggerName).log(Level.WARNING, "Misinterpretation. " + e.getMessage(), e);
    }

    /**
     * Create a new delta protocol packet storage.
     * The delta protocol needs to look up the previous packet since the
     * current packet can refer to data from the previous packet.
     * @return a new delta protocol packet storage.
     */
    public static HashMap<DeltaKey, Packet> newDeltaStore() {
        return new HashMap<DeltaKey, Packet>();
    }
}
