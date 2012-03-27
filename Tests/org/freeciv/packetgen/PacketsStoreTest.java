/*
 * Copyright (c) 2011, 2012. Sveinung Kvilhaugsvik
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

import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import static org.junit.Assert.*;

public class PacketsStoreTest {
    private static PacketsStore defaultStorage() {
        return new PacketsStore(true);
    }

    private static void assertLooksForButNoCodeYet(PacketsStore storage, Requirement looksFor, String noCodeNamed) {
        assertTrue(storage.getUnsolvedRequirements().contains(looksFor));
        for (ClassWriter code : storage.getJavaCode())
            assertFalse("Should have been skipped since requirement missing", code.getName().equals(noCodeNamed));
    }

    @Test public void registerType() throws UndefinedException {
        PacketsStore storage = defaultStorage();
        storage.registerTypeAlias("UINT32", "uint32", "int");

        assertTrue(storage.hasTypeAlias("UINT32"));
    }

    @Test public void registerTypeAlias() throws UndefinedException {
        PacketsStore storage = defaultStorage();
        storage.registerTypeAlias("UINT32", "uint32", "int");
        storage.registerTypeAlias("UNSIGNEDINT32", "UINT32");

        assertTrue(storage.hasTypeAlias("UNSIGNEDINT32"));
    }

    private static void registerPacketToPullInnFieldtype(PacketsStore storage, String fieldTypeName, int time)
            throws PacketCollisionException, UndefinedException {
        LinkedList<Field.WeakField> fields = new LinkedList<Field.WeakField>();
        fields.add(new Field.WeakField("DragInnDep", fieldTypeName));
        storage.registerPacket("DragInnDep" + time, 42 + time, fields);
    }

    @Test public void registerTypeRequiredNotExisting() throws UndefinedException, PacketCollisionException {
        PacketsStore storage = defaultStorage();
        storage.registerTypeAlias("ACTIVITY", "uint8", "enum unit_activity");

        registerPacketToPullInnFieldtype(storage, "ACTIVITY", 0);

        assertLooksForButNoCodeYet(storage, new Requirement("enum unit_activity",
                Requirement.Kind.AS_JAVA_DATATYPE), "ACTIVITY");
    }

    @Test public void registerTypeNotExisting() throws UndefinedException, PacketCollisionException {
        PacketsStore storage = defaultStorage();
        storage.registerTypeAlias("THISSHOULDNOTEXIST", "UINT32");

        registerPacketToPullInnFieldtype(storage, "THISSHOULDNOTEXIST", 0);

        assertLooksForButNoCodeYet(storage, new Requirement("UINT32", Requirement.Kind.FIELD_TYPE),
                "THISSHOULDNOTEXIST");
    }

    @Test public void registerTypeBasicTypeNotExisting() throws UndefinedException, PacketCollisionException {
        PacketsStore storage = defaultStorage();
        storage.registerTypeAlias("THISSHOULDNOTEXIST", "notexisting128(void)");

        registerPacketToPullInnFieldtype(storage, "THISSHOULDNOTEXIST", 0);

        assertLooksForButNoCodeYet(storage, new Requirement("notexisting128(void)", Requirement.Kind.FIELD_TYPE),
                "THISSHOULDNOTEXIST");
    }

    @Test public void registerTypeRequired() throws UndefinedException, PacketCollisionException {
        PacketsStore storage = defaultStorage();
        storage.addDependency(new Enum("unit_activity", false));
        storage.registerTypeAlias("ACTIVITY", "uint8", "enum unit_activity");

        registerPacketToPullInnFieldtype(storage, "ACTIVITY", 0);

        HashSet<String> allCode = new HashSet<String>();
        for (ClassWriter code : storage.getJavaCode()) {
            allCode.add(code.getName());
        }
        assertTrue("Should contain field type since used by packet", allCode.contains("ACTIVITY"));
        assertTrue("Should contain enum since used by field type used by packet", allCode.contains("unit_activity"));
    }

    @Test public void codeIsThere() throws UndefinedException, PacketCollisionException {
        PacketsStore storage = defaultStorage();

        storage.registerTypeAlias("UINT32", "uint32", "int");
        storage.registerTypeAlias("UNSIGNEDINT32", "UINT32");

        registerPacketToPullInnFieldtype(storage, "UINT32", 0);
        registerPacketToPullInnFieldtype(storage, "UNSIGNEDINT32", 1);

        Collection<ClassWriter> results = storage.getJavaCode();

        boolean hasUINT32 = false;
        boolean hasUNSIGNEDINT32 = false;
        for (ClassWriter result: results) {
            assertNotNull("Java code should not be null", result);
            if ("UINT32".equals(result.getName())) hasUINT32 = true;
            if ("UNSIGNEDINT32".equals(result.getName())) hasUNSIGNEDINT32 = true;
        }
        if (!hasUINT32) fail("UINT32 should have been registered");
        if (!hasUNSIGNEDINT32) fail("UNSIGNEDINT32 should have been registered");
    }

    @Test public void registerPacketWithoutFields() throws UndefinedException, PacketCollisionException {
        PacketsStore storage = defaultStorage();
        storage.registerPacket("PACKET_HELLO", 25, new LinkedList<Field.WeakField>());

        assertTrue(storage.hasPacket(25));
        assertTrue(storage.hasPacket("PACKET_HELLO"));
        assertTrue("PACKET_HELLO".equals(storage.getJavaCode().iterator().next().getName()));
    }

    @Test(expected = PacketCollisionException.class)
    public void registerTwoPacketsWithTheSameNumber() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = defaultStorage();
        storage.registerPacket("PACKET_HELLO", 25, new LinkedList<Field.WeakField>());
        storage.registerPacket("PACKET_HI", 25, new LinkedList<Field.WeakField>());
    }

    @Test(expected = PacketCollisionException.class)
    public void registerTwoPacketsWithTheSameName() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = defaultStorage();
        storage.registerPacket("PACKET_HELLO", 25, new LinkedList<Field.WeakField>());
        storage.registerPacket("PACKET_HELLO", 50, new LinkedList<Field.WeakField>());
    }

    @Test public void registerPacketWithFields() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = defaultStorage();
        storage.registerTypeAlias("STRING", "string", "char");
        Field.WeakField field1 = new Field.WeakField("myNameIs", "STRING",
                new Field.ArrayDeclaration(IntExpression.integer("50"), null));
        LinkedList<Field.WeakField> fields = new LinkedList<Field.WeakField>();
        fields.add(field1);
        storage.registerPacket("PACKET_HELLO", 25, fields);

        assertTrue(storage.hasTypeAlias("STRING"));
        assertTrue(storage.hasPacket(25));
        assertTrue(storage.hasPacket("PACKET_HELLO"));

        HashSet<String> allCode = new HashSet<String>();
        for (ClassWriter code : storage.getJavaCode()) {
            allCode.add(code.getName());
        }
        assertTrue("Should contain packet since all field types it uses are there", allCode.contains("PACKET_HELLO"));
        assertTrue("Should contain field type since used by packet", allCode.contains("STRING"));
    }

    @Test public void registerPacketWithFieldsStoresField() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = defaultStorage();
        Field.WeakField field1 = new Field.WeakField("myNameIs", "STRING",
                new Field.ArrayDeclaration(IntExpression.integer("50"), null));
        LinkedList<Field.WeakField> fields = new LinkedList<Field.WeakField>();
        fields.add(field1);

        storage.registerTypeAlias("STRING", "string", "char");
        storage.registerPacket("PACKET_HELLO", 25, fields);

        assertTrue(storage.hasPacket("PACKET_HELLO"));
        assertEquals("myNameIs", storage.getPacket("PACKET_HELLO").getFields().get(0).getVariableName());
        assertEquals("STRING", storage.getPacket("PACKET_HELLO").getFields().get(0).getType());
    }

    @Test public void registerPacketWithoutFieldsHasNoFields() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = defaultStorage();
        storage.registerPacket("PACKET_HELLO", 25, new LinkedList<Field.WeakField>());

        assertTrue(storage.hasPacket("PACKET_HELLO"));
        assertTrue(storage.getPacket("PACKET_HELLO").getFields().isEmpty());
    }

    @Test public void registerPacketWithUndefinedFields() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = defaultStorage();
        Field.WeakField field1 = new Field.WeakField("myNameIs", "STRING");
        LinkedList<Field.WeakField> fields = new LinkedList<Field.WeakField>();
        fields.add(field1);

        storage.registerPacket("PACKET_HELLO", 25, fields);

        assertFalse(storage.hasPacket(25));
        assertFalse(storage.hasPacket("PACKET_HELLO"));
        assertLooksForButNoCodeYet(storage, new Requirement("STRING", Requirement.Kind.FIELD_TYPE), "STRING");
    }

    @Test public void noPacketsAreListedWhenNoPacketsAreRegistered() {
        PacketsStore storage = defaultStorage();

        assertEquals("", storage.getPacketList());
    }

    @Test public void packetIsListed() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = defaultStorage();
        storage.registerPacket("PACKET_HELLO", 25, new LinkedList<Field.WeakField>());

        String[] packetList = storage.getPacketList().split("[\\t\\r\\n]");
        assertEquals("25", packetList[0]);
        assertEquals("org.freeciv.packet.PACKET_HELLO", packetList[1]);
    }

    @Test public void packetsAreListed() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = defaultStorage();
        storage.registerPacket("PACKET_HELLO", 25, new LinkedList<Field.WeakField>());
        storage.registerPacket("PACKET_HI", 26, new LinkedList<Field.WeakField>());

        String[] packetList = storage.getPacketList().split("[\\r\\n]");
        assertTrue(
                (packetList[0].matches("25\\t+org.freeciv.packet.PACKET_HELLO") &&
                        packetList[1].matches("26\\t+org.freeciv.packet.PACKET_HI")) ||
                (packetList[0].matches("26\\t+org.freeciv.packet.PACKET_HI") &&
                        packetList[1].matches("25\\t+org.freeciv.packet.PACKET_HELLO"))
        );
    }
}
