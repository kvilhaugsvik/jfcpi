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

import java.util.concurrent.locks.ReentrantLock;

public class OverImpl implements Over {
    private final ReentrantLock reflexLock;
    boolean over = false;

    public OverImpl() {
        this.reflexLock = new ReentrantLock();
    }

    public void setOver() {
        over = true;
    }

    public boolean isOver() {
        return over;
    }

    @Override
    public void networkAndReflexesLock() {
        reflexLock.lock();
    }

    @Override
    public void networkAndReflexesUnlock() {
        reflexLock.unlock();
    }

    @Override
    public boolean networkAndReflexesHeldByCurrentThread() {
        return reflexLock.isHeldByCurrentThread();
    }
}