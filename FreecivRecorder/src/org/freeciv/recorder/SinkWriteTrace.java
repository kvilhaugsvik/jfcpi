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
import org.freeciv.types.FCEnum;
import org.freeciv.types.UnderstoodBitVector;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class SinkWriteTrace extends Sink {
    private final boolean isDynamic;
    private final long began;
    private final int id;
    private final DataOutputStream traceFile;

    public SinkWriteTrace(Filter filter, OutputStream traceFile, boolean isDynamic, int id) throws IOException {
        super(filter);

        this.isDynamic = isDynamic;
        this.began = System.currentTimeMillis();

        this.id = id;
        this.traceFile = new DataOutputStream(traceFile);

        try {
            // the version of the trace format
            this.traceFile.writeChar(2);

            // file header size. It should be safe to skip unknown fields
            this.traceFile.writeByte(calculateFileHeaderSize(isDynamic));

            // record header size. It should be safe to skip unknown fields
            this.traceFile.writeByte(calculateRecordHeaderSize(isDynamic));

            // flags
            UnderstoodBitVector<TraceFlags> flags = getTraceFlags(isDynamic);

            traceFile.write(flags.getAsByteArray());

            // the time the record began
            if (isDynamic)
                this.traceFile.writeLong(System.currentTimeMillis());
        } catch (IOException e) {
            throw new IOException(id + ": Unable to write trace headers", e);
        }
    }

    private static UnderstoodBitVector<TraceFlags> getTraceFlags(boolean isDynamic) {
        UnderstoodBitVector<TraceFlags> flags = new UnderstoodBitVector<TraceFlags>(7, TraceFlags.class);

        if (isDynamic)
            flags.set(TraceFlags.INCLUDES_TIME);

        return flags;
    }

    static int calculateFileHeaderSize(boolean dynamic) {
        int size = 5; // 2 bytes for version, 1 for header size, 1 for record header size and 1 for flags
        if (dynamic)
            size += 8; // the start time
        return size;
    }

    static int calculateRecordHeaderSize(boolean dynamic) {
        int size = 1; // flags. For now only client to server
        if (dynamic)
            size += 8; // the time of the record
        return size;
    }

    public void write(boolean clientToServer, Packet packet) throws IOException {
        try {
            UnderstoodBitVector<RecordFlags> flags = getRecordFlags(clientToServer);
            traceFile.write(flags.getAsByteArray());

            if (isDynamic)
                traceFile.writeLong(System.currentTimeMillis() - began);

            packet.encodeTo(traceFile);
        } catch (IOException e) {
            throw new IOException(id + ": Failed to write a packet to trace", e);
        }
    }

    private UnderstoodBitVector<RecordFlags> getRecordFlags(boolean clientToServer) {
        UnderstoodBitVector<RecordFlags> flags = new UnderstoodBitVector<RecordFlags>(7, RecordFlags.class);

        if (clientToServer)
            flags.set(RecordFlags.CLIENT_TO_SERVER);

        return flags;
    }

    enum TraceFlags implements FCEnum {
        INCLUDES_TIME(0);

        private final int number;

        TraceFlags(int number) {
            this.number = number;
        }

        @Override
        public int getNumber() {
            return number;
        }

        public static TraceFlags valueOf(int number) {
            return Helper.<TraceFlags>valueOfUnknownIsIllegal(number, values());
        }
    }

    enum RecordFlags implements FCEnum {
        CLIENT_TO_SERVER(0),
        TRACE_HEADER_SIZE_SKIP(1), // skip this record if the trace header had unknown fields
        RECORD_HEADER_SIZE_SKIP(2); // skip this record if the record header has unknown fields

        private final int number;

        RecordFlags(int number) {
            this.number = number;
        }

        @Override
        public int getNumber() {
            return number;
        }

        public static RecordFlags valueOf(int number) {
            return Helper.<RecordFlags>valueOfUnknownIsIllegal(number, values());
        }
    }
}
