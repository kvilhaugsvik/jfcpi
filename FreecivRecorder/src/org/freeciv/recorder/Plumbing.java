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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Plumbing extends Thread {
    final HashMap<Source, List<Sink>> sourcesToSinks;
    final boolean[] timeToExit; // TODO: Consider using a SwitchPoint when breaking Java 6 compatiblity

    boolean started = false;
    boolean finished = false;

    public Plumbing(HashMap<Source, List<Sink>> sourcesToSinks, boolean[] timeToExit) {
        if (1 != timeToExit.length)
            throw new IllegalArgumentException("Need to be signalled in a boolean array of size 1");

        this.timeToExit = timeToExit;
        this.sourcesToSinks = sourcesToSinks;
    }

    public static FreecivConnection socket2Connection(Socket connectedSocket, PacketsMapping versionKnowledge, Boolean understand, Map<Integer, ReflexReaction> postReceive, Map<Integer, ReflexReaction> postSend) throws IOException {
        final Uninterpreted tmp = new Uninterpreted(
                connectedSocket.getInputStream(), connectedSocket.getOutputStream(),
                versionKnowledge.getNewPacketHeaderData(),
                postReceive, postSend);

        if (understand)
            return new Interpreted(tmp, versionKnowledge);
        else
            return tmp;
    }

    @Override
    public void run() {
        if (!started)
            started = true;
        else
            throw new IllegalStateException("Already started");

        while (allSourcesAreOpen()) {
            for (Source source : sourcesToSinks.keySet())
                proxyPacket(source, sourcesToSinks.get(source));

            globalOver();
        }

        cleanUp();

        finished = true;
    }

    // TODO: What about sinks?
    private boolean allSourcesAreOpen() {
        for (Source source : sourcesToSinks.keySet())
            if (!source.isOpen())
                return false;

        return true;
    }

    private void setAllSinksAndSourcesToOver() {
        for (Source source : sourcesToSinks.keySet()) {
            for (Sink sink : sourcesToSinks.get(source))
                sink.setOver();
            source.setOver();
        }
    }

    private void cleanUp() {
        setAllSinksAndSourcesToOver();

        for (Source source : sourcesToSinks.keySet()) {
            for (Sink sink : sourcesToSinks.get(source))
                if (sink.isOpen())
                    sink.whenOver();
            source.whenOver();
        }
    }

    private void proxyPacket(Source readFrom, List<Sink> sinks) {
        try {
            Packet packet;

            try {
                packet = readFrom.getPacket();
            } catch (IOException e) {
                throw new IOException("Couldn't read packet from " + (readFrom.isFromClient() ? "client" : "server"), e);
            }

            for (Sink sink : sinks)
                sink.filteredWrite(readFrom.isFromClient(), packet);

        } catch (NotReadyYetException e) {
            Thread.yield();
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
