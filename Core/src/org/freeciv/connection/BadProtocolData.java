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

/**
 * Exception to throw when the protocol data extracted from Freeciv aren't compatible
 */
public class BadProtocolData extends IllegalStateException {
    public BadProtocolData(String message, Throwable e) {
        super(message, e);
    }

    public BadProtocolData(String message) {
        super(message);
    }
}
