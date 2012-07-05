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
import java.net.Socket;

//TODO: Implement delta protocol
//TODO: Implement compression in protocol
public class Connect {
    private final OutputStream out;
    private final DataInputStream in;
    private final Socket server;
    private final PacketsMapping interpreter;
    private final Constructor<? extends PacketHeader> headerReader;

    public Connect(String address, int port) throws IOException {

        server = new Socket(address, port);
        in = new DataInputStream(server.getInputStream());

        out = server.getOutputStream();

        interpreter = new PacketsMapping();

        Class<? extends PacketHeader> headerReaderClass;
        switch (interpreter.getLenOfPacketNumber()) {
            case 1:
                headerReaderClass = Header_2_1.class;
                break;
            case 2:
                headerReaderClass = Header_2_2.class;
                break;
            default:
                throw new IllegalArgumentException("The packet number in the header can only be 1 or 2 bytes long.");
        }
        try {
            headerReader = headerReaderClass.getConstructor(DataInput.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not find constructor for header interpreter", e);
        }
    }

    public Packet getPacket() throws IOException {
        try {
            PacketHeader head = headerReader.newInstance(in);
            if (interpreter.canInterpret(head.getPacketKind()))
                return interpreter.interpret(head, in);
            else
                return new RawPacket(in, head.getTotalSize(), head.getPacketKind(),
                                     2 == interpreter.getLenOfPacketNumber());

        } catch (Exception e) {
            throw new IOException("Could not read packet", e);
        }
    }

    public void toSend(Packet toSend) throws IOException {
        ByteArrayOutputStream packetSerialized = new ByteArrayOutputStream(toSend.getEncodedSize());
        DataOutputStream packet = new DataOutputStream(packetSerialized);

        toSend.encodeTo(packet);

        out.write(packetSerialized.toByteArray());
    }
}
