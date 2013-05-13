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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class TraceFormat2Read {
    private final RawFCProto interpret;
    private final DataInputStream inAsData;

    private final HeaderTF2 header;

    public TraceFormat2Read(InputStream in, Over state,
                            PacketsMapping packetsHelpUnderstand, HeaderData headerData,
                            Map<Integer, ReflexReaction> postReadReflexes,
                            boolean interpreted) throws IOException {
        final ReentrantLock completeReflexesInOneStep = new ReentrantLock();

        this.inAsData = new DataInputStream(in);
        this.interpret = new RawFCProto(
                state,
                interpreted ? new InterpretWhenPossible(packetsHelpUnderstand) : new AlwaysRaw(),
                headerData,
                new ReflexPacketKind(postReadReflexes, headerData, completeReflexesInOneStep),
                packetsHelpUnderstand
        );

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
        return new RecordTF2(header, inAsData, interpret);
    }

    public String getHumanReadableHeader() {
        return header.toString();
    }
}
