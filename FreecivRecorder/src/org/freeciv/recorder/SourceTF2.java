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
import org.freeciv.connection.PacketsMapping;
import org.freeciv.packet.Packet;
import org.freeciv.recorder.traceFormat2.RecordTF2;
import org.freeciv.recorder.traceFormat2.TraceFormat2Read;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.locks.ReentrantLock;

public class SourceTF2 implements Source {
    private final boolean ignoreDynamic;
    private final boolean fromClient;
    private final TraceFormat2Read source;
    private final Over over;

    private final long beganPlaying;

    private long sendNextAt;
    private RecordTF2 rec;

    public SourceTF2(InputStream source, Over over, PacketsMapping versionKnowledge, boolean ignoreDynamic, boolean fromClient, boolean understand) throws IOException {
        this.over = over;
        this.ignoreDynamic = ignoreDynamic;
        this.fromClient = fromClient;
        this.source = new TraceFormat2Read(source, over, new ReentrantLock(),
                understand ? versionKnowledge : null,
                versionKnowledge.getNewPacketHeaderData(),
                versionKnowledge.getRequiredPostReceiveRules());

        this.beganPlaying = System.currentTimeMillis();
        this.sendNextAt = beganPlaying;
        this.rec = null;
    }

    private synchronized void readNextRecord() throws IOException {
        try {
            rec = this.source.readRecord();

            while (rec.shouldBeIgnored() || fromClient != rec.isClientToServer()) {
                rec = source.readRecord();
                continue;
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
    public void setOver() {
        over.setOver();
    }

    @Override
    public void whenOver() {
        over.whenOver();
    }

    @Override
    public boolean isOver() {
        return over.isOver();
    }

    @Override
    public boolean isOpen() {
        return over.isOpen();
    }
}
