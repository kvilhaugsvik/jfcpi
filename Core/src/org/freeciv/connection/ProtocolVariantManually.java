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
import org.freeciv.packet.PacketInterpretedDelta;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A ProtocolVariant that allows the programmer to turn optional
 * Freeciv protocol capabilities on and off.
 */
public class ProtocolVariantManually implements ProtocolVariant {
    private final ProtocolData protocolData;
    private final Map<Set<String>, Map<Integer, Method>> protocolVariants;
    private final Set<String> enabledCapabilities;
    private final Set<String> possibleCapabilities;

    private Map<Integer, Method> packetMakers;

    ProtocolVariantManually(ProtocolData protocolData, Map<Set<String>, Map<Integer, Method>> protocolVariants, Set<String> possibleCapabilities) {
        this.protocolData = protocolData;
        this.protocolVariants = protocolVariants;
        this.possibleCapabilities = possibleCapabilities;
        this.enabledCapabilities = new HashSet<String>();

        updateVariant();
    }

    /**
     * Check if this protocol variant can interpret the specified packet.
     * @param kind the packet number of the packet.
     * @return true iff the specified packet can be interpreted.
     */
    boolean canInterpret(int kind) {
        return packetMakers.containsKey(kind);
    }

    @Override
    public Packet interpret(PacketHeader header, DataInputStream in, Map<DeltaKey, Packet> old) throws IOException, IllegalAccessException {
        if (!canInterpret(header.getPacketKind()))
            throw new IOException(internalErrorMessage(header.getPacketKind()), new NoSuchElementException("Don't know how to interpret"));
        try {
            final Packet packet = (Packet) packetMakers.get(header.getPacketKind()).invoke(null, in, header, old);

            if (protocolData.isDelta()) {
                /* Let future packets with the same key get their missing
                 * fields from this packet, */
                old.put(((PacketInterpretedDelta)packet).getKey(), packet);
            }

            return packet;
        } catch (InvocationTargetException e) {
            throw new IOException(internalErrorMessage(header.getPacketKind()), e);
        }
    }

    @Override
    public Packet newPacketFromValues(int number, Constructor<? extends PacketHeader> headerMaker, Map<DeltaKey, Packet> old, Object... args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Object[] allArgs;
        final Class[] allArgTypes;
        final Class<?> packetClass;
        final Method fromValues;

        allArgTypes = new Class[args.length + 2];
        for (int i = 0; i < args.length; i++) {
            allArgTypes[i] = args[i].getClass();
        }
        allArgTypes[args.length] = java.lang.reflect.Constructor.class;
        allArgTypes[args.length + 1] = java.util.Map.class;

        allArgs = new Object[args.length + 2];
        System.arraycopy(args, 0, allArgs, 0, args.length);
        allArgs[args.length] = headerMaker;
        allArgs[args.length + 1] = old;

        /* TODO: Should probably store a more direct reference to fromValues constructors. */
        if (!packetMakers.containsKey(number)) {
            throw new ClassNotFoundException("No packet number " + number);
        }
        packetClass = packetMakers.get(number).getDeclaringClass();

        fromValues = packetClass.getMethod("fromValues", allArgTypes);

        return (Packet) fromValues.invoke(null, allArgs);
    }

    @Override
    public boolean isCapabilityEnabled(String cap) {
        validateCapability(cap);
        return this.enabledCapabilities.contains(cap);
    }

    @Override
    public boolean isDelta() {
        return protocolData.isDelta();
    }

    @Override
    public boolean isBoolFolded() {
        return protocolData.isBoolFold();
    }

    /**
     * Disable the specified capability.
     * @param cap the capability to disable.
     */
    public void disableCapability(String cap) {
        validateCapability(cap);
        this.enabledCapabilities.remove(cap);
        updateVariant();
    }

    /**
     * Enable the specified capability.
     * @param cap the capability to enable.
     */
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
