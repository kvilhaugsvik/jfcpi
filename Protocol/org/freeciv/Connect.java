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

    private final OutputStream out;
    private final DataInputStream in;
    private final Socket server;
    private final PacketsMapping interpreter;

    public Connect(String address, int port) throws IOException {

        server = new Socket(address, port);
        in = new DataInputStream(server.getInputStream());

        out = server.getOutputStream();

        interpreter = new PacketsMapping();
    }

    public Packet getPacket() throws IOException {
        int size = in.readChar();
        int kind;
        switch (interpreter.getLenOfPacketNumber()) {
            case 1:
                kind = in.readUnsignedShort();
                break;
            case 2:
                kind = in.readUnsignedByte();
                break;
            default:
                throw new IllegalArgumentException("The packet number in the header can only be 1 or 2 bytes long.");
        }
        if (interpreter.canInterpret(kind))
            return interpreter.interpret(kind, size, in);
        else
            return new RawPacket(in, size, kind, 2 == interpreter.getLenOfPacketNumber());
    }

    public void toSend(Packet toSend) throws IOException {
        ByteArrayOutputStream packetSerialized = new ByteArrayOutputStream(toSend.getEncodedSize());
        DataOutputStream packet = new DataOutputStream(packetSerialized);

        toSend.encodeTo(packet);

        out.write(packetSerialized.toByteArray());
    }
}
