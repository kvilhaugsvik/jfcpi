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

package org.freeciv.recorder;

import org.freeciv.connection.Over;
import org.freeciv.connection.PacketInputStream;
import org.freeciv.packet.PacketHeader;
import org.freeciv.recorder.traceFormat2.HeaderTF2;
import org.freeciv.recorder.traceFormat2.RecordTF2;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

public class TraceFormat2Read {
    private final PacketInputStream inAsPacket;
    private final DataInputStream inAsData;

    private final HeaderTF2 header;

    public TraceFormat2Read(InputStream in, Over state, Class<? extends PacketHeader> packetHeaderClass) throws IOException {
        this.inAsPacket = new PacketInputStream(in, state, packetHeaderClass);
        this.inAsData = new DataInputStream(in);

        try {
            header = new HeaderTF2(inAsData);
        } catch (IOException e) {
            throw new IOException("Error reading trace headers", e);
        }
    }

    public boolean isDynamic() {
        return header.includesTime();
    }

    public int getVersion() {
        return header.formatVersion;
    }

    public long getOriginalStartTime() {
        return header.recordStartedAt;
    }

    public RecordTF2 readRecord() throws IOException, InvocationTargetException {
        return new RecordTF2(inAsData, inAsPacket, header);
    }

}