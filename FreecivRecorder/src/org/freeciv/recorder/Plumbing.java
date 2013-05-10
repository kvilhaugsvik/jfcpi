/*
 * Copyright (c) 2012, 2013. Sveinung Kvilhaugsvik
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

import org.freeciv.connection.*;
import org.freeciv.packet.Packet;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class Plumbing extends Thread {
    private final Source source;
    private final List<Sink> sinks;
    final boolean[] timeToExit; // TODO: Consider using a SwitchPoint when breaking Java 6 compatiblity

    boolean started = false;
    boolean finished = false;

    public Plumbing(Source source, List<Sink> sinks, boolean[] timeToExit) {
        if (1 != timeToExit.length)
            throw new IllegalArgumentException("Need to be signalled in a boolean array of size 1");

        this.timeToExit = timeToExit;
        this.source = source;
        this.sinks = sinks;
    }

    public static FreecivConnection socket2Connection(Socket connectedSocket, PacketsMapping versionKnowledge, Boolean understand, Map<Integer, ReflexReaction> postReceive, Map<Integer, ReflexReaction> postSend) throws IOException {
        return Connection.full(
                connectedSocket.getInputStream(), connectedSocket.getOutputStream(),
                versionKnowledge.getNewPacketHeaderData(),
                postReceive, postSend, versionKnowledge, understand);
    }

    @Override
    public void run() {
        if (!started)
            started = true;
        else
            throw new IllegalStateException("Already started");

        while (canRead() && canWriteToAllSinks()) {
            try {
                proxyPacket(source, sinks);
            } catch (DoneReading doneReading) {
                // TODO: Should sinks only used by this source also be set to over? Corner case: A shared Socket
                source.setStopReadingWhenOutOfInput();

                // TODO: remove if central Over management is added
                cleanUnclosed(source);
            }

            globalOver();
        }

        cleanUp();

        finished = true;
    }

    // TODO: Consider being less strict
    private boolean canWriteToAllSinks() {
        for (Sink sink : sinks)
            if (!sink.isOpen())
                return false;
        return true;
    }

    private boolean canRead() {
        return source.isOpen() || source.packetReady();
    }

    private void setAllSinksAndSourcesToOver() {
        for (Sink sink : sinks)
            sink.setStopReadingWhenOutOfInput();
        source.setStopReadingWhenOutOfInput();
    }

    private void cleanUp() {
        setAllSinksAndSourcesToOver();

        for (Sink sink : sinks)
            cleanUnclosed(sink);
        cleanUnclosed(source);
    }

    private void cleanUnclosed(Over over) {
        if (over.isOpen())
            over.whenDone();
    }

    private void proxyPacket(Source readFrom, List<Sink> sinks) throws DoneReading {
        try {
            Packet packet;

            try {
                packet = readFrom.getPacket();
            } catch (DoneReading e) {
                throw e;
            } catch (IOException e) {
                throw new IOException("Couldn't read packet from " + (readFrom.isFromClient() ? "client" : "server"), e);
            }

            for (Sink sink : sinks)
                sink.filteredWrite(readFrom.isFromClient(), packet);

        } catch (NotReadyYetException e) {
            Thread.yield();
        } catch (DoneReading e) {
            throw e;
        } catch (IOException e) {
            System.err.println("Finishing...");
            e.printStackTrace();
            setAllSinksAndSourcesToOver();
        }
    }

    private void globalOver() {
        if (timeToExit[0]) {
            setAllSinksAndSourcesToOver();
        }
    }

    public boolean isFinished() {
        return finished;
    }
}
