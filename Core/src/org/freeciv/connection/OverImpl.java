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

public abstract class OverImpl implements Over {
    boolean over = false;
    private boolean open = true;

    @Override
    public void setStopReadingWhenOutOfInput() {
        over = true;
    }

    @Override
    public void whenDone() {
        if (!shouldIStopReadingWhenOutOfInput())
            throw new IllegalStateException("Tried to run whenOver() before it is over");

        open = false;

        whenDoneImpl();
    }

    protected abstract void whenDoneImpl();

    @Override
    public boolean shouldIStopReadingWhenOutOfInput() {
        return over;
    }

    @Override
    public boolean isOpen() {
        return open;
    }
}
