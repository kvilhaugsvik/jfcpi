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

import org.freeciv.types.FCEnum;
import org.freeciv.types.UnderstoodBitVector;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class HeaderTF2 {
    public final int formatVersion;
    public final int traceHeaderSize;
    public final int recordHeaderSize;
    public final UnderstoodBitVector<TraceFlag> flags;
    public final long recordStartedAt;

    final boolean unexpectedTraceHeaderSize;
    final boolean unexpectedRecordHeaderSize;

    public HeaderTF2(long recordStartedAt, boolean isDynamic) {
        this.flags = traceFlags(isDynamic);
        this.formatVersion = TF2.FORMAT_VERSION;
        this.traceHeaderSize = calculateFileHeaderSize(includesTime());
        this.recordHeaderSize = RecordTF2.calculateRecordHeaderSize(includesTime());
        this.recordStartedAt = recordStartedAt;

        this.unexpectedTraceHeaderSize = false;
        this.unexpectedRecordHeaderSize = false;
    }

    private static UnderstoodBitVector<TraceFlag> traceFlags(boolean isDynamic) {
        final TraceFlagVector flags = new TraceFlagVector();
        if (isDynamic)
            flags.set(TraceFlag.INCLUDES_TIME);
        return flags;
    }

    public HeaderTF2(DataInputStream inAsData) throws IOException {
        // the version of the trace format
        this.formatVersion = inAsData.readChar();

        if (TF2.FORMAT_VERSION != this.formatVersion)
            throw new IOException("Can't read version " + this.formatVersion);

        this.traceHeaderSize = inAsData.readUnsignedByte();

        this.recordHeaderSize = inAsData.readUnsignedByte();

        this.flags = new TraceFlagVector(new byte[]{inAsData.readByte()});

        // is the time a packet arrived included in the trace
        boolean dynamic = includesTime();

        this.recordStartedAt = dynamic ? inAsData.readLong() : -1;

        this.unexpectedTraceHeaderSize = traceHeaderSize != calculateFileHeaderSize(dynamic);
        this.unexpectedRecordHeaderSize = recordHeaderSize != RecordTF2.calculateRecordHeaderSize(dynamic);

        // skip unknown parts of the header
        if (unexpectedTraceHeaderSize)
            TF2.headerSkip(inAsData, (long) (traceHeaderSize - calculateFileHeaderSize(dynamic)));
    }

    public void write(DataOutputStream to) throws IOException {
        // the version of the trace format
        to.writeChar(TF2.FORMAT_VERSION);

        // file header size. It should be safe to skip unknown fields
        to.writeByte(calculateFileHeaderSize(includesTime()));

        // record header size. It should be safe to skip unknown fields
        to.writeByte(RecordTF2.calculateRecordHeaderSize(includesTime()));

        // flags
        to.write(this.flags.getAsByteArray());

        // the time the record began
        if (includesTime())
            to.writeLong(System.currentTimeMillis());
    }

    public boolean includesTime() {
        return flags.get(TraceFlag.INCLUDES_TIME);
    }

    public static int calculateFileHeaderSize(boolean dynamic) {
        int size = 5; // 2 bytes for version, 1 for header size, 1 for record header size and 1 for flags
        if (dynamic)
            size += 8; // the start time
        return size;
    }

    public static enum TraceFlag implements FCEnum {
        INCLUDES_TIME(0);

        private final int number;

        TraceFlag(int number) {
            this.number = number;
        }

        @Override
        public int getNumber() {
            return number;
        }

        public static TraceFlag valueOf(int number) {
            return Helper.<TraceFlag>valueOfUnknownIsIllegal(number, TraceFlag.values());
        }
    }

    public static class TraceFlagVector extends UnderstoodBitVector<TraceFlag> {
        public static final int SIZE_IN_BITS = 7;

        public TraceFlagVector(byte[] src) {
            super(SIZE_IN_BITS, src, TraceFlag.class);
        }

        public TraceFlagVector() {
            super(SIZE_IN_BITS, TraceFlag.class);
        }
    }
}