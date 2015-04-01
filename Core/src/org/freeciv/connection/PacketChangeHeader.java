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

import org.freeciv.packet.PacketHeader;

import java.lang.reflect.Constructor;

public interface PacketChangeHeader extends ConnectionRelated {
    /**
     * Change the packet header type
     */
    void setHeaderTypeTo(Class<? extends PacketHeader> newKind);

    /**
     * Get the current packet header constructor from DataInput
     * @return the packet header constructor from DataInput
     */
    Constructor<? extends PacketHeader> getStream2Header();

    /**
     * Get the current packet header constructor from size and kind
     * @return the packet header constructor from size and kind
     */
    Constructor<? extends PacketHeader> getFields2Header();
}
