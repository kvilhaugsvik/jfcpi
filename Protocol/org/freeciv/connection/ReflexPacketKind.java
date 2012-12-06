/*
 * Copyright (c) 2012. Sveinung Kvilhaugsvik
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

import org.freeciv.packet.RawPacket;

import java.util.HashMap;
import java.util.Map;

public class ReflexPacketKind {
    private final HashMap<Integer, ReflexReaction> quickRespond;
    private final FreecivConnection owner;

    public ReflexPacketKind(Map<Integer, ReflexReaction> reflexes, FreecivConnection owner) {
        this.owner = owner;
        this.quickRespond = new HashMap<Integer, ReflexReaction>(reflexes);
    }

    public void handle(RawPacket incoming) {
        if (quickRespond.containsKey(incoming.getHeader().getPacketKind()))
            quickRespond.get(incoming.getHeader().getPacketKind()).apply(incoming, owner);
    }
}
