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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A ProtocolVariant that automatically automatically turns optional
 * Freeciv protocol capabilities on and off based on what is sent and
 * received.
 */
public class ProtocolVariantAutomatic implements ProtocolVariant {
    private final ProtocolVariantManually variant;
    private final ReentrantLock orderLock;
    private final Condition clientIsSet;

    private boolean needToKnowCaps;
    private String clientCap;
    private String serverCap;

    /**
     * Construct a new ProtocolVariantAutomatic wrapping a
     * ProtocolVariantManually. No optional capabilities will be detected
     * if the wrapped ProtocolVariantManually is null.
     * @param variant the ProtocolVariantManually detected capabilities
     *                should be stored in.
     */
    public ProtocolVariantAutomatic(ProtocolVariantManually variant) {
        this.variant = variant;
        this.orderLock = new ReentrantLock();
        this.clientIsSet = orderLock.newCondition();

        /* In some cases, like when the protocol isn't interpreted,
         * optional capabilities aren't interesting. */
        this.needToKnowCaps = null != variant;

        this.clientCap = null;
        this.serverCap = null;
    }

    /**
     * Check if there is interest in finding optional Freeciv protocol
     * capabilities. There is interest if ordered to look for optional
     * capabilities but they still haven't been negotiated.
     * @return true if optional capabilities are interesting and they still
     * haven't been negotiated.
     */
    public boolean needToKnowCaps() {
        return needToKnowCaps;
    }

    /**
     * Have a look at the specified packet to see if it negotiates optional
     * capabilities. Extract and set the optional capabilities if that is
     * the case.
     * @param interpreted the packet to look at.
     */
    public void extractVariantInfo(Packet interpreted) {
        switch (interpreted.getHeader().getPacketKind()) {
            case 4:
                orderLock.lock();
                try {
                    this.clientCap = getGetCapabilityValue(interpreted);
                    clientIsSet.signal();
                } finally {
                    orderLock.unlock();
                }
                break;

            case 5:
                this.serverCap = getGetCapabilityValue(interpreted);

                final Set<String> clientCaps;
                orderLock.lock();
                try {
                    while (null == clientCap)
                        clientIsSet.awaitUninterruptibly();

                    clientCaps = getClientCaps(clientCap);
                } finally {
                    orderLock.unlock();
                }

                for (String cap : getCaps(clientCaps, serverCap))
                    variant.enableCapability(cap);

                this.needToKnowCaps = false;
                break;
            default:
        }
    }

    private static Set<String> getClientCaps(String fullCap) {
        final Set<String> clientCaps = new HashSet<String>();
        String[] splitClient = fullCap.split(" ");
        for (int i = 1; i < splitClient.length; i++)
            clientCaps.add(splitClient[i]);
        return clientCaps;
    }

    private static Set<String> getCaps(Set<String> clientCaps, String fullCap) {
        final Set<String> caps = new HashSet<String>();
        String[] splitServer = fullCap.split(" ");
        for (int i = 1; i < splitServer.length; i++)
            if (clientCaps.contains(splitServer[i]))
                caps.add(splitServer[i]);
        return caps;
    }

    private static String getGetCapabilityValue(Packet interpreted) {
        try {
            return (String) interpreted.getClass().getMethod("getCapabilityValue").invoke(interpreted);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to read capability value of packet " + interpreted, e);
        }
    }

    @Override
    public boolean isCapabilityEnabled(String cap) {
        return variant.isCapabilityEnabled(cap);
    }

    @Override
    public boolean isDelta() {
        return variant.isDelta();
    }

    @Override
    public boolean isBoolFolded() {
        return variant.isBoolFolded();
    }

    @Override
    public Packet interpret(PacketHeader header, DataInputStream in, Map<DeltaKey, Packet> old) throws IOException, IllegalAccessException {
        final Packet interpret = variant.interpret(header, in, old);

        if (this.needToKnowCaps())
            this.extractVariantInfo(interpret);

        return interpret;
    }

    @Override
    public Packet newPacketFromValues(int number, Constructor<? extends PacketHeader> headerMaker, Map<DeltaKey, Packet> old, Object... args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return variant.newPacketFromValues(number, headerMaker, old, args);
    }
}
