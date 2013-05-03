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

import java.util.HashMap;
import java.util.Map;

public class ReflexPacketKind<WorksOn extends ConnectionRelated> {
    private final HashMap<Integer, ReflexReaction> quickRespond;
    private final WorksOn owner;

    public ReflexPacketKind(Map<Integer, ReflexReaction<WorksOn>> reflexes, WorksOn owner) {
        this.owner = owner;
        this.quickRespond = new HashMap<Integer, ReflexReaction>(reflexes);
    }

    public void handle(int packetKindNumber) {
        if (quickRespond.containsKey(packetKindNumber))
            quickRespond.get(packetKindNumber).apply(owner);
    }

    public static Map<Integer, ReflexReaction> layer(
            Map<Integer, ReflexReaction> base,
            Map<Integer, ReflexReaction> over) {
        HashMap<Integer, ReflexReaction> out = new HashMap<Integer, ReflexReaction>(base);
        out.putAll(over);
        return out;
    }
}
