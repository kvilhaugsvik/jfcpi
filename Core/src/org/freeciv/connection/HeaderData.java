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
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class HeaderData implements PacketChangeHeader {
    private final ReentrantReadWriteLock lock;

    private Constructor<? extends PacketHeader> constructFromStream;
    private Constructor<? extends PacketHeader> constructFromPNumAndSize;
    private int headerSize;

    public HeaderData(final Class<? extends PacketHeader> packetHeaderClass) {
        this.lock = new ReentrantReadWriteLock();
        setHeaderTypeTo(packetHeaderClass);
    }

    public PacketHeader newHeaderFromStream(DataInput stream) {
        try {
            return getStream2Header().newInstance(stream);
        } catch (InstantiationException e) {
            throw new BadProtocolData("Header from stream issue", e);
        } catch (IllegalAccessException e) {
            throw new BadProtocolData("Header from stream issue", e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Exception thrown while reading header", e);
        }
    }

    @Override
    public void setHeaderTypeTo(Class<? extends PacketHeader> packetHeaderClass) {
        this.lock.writeLock().lock();
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
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public Constructor<? extends PacketHeader> getStream2Header() {
        this.lock.readLock().lock();
        try {
            return constructFromStream;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public Constructor<? extends PacketHeader> getFields2Header() {
        this.lock.readLock().lock();
        try {
            return constructFromPNumAndSize;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public int getHeaderSize() {
        this.lock.readLock().lock();
        try {
            return headerSize;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public boolean sameType(Packet packet) {
        this.lock.readLock().lock();
        try {
            return constructFromStream.getDeclaringClass().isInstance(packet.getHeader());
        } finally {
            this.lock.readLock().unlock();
        }
    }
}
