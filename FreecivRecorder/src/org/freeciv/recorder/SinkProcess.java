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
import org.freeciv.connection.OverImpl;
import org.freeciv.connection.ProtocolData;
import org.freeciv.connection.ProtocolVariantManually;
import org.freeciv.packet.DeltaKey;
import org.freeciv.packet.Packet;
import org.freeciv.packet.RawPacket;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;

public abstract class SinkProcess extends Sink {
    protected final HashMap<DeltaKey, Packet> oldFromServer = new HashMap<DeltaKey, Packet>();
    protected final HashMap<DeltaKey, Packet> oldFromClient = new HashMap<DeltaKey, Packet>();

    private final Over over;
    private final ProtocolVariantManually versionKnowledge;

    public SinkProcess(Filter filter, ProtocolData versionKnowledge) {
        super(filter);

        // FIXME: the packet mapper should come from the connection as it will have a state when capabilities can be set
        this.versionKnowledge = versionKnowledge.getNewPacketMapper();
        this.over = new OverImpl() {
            @Override
            protected void whenDoneImpl() {
                // use only where no closing is required in the processing
            }
        };
    }

    @Override
    public abstract void write(boolean clientToServer, Packet packet) throws IOException;

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

    public Packet interpret(boolean clientToServer, Packet packet) throws IOException {
        final Packet interpreted;
        if (packet instanceof RawPacket)
            interpreted = versionKnowledge.interpret(packet.getHeader(),
                    new DataInputStream(new ByteArrayInputStream(((RawPacket)packet).getBodyBytes())),
                    clientToServer ? oldFromClient : oldFromServer);
        else
            interpreted = packet;
        return interpreted;
    }
}
