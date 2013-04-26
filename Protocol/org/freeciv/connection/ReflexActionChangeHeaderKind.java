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

import org.freeciv.packet.Packet;
import org.freeciv.packet.PacketHeader;

public class ReflexActionChangeHeaderKind implements ReflexReaction<PacketChangeHeader> {
    private final Class<? extends PacketHeader> to;

    public ReflexActionChangeHeaderKind(Class<? extends PacketHeader> to) {
        this.to = to;
    }

    @Override
    public void apply(Packet incoming, PacketChangeHeader connection) {
        connection.setHeaderTypeTo(to);
    }
}
