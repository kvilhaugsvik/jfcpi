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

import org.freeciv.connection.DoneReading;
import org.freeciv.connection.RawFCProto;
import org.freeciv.packet.Packet;
import org.freeciv.types.FCEnum;
import org.freeciv.types.UnderstoodBitVector;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.LinkedList;

public class RecordTF2 {
    public static final int NO_CONNECTION_ID = -1;

    /*********************
     * Stored in the trace
     *********************/
    private final UnderstoodBitVector<RecordFlag> flags;
    private final long when;
    private final int connID;
    private final Packet packet;

    /************************
     * Not stored in the trace
     *************************/
    private final boolean ignoreMe;
    private final HeaderTF2 traceHeader;

    public RecordTF2(HeaderTF2 traceHeader, boolean client2server, long when, Packet packet, boolean traceHeaderSizeSkip, int connID) {
        this.traceHeader = traceHeader;
        this.connID = connID;
        this.flags = recordFlags(client2server, traceHeaderSizeSkip, skipUnlessYouReadConnectionID(traceHeader, connID));
        this.ignoreMe = false;
        this.when = when;
        this.packet = packet;
    }

    private static boolean skipUnlessYouReadConnectionID(HeaderTF2 traceHeader, int connID) {
        return traceHeader.includesConnectionID() && connID != traceHeader.defaultConnection;
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

    public RecordTF2(HeaderTF2 traceHeader, DataInputStream inAsData, RawFCProto interpret) throws IOException, InvocationTargetException {
        try {
            this.flags = new RecordFlagVector(new byte[]{inAsData.readByte()});
        } catch (EOFException e) {
            throw new DoneReading("Out of data before the first byte of the record was read");
        }

        this.traceHeader = traceHeader;

        this.when = traceHeader.includesTime() ? inAsData.readLong() : -1;

        this.connID = traceHeader.includesConnectionID() ? inAsData.readChar() : NO_CONNECTION_ID;

        this.ignoreMe = ((skipIfUnknownTraceHeader() && traceHeader.isTraceHeaderSizeUnexpected()) ||
                (skipIfUnknownRecordHeader() && traceHeader.isRecordHeaderSizeUnexpected()));

        if (traceHeader.isRecordHeaderSizeUnexpected())
            TF2.headerSkip(inAsData, traceHeader.getRecordHeaderSize() - calculateRecordHeaderSize(traceHeader.includesTime(), traceHeader.includesConnectionID()));

        LinkedList<Packet> p = new LinkedList<Packet>();
        interpret.fromInputStream(inAsData).putPackets(p);

        if (1 != p.size())
            throw new IllegalArgumentException("Only one packet pr record is supported at the moment");

        // TODO: Handle more than one packet
        this.packet = p.get(0);
    }

    public void write(DataOutputStream to) throws IOException {
        to.write(flags.getAsByteArray());

        if (traceHeader.includesTime())
            to.writeLong(this.when);

        if (traceHeader.includesConnectionID())
            to.writeChar(connID);

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

    public long getTimestamp() {
        return when;
    }

    /**
     * Get the connection ID of this record
     * @return the connection number if one is defined and NO_CONNECTION_ID if not
     */
    public int getConnectionID() {
        return connID;
    }

    public Packet getPacket() {
        return packet;
    }

    public boolean shouldBeIgnored() {
        return ignoreMe;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();

        out.append("Flags: ");
        out.append(flags.toString());
        out.append("\t");

        if (traceHeader.includesTime()) {
            out.append("When: ");
            out.append(new Date(when + traceHeader.getOriginalStartTime()));
            out.append("\t");
        }

        if (traceHeader.includesConnectionID()) {
            out.append("Connection id: ");
            out.append(connID);
            out.append("\t");
        }

        out.append("Packet: ");
        out.append(packet.toString());

        out.append("\n");

        return out.toString();
    }

    public static int calculateRecordHeaderSize(boolean dynamic, boolean multiConn) {
        int size = 1; // flags. For now only client to server
        if (dynamic)
            size += 8; // the time of the record
        if (multiConn)
            size += 2; // the connection ID
        return size;
    }

    public static enum RecordFlag implements FCEnum {
        CLIENT_TO_SERVER(0),
        TRACE_HEADER_SIZE_SKIP(1), // skip this record if the trace header had unknown fields
        RECORD_HEADER_SIZE_SKIP(2), // skip this record if the record header has unknown fields

        UNKNOWN(-1); // a new flag or an error

        private final int number;

        RecordFlag(int number) {
            this.number = number;
        }

        @Override
        public int getNumber() {
            return number;
        }

        public static RecordFlag valueOf(int number) {
            return Helper.<RecordFlag>valueOfUnknownIsMinusOne(number, RecordFlag.values());
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
