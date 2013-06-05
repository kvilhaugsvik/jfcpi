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

package org.freeciv.connection;

import org.freeciv.packet.DeltaKey;
import org.freeciv.packet.Packet;
import org.freeciv.packet.PacketHeader;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class ProtocolVariantManually {
    private final Map<Set<String>, Map<Integer, Method>> protocolVariants;
    private final Set<String> enabledCapabilities;
    private final Set<String> possibleCapabilities;

    private Map<Integer, Method> packetMakers;

    ProtocolVariantManually(Map<Set<String>, Map<Integer, Method>> protocolVariants, Set<String> possibleCapabilities) {
        this.protocolVariants = protocolVariants;
        this.possibleCapabilities = possibleCapabilities;
        this.enabledCapabilities = new HashSet<String>();

        updateVariant();
    }

    boolean canInterpret(int kind) {
        return packetMakers.containsKey(kind);
    }

    public Packet interpret(PacketHeader header, DataInputStream in, Map<DeltaKey, Packet> old) throws IOException {
        if (!canInterpret(header.getPacketKind()))
            throw new IOException(internalErrorMessage(header.getPacketKind()), new NoSuchElementException("Don't know how to interpret"));
        try {
            return (Packet)packetMakers.get(header.getPacketKind()).invoke(null, in, header, old);
        } catch (IllegalAccessException e) {
            throw new BadProtocolData(internalErrorMessage(header.getPacketKind()), e);
        } catch (InvocationTargetException e) {
            throw new IOException(internalErrorMessage(header.getPacketKind()), e);
        }
    }

    public boolean isCapabilityEnabled(String cap) {
        validateCapability(cap);
        return this.enabledCapabilities.contains(cap);
    }

    public void disableCapability(String cap) {
        validateCapability(cap);
        this.enabledCapabilities.remove(cap);
        updateVariant();
    }

    public void enableCapability(String cap) {
        validateCapability(cap);
        this.enabledCapabilities.add(cap);
        updateVariant();
    }

    private void validateCapability(String cap) {
        if (!this.possibleCapabilities.contains(cap))
            throw new IllegalArgumentException("Capability \"" + cap + "\" not supported.");
    }

    private void updateVariant() {
        this.packetMakers = this.protocolVariants.get(this.enabledCapabilities);
    }

    private static String internalErrorMessage(int packetKind) {
        return "Internal error while trying to read packet numbered " + packetKind + " from network";
    }
}
