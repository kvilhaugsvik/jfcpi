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

import org.freeciv.connection.PacketWrite;
import org.freeciv.packet.Packet;

import java.io.IOException;

class SinkForward extends Sink {
    private final PacketWrite writeTo;

    SinkForward(PacketWrite writeTo, Filter filter) {
        super(filter);
        this.writeTo = writeTo;
    }

    public synchronized void write(Packet packet, boolean clientToServer, int connectionID) throws IOException {
        try {
            writeTo.send(packet);
        } catch (IOException e) {
            throw new IOException("Couldn't forward packet to " + (clientToServer ? "server" : "client"), e);
        }
    }

    @Override
    public void setStopReadingWhenOutOfInput() {
        writeTo.setStopReadingWhenOutOfInput();
    }

    @Override
    public void whenDone() {
        writeTo.whenDone();
    }

    @Override
    public boolean shouldIStopReadingWhenOutOfInput() {
        return writeTo.shouldIStopReadingWhenOutOfInput();
    }

    @Override
    public boolean isOpen() {
        return writeTo.isOpen();
    }
}
