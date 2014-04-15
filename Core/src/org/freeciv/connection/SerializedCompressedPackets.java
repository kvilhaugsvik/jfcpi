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

package org.freeciv.connection;

import org.freeciv.packet.Packet;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class SerializedCompressedPackets implements SerializedPacketGroup {
    private final byte[] packet;
    private final boolean jumbo;
    private final BasicProtocolData protoCode;
    private final ToPacket converter;
    private final HeaderData headerData;
    private final ReflexPacketKind quickRespond;

    private final LinkedList<Packet> subPackets;

    public SerializedCompressedPackets(byte[] packet, boolean jumbo, BasicProtocolData protoCode, ToPacket converter, HeaderData headerData, ReflexPacketKind quickRespond) {
        this.packet = packet;
        this.jumbo = jumbo;
        this.protoCode = protoCode;
        this.converter = converter;
        this.headerData = headerData;
        this.quickRespond = quickRespond;

        this.subPackets = new LinkedList<Packet>();
    }

    @Override
    public byte[] getAsData() {
        return packet;
    }

    @Override
    public int putPackets(List<Packet> in) {
        mustBeConverted();
        in.addAll(subPackets);
        return subPackets.size();
    }

    private void mustBeConverted() {
        final int headerSize = jumbo ? 2 + 4 : 2;

        if (subPackets.isEmpty()) {
            quickRespond.startedReceivingOrSending();
            try {
                Inflater uncompres = new Inflater();

                uncompres.setInput(packet, headerSize, packet.length - headerSize);

                final byte[] buffer = new byte[protoCode.getCompressionBorder()];
                final ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

                while(!uncompres.finished())
                    readPacket(uncompres, buffer, byteBuffer);
            } catch (DataFormatException e) {
                throw new IllegalArgumentException("Bad packet data. Error reading it?", e);
            } finally {
                quickRespond.finishedRunningTheReflexes();
            }
        }
    }

    private void readPacket(Inflater uncompres, byte[] buffer, ByteBuffer byteBuffer) throws DataFormatException {
        if (2 != uncompres.inflate(buffer, 0, 2))
            throw new UnsupportedOperationException("TODO");

        final int size = (int) byteBuffer.getChar(0);
        if (size - 2 != uncompres.inflate(buffer, 2, size - 2))
            throw new UnsupportedOperationException("TODO");

        final Packet me = converter.convert(buffer, headerData);
        subPackets.add(me);
        quickRespond.handle(me.getHeader().getPacketKind());
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("Compressed packets");
        out.append("\n\theader = (");
        out.append(jumbo ? "jumbo" : "not jumbo");
        out.append(", size = ");
        out.append(packet.length);
        out.append(")");
        out.append("\n\tsubpackets = (");

        for (Packet p: subPackets) {
            out.append("\n  ");
            out.append(p.toString());
        }

        return out.toString();
    }
}
