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
import java.util.concurrent.locks.Lock;

public class BackgroundReader extends Thread {
    private final PacketInputStream in;
    private final LinkedList<Packet> buffered;
    private final Connection parent;

    public BackgroundReader(InputStream in, Connection parent, Lock completeReflexesInOneStep, ReflexPacketKind quickRespond,
                            final HeaderData currentHeader, PacketsMapping protoCode)
            throws IOException {
        this.in = new PacketInputStream(in, parent, completeReflexesInOneStep, currentHeader, quickRespond, protoCode);
        this.parent = parent;
        this.buffered = new LinkedList<Packet>();

        this.setDaemon(true);
    }

    @Override
    public void run() {
        try {
            while(true) {
                Packet incoming = in.readPacket();

                synchronized (buffered) {
                    buffered.add(incoming);
                }
            }
        } catch (DoneReading e) {
            // Looks good
        } catch (Exception e) {
            System.err.println("Problem in the thread that reads from the network");
            e.printStackTrace();
        } finally {
            parent.setOver();
            parent.whenOver();
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
