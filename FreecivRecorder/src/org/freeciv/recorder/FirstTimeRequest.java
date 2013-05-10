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

package org.freeciv.recorder;

/**
 * Return the time the first time getTime was called
 */
public class FirstTimeRequest {
    private long firstTime;

    public FirstTimeRequest() {
        this.firstTime  = Long.MIN_VALUE;
    }

    /**
     * Get the time this method first was called
     * @return the time this method first was called
     */
    public long getTime() {
        if (Long.MIN_VALUE == firstTime)
            firstTime = System.currentTimeMillis();

        return firstTime;
    }
}
