/*
 * Copyright (c) 2012, 2013. Sveinung Kvilhaugsvik
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
import java.util.LinkedList;

public class BackgroundReader extends Thread {
    private final InputStream in;
    private final LinkedList<Packet> buffered;
    private final Over parent;

    private final SerializedPacketGroup.SeparateSerializedPacketGroups separator;

    public BackgroundReader(InputStream in, Connection parent, SerializedCompressedPackets.SeparateSerializedPacketGroups splitter)
            throws IOException {
        this.in = in;
        this.parent = parent;
        this.buffered = new LinkedList<Packet>();
        this.separator = splitter;

        this.setDaemon(true);
    }

    @Override
    public void run() {
        try {
            while(true) {
                SerializedPacketGroup incoming = separator.fromInputStream(in);

                synchronized (buffered) {
                    incoming.putPackets(buffered);
                }
            }
        } catch (DoneReading e) {
            // Looks good
        } catch (Exception e) {
            System.err.println("Problem in the thread that reads from the network");
            e.printStackTrace();
        } finally {
            parent.setStopReadingWhenOutOfInput();
            parent.whenDone();
        }
    }

    public boolean hasPacket() {
        synchronized (buffered) {
            return !buffered.isEmpty();
        }
    }

    public Packet getPacket() {
        synchronized (buffered) {
            return buffered.removeFirst();
        }
    }
}
