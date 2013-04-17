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
import java.lang.reflect.Constructor;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//TODO: Implement compression in protocol
public class Interpreted implements FreecivConnection {
    private final Uninterpreted toProcess;

    private final PacketsMapping interpreter;
    private final HashMap<DeltaKey, Packet> oldRead;

    public Interpreted(Socket connection, Map<Integer, ReflexReaction> postReceiveReflexes, Map<Integer, ReflexReaction> postSendReflexes) throws IOException {
        this(connection, new PacketsMapping(), postReceiveReflexes, postSendReflexes);
    }

    private Interpreted(Socket connection, PacketsMapping interpreter, Map<Integer, ReflexReaction> postReceiveReflexes, Map<Integer, ReflexReaction> postSendReflexes)
            throws IOException {
        this(
                new Uninterpreted(connection, interpreter.getPacketHeaderClass(),
                        ReflexPacketKind.layer(interpreter.getRequiredPostReceiveRules(), postReceiveReflexes),
                        ReflexPacketKind.layer(interpreter.getRequiredPostSendRules(), postSendReflexes)),
                interpreter);
    }

    public Interpreted(Uninterpreted connection, PacketsMapping interpreter) throws IOException {
        this.interpreter = interpreter;
        this.toProcess = connection;
        this.oldRead = new HashMap<DeltaKey, Packet>();
    }

    public Packet getPacket() throws IOException, NotReadyYetException {
        RawPacket out = toProcess.getPacket();

        try {
            return interpreter.interpret(out.getHeader(),
                    new DataInputStream(new ByteArrayInputStream(out.getBodyBytes())),
                    oldRead);
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

    @Override
    public void setHeaderTypeTo(Class<? extends PacketHeader> newKind) {
        toProcess.setHeaderTypeTo(newKind);
    }

    @Override
    public Constructor<? extends PacketHeader> getStream2Header() {
        return toProcess.getStream2Header();
    }

    @Override
    public Constructor<? extends PacketHeader> getFields2Header() {
        return toProcess.getFields2Header();
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
