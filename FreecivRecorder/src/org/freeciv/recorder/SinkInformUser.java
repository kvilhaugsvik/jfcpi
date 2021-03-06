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

import java.util.concurrent.locks.ReentrantLock;

class SinkInformUser extends Sink {
    private final int proxyNumber;
    private final OverImpl over;
    private final ReentrantLock writeLock;

    private SinkInformUser(final Filter filter, final int proxyNumber, ReentrantLock writeLock) {
        super(filter);
        this.proxyNumber = proxyNumber;
        this.writeLock = writeLock;
        this.over = new OverImpl() {
            @Override
            protected void whenDoneImpl() {
                // no need to close System.out
                // but "close" the user
                System.out.println(proxyNumber + " is finished");
            }
        };
    }

    public void write(Packet packet, boolean clientToServer, int connectionID) {
        assert proxyNumber == connectionID
                : "Can only handle one connection. Expected " + proxyNumber + ", got " + connectionID + ".";

        writeLock.lock();
        try {
            System.out.println(proxyNumber + (clientToServer ? " c2s: " : " s2c: ") + packet);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void setStopReadingWhenOutOfInput() {
        over.setStopReadingWhenOutOfInput();
    }

    @Override
    public void whenDone() {
        over.whenDone();
    }

    @Override
    public boolean shouldIStopReadingWhenOutOfInput() {
        return over.shouldIStopReadingWhenOutOfInput();
    }

    @Override
    public boolean isOpen() {
        return over.isOpen();
    }

    public static class SharedData {
        private final Filter filter;
        private final ReentrantLock writeLock;

        public SharedData(Filter filter) {
            this.filter = filter;
            this.writeLock = new ReentrantLock();
        }

        public SinkInformUser forConnection(int id) {
            return new SinkInformUser(filter, id, writeLock);
        }
    }
}
