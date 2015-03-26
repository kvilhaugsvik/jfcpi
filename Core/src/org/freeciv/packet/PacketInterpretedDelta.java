/*
 * Copyright (c) 2015. Sveinung Kvilhaugsvik
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

package org.freeciv.packet;

/**
 * An interpreted packet using the Freeciv delta protocol.
 */
public abstract class PacketInterpretedDelta extends InterpretedPacket {
    /**
     * Get the delta key for this packet. The DeltaKey is used to find the
     * old packet the delta protocol should take missing field values from.
     * The old packet is the previous packet of the same kind where all key
     * fields have the same value. If no key field exists any packet of the
     * same kind will do.
     * @return the delta key for this packet.
     */
    public abstract org.freeciv.packet.DeltaKey getKey();
}
