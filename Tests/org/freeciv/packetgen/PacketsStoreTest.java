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

import org.freeciv.Util;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.Enum;
import org.freeciv.packetgen.enteties.FieldTypeBasic;
import org.freeciv.packetgen.enteties.supporting.*;
import org.freeciv.packetgen.javaGenerator.ClassWriter;
import org.freeciv.packetgen.javaGenerator.CodeAtoms;
import org.freeciv.packetgen.javaGenerator.HasAtoms;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class PacketsStoreTest {
    private static PacketsStore defaultStorage() {
        return new PacketsStore(2);
    }

    private static void assertLooksForButNoCodeYet(PacketsStore storage, Requirement looksFor, String noCodeNamed) {
        assertTrue(storage.getUnsolvedRequirements().contains(looksFor));
        for (ClassWriter code : storage.getJavaCode())
            assertFalse("Should have been skipped since requirement missing", code.getName().equals(noCodeNamed));
    }

    @Test public void registerType() throws UndefinedException {
        PacketsStore storage = defaultStorage();
        storage.registerTypeAlias("ALIAS", "uint32", "int");

        assertTrue(storage.doesFieldTypeAliasResolve("ALIAS"));
    }

    @Test public void registerTypePreCondKnownLater() throws UndefinedException {
        PacketsStore storage = defaultStorage();
        storage.registerTypeAlias("ALIAS", "sint16", "fbbf");
        storage.addDependency(new SimpleTypeAlias("fbbf", "BitString", Collections.<Requirement>emptySet()));

        assertTrue(storage.doesFieldTypeAliasResolve("ALIAS"));
    }

    @Test public void registerTypeAlias() throws UndefinedException {
        PacketsStore storage = defaultStorage();
        storage.registerTypeAlias("ALIASED", "uint32", "int");
        storage.registerTypeAlias("ALIAS", "ALIASED");

        assertTrue(storage.doesFieldTypeAliasResolve("ALIAS"));
    }

    @Test public void registerTypeAliasToTypeRegisteredLater() throws UndefinedException {
        PacketsStore storage = defaultStorage();
        storage.registerTypeAlias("ALIAS", "ALIASED");
        storage.registerTypeAlias("ALIASED", "uint32", "int");

        assertTrue(storage.doesFieldTypeAliasResolve("ALIAS"));
    }

    private static void registerPacketToPullInnFieldtype(PacketsStore storage, String fieldTypeName, int time)
            throws PacketCollisionException, UndefinedException {
        LinkedList<WeakField> fields = new LinkedList<WeakField>();
        fields.add(new WeakField("DragInnDep", fieldTypeName, Collections.<WeakFlag>emptyList()));
        storage.registerPacket("DragInnDep" + time, 42 + time, Collections.<WeakFlag>emptyList(), fields);
    }

    @Test public void registerTypeRequiredNotExisting() throws UndefinedException, PacketCollisionException {
        PacketsStore storage = defaultStorage();
        storage.registerTypeAlias("ACTIVITY", "uint8", "enum unit_activity");

        registerPacketToPullInnFieldtype(storage, "ACTIVITY", 0);

        assertLooksForButNoCodeYet(storage, new Requirement("enum unit_activity",
                DataType.class), "ACTIVITY");
    }

    @Test public void registerTypeNotExisting() throws UndefinedException, PacketCollisionException {
        PacketsStore storage = defaultStorage();
        storage.registerTypeAlias("THISSHOULDNOTEXIST", "UINT32");

        registerPacketToPullInnFieldtype(storage, "THISSHOULDNOTEXIST", 0);

        assertLooksForButNoCodeYet(storage, new Requirement("UINT32", FieldTypeBasic.FieldTypeAlias.class),
                "THISSHOULDNOTEXIST");
    }

    @Test public void registerTypeBasicTypeNotExisting() throws UndefinedException, PacketCollisionException {
        PacketsStore storage = defaultStorage();
        storage.registerTypeAlias("THISSHOULDNOTEXIST", "notexisting128(void)");

        registerPacketToPullInnFieldtype(storage, "THISSHOULDNOTEXIST", 0);

        assertLooksForButNoCodeYet(storage, new Requirement("notexisting128(void)", FieldTypeBasic.FieldTypeAlias.class),
                "THISSHOULDNOTEXIST");
    }

    @Test public void registerTypeRequired() throws UndefinedException, PacketCollisionException {
        PacketsStore storage = defaultStorage();
        storage.addDependency(Enum.fromArray("unit_activity", false));
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
        storage.registerPacket("PACKET_HELLO", 25, Collections.<WeakFlag>emptyList(), new LinkedList<WeakField>());

        assertTrue(storage.hasPacket(25));
        assertTrue(storage.hasPacket("PACKET_HELLO"));
        assertEquals("PACKET_HELLO", getSourceOf(storage, "org.freeciv.packet.PACKET_HELLO").getName());
    }

    @Test(expected = PacketCollisionException.class)
    public void registerTwoPacketsWithTheSameNumber() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = defaultStorage();
        storage.registerPacket("PACKET_HELLO", 25, Collections.<WeakFlag>emptyList(), new LinkedList<WeakField>());
        storage.registerPacket("PACKET_HI", 25, Collections.<WeakFlag>emptyList(), new LinkedList<WeakField>());
    }

    @Test(expected = PacketCollisionException.class)
    public void registerTwoPacketsWithTheSameName() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = defaultStorage();
        storage.registerPacket("PACKET_HELLO", 25, Collections.<WeakFlag>emptyList(), new LinkedList<WeakField>());
        storage.registerPacket("PACKET_HELLO", 50, Collections.<WeakFlag>emptyList(), new LinkedList<WeakField>());
    }

    @Test public void registerPacketWithFields() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = defaultStorage();
        storage.registerTypeAlias("STRING", "string", "char");
        WeakField field1 = new WeakField("myNameIs", "STRING", Collections.<WeakFlag>emptyList(),
                new WeakField.ArrayDeclaration(IntExpression.integer("50"), null));
        LinkedList<WeakField> fields = new LinkedList<WeakField>();
        fields.add(field1);
        storage.registerPacket("PACKET_HELLO", 25, Collections.<WeakFlag>emptyList(), fields);

        assertTrue(storage.doesFieldTypeAliasResolve("STRING"));
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
        WeakField field1 = new WeakField("myNameIs", "STRING", Collections.<WeakFlag>emptyList(),
                new WeakField.ArrayDeclaration(IntExpression.integer("50"), null));
        LinkedList<WeakField> fields = new LinkedList<WeakField>();
        fields.add(field1);

        storage.registerTypeAlias("STRING", "string", "char");
        storage.registerPacket("PACKET_HELLO", 25, Collections.<WeakFlag>emptyList(), fields);

        assertTrue(storage.hasPacket("PACKET_HELLO"));
        assertEquals("myNameIs", storage.getPacket("PACKET_HELLO").getFields().get(0).getFieldName());
        assertEquals("STRING", storage.getPacket("PACKET_HELLO").getFields().get(0).getType());
    }

    @Test public void registerPacketWithoutFieldsHasNoFields() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = defaultStorage();
        storage.registerPacket("PACKET_HELLO", 25, Collections.<WeakFlag>emptyList(), new LinkedList<WeakField>());

        assertTrue(storage.hasPacket("PACKET_HELLO"));
        assertTrue(storage.getPacket("PACKET_HELLO").getFields().isEmpty());
    }

    @Test public void registerPacketWithUndefinedFields() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = defaultStorage();
        WeakField field1 = new WeakField("myNameIs", "STRING", Collections.<WeakFlag>emptyList());
        LinkedList<WeakField> fields = new LinkedList<WeakField>();
        fields.add(field1);

        storage.registerPacket("PACKET_HELLO", 25, Collections.<WeakFlag>emptyList(), fields);

        assertFalse(storage.hasPacket(25));
        assertFalse(storage.hasPacket("PACKET_HELLO"));
        assertLooksForButNoCodeYet(storage, new Requirement("STRING", FieldTypeBasic.FieldTypeAlias.class), "STRING");
    }

    private static ClassWriter getVersionData(PacketsStore storage) {
        return getSourceOf(storage, Util.VERSION_DATA_CLASS);
    }

    private static ClassWriter getSourceOf(PacketsStore storage, String toGet) {
        for (ClassWriter item : storage.getJavaCode()) {
            if (toGet.equals(item.getPackage() + "." + item.getName())) return item;
        }
        return null;
    }

    @Test public void versionDataIsAddedToCode() {
        assertNotNull("Version data should be added as generated code", getVersionData(defaultStorage()));
    }

    @Test public void versionDataHasNetworkHeaderPacketNumberBytes() {
        assertTrue("Could not find the number of bytes of the packet header the packet number should take",
                getVersionData(defaultStorage()).hasConstant("networkHeaderPacketNumberBytes"));
    }

    @Test public void versionDataUnderstandsPackets() {
        assertTrue("Could not find the number of bytes of the packet header the packet number should take",
                   getVersionData(defaultStorage()).hasConstant("understandsPackets"));
    }

    @Test public void noPacketsAreListedWhenNoPacketsAreRegistered() {
        CodeAtoms packList = new CodeAtoms(getVersionData(defaultStorage()).getField("understandsPackets").getValue());

        assertEquals(HasAtoms.ALS, packList.get(0).getAtom());
        assertEquals(HasAtoms.ALE, packList.get(1).getAtom());
    }

    @Test public void packetIsListed() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = defaultStorage();
        storage.registerPacket("PACKET_HELLO", 0, Collections.<WeakFlag>emptyList(), new LinkedList<WeakField>());

        assertEquals("{\"org.freeciv.packet.PACKET_HELLO\"}",
                Util.joinStringArray(ClassWriter.DEFAULT_STYLE_INDENT.asFormattedLines(
                        new CodeAtoms(getVersionData(storage).getField("understandsPackets").getValue())).toArray(),
                        "\n", "", ""));
    }
}
