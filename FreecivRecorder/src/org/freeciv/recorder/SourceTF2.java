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

import org.freeciv.connection.NotReadyYetException;
import org.freeciv.connection.Over;
import org.freeciv.connection.ProtocolData;
import org.freeciv.packet.Packet;
import org.freeciv.recorder.traceFormat2.RecordTF2;
import org.freeciv.recorder.traceFormat2.TraceFormat2Read;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

public class SourceTF2 implements Source {
    private final boolean ignoreDynamic;
    private final boolean fromClient;
    private final int interestingConnectionID;
    private final TraceFormat2Read source;
    private final Over over;

    private final boolean careAboutConnectionID;
    private final long beganPlaying;

    private long sendNextAt;
    private RecordTF2 rec;

    /**
     * A Trace Format 2 source
     *
     * @param source the input stream to read from
     * @param over over implementation
     * @param versionKnowledge protocol data
     * @param ignoreDynamic should new packets appear at the same time they originally did?
     * @param fromClient the direction to not ignore (true if client2server, false if server2client)
     * @param interestingConnectionID the connection ID to not ignore. Set to -1 to let everything pass.
     * @param understand should the packets be interpreted?
     * @param beganPlayBack the time when the play back started
     * @throws IOException when there is a problem while reading the headers
     */
    public SourceTF2(InputStream source, Over over, ProtocolData versionKnowledge,
                     boolean ignoreDynamic, boolean fromClient, int interestingConnectionID, boolean understand,
                     long beganPlayBack) throws IOException {
        this.over = over;
        this.ignoreDynamic = ignoreDynamic;
        this.fromClient = fromClient;
        this.interestingConnectionID = interestingConnectionID;
        this.careAboutConnectionID = interestingConnectionID != RecordTF2.NO_CONNECTION_ID;
        this.source = new TraceFormat2Read(source, over,
                versionKnowledge,
                versionKnowledge.getNewPacketHeaderData(),
                versionKnowledge.getRequiredPostReceiveRules(),
                understand);

        this.beganPlaying = beganPlayBack;
        this.sendNextAt = beganPlaying;
        this.rec = null;
    }

    private synchronized void readNextRecord() throws IOException {
        try {
            rec = this.source.readRecord();

            while (rec.shouldBeIgnored()
                    || fromClient != rec.isClientToServer()
                    || careAboutConnectionID && interestingConnectionID != rec.getConnectionID()) {
                rec = null; // remove the disqualified record in case the following read fails
                rec = source.readRecord();
            }

            sendNextAt = source.isDynamic() && !ignoreDynamic ? beganPlaying + rec.getTimestamp() : sendNextAt + 1000;

        } catch (InvocationTargetException e) {
            throw new IOException("Exception while trying to read a packet in Trace format 2", e);
        }
    }

    @Override
    public boolean isFromClient() {
        return fromClient;
    }

    @Override
    public int getConnectionID() {
        return interestingConnectionID;
    }

    @Override
    public boolean packetReady() {
        return null != rec;
    }

    @Override
    public Packet getPacket() throws IOException, NotReadyYetException {
        if (null == this.rec)
            readNextRecord();

        if (System.currentTimeMillis() < sendNextAt) // TODO: Evaluate if more precision is needed
            throw new NotReadyYetException("To early");

        final Packet packet = rec.getPacket();
        this.rec = null;

        return packet;
    }

    @Override
    public void setStopReadingWhenOutOfInput() {
        over.setStopReadingWhenOutOfInput();
    }

    @Override
    public void whenDone() {
        over.whenDone();
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
