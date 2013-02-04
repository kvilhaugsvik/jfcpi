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

import org.freeciv.packet.Packet;

import java.io.IOException;
import java.util.List;

abstract class Sink {
    private final List<Filter> filters;

    Sink(List<Filter> filters) {
        this.filters = filters;
    }

    public abstract void write(boolean clientToServer, Packet packet) throws IOException;

    public void filteredWrite(boolean clientToServer, Packet packet) throws IOException {
        for (Filter step : filters)
            step.update(packet);

        if (isPacketWanted(packet, filters))
            this.write(clientToServer, packet);

        for (Filter step : filters)
            step.inform(packet);
    }

    private boolean isPacketWanted(Packet packet, List<Filter> filters) {
        for (Filter step : filters)
            if (step.isRequested(packet))
                return true;

        return false;
    }
}
