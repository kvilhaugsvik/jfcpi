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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

public class Uninterpreted implements FreecivConnection {
    private final LinkedList<RawPacket> buffered;
    private final Socket connection;

    private final ReflexPacketKind quickRespond;
    private final Constructor<? extends PacketHeader> headerReader;
    private final int headerSize;

    private boolean over = false;

    public Uninterpreted(
            final Socket connection,
            final Class<? extends PacketHeader> packetHeaderClass,
            final Map<Integer, ReflexReaction> reflexes
    ) throws IOException {
        buffered = new LinkedList<RawPacket>();
        quickRespond = new ReflexPacketKind(reflexes, this);
        this.connection = connection;

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

        final InputStream in = connection.getInputStream();

        final Uninterpreted parent = this;

        Thread fastReader = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    while(0 < in.available() || !parent.isOver()) {
                        byte[] headerStart = readXBytesFrom(headerSize, in, parent);
                        if (headerStart.length < headerSize)
                            break; // incomplete data was returned as its over
                        PacketHeader head =
                                headerReader.newInstance(new DataInputStream(new ByteArrayInputStream(headerStart)));

                        byte[] body = readXBytesFrom(head.getBodySize(), in, parent);
                        if (body.length < head.getBodySize())
                            break; // incomplete data was returned as its over
                        RawPacket incoming = new RawPacket(body, head);

                        quickRespond.handle(incoming);

                        synchronized (buffered) {
                            buffered.add(incoming);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Problem in the thread that reads from the network");
                    e.printStackTrace();
                    parent.setOver();
                } finally {
                    try {
                        connection.close();
                    } catch (IOException e) {
                        System.err.println("Problems while closing network connection. Packets may not have been sent");
                        e.printStackTrace();
                    }
                }
            }
        });
        fastReader.setDaemon(true);
        fastReader.start();
    }

    private static byte[] readXBytesFrom(int wanted, InputStream from, Uninterpreted parent) throws IOException {
        byte[] out = new byte[wanted];
        int alreadyRead = 0;
        while(alreadyRead < wanted) {
            int bytesRead = from.read(out, alreadyRead, wanted - alreadyRead);
            if (0 <= bytesRead)
                alreadyRead += bytesRead;
            else if (parent.isOver())
                return Arrays.copyOfRange(out, 0, alreadyRead);
            if (alreadyRead < wanted)
                Thread.yield();
        }
        return out;
    }

    public boolean isEmpty() {
        return buffered.isEmpty();
    }

    public RawPacket getNext() {
        synchronized (buffered) {
            return buffered.removeFirst();
        }
    }

    public boolean isOpen() {
        return !connection.isClosed();
    }

    public void toSend(Packet toSend) throws IOException {
        ByteArrayOutputStream packetSerialized = new ByteArrayOutputStream(toSend.getHeader().getTotalSize());
        DataOutputStream packet = new DataOutputStream(packetSerialized);

        toSend.encodeTo(packet);

        connection.getOutputStream().write(packetSerialized.toByteArray());
    }

    public void setOver() {
        over = true;
    }

    public boolean isOver() {
        return over;
    }
}
