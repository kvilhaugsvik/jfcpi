/*
 * Copyright (c) 2014. Sveinung Kvilhaugsvik
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
 * Basic protocol information that even an uninterpreted connection will need.
 */
public interface BasicProtocolData {
    HeaderData getNewPacketHeaderData();

    /**
     * Magic number indicating compression. Given as packet size. Packets this "size" or larger are compressed.
     * To get the real size subtract the compression border.
     * @return size limit for uncompressed packets.
     */
    int getCompressionBorder();

    /**
     * Magic number given as packet size. Indicates that the real size is given in the next four bytes.
     * @return the magic number JumboSize
     */
    int getJumboSize();
}
