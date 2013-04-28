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
import org.freeciv.recorder.traceFormat2.HeaderTF2;
import org.freeciv.recorder.traceFormat2.RecordTF2;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class SinkWriteTrace extends Sink {
    private final HeaderTF2 header;
    private final long began;
    private final int id;
    private final DataOutputStream traceFile;

    public SinkWriteTrace(Filter filter, OutputStream traceFile, boolean isDynamic, int id) throws IOException {
        super(filter);

        this.began = System.currentTimeMillis();

        this.id = id;
        this.traceFile = new DataOutputStream(traceFile);

        try {
            header = new HeaderTF2(System.currentTimeMillis(), isDynamic);
            header.write(this.traceFile);
        } catch (IOException e) {
            throw new IOException(id + ": Unable to write trace headers", e);
        }
    }

    public void write(boolean clientToServer, Packet packet) throws IOException {
        try {
            new RecordTF2(header, clientToServer, System.currentTimeMillis() - began, packet, false, false).write(traceFile);
        } catch (IOException e) {
            throw new IOException(id + ": Failed to write a packet to trace", e);
        }
    }

}
