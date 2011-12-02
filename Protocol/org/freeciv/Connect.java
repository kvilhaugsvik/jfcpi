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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;

//TODO: Implement delta protocol
//TODO: Implement compression in protocol
public class Connect {
    OutputStream out;
    DataInputStream in;
    Socket server;
    HashMap<Integer, Constructor> packetMakers = new HashMap<Integer, Constructor>();

    public Connect(String address, int port) throws IOException {

        server = new Socket(address, port);
        in = new DataInputStream(server.getInputStream());

        out = server.getOutputStream();

        loadPacketClasses();
    }

    private void loadPacketClasses() throws IOException {
        URL packetList = this.getClass().getResource("/org/freeciv/packet/" + "packets.txt");
        if (null == packetList) {
            throw new IOException("No packet list found");
        }

        BufferedReader packets = new BufferedReader(new InputStreamReader(packetList.openStream()));
        String packetMetaData;
        while((packetMetaData = packets.readLine()) != null) {
            try {
                String[] packet = packetMetaData.split("\t");
                this.packetMakers.put(Integer.parseInt(packet[0]),
                        Class.forName(packet[1].trim()).getConstructor(DataInput.class, Integer.TYPE, Integer.TYPE));
            } catch (ClassNotFoundException e) {
                throw new IOException("packets.txt" + " claims that " +
                        packetMetaData + " is generated but it was not found.");
            } catch (NoSuchMethodException e) {
                throw new IOException(packetMetaData + " is not compatible.\n" +
                        "(No constructor from DataInput, int, int found)");
            }
        }
        packets.close();
    }

    public Packet getPacket() throws IOException {
        int size = in.readChar();
        int kind = in.readUnsignedByte();
        if (packetMakers.containsKey(kind)) {
            try {
                return (Packet)packetMakers.get(kind).newInstance(in, size, kind);
            } catch (InstantiationException e) {
                throw new IOException("Internal error while trying to read packet from network");
            } catch (IllegalAccessException e) {
                throw new IOException("Internal error while trying to read packet from network");
            } catch (InvocationTargetException e) {
                throw new IOException("Internal error while trying to read packet from network");
            }
        } else {
            return new RawPacket(in, size, kind);
        }
    }

    public void toSend(Packet toSend) throws IOException {
        ByteArrayOutputStream packetSerialized = new ByteArrayOutputStream(toSend.getEncodedSize());
        DataOutputStream packet = new DataOutputStream(packetSerialized);

        toSend.encodeTo(packet);

        out.write(packetSerialized.toByteArray());
    }
}
