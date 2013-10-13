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

import org.freeciv.connection.OverImpl;
import org.freeciv.packet.Packet;
import org.freeciv.recorder.traceFormat2.HeaderTF2;
import org.freeciv.recorder.traceFormat2.RecordTF2;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class SinkWriteTrace extends Sink {
    private final HeaderTF2 header;
    private final int id;
    private final DataOutputStream traceFile;
    private final OverImpl over;

    public SinkWriteTrace(Filter filter, OutputStream traceFile, boolean isDynamic, int id, long recordStartedAt) throws IOException {
        super(filter);

        this.id = id;
        this.traceFile = new DataOutputStream(traceFile);

        final SinkWriteTrace me = this;
        this.over = new OverImpl() {
            @Override
            protected void whenDoneImpl() {
                me.whenOverImpl();
            }
        };

        try {
            header = new HeaderTF2(recordStartedAt, isDynamic, false, id);
            header.write(this.traceFile);
        } catch (IOException e) {
            throw new IOException(id + ": Unable to write trace headers", e);
        }
    }

    public synchronized void write(Packet packet, boolean clientToServer, int connectionID) throws IOException {
        assert id == connectionID : "Many connections in one file not supported yet";
        try {
            final RecordTF2 record =
                    new RecordTF2(header, clientToServer,
                            System.currentTimeMillis() - header.getOriginalStartTime(),
                            packet, false, connectionID);
            record.write(traceFile);
        } catch (IOException e) {
            throw new IOException(id + ": Failed to write a packet to trace", e);
        }
    }

    @Override
    public void setStopReadingWhenOutOfInput() {
        over.setStopReadingWhenOutOfInput();
    }

    @Override
    public void whenDone() {
        over.whenDone();
    }

    private synchronized void whenOverImpl() {
        try {
            traceFile.close();
        } catch (IOException e) {
            System.err.println("Some data may not have been written to the trace for connection " + id);
            e.printStackTrace();
        }
    }

    @Override
    public boolean shouldIStopReadingWhenOutOfInput() {
        return over.shouldIStopReadingWhenOutOfInput();
    }

    @Override
    public boolean isOpen() {
        return over.isOpen();
    }
}
