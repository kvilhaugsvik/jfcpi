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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class FilterOr implements Filter {
    private final List<Filter> filters;

    FilterOr(Filter... any) {
        this(Arrays.asList(any));
    }

    FilterOr(List<Filter> any) {
        this.filters = new ArrayList<Filter>(any);
    }

    public void update(Packet packet) {
        for (Filter step : filters)
            step.update(packet);
    }

    public boolean isAccepted(Packet packet, boolean clientToServer) {
        for (Filter step : filters)
        if (step.isAccepted(packet, clientToServer))
            return true;

        return false;
    }

    public void inform(Packet packet) {
        for (Filter step : filters)
            step.inform(packet);
    }
}
