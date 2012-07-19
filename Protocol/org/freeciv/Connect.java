/*
 * Copyright (c) 2011. Sveinung Kvilhaugsvik
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

package org.freeciv;

import org.freeciv.packet.*;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.LinkedList;

//TODO: Implement delta protocol
//TODO: Implement compression in protocol
public class Connect {
    private final OutputStream out;
    private final InputStream in;
    private final Socket server;
    private final LinkedList<RawPacket> toProcess;
    private final PacketsMapping interpreter;
    private final Constructor<? extends PacketHeader> headerReader;
    private final int headerSize;

    public Connect(String address, int port) throws IOException {

        server = new Socket(address, port);
        in = server.getInputStream();

        out = server.getOutputStream();

        interpreter = new PacketsMapping();
        toProcess = new LinkedList<RawPacket>();

        try {
            headerReader = interpreter.getPacketHeaderClass().getConstructor(DataInput.class);
            headerSize = interpreter.getPacketHeaderClass().getField("HEADER_SIZE").getInt(null);
        } catch (NoSuchMethodException e) {
            throw new IOException("Could not find constructor for header interpreter", e);
        } catch (NoSuchFieldException e) {
            throw new IOException("Could not find header size in header interpreter", e);
        } catch (IllegalAccessException e) {
            throw new IOException("Could not access header size in header interpreter", e);
        }

        Thread fastReader = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    while(server.isConnected()) {
                        PacketHeader head = headerReader
                                .newInstance(new DataInputStream(new ByteArrayInputStream(readXBytesFrom(headerSize, in))));
                        byte[] body = readXBytesFrom(head.getBodySize(), in);
                        toProcess.add(new RawPacket(body, head));
                    }
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

    public Packet getPacket() throws IOException, NotReadyYetException {
        if (toProcess.isEmpty())
            throw new NotReadyYetException("No packets waiting");

        try {
            RawPacket out = toProcess.removeFirst();
            if (interpreter.canInterpret(out.getHeader().getPacketKind()))
                return interpreter.interpret(out.getHeader(),
                        new DataInputStream(new ByteArrayInputStream(out.getBodyBytes())));
            else
                return out;
        } catch (Exception e) {
            throw new IOException("Could not read packet", e);
        }
    }

    public static byte[] readXBytesFrom(int wanted, InputStream from) throws IOException {
        byte[] out = new byte[wanted];
        int alreadyRead = 0;
        while(alreadyRead < wanted) {
            int bytesRead = from.read(out, alreadyRead, wanted - alreadyRead);
            if (0 < bytesRead)
                alreadyRead += bytesRead;
            if (alreadyRead < wanted)
                Thread.yield();
        }
        return out;
    }

    public void toSend(Packet toSend) throws IOException {
        ByteArrayOutputStream packetSerialized = new ByteArrayOutputStream(toSend.getEncodedSize());
        DataOutputStream packet = new DataOutputStream(packetSerialized);

        toSend.encodeTo(packet);

        out.write(packetSerialized.toByteArray());
    }

    public String getCapStringMandatory() {
        return interpreter.getCapStringMandatory();
    }

    public String getCapStringOptional() {
        return interpreter.getCapStringOptional();
    }

    public String getVersionLabel() {
        return interpreter.getVersionLabel();
    }

    public long getVersionMajor() {
        return interpreter.getVersionMajor();
    }

    public long getVersionMinor() {
        return interpreter.getVersionMinor();
    }

    public long getVersionPatch() {
        return interpreter.getVersionPatch();
    }
}
