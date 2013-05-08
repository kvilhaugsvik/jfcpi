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

package org.freeciv.recorder.traceFormat2;

import org.freeciv.connection.*;
import org.freeciv.recorder.traceFormat2.HeaderTF2;
import org.freeciv.recorder.traceFormat2.RecordTF2;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.locks.Lock;

public class TraceFormat2Read {
    private final PacketInputStream inAsPacket;
    private final DataInputStream inAsData;

    private final HeaderTF2 header;

    public TraceFormat2Read(InputStream in, Over state, Lock completeReflexesInOneStep,
                            PacketsMapping packetsHelpUnderstand, HeaderData headerData,
                            Map<Integer, ReflexReaction> postReadReflexes) throws IOException {
        this.inAsPacket = new PacketInputStream(in, state, completeReflexesInOneStep, headerData,
                new ReflexPacketKind(postReadReflexes, headerData), packetsHelpUnderstand);
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
        return header.getFormatVersion();
    }

    public long getOriginalStartTime() {
        return header.getOriginalStartTime();
    }

    public RecordTF2 readRecord() throws IOException, InvocationTargetException {
        return new RecordTF2(header, inAsData, inAsPacket);
    }

    public String getHumanReadableHeader() {
        return header.toString();
    }
}
