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

import org.freeciv.connection.PacketInputStream;
import org.freeciv.packet.Packet;
import org.freeciv.types.FCEnum;
import org.freeciv.types.UnderstoodBitVector;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class RecordTF2 {
    private final HeaderTF2 traceHeader;
    public final UnderstoodBitVector<RecordFlag> flags;
    public final boolean ignoreMe;
    public final long when;
    public final Packet packet;

    public RecordTF2(boolean client2server, long when, Packet packet, HeaderTF2 traceHeader, boolean traceHeaderSizeSkip, boolean recordHeaderSizeSkip) {
        this.traceHeader = traceHeader;
        this.flags = recordFlags(client2server, traceHeaderSizeSkip, recordHeaderSizeSkip);
        this.ignoreMe = false;
        this.when = when;
        this.packet = packet;
    }

    private static UnderstoodBitVector<RecordFlag> recordFlags(boolean client2server, boolean traceHeaderSizeSkip, boolean recordHeaderSizeSkip) {
        final UnderstoodBitVector<RecordFlag> flags = new RecordFlagVector();
        if (client2server)
            flags.set(RecordFlag.CLIENT_TO_SERVER);
        if (traceHeaderSizeSkip)
            flags.set(RecordFlag.TRACE_HEADER_SIZE_SKIP);
        if (recordHeaderSizeSkip)
            flags.set(RecordFlag.RECORD_HEADER_SIZE_SKIP);
        return flags;
    }

    public RecordTF2(DataInputStream inAsData, PacketInputStream inAsPacket, HeaderTF2 traceHeader) throws IOException, InvocationTargetException {
        this.flags = new RecordFlagVector(new byte[]{inAsData.readByte()});

        this.traceHeader = traceHeader;

        this.when = traceHeader.includesTime() ? inAsData.readLong() : -1;

        this.ignoreMe = ((skipIfUnknownTraceHeader() && traceHeader.unexpectedTraceHeaderSize) ||
                (skipIfUnknownRecordHeader() && traceHeader.unexpectedRecordHeaderSize));

        if (traceHeader.unexpectedRecordHeaderSize)
            TF2.headerSkip(inAsData, traceHeader.recordHeaderSize - calculateRecordHeaderSize(traceHeader.includesTime()));

        this.packet = inAsPacket.readPacket();
    }

    public void write(DataOutputStream to) throws IOException {
        to.write(flags.getAsByteArray());

        if (traceHeader.includesTime())
            to.writeLong(this.when);

        packet.encodeTo(to);
    }

    public boolean isClientToServer() {
        return flags.get(RecordFlag.CLIENT_TO_SERVER);
    }

    public boolean skipIfUnknownTraceHeader() {
        return flags.get(RecordFlag.TRACE_HEADER_SIZE_SKIP);
    }

    public boolean skipIfUnknownRecordHeader() {
        return flags.get(RecordFlag.RECORD_HEADER_SIZE_SKIP);
    }

    public static int calculateRecordHeaderSize(boolean dynamic) {
        int size = 1; // flags. For now only client to server
        if (dynamic)
            size += 8; // the time of the record
        return size;
    }

    public static enum RecordFlag implements FCEnum {
        CLIENT_TO_SERVER(0),
        TRACE_HEADER_SIZE_SKIP(1), // skip this record if the trace header had unknown fields
        RECORD_HEADER_SIZE_SKIP(2); // skip this record if the record header has unknown fields

        private final int number;

        RecordFlag(int number) {
            this.number = number;
        }

        @Override
        public int getNumber() {
            return number;
        }

        public static RecordFlag valueOf(int number) {
            return Helper.<RecordFlag>valueOfUnknownIsIllegal(number, RecordFlag.values());
        }
    }

    public static class RecordFlagVector extends UnderstoodBitVector<RecordFlag> {
        public static final int SIZE_IN_BITS = 7;

        public RecordFlagVector(byte[] src) {
            super(SIZE_IN_BITS, src, RecordFlag.class);
        }

        public RecordFlagVector() {
            super(SIZE_IN_BITS, RecordFlag.class);
        }
    }
}
