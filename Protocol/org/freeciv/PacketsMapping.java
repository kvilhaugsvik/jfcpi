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

package org.freeciv;

import org.freeciv.packet.Packet;
import org.freeciv.packet.PacketHeader;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;

public class PacketsMapping {
    public static final String packetsList =
            "/" + Packet.class.getPackage().getName().replace('.', '/') + "/" + "packets.txt";

    private final HashMap<Integer, Constructor> packetMakers = new HashMap<Integer, Constructor>();
    private final int packetNumberBytes;

    public PacketsMapping() throws IOException {
        URL packetList = this.getClass().getResource(packetsList);
        if (null == packetList) {
            throw new IOException("No packet list found");
        }

        BufferedReader packets = new BufferedReader(new InputStreamReader(packetList.openStream()));
        String packetMetaData = packets.readLine();
        packetNumberBytes = Integer.parseInt(packetMetaData.split("//|\\s")[0]);
        while(null != (packetMetaData = packets.readLine())) {
            try {
                String[] packet = packetMetaData.split("\t");
                this.packetMakers.put(Integer.parseInt(packet[0]),
                        Class.forName(packet[1].trim()).getConstructor(DataInput.class, Integer.TYPE, Integer.TYPE));
            } catch (ClassNotFoundException e) {
                throw new IOException("List of packets claims that " +
                        packetMetaData + " is generated but it was not found.");
            } catch (NoSuchMethodException e) {
                throw new IOException(packetMetaData + " is not compatible.\n" +
                        "(No constructor from DataInput, int, int found)");
            }
        }
        packets.close();
    }

    public Packet interpret(PacketHeader header, DataInputStream in) throws IOException {
        try {
            return (Packet)packetMakers.get(header.getPacketKind()).newInstance(in, header.getTotalSize(),
                                                                                header.getPacketKind());
        } catch (InstantiationException e) {
            throw packetReadingError(header.getPacketKind(), e);
        } catch (IllegalAccessException e) {
            throw packetReadingError(header.getPacketKind(), e);
        } catch (InvocationTargetException e) {
            throw packetReadingError(header.getPacketKind(), e);
        }
    }

    private static IOException packetReadingError(int kind, Exception exception) {
        return new IOException("Internal error while trying to read packet numbered " + kind + " from network", exception);
    }

    public boolean canInterpret(int kind) {
        return packetMakers.containsKey(kind);
    }

    public int getLenOfPacketNumber() {
        return packetNumberBytes;
    }
}
