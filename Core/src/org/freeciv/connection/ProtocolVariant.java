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

public interface ProtocolVariant {
    public Packet interpret(PacketHeader header, DataInputStream in, Map<DeltaKey, Packet> old) throws IOException;

    /**
     * Creates a new instance of the specified Freeciv packet for the
     * current Freeciv protocol variant. The value of each field of the
     * packet body must be specified.
     * @param number the packet number of the Freeciv packet.
     * @param headerMaker constructor for the current packet header kind.
     * @param old the delta packet storage. This is where previously sent
     *            packets of the same kind can be found.
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

    public boolean isCapabilityEnabled(String cap);
}
