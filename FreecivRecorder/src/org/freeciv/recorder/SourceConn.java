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
import org.freeciv.connection.PacketRead;
import org.freeciv.packet.Packet;

import java.io.IOException;

public class SourceConn implements Source {
    private final PacketRead source;
    private final boolean fromClient;
    private final int connectionID;

    public SourceConn(PacketRead source, boolean fromClient, int connectionID) {
        this.source = source;
        this.fromClient = fromClient;
        this.connectionID = connectionID;
    }

    @Override
    public boolean isFromClient() {
        return fromClient;
    }

    @Override
    public int getConnectionID() {
        return connectionID;
    }

    @Override
    public boolean packetReady() {
        return source.packetReady();
    }

    @Override
    public Packet getPacket() throws IOException, NotReadyYetException {
        return source.getPacket();
    }

    @Override
    public void setStopReadingWhenOutOfInput() {
        source.setStopReadingWhenOutOfInput();
    }

    @Override
    public void whenDone() {
        source.whenDone();
    }

    @Override
    public boolean shouldIStopReadingWhenOutOfInput() {
        return source.shouldIStopReadingWhenOutOfInput();
    }

    @Override
    public boolean isOpen() {
        return source.isOpen();
    }
}
