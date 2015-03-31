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

import org.freeciv.packet.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InterpretWhenPossible implements ToPacket {
    private final ProtocolVariant map;
    private final Map<DeltaKey, Packet> receivedBefore;
    private final Map<DeltaKey, Packet> sentBefore;
    private final String loggerName;

    public InterpretWhenPossible(ProtocolVariant map, String loggerName) {
        this.map = map;
        this.receivedBefore = newDeltaStore();
        this.sentBefore = newDeltaStore();
        this.loggerName = loggerName;
    }

    @Override
    public Packet convert(byte[] packet, HeaderData headerData) {
        try {
            DataInputStream entirePacket = new DataInputStream(new ByteArrayInputStream(packet));

            PacketHeader head = headerData.newHeaderFromStream(entirePacket);

            final Packet interpreted = map.interpret(head, entirePacket, receivedBefore);

            if (map.isDelta()) {
                /* Let future packets with the same key get their missing
                 * fields from this packet, */
                receivedBefore.put(((PacketInterpretedDelta)interpreted).getKey(), interpreted);
            }

            return interpreted;
        } catch (IOException | IllegalAccessException e) {
            /* Log the misinterpretation. */
            log(e);

            return new RawPacket(packet, headerData);
        }
    }

    @Override
    public byte[] encode(final Packet packet, final HeaderData headerData) throws IOException, IllegalAccessException {
        final byte[] out = packet.toBytes();
        final InterpretedPacket iPacket;

        if (map.isDelta()) {
            if (packet instanceof PacketInterpretedDelta) {
                /* The packet is usable as a field source for future delta
                 * packets as is. */
                iPacket = (PacketInterpretedDelta) packet;
            } else {
                /* The packet must be converted so future packets can check
                 * its fields. */
                try {
                    final DataInputStream entirePacket = new DataInputStream(new ByteArrayInputStream(out));

                    final PacketHeader head = headerData.newHeaderFromStream(entirePacket);

                    iPacket = (PacketInterpretedDelta) map.interpret(head, entirePacket, sentBefore);
                } catch (IOException | IllegalAccessException e) {
                    /* Log the misinterpretation. */
                    log(e);

                    throw e;
                }
            }

            /* Keep the storage of previous packets up to date. */
            sentBefore.put(((PacketInterpretedDelta)iPacket).getKey(), iPacket);
        }

        return out;
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
