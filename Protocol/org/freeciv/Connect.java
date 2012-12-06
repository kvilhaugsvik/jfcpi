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
import org.freeciv.connection.FreecivConnection;
import org.freeciv.connection.ReflexReaction;
import org.freeciv.packet.*;

import java.io.*;
import java.net.Socket;
import java.util.Map;

//TODO: Implement delta protocol
//TODO: Implement compression in protocol
public class Connect implements FreecivConnection {
    private final OutputStream out;
    private final BufferIncoming toProcess;

    private final PacketsMapping interpreter;

    public Connect(String address, int port, Map<Integer, ReflexReaction> reflexes) throws IOException {
        this(new Socket(address, port), reflexes);
    }

    public Connect(Socket connection, Map<Integer, ReflexReaction> reflexes) throws IOException {
        interpreter = new PacketsMapping();

        out = connection.getOutputStream();

        toProcess = new BufferIncoming(this, connection, interpreter.getPacketHeaderClass(), reflexes);
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

    @Override
    public void toSend(Packet toSend) throws IOException {
        ByteArrayOutputStream packetSerialized = new ByteArrayOutputStream(toSend.getHeader().getTotalSize());
        DataOutputStream packet = new DataOutputStream(packetSerialized);

        toSend.encodeTo(packet);

        out.write(packetSerialized.toByteArray());
    }

    @Override
    public void setOver() {
        toProcess.setOver();
    }

    @Override
    public boolean isOver() {
        return toProcess.isOver();
    }

    public boolean isOpen() {
        return !toProcess.isClosed();
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
