/*
 * Copyright (c) 2013 Sveinung Kvilhaugsvik.
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

import org.freeciv.packet.Packet;

import java.io.DataOutputStream;
import java.io.IOException;

class SinkWriteTrace extends Sink {
    private final boolean isDynamic;
    private final int id;
    private final DataOutputStream traceFile;

    public SinkWriteTrace(Filter filter, DataOutputStream traceFile, boolean isDynamic, int id) throws IOException {
        super(filter);

        this.isDynamic = isDynamic;
        this.id = id;
        this.traceFile = traceFile;

        try {
            // the version of the trace format
            traceFile.writeChar(1);
            // is the time a packet arrived included in the trace
            traceFile.writeBoolean(isDynamic);
        } catch (IOException e) {
            throw new IOException(id + ": Unable to write trace headers", e);
        }
    }

    public void write(boolean clientToServer, Packet packet) throws IOException {
        try {
            traceFile.writeBoolean(clientToServer);
            if (isDynamic)
                traceFile.writeLong(System.currentTimeMillis());
            packet.encodeTo(traceFile);
        } catch (IOException e) {
            throw new IOException(id + ": Failed to write a packet to trace", e);
        }
    }
}
