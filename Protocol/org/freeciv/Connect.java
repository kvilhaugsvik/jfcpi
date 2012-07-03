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

import org.freeciv.packet.Packet;
import org.freeciv.packet.RawPacket;

import java.io.*;
import java.net.Socket;

//TODO: Implement delta protocol
//TODO: Implement compression in protocol
public class Connect {
    public static final String packetsList =
            "/" + Packet.class.getPackage().getName().replace('.', '/') + "/" + "packets.txt";

    private final boolean hasTwoBytePacketNumber;
    private final OutputStream out;
    private final DataInputStream in;
    private final Socket server;
    private final PacketsMapping interpreter;

    public Connect(String address, int port) throws IOException {
        this(address, port, true);
    }

    public Connect(String address, int port, boolean hasTwoBytePacketNumber) throws IOException {
        this.hasTwoBytePacketNumber = hasTwoBytePacketNumber;

        server = new Socket(address, port);
        in = new DataInputStream(server.getInputStream());

        out = server.getOutputStream();

        interpreter = new PacketsMapping();
    }

    public Packet getPacket() throws IOException {
        int size = in.readChar();
        int kind = hasTwoBytePacketNumber ? in.readUnsignedShort() : in.readUnsignedByte();
        if (interpreter.canInterpret(kind))
            return interpreter.interpret(kind, size, in);
        else
            return new RawPacket(in, size, kind, hasTwoBytePacketNumber);
    }

    public void toSend(Packet toSend) throws IOException {
        ByteArrayOutputStream packetSerialized = new ByteArrayOutputStream(toSend.getEncodedSize());
        DataOutputStream packet = new DataOutputStream(packetSerialized);

        toSend.encodeTo(packet);

        out.write(packetSerialized.toByteArray());
    }
}
