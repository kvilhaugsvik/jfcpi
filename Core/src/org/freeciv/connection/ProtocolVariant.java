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
import java.util.Map;

/**
 * Handle optional Freeciv protocol capabilities.
 * The Freeciv protocol supports optional capabilities. A protocol
 * capability is a change to the protocol. If both the client and the
 * server supports it they will enable it and use the changed protocol. If
 * not the old protocol will be used.
 * This allows the Freeciv protocol to change even when after a version's
 * protocol is frozen.
 */
public interface ProtocolVariant {
    /**
     * Deserialize the body of the Freeciv packet type given in its header
     * for the for the current Freeciv protocol variant. The packet header
     * and the source to deserialize the body from must be specified.
     * @param header the header of the packet.
     * @param in the source to deserialize the packet from.
     * @param old the delta packet storage. This is where the previously
     *            read packet of the same kind can be found. The delta
     *            protocol will use the old packet's fields to fill inn
     *            field values missing in the new packet.
     * @return an instance of the specified packet with its fields
     * deserialized from the source.
     * @throws IOException when there is a problem reading the packet or
     * deserializing its data.
     * @throws IllegalAccessException if accessing this is forbidden by
     * Java's access control.
     */
    public Packet interpret(PacketHeader header, DataInputStream in, Map<DeltaKey, Packet> old) throws IOException, IllegalAccessException;

    /**
     * Creates a new instance of the specified Freeciv packet for the
     * current Freeciv protocol variant. The value of each field of the
     * packet body must be specified.
     * @param number the packet number of the Freeciv packet.
     * @param headerMaker constructor for the current packet header kind.
     * @param old the delta packet storage. This is where the previously
     *            sent packet of the same kind can be found. The delta
     *            protocol will use the old packet's fields to find out
     *            what field values it won't have to send in the new
     *            packet.
     * @param args the fields of the body of the packet.
     * @return a new instance of the specified packet.
     * @throws ClassNotFoundException if no packet with the given number
     * exists.
     * @throws NoSuchMethodException if the packet don't have the expected
     * method. Can be caused by wrong arguments, by the wrong number of
     * arguments or by the packet being created by an incompatible packet
     * generator.
     * @throws java.lang.reflect.InvocationTargetException if there is a
     * problem while creating the packet.
     * @throws IllegalAccessException if accessing this is forbidden by
     * Java's access control.
     */
    public Packet newPacketFromValues(final int number,
                                      final Constructor<? extends PacketHeader> headerMaker,
                                      final Map<DeltaKey, Packet> old,
                                      final Object... args) throws ClassNotFoundException,
                                                         NoSuchMethodException,
                                                         InvocationTargetException,
                                                         IllegalAccessException;

    /**
     * Check if the specified Freeciv protocol variant capability is
     * enabled.
     * @param cap name of the capability to check.
     * @return true iff the specified Freeciv protocol variant capability
     * is there.
     */
    public boolean isCapabilityEnabled(String cap);
}
