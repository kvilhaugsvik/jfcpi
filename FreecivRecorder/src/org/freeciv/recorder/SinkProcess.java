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
import org.freeciv.connection.PacketsMapping;
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
    private final PacketsMapping versionKnowledge;

    public SinkProcess(Filter filter, PacketsMapping versionKnowledge) {
        super(filter);

        this.versionKnowledge = versionKnowledge;
        this.over = new OverImpl() {
            @Override
            protected void whenOverImpl() {
                // use only where no closing is required in the processing
            }
        };
    }

    @Override
    public abstract void write(boolean clientToServer, Packet packet) throws IOException;

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
