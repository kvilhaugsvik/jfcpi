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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class SerializedSinglePacket implements SerializedPacketGroup {
    private final byte[] packet;
    private final ToPacket converter;
    private final HeaderData headerData;
    private final ReflexPacketKind quickRespond;

    private Packet me = null;

    public SerializedSinglePacket(byte[] packet, ToPacket converter, HeaderData headerData, ReflexPacketKind quickRespond) {
        this.packet = packet;
        this.converter = converter;
        this.headerData = headerData;
        this.quickRespond = quickRespond;
    }

    @Override
    public byte[] getAsData() {
        return packet;
    }

    @Override
    public int putPackets(List<Packet> in) {
        mustBeConverted();
        in.add(me);
        return 1;
    }

    private void mustBeConverted() {
        if (null == me) {
            quickRespond.startedReceivingOrSending();
            try {
                me = converter.convert(packet, headerData);
                quickRespond.handle(me.getHeader().getPacketKind());
            } finally {
                quickRespond.finishedRunningTheReflexes();
            }
        }
    }
}
