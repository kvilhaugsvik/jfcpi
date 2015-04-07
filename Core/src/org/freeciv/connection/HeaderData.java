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

/**
 * Keep track of and make use of the current packet header kind.
 *
 * Some versions of Freeciv change header kind after some packets have been
 * exchanged. HeaderData can perform various header operations using the
 * correct header kind.
 */
public class HeaderData implements PacketChangeHeader {
    private final ReentrantReadWriteLock lock;

    private Constructor<? extends PacketHeader> constructFromStream;
    private Constructor<? extends PacketHeader> constructFromPNumAndSize;
    private int headerSize;

    /**
     * Instantiate a HeaderData that will start with the given header kind.
     * @param packetHeaderClass the kind of header to start with.
     */
    public HeaderData(final Class<? extends PacketHeader> packetHeaderClass) {
        this.lock = new ReentrantReadWriteLock();
        setHeaderTypeTo(packetHeaderClass);
    }

    /**
     * Read a packet header from the specified stream.
     * @param stream the stream to read the packet header from.
     * @return the packet header in the stream.
     */
    public PacketHeader newHeaderFromStream(DataInput stream) {
        try {
            return getStream2Header().newInstance(stream);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new BadProtocolData("Header from stream issue", e);
        }
    }

    /**
     * Creates a new packet header with the given size and packet number.
     * @param size total packet size.
     * @param number packet number.
     * @return a new packet header with the given size and packet number.
     */
    public PacketHeader newHeader(final int size, final int number) {
        try {
            return getFields2Header().newInstance(size, number);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new BadProtocolData("Header from fields issue", e);
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

    /**
     * Get the size of the current header kind in bytes.
     * @return the size of the current header kind.
     */
    public int getHeaderSize() {
        this.lock.readLock().lock();
        try {
            return headerSize;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    /**
     * Check if the given packet has the current header kind.
     * @param packet the packet who will have the type of its header
     *               checked.
     * @return true iff the given packet has the current header kind.
     */
    public boolean sameType(Packet packet) {
        this.lock.readLock().lock();
        try {
            return constructFromStream.getDeclaringClass().isInstance(packet.getHeader());
        } finally {
            this.lock.readLock().unlock();
        }
    }
}
