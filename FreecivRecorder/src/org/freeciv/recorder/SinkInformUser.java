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

import org.freeciv.connection.OverImpl;
import org.freeciv.packet.Packet;

import java.util.List;

class SinkInformUser extends Sink {
    private final int proxyNumber;
    private final OverImpl over;

    SinkInformUser(Filter filter, int proxyNumber) {
        super(filter);
        this.proxyNumber = proxyNumber;
        this.over = new OverImpl() {
            @Override
            protected void whenOverImpl() {
                // no need to close System.out
            }
        };
    }

    public void write(boolean clientToServer, Packet packet) {
        System.out.println(proxyNumber + (clientToServer ? " c2s: " : " s2c: ") + packet);
    }

    @Override
    public void setOver() {
        over.setOver();
    }

    @Override
    public void whenOver() {
        over.whenOver();
    }

    @Override
    public boolean isOver() {
        return over.isOver();
    }

    @Override
    public boolean isOpen() {
        return over.isOpen();
    }
}
