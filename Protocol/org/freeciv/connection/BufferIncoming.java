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

import org.freeciv.Connect;
import org.freeciv.packet.PacketHeader;
import org.freeciv.packet.RawPacket;

import java.io.*;
import java.lang.reflect.Constructor;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Map;

public class BufferIncoming {
    private final LinkedList<RawPacket> buffered;

    private final ReflexPacketKind quickRespond;
    private final Constructor<? extends PacketHeader> headerReader;
    private final int headerSize;

    public BufferIncoming(
            final Connect owner,
            final Socket server,
            final Class<? extends PacketHeader> packetHeaderClass,
            final Map<Integer, ReflexReaction> reflexes
    ) throws IOException {
        buffered = new LinkedList<RawPacket>();
        quickRespond = new ReflexPacketKind(reflexes, owner);

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

        final InputStream in = server.getInputStream();

        Thread fastReader = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    while(0 < in.available() || !owner.isOver()) {
                        PacketHeader head = headerReader
                                .newInstance(new DataInputStream(new ByteArrayInputStream(readXBytesFrom(headerSize, in, owner))));
                        byte[] body = readXBytesFrom(head.getBodySize(), in, owner);
                        RawPacket incoming = new RawPacket(body, head);
                        quickRespond.handle(incoming);
                        synchronized (buffered) {
                            buffered.add(incoming);
                        }
                    }
                    server.close();
                } catch (Exception e) {
                    System.err.println("Problem in the thread that reads from the network");
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        });
        fastReader.setDaemon(true);
        fastReader.start();
    }

    private static byte[] readXBytesFrom(int wanted, InputStream from, Connect owner) throws IOException {
        byte[] out = new byte[wanted];
        int alreadyRead = 0;
        while(alreadyRead < wanted) {
            int bytesRead = from.read(out, alreadyRead, wanted - alreadyRead);
            if (0 <= bytesRead)
                alreadyRead += bytesRead;
            else if (owner.isOver())
                break;
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
}
