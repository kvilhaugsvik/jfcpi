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
    private final HashMap<Integer, Constructor> packetMakers = new HashMap<Integer, Constructor>();
    private final int packetNumberBytes;

    public PacketsMapping() throws IOException {
      try {
        Class versionData = Class.forName("org.freeciv.VersionData");
        String[] understandsPackets = (String[])versionData.getField("understandsPackets").get(null);
        packetNumberBytes = versionData.getField("networkHeaderPacketNumberBytes").getInt(null);

        for (int number = 0; number < understandsPackets.length; number++) {
            try {
                this.packetMakers.put(number,
                        Class.forName(understandsPackets[number]).getConstructor(DataInput.class, PacketHeader.class));
            } catch (ClassNotFoundException e) {
                throw new IOException("List of packets claims that " +
                        understandsPackets[number] + " is generated but it was not found.");
            } catch (NoSuchMethodException e) {
                throw new IOException(understandsPackets[number] + " is not compatible.\n" +
                        "(No constructor from DataInput, PacketHeader found)");
            }
        }
      } catch (ClassNotFoundException e) {
          throw new IOException("Version information missing", e);
      } catch (NoSuchFieldException e) {
          throw new IOException("Version information not compatible", e);
      } catch (IllegalAccessException e) {
          throw new IOException("Refused to read version information", e);
      }
    }

    public Packet interpret(PacketHeader header, DataInputStream in) throws IOException {
        try {
            return (Packet)packetMakers.get(header.getPacketKind()).newInstance(in, header);
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
