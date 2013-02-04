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
import org.freeciv.packet.RawPacket;

import java.util.HashSet;

class FilterSometimesWorking implements Filter {
    private final HashSet<Integer> debugHadProblem = new HashSet<Integer>();
    private final HashSet<Integer> debugDidWork = new HashSet<Integer>();

    public void update(Packet packet) {
        if (packet instanceof RawPacket)
            debugHadProblem.add(packet.getHeader().getPacketKind());
        else
            debugDidWork.add(packet.getHeader().getPacketKind());
    }

    public boolean isRequested(Packet packet) {
        return debugHadProblem.contains(packet.getHeader().getPacketKind()) &&
                debugDidWork.contains(packet.getHeader().getPacketKind());
    }

    public void inform(Packet packet) {
        if (isRequested(packet))
            System.out.println("Debug: " + packet.getHeader().getPacketKind() + " fails but not always");
    }
}
