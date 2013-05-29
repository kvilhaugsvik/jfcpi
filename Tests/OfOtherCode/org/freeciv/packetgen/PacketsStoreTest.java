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

import com.kvilhaugsvik.dependency.UndefinedException;
import com.kvilhaugsvik.javaGenerator.DefaultStyle;
import com.kvilhaugsvik.javaGenerator.TargetClass;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import org.freeciv.packetgen.enteties.Constant;
import org.freeciv.utility.Util;
import com.kvilhaugsvik.dependency.Dependency;
import com.kvilhaugsvik.dependency.Requirement;
import org.freeciv.packetgen.enteties.Enum;
import org.freeciv.packetgen.enteties.FieldType;
import org.freeciv.packetgen.enteties.supporting.*;
import com.kvilhaugsvik.javaGenerator.ClassWriter;
import com.kvilhaugsvik.javaGenerator.representation.CodeAtoms;
import com.kvilhaugsvik.javaGenerator.representation.HasAtoms;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class PacketsStoreTest {
    private static PacketsStore defaultStorage() {
        final PacketsStore packetsStore = new PacketsStore(PacketHeaderKinds.FC_2_4_99_2011_11_02, GeneratorDefaults.LOG_TO, false, false);

        // String needs this
        packetsStore.addDependency(Constant.isString("FC_DEFAULT_DATA_ENCODING", BuiltIn.literal("UTF-8")));

        return packetsStore;
    }

    private static void assertLooksForButNoCodeYet(PacketsStore storage, Requirement looksFor, String noCodeNamed) {
        assertTrue(storage.getUnsolvedRequirements().contains(looksFor));
        for (ClassWriter code : storage.getJavaCode())
            assertFalse("Should have been skipped since requirement missing", code.getName().equals(noCodeNamed));
    }

    @Test public void registerType() throws UndefinedException {
        PacketsStore storage = defaultStorage();
        storage.registerTypeAlias("ALIAS", "uint32", "int");

        assertTrue(storage.doesFieldTypeResolve("ALIAS"));
    }

    @Test public void registerTypePreCondKnownLater() throws UndefinedException {
        PacketsStore storage = defaultStorage();
        storage.registerTypeAlias("ALIAS", "sint16", "fbbf");
        storage.addDependency(new SimpleTypeAlias("fbbf", TargetClass.from("org.freeciv.types", "BitString"), null, 0));

        assertTrue(storage.doesFieldTypeResolve("ALIAS"));
    }

    @Test public void registerTypeAlias() throws UndefinedException {
        PacketsStore storage = defaultStorage();
        storage.registerTypeAlias("ALIASED", "uint32", "int");
        storage.registerTypeAlias("ALIAS", "ALIASED");

        assertTrue(storage.doesFieldTypeResolve("ALIAS"));
    }

    @Test public void registerTypeAliasToTypeRegisteredLater() throws UndefinedException {
        PacketsStore storage = defaultStorage();
        storage.registerTypeAlias("ALIAS", "ALIASED");
        storage.registerTypeAlias("ALIASED", "uint32", "int");

        assertTrue(storage.doesFieldTypeResolve("ALIAS"));
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

        assertLooksForButNoCodeYet(storage, new Requirement("UINT32", FieldType.class),
                "THISSHOULDNOTEXIST");
    }

    @Test public void registerTypeBasicTypeNotExisting() throws UndefinedException, PacketCollisionException {
        PacketsStore storage = defaultStorage();
        storage.registerTypeAlias("THISSHOULDNOTEXIST", "notexisting128(void)");

        registerPacketToPullInnFieldtype(storage, "THISSHOULDNOTEXIST", 0);

        assertLooksForButNoCodeYet(storage, new Requirement("notexisting128(void)", FieldType.class),
                "THISSHOULDNOTEXIST");
    }

    @Test public void registerTypeRequired() throws UndefinedException, PacketCollisionException {
        PacketsStore storage = defaultStorage();
        storage.addDependency(Enum.specEnum("unit_activity", false, false, Collections.<Enum.EnumElementFC>emptyList()));
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

        assertTrue(storage.doesFieldTypeResolve("STRING"));
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
        assertEquals("myNameIs", storage.getPacket("PACKET_HELLO").getFields().get(0).getName());
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

        assertNull(storage.getPacket(25));
        assertNull(storage.getPacket("PACKET_HELLO"));
        assertLooksForButNoCodeYet(storage, new Requirement("STRING", FieldType.class), "STRING");
    }

    @Test public void registerPacketBeforeItsFieldType() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = defaultStorage();
        WeakField field1 = new WeakField("myNameIs", "STRING",
                Collections.<WeakFlag>emptyList(), new WeakField.ArrayDeclaration(IntExpression.integer("50"), null));
        storage.registerPacket("PACKET_HELLO", 25, Collections.<WeakFlag>emptyList(), Arrays.asList(field1));
        storage.registerTypeAlias("STRING", "string", "char");

        assertTrue("Packet not created", storage.hasPacket("PACKET_HELLO"));
        assertEquals("Should have one field", 1, storage.getPacket("PACKET_HELLO").getFields().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addDependencyUnknownKind() {
        PacketsStore storage = defaultStorage();
        storage.addDependency(new Dependency() {
        });
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
                getVersionData(defaultStorage()).hasConstant(Util.HEADER_NAME));
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

        assertEquals("{org.freeciv.packet.PACKET_HELLO.class}",
                Util.joinStringArray(DefaultStyle.DEFAULT_STYLE_INDENT.asFormattedLines(
                        new CodeAtoms(getVersionData(storage).getField("understandsPackets").getValue())).toArray(),
                        "\n", "", ""));
    }

    @Test
    public void generateFieldArraySimple() throws UndefinedException, PacketCollisionException {
        PacketsStore storage = defaultStorage();

        storage.registerTypeAlias("DEX", "uint8", "int");
        storage.registerTypeAlias("UNDER", "uint8", "int");

        List<WeakField> fieldList = Arrays.asList(
                new WeakField("elems", "DEX", Collections.<WeakFlag>emptyList()),
                new WeakField("field", "UNDER_1", Collections.<WeakFlag>emptyList(),
                        new WeakField.ArrayDeclaration(IntExpression.integer("5"), "elems")));

        storage.registerPacket("Array1D", 44, Collections.<WeakFlag>emptyList(), fieldList);

        HashMap<String, ClassWriter> results = getJavaCodeIndexedOnClassName(storage);

        assertTrue("Can't test as underlying not generated...", results.containsKey("UNDER"));
        assertTrue("Failed to generate simple 1 dimensional array", results.containsKey("UNDER_1"));

        assertPacketExistsAndHasField(results, "Array1D", "field", "UNDER_1");
    }

    @Test
    public void generateFieldArray5Dimensions() throws UndefinedException, PacketCollisionException {
        PacketsStore storage = defaultStorage();

        storage.registerTypeAlias("DEX", "uint8", "int");
        storage.registerTypeAlias("UNDER", "uint8", "int");

        List<WeakField> fieldList = Arrays.asList(
                new WeakField("elems", "DEX", Collections.<WeakFlag>emptyList()),
                new WeakField("field", "UNDER_5", Collections.<WeakFlag>emptyList(),
                        new WeakField.ArrayDeclaration(IntExpression.integer("5"), "elems")));

        storage.registerPacket("Array5D", 44, Collections.<WeakFlag>emptyList(), fieldList);

        HashMap<String, ClassWriter> results = getJavaCodeIndexedOnClassName(storage);

        assertTrue("Can't test as underlying not generated...", results.containsKey("UNDER"));
        assertTrue("Failed to generate array dimension 1", results.containsKey("UNDER_1"));
        assertTrue("Failed to generate array dimension 2", results.containsKey("UNDER_2"));
        assertTrue("Failed to generate array dimension 3", results.containsKey("UNDER_3"));
        assertTrue("Failed to generate array dimension 4", results.containsKey("UNDER_4"));
        assertTrue("Failed to generate 5 dimensional array", results.containsKey("UNDER_5"));

        assertPacketExistsAndHasField(results, "Array5D", "field", "UNDER_5");
    }

    @Test
    public void generateFieldArray15Dimensions() throws UndefinedException, PacketCollisionException {
        PacketsStore storage = defaultStorage();

        storage.registerTypeAlias("DEX", "uint8", "int");
        storage.registerTypeAlias("UNDER", "uint8", "int");

        List<WeakField> fieldList = Arrays.asList(
                new WeakField("elems", "DEX", Collections.<WeakFlag>emptyList()),
                new WeakField("field", "UNDER_15", Collections.<WeakFlag>emptyList(),
                        new WeakField.ArrayDeclaration(IntExpression.integer("5"), "elems")));

        storage.registerPacket("Array15D", 44, Collections.<WeakFlag>emptyList(), fieldList);

        HashMap<String, ClassWriter> results = getJavaCodeIndexedOnClassName(storage);

        assertTrue("Can't test as underlying not generated...", results.containsKey("UNDER"));
        assertTrue("Failed to generate array dimension 1", results.containsKey("UNDER_1"));
        assertTrue("Failed to generate array dimension 9", results.containsKey("UNDER_9"));
        assertTrue("Failed to generate array dimension 10", results.containsKey("UNDER_10"));
        assertTrue("Failed to generate array dimension 11", results.containsKey("UNDER_11"));
        assertTrue("Failed to generate 15 dimensional array", results.containsKey("UNDER_15"));

        assertPacketExistsAndHasField(results, "Array15D", "field", "UNDER_15");
    }

    @Test
    public void generateFieldArrayEaterUnder() throws UndefinedException, PacketCollisionException {
        PacketsStore storage = defaultStorage();

        storage.registerTypeAlias("DEX", "uint8", "int");
        storage.registerTypeAlias("UNDER", "string", "char");

        List<WeakField> fieldList = Arrays.asList(
                new WeakField("elems", "DEX", Collections.<WeakFlag>emptyList()),
                new WeakField("field", "UNDER_3", Collections.<WeakFlag>emptyList(),
                        new WeakField.ArrayDeclaration(IntExpression.integer("5"), "elems")));

        storage.registerPacket("ArrayEat", 44, Collections.<WeakFlag>emptyList(), fieldList);

        HashMap<String, ClassWriter> results = getJavaCodeIndexedOnClassName(storage);

        assertTrue("Can't test as underlying not generated...", results.containsKey("UNDER"));
        assertFalse("Dimension 1 should be eaten", results.containsKey("UNDER_1"));
        assertTrue("Failed to generate array dimension 2", results.containsKey("UNDER_2"));
        assertTrue("Failed to generate array dimension 3", results.containsKey("UNDER_3"));

        assertPacketExistsAndHasField(results, "ArrayEat", "field", "UNDER_3");
    }

    @Test
    public void generateFieldArrayButNotFor1DArrayEater() throws UndefinedException, PacketCollisionException {
        PacketsStore storage = defaultStorage();

        storage.registerTypeAlias("DEX", "uint8", "int");
        storage.registerTypeAlias("UNDER", "string", "char");

        List<WeakField> fieldList = Arrays.asList(
                new WeakField("elems", "DEX", Collections.<WeakFlag>emptyList()),
                new WeakField("field", "UNDER_1", Collections.<WeakFlag>emptyList(),
                        new WeakField.ArrayDeclaration(IntExpression.integer("5"), "elems")));

        storage.registerPacket("ArrayEat", 44, Collections.<WeakFlag>emptyList(), fieldList);

        HashMap<String, ClassWriter> results = getJavaCodeIndexedOnClassName(storage);

        assertFalse("Dimension 1 should be eaten", results.containsKey("UNDER_1"));

        assertPacketExistsAndHasField(results, "ArrayEat", "field", "UNDER");
    }

    private static void assertPacketExistsAndHasField(HashMap<String, ClassWriter> results,
                                               String packetName, String fieldName, String fieldType) {
        assertTrue("Packet not generated", results.containsKey(packetName));
        assertNotNull("Probable bug in test", results.get(packetName));
        assertTrue("Field should exist and be of the correct type",
                results.get(packetName).getField(fieldName).getType().equals(fieldType));
    }

    private static HashMap<String, ClassWriter> getJavaCodeIndexedOnClassName(PacketsStore storage) {
        HashMap<String, ClassWriter> results = new HashMap<String, ClassWriter>();
        for (ClassWriter item : storage.getJavaCode()) {
            results.put(item.getName(), item);
        }
        return results;
    }
}
