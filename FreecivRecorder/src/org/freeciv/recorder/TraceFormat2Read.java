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
import org.freeciv.packet.RawPacket;
import org.freeciv.types.UnderstoodBitVector;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

public class TraceFormat2Read {
    private final PacketInputStream inAsPacket;
    private final DataInputStream inAsData;

    private final int version;
    private final boolean traceHeaderSizeAsExpected;
    private final int recordHeaderSize;
    private final boolean dynamic;
    private final boolean weirdRecordHeaderSize;
    private final long recordStart;

    public TraceFormat2Read(InputStream in, Over state, Class<? extends PacketHeader> packetHeaderClass) throws IOException {
        inAsPacket = new PacketInputStream(in, state, packetHeaderClass);

        this.inAsData = new DataInputStream(in);

        try {
            // the version of the trace format
            this.version = inAsData.readChar();

            int traceHeaderSize = inAsData.readUnsignedByte();

            this.recordHeaderSize = inAsData.readUnsignedByte();

            final UnderstoodBitVector<SinkWriteTrace.TraceFlags> flags =
                    new UnderstoodBitVector<SinkWriteTrace.TraceFlags>(7, new byte[]{inAsData.readByte()},
                            SinkWriteTrace.TraceFlags.class);

            // is the time a packet arrived included in the trace
            this.dynamic = flags.get(SinkWriteTrace.TraceFlags.INCLUDES_TIME);

            this.traceHeaderSizeAsExpected = traceHeaderSize == SinkWriteTrace.calculateFileHeaderSize(dynamic);
            this.weirdRecordHeaderSize = recordHeaderSize != SinkWriteTrace.calculateRecordHeaderSize(dynamic);

            this.recordStart = dynamic ? inAsData.readLong() : -1;

            // skip unknown parts of the header
            if (!traceHeaderSizeAsExpected) {
                long toSkip = traceHeaderSize - SinkWriteTrace.calculateFileHeaderSize(dynamic);
                headerSkip(toSkip);
            }
        } catch (IOException e) {
            throw new IOException("Error reading trace headers", e);
        }

        if (2 != this.version)
            throw new IOException("Can't read this version " + version);
    }

    private void headerSkip(long toSkip) throws IOException {
        if (toSkip < 0)
            throw new IOException("Wrong format");
        while (0 != toSkip) {
            final long skipped = inAsData.skip(toSkip);
            if (0 < skipped)
                toSkip = toSkip - skipped;
        }
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public int getVersion() {
        return version;
    }

    public long getOriginalStartTime() {
        return recordStart;
    }

    public Record readRecord() throws IOException, InvocationTargetException {
        final UnderstoodBitVector<SinkWriteTrace.RecordFlags> flags =
                new UnderstoodBitVector<SinkWriteTrace.RecordFlags>(7, new byte[]{inAsData.readByte()},
                        SinkWriteTrace.RecordFlags.class);

        final boolean client2server = flags.get(SinkWriteTrace.RecordFlags.CLIENT_TO_SERVER);

        final long when = dynamic ? inAsData.readLong() : -1;

        boolean ignoreMe =
                ((flags.get(SinkWriteTrace.RecordFlags.TRACE_HEADER_SIZE_SKIP) && !traceHeaderSizeAsExpected) ||
                        (flags.get(SinkWriteTrace.RecordFlags.RECORD_HEADER_SIZE_SKIP) && weirdRecordHeaderSize));

        if (weirdRecordHeaderSize)
            headerSkip(recordHeaderSize - SinkWriteTrace.calculateRecordHeaderSize(dynamic));

        final RawPacket packet = inAsPacket.readPacket();

        return new Record(ignoreMe, client2server, when, packet);
    }

    public class Record {
        public final boolean ignoreMe;
        public final boolean client2server;
        public final long when;
        public final RawPacket packet;

        public Record(boolean ignoreMe, boolean client2server, long when, RawPacket packet) {
            this.ignoreMe = ignoreMe;
            this.client2server = client2server;
            this.when = when;
            this.packet = packet;
        }
    }
}
