/*
 * Copyright (c) 2013. Sveinung Kvilhaugsvik
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

package org.freeciv.packet;

import org.freeciv.connection.*;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.InflaterInputStream;

public class DecompressedPacket extends CompressedPacket {
    final LinkedList<Packet> subPackets;
    public DecompressedPacket(final int startSize, Over state, InputStream in,
                              final int COMPRESSION_BORDER, final int JUMBO_SIZE,
                              final HeaderData headerData,
                              ReflexPacketKind quickRespond,
                              PacketsMapping protoCode,
                              boolean interpreted
    ) throws IOException, InvocationTargetException {
        super(startSize, state, in, COMPRESSION_BORDER, JUMBO_SIZE);

        this.subPackets = new LinkedList<Packet>();

        PacketInputStream st = new PacketInputStream(
                new InflaterInputStream(new ByteArrayInputStream(content)),
                new OverImpl() {
                    @Override protected void whenDoneImpl() {}
                },
                headerData,
                quickRespond,
                protoCode,
                interpreted);
        try {
            while (true) {
                subPackets.add(st.readPacket());
            }
        } catch (DoneReading e) {
        }
    }

    public List<Integer> getKinds() {
        LinkedList<Integer> out = new LinkedList<Integer>();
        for (Packet p : subPackets)
            out.add(p.getHeader().packetKind);
        return out;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("Compressed package");
        out.append("\n\theader = ");
        out.append(header.toString());
        out.append("\n\tsubpackets = (");

        for (Packet p: subPackets) {
            out.append("\n  ");
            out.append(p.toString());
        }

        return out.toString();
    }
}
