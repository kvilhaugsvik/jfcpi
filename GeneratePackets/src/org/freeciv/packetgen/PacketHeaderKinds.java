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

package org.freeciv.packetgen;

/**
 * The kind of Freeciv protocol header. Does not include the delta header.
 */
public enum PacketHeaderKinds {
    /**
     * The header kind used in Freeciv 2.4 and 2.5. The header is 3 bytes long. The first two bytes are packet size. The
     * last byte is the packet number.
     */
    FC_2_4,

    /**
     * The header kind currently used in Freeciv trunk. The header changes size from 3 bytes in the beginning to 4 bytes
     * when the connection is set up. The pre connection header is like {@see #FC_2_4}. The post connection header is
     * like {@see #FC_2_4_99_2011_11_02}
     */
    FC_trunk,

    /**
     * The header kind used for a time in trunk after Freeciv 2.4. The header is 4 bytes long. The first two bytes are
     * packet size. The last two bytes are the packet number.
     */
    FC_2_4_99_2011_11_02 /* Never used in a stable release */
}
