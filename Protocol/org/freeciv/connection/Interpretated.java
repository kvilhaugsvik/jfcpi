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

package org.freeciv.connection;

import org.freeciv.packet.*;

import java.io.*;
import java.net.Socket;
import java.util.Map;

//TODO: Implement delta protocol
//TODO: Implement compression in protocol
public class Interpretated implements FreecivConnection {
    private final Uninterpreted toProcess;

    private final PacketsMapping interpreter;

    public Interpretated(String address, int port, Map<Integer, ReflexReaction> reflexes) throws IOException {
        this(new Socket(address, port), reflexes);
    }

    public Interpretated(Socket connection, Map<Integer, ReflexReaction> reflexes) throws IOException {
        interpreter = new PacketsMapping();

        toProcess = new Uninterpreted(connection, interpreter.getPacketHeaderClass(), reflexes);
    }

    public Packet getPacket() throws IOException, NotReadyYetException {
        RawPacket out;
        if (!toProcess.packetReady())
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
        toProcess.toSend(toSend);
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
        return toProcess.isOpen();
    }

    public boolean packetReady() {
        return toProcess.packetReady();
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
