/*
 * Copyright (c) 2013 Sveinung Kvilhaugsvik.
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

package org.freeciv.recorder;

import org.freeciv.connection.Over;
import org.freeciv.packet.Packet;

import java.io.IOException;

abstract class Sink implements Over {
    private final Filter filter;

    Sink(Filter filter) {
        this.filter = filter;
    }

    public abstract void write(Packet packet, boolean clientToServer, int connectionID) throws IOException, IllegalAccessException;

    public void filteredWrite(Packet packet, boolean clientToServer, int connectionID) throws IOException, IllegalAccessException {
        filter.update(packet);

        if (filter.isAccepted(packet, clientToServer))
            this.write(packet, clientToServer, connectionID);

        filter.inform(packet);
    }
}
