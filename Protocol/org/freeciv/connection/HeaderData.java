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

import org.freeciv.packet.Packet;
import org.freeciv.packet.PacketHeader;

import java.io.DataInput;
import java.lang.reflect.Constructor;

public class HeaderData implements PacketChangeHeader {
    private Constructor<? extends PacketHeader> constructFromStream;
    private Constructor<? extends PacketHeader> constructFromPNumAndSize;
    private int headerSize;

    public HeaderData(final Class<? extends PacketHeader> packetHeaderClass) {
        setHeaderTypeTo(packetHeaderClass);
    }

    public void setHeaderTypeTo(Class<? extends PacketHeader> packetHeaderClass) {
        try {
            this.constructFromStream = packetHeaderClass.getConstructor(DataInput.class);
            this.constructFromPNumAndSize = packetHeaderClass.getConstructor(int.class, int.class);
            this.headerSize = packetHeaderClass.getField("HEADER_SIZE").getInt(null);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Could not find constructor for header interpreter", e);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Could not find header size in header interpreter", e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not access header size in header interpreter", e);
        }
    }

    @Override
    public Constructor<? extends PacketHeader> getStream2Header() {
        return constructFromStream;
    }

    @Override
    public Constructor<? extends PacketHeader> getFields2Header() {
        return constructFromPNumAndSize;
    }

    public int getHeaderSize() {
        return headerSize;
    }

    public boolean sameType(Packet packet) {
        return constructFromStream.getDeclaringClass().isInstance(packet.getHeader());
    }
}
