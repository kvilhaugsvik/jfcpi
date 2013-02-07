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
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Map;

public class Uninterpreted implements FreecivConnection {
    private final BackgroundReader in;
    private final OutputStream out;

    private final OverImpl overImpl = new OverImpl();

    private boolean stillOpen;

    public Uninterpreted(
            final Socket connection,
            final Class<? extends PacketHeader> packetHeaderClass,
            final Map<Integer, ReflexReaction> reflexes
    ) throws IOException {
        this.stillOpen = true;
        this.out = connection.getOutputStream();
        this.in = new BackgroundReader(connection.getInputStream(), this,
                new ReflexPacketKind(reflexes, this), packetHeaderClass);

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
        ByteArrayOutputStream packetSerialized = new ByteArrayOutputStream(toSend.getHeader().getTotalSize());
        DataOutputStream packet = new DataOutputStream(packetSerialized);

        toSend.encodeTo(packet);

        out.write(packetSerialized.toByteArray());
    }

    public void setOver() {
        overImpl.setOver();
    }

    public boolean isOver() {
        return overImpl.isOver();
    }

    private static class BackgroundReader extends Thread {
        private final InputStream in;
        private final LinkedList<RawPacket> buffered;
        private final Uninterpreted parent;
        private final Constructor<? extends PacketHeader> headerReader;
        private final int headerSize;

        private final ReflexPacketKind quickRespond;

        public BackgroundReader(InputStream in, Uninterpreted parent, ReflexPacketKind quickRespond,
                                final Class<? extends PacketHeader> packetHeaderClass)
                throws IOException {
            this.in = in;
            this.parent = parent;
            this.quickRespond = quickRespond;
            this.buffered = new LinkedList<RawPacket>();

            try {
                headerReader = packetHeaderClass.getConstructor(DataInput.class);
                headerSize = packetHeaderClass.getField("HEADER_SIZE").getInt(null);
            } catch (NoSuchMethodException e) {
                throw new IOException("Could not find constructor for header interpreter", e);
            } catch (NoSuchFieldException e) {
                throw new IOException("Could not find header size in header interpreter", e);
            } catch (IllegalAccessException e) {
                throw new IOException("Could not access header size in header interpreter", e);
            }

            this.setDaemon(true);
        }

        @Override
        public void run() {
            try {
                while(true) {
                    RawPacket incoming = readPacket();

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

        private RawPacket readPacket()
                throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException {
            final byte[] headerStart = readXBytesFrom(headerSize, in, parent, true);
            PacketHeader head =
                    headerReader.newInstance(new DataInputStream(new ByteArrayInputStream(headerStart)));

            byte[] body = readXBytesFrom(head.getBodySize(), in, parent, false);
            return new RawPacket(body, head);
        }

        private static byte[] readXBytesFrom(int wanted, InputStream from, Uninterpreted parent, boolean clean)
                throws IOException {
            byte[] out = new byte[wanted];
            int alreadyRead = 0;
            while(alreadyRead < wanted) {
                final int bytesRead;
                try {
                    bytesRead = from.read(out, alreadyRead, wanted - alreadyRead);
                } catch (EOFException e) {
                    throw done(wanted, clean, alreadyRead);
                }
                if (0 <= bytesRead)
                    alreadyRead += bytesRead;
                else if (parent.isOver())
                    throw done(wanted, clean, alreadyRead);
                Thread.yield();
            }
            return out;
        }

        private static IOException done(int wanted, boolean clean, int alreadyRead) throws DoneReading, EOFException {
            if (clean && 0 == alreadyRead)
                return new DoneReading("Nothing to read and nothing is waiting");
            else
                return new EOFException("Nothing to read and nothing is waiting." +
                        "Read " + alreadyRead + " of " + wanted + " bytes");
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

    static class DoneReading extends IOException {
        public DoneReading(String message) {
            super(message);
        }
    }
}
