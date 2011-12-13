/*
 * Copyright (c) 2011. Sveinung Kvilhaugsvik
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

package org.freeciv.test;

import org.freeciv.packetgen.*;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;

import static org.junit.Assert.*;

public class PacketsStoreTest {
    private static PacketsStore dev() {
        return new PacketsStore(true, true);
    }

    private static PacketsStore noDev() {
        return new PacketsStore(false, true);
    }

    @Test public void registerType() throws UndefinedException {
        PacketsStore storage = noDev();
        storage.registerTypeAlias("UINT32", "uint32(int)");

        assertTrue(storage.hasTypeAlias("UINT32"));
    }

    @Test public void registerTypeAlias() throws UndefinedException {
        PacketsStore storage = noDev();
        storage.registerTypeAlias("UINT32", "uint32(int)");
        storage.registerTypeAlias("UNSIGNEDINT32", "UINT32");

        assertTrue(storage.hasTypeAlias("UNSIGNEDINT32"));
    }

    @Test public void registerTypeNotExistingDevMode() throws UndefinedException {
        PacketsStore storage = dev();
        storage.registerTypeAlias("THISSHOULDNOTEXIST", "UINT32");

        assertFalse(storage.hasTypeAlias("THISSHOULDNOTEXIST"));
    }

    @Test(expected = UndefinedException.class)
    public void registerTypeNotExisting() throws UndefinedException {
        PacketsStore storage = noDev();
        storage.registerTypeAlias("THISSHOULDNOTEXIST", "UINT32");
    }

    @Test(expected = UndefinedException.class)
    public void registerTypeBasicTypeNotExisting() throws UndefinedException {
        PacketsStore storage = noDev();
        storage.registerTypeAlias("THISSHOULDNOTEXIST", "notexisting128(void)");
    }

    @Test public void codeIsThere() throws UndefinedException {
        PacketsStore storage = noDev();

        storage.registerTypeAlias("UINT32", "uint32(int)");
        storage.registerTypeAlias("UNSIGNEDINT32", "UINT32");

        HashMap results = storage.getJavaCode();

        assertTrue(results.containsKey("UINT32"));
        assertTrue(results.containsKey("UNSIGNEDINT32"));
        assertNotNull(results.get("UINT32"));
    }

    @Test public void registerPacketWithoutFields() throws UndefinedException, PacketCollisionException {
        PacketsStore storage = noDev();
        storage.registerPacket("PACKET_HELLO", 25);

        assertTrue(storage.hasPacket(25));
        assertTrue(storage.hasPacket("PACKET_HELLO"));
    }

    @Test(expected = PacketCollisionException.class)
    public void registerTwoPacketsWithTheSameNumber() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = noDev();
        storage.registerPacket("PACKET_HELLO", 25);
        storage.registerPacket("PACKET_HI", 25);
    }

    @Test(expected = PacketCollisionException.class)
    public void registerTwoPacketsWithTheSameName() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = noDev();
        storage.registerPacket("PACKET_HELLO", 25);
        storage.registerPacket("PACKET_HELLO", 50);
    }

    @Test public void registerPacketWithFields() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = noDev();
        storage.registerTypeAlias("STRING", "string(char)");
        String[] field1 = {"STRING", "myNameIs"};
        LinkedList<String[]> fields = new LinkedList<String[]>();
        fields.add(field1);
        storage.registerPacket("PACKET_HELLO", 25, fields);

        assertTrue(storage.hasTypeAlias("STRING"));
        assertTrue(storage.hasPacket(25));
        assertTrue(storage.hasPacket("PACKET_HELLO"));
    }

    @Test public void registerPacketWithFieldsStoresField() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = noDev();
        String[] field1 = {"STRING", "myNameIs"};
        LinkedList<String[]> fields = new LinkedList<String[]>();
        fields.add(field1);

        storage.registerTypeAlias("STRING", "string(char)");
        storage.registerPacket("PACKET_HELLO", 25, fields);

        assertTrue(storage.hasPacket("PACKET_HELLO"));
        assertEquals("myNameIs", storage.getPacket("PACKET_HELLO").getFields().get(0).getVariableName());
        assertEquals("STRING", storage.getPacket("PACKET_HELLO").getFields().get(0).getType());
    }

    @Test public void registerPacketWithoutFieldsHasNoFields() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = noDev();
        storage.registerPacket("PACKET_HELLO", 25);

        assertTrue(storage.hasPacket("PACKET_HELLO"));
        assertTrue(storage.getPacket("PACKET_HELLO").getFields().isEmpty());
    }

    @Test public void registerPacketWithUndefinedFieldsDevMode() throws UndefinedException, PacketCollisionException {
        PacketsStore storage = dev();
        String[] field1 = {"STRING", "myNameIs"};
        LinkedList<String[]> fields = new LinkedList<String[]>();
        fields.add(field1);

        storage.registerPacket("PACKET_HELLO", 25, fields);

        assertFalse(storage.hasPacket(25));
        assertFalse(storage.hasPacket("PACKET_HELLO"));
    }

    @Test(expected = UndefinedException.class)
    public void registerPacketWithUndefinedFields() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = noDev();
        String[] field1 = {"STRING", "myNameIs"};
        LinkedList<String[]> fields = new LinkedList<String[]>();
        fields.add(field1);

        storage.registerPacket("PACKET_HELLO", 25, fields);
    }

    @Test public void noPacketsAreListedWhenNoPacketsAreRegistered() {
        PacketsStore storage = noDev();

        assertEquals("", storage.getPacketList());
    }

    @Test public void packetIsListed() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = noDev();
        storage.registerPacket("PACKET_HELLO", 25);

        String[] packetList = storage.getPacketList().split("[\\t\\r\\n]");
        assertEquals("25", packetList[0]);
        assertEquals("org.freeciv.packet.PACKET_HELLO", packetList[1]);
    }

    @Test public void packetsAreListed() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = noDev();
        storage.registerPacket("PACKET_HELLO", 25);
        storage.registerPacket("PACKET_HI", 26);

        String[] packetList = storage.getPacketList().split("[\\r\\n]");
        assertTrue(
                (packetList[0].matches("25\\t+org.freeciv.packet.PACKET_HELLO") &&
                        packetList[1].matches("26\\t+org.freeciv.packet.PACKET_HI")) ||
                (packetList[0].matches("26\\t+org.freeciv.packet.PACKET_HI") &&
                        packetList[1].matches("25\\t+org.freeciv.packet.PACKET_HELLO"))
        );
    }
}
