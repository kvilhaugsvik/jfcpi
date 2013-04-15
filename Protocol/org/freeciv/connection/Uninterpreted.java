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
import org.freeciv.packet.PacketHeader;
import org.freeciv.packet.RawPacket;

import java.io.*;
import java.lang.reflect.Constructor;
import java.net.Socket;
import java.util.*;

public class Uninterpreted implements FreecivConnection {
    private final BackgroundReader in;
    private final OutputStream out;

    private final OverImpl overImpl = new OverImpl();
    private final ReflexPacketKind postSend;
    private final HeaderData currentHeader;

    private boolean stillOpen;

    public Uninterpreted(
            final Socket connection,
            final Class<? extends PacketHeader> packetHeaderClass,
            final Map<Integer, ReflexReaction> postReceive,
            final Map<Integer, ReflexReaction> postSend
    ) throws IOException {
        this.stillOpen = true;
        this.currentHeader = new HeaderData(packetHeaderClass);
        this.out = connection.getOutputStream();
        this.in = new BackgroundReader(connection.getInputStream(), this,
                new ReflexPacketKind(postReceive, this), currentHeader);
        this.postSend = new ReflexPacketKind(postSend, this);

        this.in.start();
    }

    public boolean packetReady() {
        return in.hasPacket();
    }

    public RawPacket getPacket() throws NotReadyYetException {
        if (!packetReady())
            throw new NotReadyYetException("No packets waiting");

        return in.getPacket();
    }

    public boolean isOpen() {
        return stillOpen;
    }

    public void close() {
        setOver();
        stillOpen = false;
        try {
            out.close(); // Since out is from a Socket this closes it as well
        } catch (IOException e) {
            System.err.println("Problems while closing network connection. Packets may not have been sent");
            e.printStackTrace();
        }
    }

    public void toSend(Packet toSend) throws IOException {
        if (!isOpen()) {
            throw new IOException("Is closed. Can't send.");
        } else if (!currentHeader.sameType(toSend)) {
            throw new IllegalArgumentException("Unexpected header kind");
        }

        ByteArrayOutputStream packetSerialized = new ByteArrayOutputStream(toSend.getHeader().getTotalSize());
        DataOutputStream packet = new DataOutputStream(packetSerialized);

        toSend.encodeTo(packet);

        try {
            out.write(packetSerialized.toByteArray());
            this.postSend.handle(toSend);
        } catch (IOException e) {
            close();
            throw new IOException("Can't send", e);
        }
    }

    @Override
    public void setHeaderTypeTo(Class<? extends PacketHeader> newKind) {
        currentHeader.setHeaderTypeTo(newKind);
    }

    @Override
    public Constructor<? extends PacketHeader> getStream2Header() {
        return currentHeader.getStream2Header();
    }

    @Override
    public Constructor<? extends PacketHeader> getFields2Header() {
        return currentHeader.getFields2Header();
    }

    public void setOver() {
        overImpl.setOver();
    }

    public boolean isOver() {
        return overImpl.isOver();
    }

    private static class BackgroundReader extends Thread {
        private final PacketInputStream in;
        private final LinkedList<RawPacket> buffered;
        private final Uninterpreted parent;

        private final ReflexPacketKind quickRespond;

        public BackgroundReader(InputStream in, Uninterpreted parent, ReflexPacketKind quickRespond,
                                final HeaderData currentHeader)
                throws IOException {
            this.in = new PacketInputStream(in, parent, currentHeader);
            this.parent = parent;
            this.quickRespond = quickRespond;
            this.buffered = new LinkedList<RawPacket>();

            this.setDaemon(true);
        }

        @Override
        public void run() {
            try {
                while(true) {
                    RawPacket incoming = in.readPacket();

                    quickRespond.handle(incoming);

                    synchronized (buffered) {
                        buffered.add(incoming);
                    }
                }
            } catch (DoneReading e) {
                // Looks good
            } catch (Exception e) {
                System.err.println("Problem in the thread that reads from the network");
                e.printStackTrace();
                parent.setOver();
            } finally {
                parent.close();
            }
        }

        public boolean hasPacket() {
            synchronized (buffered) {
                return !buffered.isEmpty();
            }
        }

        public RawPacket getPacket() {
            synchronized (buffered) {
                return buffered.removeFirst();
            }
        }
    }

}
