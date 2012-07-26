/*
 * Copyright (c) 2011. Sveinung Kvilhaugsvik
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

package org.freeciv;

import org.freeciv.connection.BufferIncoming;
import org.freeciv.connection.ReflexReaction;
import org.freeciv.packet.*;

import java.io.*;
import java.net.Socket;
import java.util.Map;

//TODO: Implement delta protocol
//TODO: Implement compression in protocol
public class Connect {
    private final OutputStream out;
    private final Socket server;
    private final BufferIncoming toProcess;

    private final PacketsMapping interpreter;

    private boolean over = false;

    public Connect(String address, int port, Map<Integer, ReflexReaction> reflexes) throws IOException {
        interpreter = new PacketsMapping();

        server = new Socket(address, port);
        out = server.getOutputStream();

        toProcess = new BufferIncoming(this, server, interpreter.getPacketHeaderClass(), reflexes);
    }

    public Packet getPacket() throws IOException, NotReadyYetException {
        RawPacket out;
        if (toProcess.isEmpty())
            throw new NotReadyYetException("No packets waiting");
        else
            out = toProcess.getNext();

        try {
            if (interpreter.canInterpret(out.getHeader().getPacketKind()))
                return interpreter.interpret(out.getHeader(),
                        new DataInputStream(new ByteArrayInputStream(out.getBodyBytes())));
            else
                return out;
        } catch (IOException e) {
            return out;
        }
    }

    public void toSend(Packet toSend) throws IOException {
        ByteArrayOutputStream packetSerialized = new ByteArrayOutputStream(toSend.getHeader().getTotalSize());
        DataOutputStream packet = new DataOutputStream(packetSerialized);

        toSend.encodeTo(packet);

        out.write(packetSerialized.toByteArray());
    }

    /**
     * Close the connection as soon as its data has been read
     */
    public void setOver() {
        over = true;
    }

    /**
     * Will the connection be closed (unless it already is) as soon as its empty?
     * @return true if the connection is closed or soon will be
     */
    public boolean isOver() {
        return over;
    }

    public boolean isOpen() {
        return !server.isClosed();
    }

    public boolean hasMorePackets() {
        return !toProcess.isEmpty();
    }

    public String getCapStringMandatory() {
        return interpreter.getCapStringMandatory();
    }

    public String getCapStringOptional() {
        return interpreter.getCapStringOptional();
    }

    public String getVersionLabel() {
        return interpreter.getVersionLabel();
    }

    public long getVersionMajor() {
        return interpreter.getVersionMajor();
    }

    public long getVersionMinor() {
        return interpreter.getVersionMinor();
    }

    public long getVersionPatch() {
        return interpreter.getVersionPatch();
    }
}
