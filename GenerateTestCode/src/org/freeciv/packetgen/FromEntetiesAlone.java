/*
 * Copyright (c) 2011. Sveinung Kvilhaugsvik
 * Portions are data from Freeciv's common/packets.def. Copyright
 * of those (if copyrightable) belong to their respective copyright
 * holders.
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
import com.kvilhaugsvik.javaGenerator.expression.ArrayLiteral;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AString;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AValue;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AnInt;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import org.freeciv.packet.Header_2_1;
import com.kvilhaugsvik.dependency.Dependency;
import com.kvilhaugsvik.dependency.Requirement;
import com.kvilhaugsvik.dependency.TotalOrderNoCircles;
import org.freeciv.packetgen.enteties.*;
import org.freeciv.packetgen.enteties.Enum;
import org.freeciv.packetgen.enteties.supporting.*;
import com.kvilhaugsvik.javaGenerator.*;
import org.freeciv.utility.Util;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.util.*;

import static org.freeciv.packetgen.enteties.Enum.EnumElementKnowsNumber.newEnumValue;
import static org.freeciv.packetgen.enteties.Enum.EnumElementKnowsNumber.newInvalidEnum;

public class FromEntetiesAlone {
    public static final Constant<AString> FC_DEFAULT_DATA_ENCODING =
            Constant.isString("FC_DEFAULT_DATA_ENCODING", BuiltIn.literal("UTF-8"));
    public static final Constant<AnInt> STRING_ENDER =
            Constant.isInt("STRING_ENDER", IntExpression.integer("0"));
    public static final Constant<AnInt> DIFF_ARRAY_ENDER =
            Constant.isInt("DIFF_ARRAY_ENDER", IntExpression.integer("255"));

    public static final Constant<? extends AValue> PACKET_HEAD =
            Constant.isClass(Util.HEADER_NAME, TargetClass.from(Header_2_1.class).callV("class"));
    public static final Constant<? extends AValue> ENABLE_DELTA =
            Constant.isBool("enableDelta", BuiltIn.literal(true));
    public static final Constant<? extends AValue> ENABLE_DELTA_BOOL_FOLD =
            Constant.isBool("enableDeltaBoolFolding", BuiltIn.literal(true));
    public static final Constant<? extends AValue> RULES = Constant.isOther(
            TargetArray.from(TargetClass.from(org.freeciv.connection.ReflexRule.class), 1),
            Util.RULES_NAME,
            new ArrayLiteral(new Typed[0]));

    public static final Constant<AString> NETWORK_CAPSTRING_MANDATORY =
            Constant.isString("NETWORK_CAPSTRING_MANDATORY", BuiltIn.literal("TestData"));
    public static final Constant<AString> NETWORK_CAPSTRING_OPTIONAL =
            Constant.isString("NETWORK_CAPSTRING_OPTIONAL", BuiltIn.literal("cap1 cap2 isAdded isRemoved updated"));
    public static final Constant<AString> VERSION_LABEL =
            Constant.isString("VERSION_LABEL", BuiltIn.literal(""));
    public static final Constant<AString> MAJOR_VERSION =
            Constant.isString("MAJOR_VERSION", BuiltIn.literal("0"));
    public static final Constant<AString> MINOR_VERSION =
            Constant.isString("MINOR_VERSION", BuiltIn.literal("0"));
    public static final Constant<AString> PATCH_VERSION =
            Constant.isString("PATCH_VERSION", BuiltIn.literal("0"));

    public static final Constant<AnInt> COMPRESSION_BORDER =
            Constant.isInt("COMPRESSION_BORDER", IntExpression.integer("16000"));
    public static final Constant<AnInt> JUMBO_SIZE =
            Constant.isInt("JUMBO_SIZE", IntExpression.integer("0xffff"));

    public static void main(String[] args) throws IOException, UndefinedException {
        (new FromEntetiesAlone()).generate(args);
    }

    public void generate(String[] args) throws IOException, UndefinedException {
        final String targetFolder;
        if (args.length < 1)
            targetFolder = GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER;
        else
            targetFolder = args[0];

        prepareFolder(targetFolder);

        writeEnumSimple(targetFolder);
        writeEnumDefaultInvalid(targetFolder);
        Enum enumNamedCount = writeEnumNamedCount(targetFolder);
        writeEnumBitwise(targetFolder);
        writeEnumWithSettableName(targetFolder);

        writeStructThatHasAnArrayField(targetFolder);

        writeGenBVTestPeers(targetFolder);
        writeBitStringTestPeers(targetFolder);
        writeTypedBitVectorType(targetFolder, enumNamedCount);

        writeConstantClass(targetFolder);

        Parts items = new Parts();
        FieldType uint8 = writeFieldTypeUINT8(targetFolder, items);
        FieldType uint32 = writeFieldTypeUINT32(targetFolder, items);
        FieldType uint32s = writeTerminatedArrayFieldArray(targetFolder, uint32);
        FieldType uint32s2D = writeTerminatedArrayFieldArray2D(targetFolder, uint32s);
        FieldType string = writeFieldTypeString(targetFolder, items);
        FieldType bool = writeFieldTypeBool(targetFolder, items);
        FieldType connection = writeFieldTypeConnection(targetFolder, items);

        writeDeltaVectorTestPeerPacket(targetFolder, uint8, uint32, string);
        writeTerminatedArrayFieldArrayDiffElement(targetFolder, uint8, uint32);

        remaining(targetFolder, uint8, uint32, uint32s, uint32s2D, string, bool, connection);
    }

    public void remaining(String targetFolder,
                          FieldType uint8,
                          FieldType uint32,
                          FieldType uint32s,
                          FieldType uint32s2d,
                          FieldType string,
                          FieldType bool,
                          FieldType connection) throws IOException, UndefinedException {
        FieldType uint8s = TerminatedArray.fieldArray("n", "a", uint8, null, null, false).createFieldType("UINT8S");
        writeJavaFile(uint8s, targetFolder);

        FieldType uint8s2D = TerminatedArray.fieldArray("n", "a", uint8s, null, null, false).createFieldType("UINT8S2D");
        writeJavaFile(uint8s2D, targetFolder);

        FieldType strings = TerminatedArray.fieldArray("n", "a", string, null, null, false).createFieldType("STRINGS");
        writeJavaFile(strings, targetFolder);

        writePacket(new Packet("SERVER_JOIN_REQ",
                4,
                Collections.<Annotate>emptyList(), false, false, null,
                Arrays.asList(
                        new Field("username", string, "SERVER_JOIN_REQ", Collections.<WeakFlag>emptyList(),
                                new WeakField.ArrayDeclaration(IntExpression.integer("1000"), null)),
                        new Field("capability", string, "SERVER_JOIN_REQ", Collections.<WeakFlag>emptyList(),
                                new WeakField.ArrayDeclaration(IntExpression.integer("1000"), null)),
                        new Field("version_label", string, "SERVER_JOIN_REQ", Collections.<WeakFlag>emptyList(),
                                new WeakField.ArrayDeclaration(IntExpression.integer("1000"), null)),
                        new Field("major_version", uint32, "SERVER_JOIN_REQ", Collections.<WeakFlag>emptyList()),
                        new Field("minor_version", uint32, "SERVER_JOIN_REQ", Collections.<WeakFlag>emptyList()),
                        new Field("patch_version", uint32, "SERVER_JOIN_REQ", Collections.<WeakFlag>emptyList())),
                new TreeSet<String>()),
                targetFolder);
        writePacket(new Packet("SERVER_JOIN_REPLY",
                5,
                Collections.<Annotate>emptyList(), false, false, null,
                Arrays.asList(
                        new Field("you_can_join", bool, "you_can_join", Collections.<WeakFlag>emptyList()),
                        new Field("message", string, "you_can_join", Collections.<WeakFlag>emptyList(),
                                new WeakField.ArrayDeclaration(IntExpression.integer("1000"), null)),
                        new Field("capability", string, "you_can_join", Collections.<WeakFlag>emptyList(),
                                new WeakField.ArrayDeclaration(IntExpression.integer("1000"), null)),
                        new Field("challenge_file", string, "you_can_join", Collections.<WeakFlag>emptyList(),
                                new WeakField.ArrayDeclaration(IntExpression.integer("1000"), null)),
                        new Field("conn_id", connection, "you_can_join", Collections.<WeakFlag>emptyList())),
                new TreeSet<String>()),
                targetFolder);
        writePacket(new Packet("CONN_PING", 88,
                Collections.<Annotate>emptyList(), false, false, null,
                Collections.<Field>emptyList(), new TreeSet<String>()), targetFolder);
        writePacket(new Packet("CONN_PONG", 89,
                Collections.<Annotate>emptyList(), false, false, null,
                Collections.<Field>emptyList(), new TreeSet<String>()), targetFolder);
        writePacket(new Packet("TestArray",
                926,
                Collections.<Annotate>emptyList(), false, false, null,
                Arrays.asList(
                        new Field("theArray", uint32s, "TestArray", Collections.<WeakFlag>emptyList(),
                                new WeakField.ArrayDeclaration(IntExpression.integer("2"), null))),
                new TreeSet<String>()),
                targetFolder);
        writePacket(new Packet("TestArrayTransfer",
                927,
                Collections.<Annotate>emptyList(), false, false, null,
                Arrays.asList(
                        new Field("toTransfer", uint8, "TestArrayTransfer", Collections.<WeakFlag>emptyList()),
                        new Field("theArray", uint32s, "TestArrayTransfer", Collections.<WeakFlag>emptyList(),
                                new WeakField.ArrayDeclaration(IntExpression.integer("4"), "toTransfer"))),
                new TreeSet<String>()),
                targetFolder);
        writePacket(new Packet("TestArrayDouble",
                928,
                Collections.<Annotate>emptyList(), false, false, null,
                Arrays.asList(
                        new Field("theArray", uint32s2d, "TestArrayDouble", Collections.<WeakFlag>emptyList(),
                                new WeakField.ArrayDeclaration(IntExpression.integer("2"), null),
                                new WeakField.ArrayDeclaration(IntExpression.integer("3"), null))),
                new TreeSet<String>()),
                targetFolder);
        writePacket(new Packet("TestArrayDoubleTransfer",
                929,
                Collections.<Annotate>emptyList(), false, false, null,
                Arrays.asList(
                        new Field("toTransfer", uint8, "TestArrayDoubleTransfer", Collections.<WeakFlag>emptyList()),
                        new Field("toTransfer2", uint8, "TestArrayDoubleTransfer", Collections.<WeakFlag>emptyList()),
                        new Field("theArray", uint32s2d, "TestArrayDoubleTransfer", Collections.<WeakFlag>emptyList(),
                                new WeakField.ArrayDeclaration(IntExpression.integer("4"), "toTransfer"),
                                new WeakField.ArrayDeclaration(IntExpression.integer("5"), "toTransfer2"))),
                new TreeSet<String>()),
                targetFolder);
        writePacket(new Packet("StringArray",
                930,
                Collections.<Annotate>emptyList(), false, false, null,
                Arrays.asList(
                        new Field("notAnArray", string, "StringArray", Collections.<WeakFlag>emptyList(),
                                new WeakField.ArrayDeclaration(IntExpression.integer("15"), null)),
                        new Field("theArray", strings, "StringArray", Collections.<WeakFlag>emptyList(),
                                new WeakField.ArrayDeclaration(IntExpression.integer("3"), null),
                                new WeakField.ArrayDeclaration(IntExpression.integer("10"), null))),
                new TreeSet<String>()),
                targetFolder);
    }

    @Test
    public void generateRemaining() throws IOException, UndefinedException {
        Parts items = new Parts();
        FieldType uint8 = createFieldTypeUINT8(items);
        FieldType uint32 = createFieldTypeUINT32(items);
        FieldType uint32s = createUINT32_1d(uint32);
        FieldType uint32s2D = createUINT32_2D(uint32s);
        FieldType string = createFieldTypeSTRING();
        FieldType bool = createFieldTypeBool(items);
        FieldType connection = createFieldTypeConnection(items);

        remaining(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER,
                uint8, uint32, uint32s, uint32s2D, string, bool, connection);
    }

    @BeforeClass
    public static void prepareFolder() {
        prepareFolder(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER);
    }

    public static void prepareFolder(String targetFolder) {
        for (Package pack: new Package[]{
                org.freeciv.types.FCEnum.class.getPackage(),
                org.freeciv.packet.Packet.class.getPackage(),
                org.freeciv.packet.fieldtype.FieldType.class.getPackage()
        }) {
            (new File(targetFolder + "/" + "generated" + "/" + pack.getName().replace('.', '/'))).mkdirs();
        }
    }

    @Test
    public void writeFieldTypeUINT8() throws IOException, UndefinedException {
        writeFieldTypeUINT8(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER, new Parts());
    }

    private FieldType writeFieldTypeUINT8(String targetFolder, Parts items) throws UndefinedException, IOException {
        FieldType uint8 = createFieldTypeUINT8(items);
        writeJavaFile(uint8, targetFolder);
        return uint8;
    }

    private FieldType createFieldTypeUINT8(Parts items) throws UndefinedException {
        return getPrimitiveFieldType(items, "uint8", "int", "UINT8");
    }

    @Test
    public void writeFieldTypeUINT32() throws IOException, UndefinedException {
        writeFieldTypeUINT32(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER, new Parts());
    }

    private FieldType writeFieldTypeUINT32(String targetFolder, Parts items) throws UndefinedException, IOException {
        FieldType uint32 = createFieldTypeUINT32(items);
        writeJavaFile(uint32, targetFolder);
        return uint32;
    }

    private FieldType createFieldTypeUINT32(Parts items) throws UndefinedException {
        return getPrimitiveFieldType(items, "uint32", "int", "UINT32");
    }

    @Test
    public void writeFieldTypeString() throws IOException, UndefinedException {
        writeFieldTypeString(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER, new Parts());
    }

    private FieldType writeFieldTypeString(String targetFolder, Parts items) throws UndefinedException, IOException {
        FieldType string = createFieldTypeSTRING();
        writeJavaFile(string, targetFolder);
        return string;
    }

    private FieldType createFieldTypeSTRING() throws UndefinedException {
        return ((FieldType)Hardcoded.stringBasicFieldType
                .produce(new Requirement("string(char)", FieldType.class),
                        STRING_ENDER,
                        FC_DEFAULT_DATA_ENCODING)
        ).createFieldType("STRING");
    }

    @Test
    public void writeFieldTypeBool() throws IOException, UndefinedException {
        writeFieldTypeBool(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER, new Parts());
    }

    private FieldType writeFieldTypeBool(String targetFolder, Parts items) throws UndefinedException, IOException {
        FieldType bool = createFieldTypeBool(items);
        writeJavaFile(bool, targetFolder);
        return bool;
    }

    private FieldType createFieldTypeBool(Parts items) throws UndefinedException {
        return getPrimitiveFieldType(items, "bool8", "bool", "BOOL");
    }

    @Test
    public void writeFieldTypeConnection() throws IOException, UndefinedException {
        writeFieldTypeConnection(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER, new Parts());
    }

    private FieldType writeFieldTypeConnection(String targetFolder, Parts items) throws UndefinedException, IOException {
        FieldType connection = createFieldTypeConnection(items);
        writeJavaFile(connection, targetFolder);
        return connection;
    }

    private FieldType createFieldTypeConnection(Parts items) throws UndefinedException {
        return getPrimitiveFieldType(items, "sint16", "int", "CONNECTION");
    }

    @Test
    public void writeEnumSimple() throws IOException {
        writeEnumSimple(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER);
    }

    public static void writeEnumSimple(String targetFolder) throws IOException {
        Enum test = Enum.specEnumBitwise("test", false, false, Arrays.<Enum.EnumElementFC>asList(
                newEnumValue("one", 1),
                newEnumValue("two", 2, BuiltIn.literal("2nd")),
                newEnumValue("three", 3),
                newInvalidEnum(-3)));

                writeJavaFile(test, targetFolder);
    }

    @Test
    public void writeEnumDefaultInvalid() throws IOException {
        writeEnumDefaultInvalid(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER);
    }

    public static void writeEnumDefaultInvalid(String targetFolder) throws IOException {
        Enum testDefaultInvalid = Enum.specEnumBitwise("testDefaultInvalid", false, false, Arrays.<Enum.EnumElementFC>asList(
                newEnumValue("one", 1),
                newEnumValue("two", 2, BuiltIn.literal("2nd")),
                newEnumValue("three", 3)));

        writeJavaFile(testDefaultInvalid, targetFolder);
    }

    @Test
    public void writeEnumNamedCount() throws IOException {
        writeEnumNamedCount(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER);
    }

    public static Enum writeEnumNamedCount(String targetFolder) throws IOException {
        Enum testCount = createEnumNameCount();

        writeJavaFile(testCount, targetFolder);

        return testCount;
    }

    private static Enum createEnumNameCount() {
        return Enum.specEnumCountNamed("testCount", false, "COUNT", BuiltIn.literal("numbers listed"), Arrays.<Enum.EnumElementFC>asList(
                newEnumValue("zero", 0),
                newEnumValue("one", 1),
                newEnumValue("two", 2, BuiltIn.literal("2nd")),
                newEnumValue("three", 3)));
    }

    @Test
    public void writeEnumBitwise() throws IOException {
        writeEnumBitwise(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER);
    }

    public static void writeEnumBitwise(String targetFolder) throws IOException {
        Enum bitwise = Enum.specEnumBitwise("bitwise", false, true, Arrays.<Enum.EnumElementFC>asList(
                newEnumValue("one", 1),
                newEnumValue("two", 2),
                newEnumValue("four", 4)));

        writeJavaFile(bitwise, targetFolder);
    }

    @Test
    public void writeEnumWithSettableName() throws IOException {
        writeEnumWithSettableName(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER);
    }

    public static void writeEnumWithSettableName(String targetFolder) throws IOException {
        Enum result = Enum.specEnumBitwise("UserMod", true, false, Arrays.asList(
                Enum.EnumElementFC.newEnumValue("user1", IntExpression.integer("1")),
                Enum.EnumElementFC.newEnumValue("user2", IntExpression.integer("2")),
                Enum.EnumElementFC.newEnumValue("user3", IntExpression.integer("3")),
                Enum.EnumElementFC.newEnumValue("user4", IntExpression.integer("4"))
        ));
        writeJavaFile(result, targetFolder);
    }

    @Test
    public void writeStructThatHasAnArrayField() throws IOException {
        writeStructThatHasAnArrayField(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER);
    }

    private static void writeStructThatHasAnArrayField(String targetFolder) throws IOException {
        LinkedList<WeakVarDec> fields = new LinkedList<WeakVarDec>();
        LinkedList<DataType> types = new LinkedList<DataType>();

        fields.add(new WeakVarDec(new Requirement("int", DataType.class), "aNumber"));
        types.add(new SimpleTypeAlias("n/a", int.class, 0, BuiltIn.literal(0)));

        fields.add(new WeakVarDec(new Requirement("int", DataType.class), "theArray", new WeakVarDec.ArrayDeclaration(IntExpression.integer("5"))));
        types.add(new SimpleTypeAlias("n/a", int[].class, 1, BuiltIn.literal(0)));

        Struct result = new Struct("StructArrayField", fields, types, null);

        writeJavaFile(result, targetFolder);
    }

    @Test public void writeTerminatedArrayFieldArray() throws IOException, UndefinedException {
        writeTerminatedArrayFieldArray(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER, createFieldTypeUINT32(new Parts()));
    }

    private FieldType writeTerminatedArrayFieldArray(String targetFolder, FieldType uint32) throws IOException, UndefinedException {
        FieldType array = createUINT32_1d(uint32);
        writeJavaFile(array, targetFolder);
        return array;
    }

    private FieldType createUINT32_1d(FieldType uint32) throws UndefinedException {
        return TerminatedArray.fieldArray("x", "y", uint32, null, null, false).createFieldType("UINT32S");
    }

    @Test public void writeTerminatedArrayFieldArray2D() throws IOException, UndefinedException {
        writeTerminatedArrayFieldArray(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER, createFieldTypeUINT32(new Parts()));
    }

    private FieldType writeTerminatedArrayFieldArray2D(String targetFolder, FieldType uint32_1d) throws IOException, UndefinedException {
        FieldType array = createUINT32_2D(uint32_1d);
        writeJavaFile(array, targetFolder);
        return array;
    }

    private FieldType createUINT32_2D(FieldType uint32_1d) throws UndefinedException {
        return TerminatedArray.fieldArray("x", "y", uint32_1d, null, null, false).createFieldType("UINT32S_2D");
    }

    @Test public void writeTerminatedArrayFieldArrayDiffElement() throws IOException, UndefinedException {
        final Parts items = new Parts();
        writeTerminatedArrayFieldArrayDiffElement(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER,
                createFieldTypeUINT8(items), createFieldTypeUINT32(items));
    }

    private void writeTerminatedArrayFieldArrayDiffElement(String targetFolder, FieldType uint8, FieldType uint32) throws IOException, UndefinedException {
        Dependency.Item diffElementType = createUINT32DiffElementData(uint32);
        writeJavaFile((ClassWriter) diffElementType, targetFolder);

        Dependency.Item diffElementField = createUINT32DiffElementField(uint8, diffElementType, uint32, DIFF_ARRAY_ENDER);
        writeJavaFile((ClassWriter) diffElementField, targetFolder);

        Dependency.Item diffArray = createUINT32DiffArray(uint32, (FieldType)diffElementField, DIFF_ARRAY_ENDER);
        writeJavaFile((ClassWriter) diffArray, targetFolder);
    }

    private Dependency.Item createUINT32DiffElementData(FieldType uint32) throws UndefinedException {
        final SimpleTypeAlias integer = new SimpleTypeAlias("n/a", Integer.class, 0,
                BuiltIn.literal(0));

        return (new DiffArrayElementDataType())
                .produce(new Requirement("UINT32_diff", FieldType.class), uint32, integer);
    }

    private Dependency.Item createUINT32DiffElementField(FieldType uint8, Dependency.Item diffElementType, FieldType uint32, Constant<AnInt> diff_array_ender) throws UndefinedException {
        return (new DiffArrayElementFieldType())
                .produce(new Requirement("UINT32_DIFF", FieldType.class), uint8, uint32, diffElementType, diff_array_ender);
    }

    private Dependency.Item createUINT32DiffArray(FieldType uint32, FieldType diffElementField, Constant<AnInt> diff_array_ender) {
        return TerminatedArray.fieldArray("n", "a", uint32, diff_array_ender, diffElementField, true).createFieldType("UINT32_DIFF_ARRAY");
    }

    @Test public void writeTypedBitVectorTestPeers() throws IOException, UndefinedException {
        writeTypedBitVectorType(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER, createEnumNameCount());
    }

    private void writeTypedBitVectorType(String targetFolder, Enum enumNameCount) throws IOException, UndefinedException {
        BitVector type = new BitVector("bv_enum_typed", enumNameCount);
        writeJavaFile(type, targetFolder);
    }

    @Test public void writeBitStringTestPeers() throws IOException, UndefinedException {
        writeBitStringTestPeers(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER);
    }

    private void writeBitStringTestPeers(String targetFolder) throws IOException, UndefinedException {
        BitVector type = writeBitStringType(targetFolder);

        FieldType fieldAlias = writeBitStringFieldType(targetFolder, type);

        writeBitStringUsingPacket(targetFolder, fieldAlias);
    }

    private BitVector writeBitStringType(String targetFolder) throws IOException {
        BitVector type = new BitVector();
        writeJavaFile(type, targetFolder);
        return type;
    }

    private FieldType writeBitStringFieldType(String targetFolder, BitVector type) throws UndefinedException, IOException {
        FieldType fieldAlias =
                ((FieldType) type.produce(
                        new Requirement("bit_string" + "(" + "BIT" + ")", FieldType.class),
                        NetworkIO.simple("uint16", 2, "readChar", int.class, "writeChar"))
                ).createFieldType("BITSTRING");
        writeJavaFile(fieldAlias, targetFolder);
        return fieldAlias;
    }

    private void writeBitStringUsingPacket(String targetFolder, FieldType fieldAlias) throws UndefinedException, IOException {
        Packet packet = new Packet("TestBitString", 931,
                Collections.<Annotate>emptyList(),
                false,
                false,
                null,
                Arrays.asList(new Field("theBitStingField", fieldAlias, "TestBitString", Collections.<WeakFlag>emptyList(),
                        new WeakField.ArrayDeclaration(IntExpression.integer("9"), null))),
                new TreeSet<String>());
        writeJavaFile(packet, targetFolder);
    }

    @Test public void writeGenBVTestPeers() throws IOException, UndefinedException {
        writeGenBVTestPeers(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER);
    }

    private void writeGenBVTestPeers(String targetFolder) throws IOException, UndefinedException {
        BitVector type = writeGenBVType(targetFolder);

        FieldType fieldAlias = writeGenBVFieldType(targetFolder, type);
    }

    private BitVector writeGenBVType(String targetFolder) throws IOException {
        BitVector type = new BitVector("BV_General");
        writeJavaFile(type, targetFolder);
        return type;
    }

    private FieldType writeGenBVFieldType(String targetFolder, BitVector type) throws UndefinedException, IOException {
        FieldType fieldAlias =
                ((FieldType) type.produce(new Requirement("bit_string" + "(" + "BIT" + ")", FieldType.class)))
                        .createFieldType("BV_GENERAL");
        writeJavaFile(fieldAlias, targetFolder);
        return fieldAlias;
    }

    @Test public void writeDeltaVectorTestPeer() throws IOException, UndefinedException {
        Parts items = new Parts();
        FieldType uint8 = createFieldTypeUINT8(items);
        FieldType uint32 = createFieldTypeUINT32(items);
        FieldType string = createFieldTypeSTRING();

        writeDeltaVectorTestPeerPacket(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER, uint8, uint32, string);
    }

    private void writeDeltaVectorTestPeerPacket(String targetFolder, FieldType uint8, FieldType uint32, FieldType string) throws UndefinedException, IOException {
        writeJavaFile(Hardcoded.deltaBasic, targetFolder);
        writeJavaFile(Hardcoded.deltaField, targetFolder);
        writePacket(new Packet("DeltaVectorTest",
                933,
                Collections.<Annotate>emptyList(), true, false, Hardcoded.deltaField,
                Arrays.asList(
                        new Field("id", uint8, "DeltaTest", Arrays.asList(new WeakFlag("key"))),
                        new Field("field1", string, "DeltaTest", Collections.<WeakFlag>emptyList(),
                                new WeakField.ArrayDeclaration(IntExpression.integer("15"), null)),
                        new Field("field2", uint32, "DeltaTest", Collections.<WeakFlag>emptyList())),
                new TreeSet<String>()),
                targetFolder);
    }

    @Test
    public void writeConstantClass() throws IOException {
        writeConstantClass(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER);
    }

    private void writeConstantClass(String targetFolder) throws IOException {
        ArrayList<Dependency.Item> willBeSorted = new ArrayList<Dependency.Item>();

        willBeSorted.add(DIFF_ARRAY_ENDER);
        willBeSorted.add(STRING_ENDER);
        willBeSorted.add(FC_DEFAULT_DATA_ENCODING);

        willBeSorted.add(PACKET_HEAD);
        willBeSorted.add(ENABLE_DELTA);
        willBeSorted.add(ENABLE_DELTA_BOOL_FOLD);
        willBeSorted.add(RULES);

        willBeSorted.add(NETWORK_CAPSTRING_MANDATORY);
        willBeSorted.add(NETWORK_CAPSTRING_OPTIONAL);
        willBeSorted.add(VERSION_LABEL);
        willBeSorted.add(MAJOR_VERSION);
        willBeSorted.add(MINOR_VERSION);
        willBeSorted.add(PATCH_VERSION);

        willBeSorted.add(COMPRESSION_BORDER);
        willBeSorted.add(JUMBO_SIZE);

        Set<Constant> constants = new TreeSet<Constant>(new TotalOrderNoCircles(willBeSorted));
        for (Dependency.Item stringEnd : willBeSorted)
            if (stringEnd instanceof Constant)
                constants.add((Constant)stringEnd);

        TreeMap<Integer, TargetClass> packets = new TreeMap<Integer, TargetClass>();
        packets.put(4, TargetClass.from("org.freeciv.packet", "SERVER_JOIN_REQ"));
        packets.put(5, TargetClass.from("org.freeciv.packet", "SERVER_JOIN_REPLY"));
        packets.put(88, TargetClass.from("org.freeciv.packet", "CONN_PING"));
        packets.put(89, TargetClass.from("org.freeciv.packet", "CONN_PONG"));
        packets.put(926, TargetClass.from("org.freeciv.packet", "TestArray"));
        packets.put(927, TargetClass.from("org.freeciv.packet", "TestArrayTransfer"));
        packets.put(928, TargetClass.from("org.freeciv.packet", "TestArrayDouble"));
        packets.put(929, TargetClass.from("org.freeciv.packet", "TestArrayDoubleTransfer"));
        packets.put(930, TargetClass.from("org.freeciv.packet", "StringArray"));
        packets.put(1002, TargetClass.from("org.freeciv.packet", "PACKET_ONE_FIELD"));

        writeJavaFile(PacketsStore.generateVersionData(packets, constants), targetFolder);
    }

    private static FieldType getPrimitiveFieldType(Parts items,
                String netType, String pType, String alias) throws UndefinedException {
        if (items.primitiveTypes.containsKey(netType + "(" + pType + ")"))
            return items.primitiveTypes.get(netType + "(" + pType + ")").createFieldType(alias);
        else
            return ((FieldType)items.generators.get(pType)
                    .produce(new Requirement(netType + "(" + pType + ")", FieldType.class),
                            items.network.get(netType)))
                    .createFieldType(alias);
    }

    private void writePacket(Packet packet, String targetFolder) throws IOException {
        writeJavaFile(packet, targetFolder);
    }

    private static void writeJavaFile(ClassWriter content, String targetFolder) throws IOException {
        String packagePath = content.getPackage().replace('.', '/');
        File classFile = new File(targetFolder + "/" + "generated" +
                "/" + packagePath + "/" + content.getName() + ".java");
        System.out.println(classFile.getAbsolutePath());
        classFile.createNewFile();
        FileWriter toClass = new FileWriter(classFile);
        toClass.write(content.toString());
        toClass.close();
    }

    public static class Parts {
        final HashMap<String, FieldType> primitiveTypes = new HashMap<String, FieldType>();
        final HashMap<String, Dependency.Maker> generators = new HashMap<String, Dependency.Maker>();
        final HashMap<String, NetworkIO> network = new HashMap<String, NetworkIO>();

        public Parts() {
            for (Dependency.Item mayBeNeeded : Hardcoded.values()) {
                if (mayBeNeeded instanceof FieldType)
                    primitiveTypes.put(((FieldType) mayBeNeeded).getIFulfillReq().getName(), (FieldType)mayBeNeeded);
                else if (mayBeNeeded instanceof Dependency.Maker)
                    generators.put(mayBeNeeded.getIFulfillReq().getName(),
                            (Dependency.Maker)mayBeNeeded);
                else if (mayBeNeeded instanceof NetworkIO)
                    network.put(mayBeNeeded.getIFulfillReq().getName(), (NetworkIO)mayBeNeeded);
            }
        }
    }
}
