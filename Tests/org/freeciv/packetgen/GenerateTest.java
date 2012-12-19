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

import org.freeciv.packet.Header_2_1;
import org.freeciv.packet.Header_2_2;
import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.*;
import org.freeciv.packetgen.enteties.Enum;
import org.freeciv.packetgen.enteties.supporting.*;
import org.freeciv.packetgen.javaGenerator.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.util.*;

import static org.freeciv.packetgen.enteties.Enum.EnumElementKnowsNumber.newEnumValue;
import static org.freeciv.packetgen.enteties.Enum.EnumElementKnowsNumber.newInvalidEnum;

public class GenerateTest {
    public static void main(String[] args) throws IOException, UndefinedException {
        (new GenerateTest()).generate(args);
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
        writeEnumNamedCount(targetFolder);
        writeEnumBitwise(targetFolder);
        writeEnumWithSettableName(targetFolder);

        writeStructThatHasAnArrayField(targetFolder);

        writeTerminatedArrayFieldArray(targetFolder);

        writeConstantClass(targetFolder);

        Parts items = new Parts();
        FieldTypeBasic.FieldTypeAlias uint8 = writeFieldTypeUINT8(targetFolder, items);
        FieldTypeBasic.FieldTypeAlias uint32 = writeFieldTypeUINT32(targetFolder, items);
        FieldTypeBasic.FieldTypeAlias string = writeFieldTypeString(targetFolder, items);
        FieldTypeBasic.FieldTypeAlias bool = writeFieldTypeBool(targetFolder, items);
        FieldTypeBasic.FieldTypeAlias connection = writeFieldTypeConnection(targetFolder, items);

        remaining(targetFolder, uint8, uint32, string, bool, connection);
    }

    public void remaining(String targetFolder,
                          FieldTypeBasic.FieldTypeAlias uint8,
                          FieldTypeBasic.FieldTypeAlias uint32,
                          FieldTypeBasic.FieldTypeAlias string,
                          FieldTypeBasic.FieldTypeAlias bool,
                          FieldTypeBasic.FieldTypeAlias connection) throws IOException, UndefinedException {
        FieldTypeBasic.FieldTypeAlias uint8s = TerminatedArray.fieldArray("n", "a", uint8).createFieldType("UINT8S");
        writeJavaFile(uint8s, targetFolder);

        FieldTypeBasic.FieldTypeAlias uint8s2D = TerminatedArray.fieldArray("n", "a", uint8s).createFieldType("UINT8S2D");
        writeJavaFile(uint8s2D, targetFolder);

        FieldTypeBasic.FieldTypeAlias strings = TerminatedArray.fieldArray("n", "a", string).createFieldType("STRINGS");
        writeJavaFile(strings, targetFolder);

        writePacket(new Packet("SERVER_JOIN_REQ",
                4,
                new TargetClass(Header_2_1.class.getSimpleName()),
                GeneratorDefaults.LOG_TO, Collections.<Annotate>emptyList(),
                new Field("username", string, "SERVER_JOIN_REQ", Collections.<WeakFlag>emptyList(),
                          new WeakField.ArrayDeclaration(IntExpression.integer("1000"), null)),
                new Field("capability", string, "SERVER_JOIN_REQ", Collections.<WeakFlag>emptyList(),
                          new WeakField.ArrayDeclaration(IntExpression.integer("1000"), null)),
                new Field("version_label", string, "SERVER_JOIN_REQ", Collections.<WeakFlag>emptyList(),
                          new WeakField.ArrayDeclaration(IntExpression.integer("1000"), null)),
                new Field("major_version", uint32, "SERVER_JOIN_REQ", Collections.<WeakFlag>emptyList()),
                new Field("minor_version", uint32, "SERVER_JOIN_REQ", Collections.<WeakFlag>emptyList()),
                new Field("patch_version", uint32, "SERVER_JOIN_REQ", Collections.<WeakFlag>emptyList())), targetFolder);
        writePacket(new Packet("SERVER_JOIN_REPLY",
                5,
                new TargetClass(Header_2_1.class.getSimpleName()),
                GeneratorDefaults.LOG_TO, Collections.<Annotate>emptyList(),
                new Field("you_can_join", bool, "you_can_join", Collections.<WeakFlag>emptyList()),
                new Field("message", string, "you_can_join", Collections.<WeakFlag>emptyList(),
                          new WeakField.ArrayDeclaration(IntExpression.integer("1000"), null)),
                new Field("capability", string, "you_can_join", Collections.<WeakFlag>emptyList(),
                          new WeakField.ArrayDeclaration(IntExpression.integer("1000"), null)),
                new Field("challenge_file", string, "you_can_join", Collections.<WeakFlag>emptyList(),
                          new WeakField.ArrayDeclaration(IntExpression.integer("1000"), null)),
                new Field("conn_id", connection, "you_can_join", Collections.<WeakFlag>emptyList())), targetFolder);
        writePacket(new Packet("CONN_PING", 88, new TargetClass(Header_2_1.class.getSimpleName()),
                GeneratorDefaults.LOG_TO, Collections.<Annotate>emptyList()), targetFolder);
        writePacket(new Packet("CONN_PONG", 89, new TargetClass(Header_2_1.class.getSimpleName()),
                GeneratorDefaults.LOG_TO, Collections.<Annotate>emptyList()), targetFolder);
        writePacket(new Packet("SERVER_JOIN_REQ2ByteKind",
                4,
                new TargetClass(Header_2_2.class.getSimpleName()),
                GeneratorDefaults.LOG_TO, Collections.<Annotate>emptyList(),
                new Field("username", string, "SERVER_JOIN_REQ2ByteKind", Collections.<WeakFlag>emptyList(),
                          new WeakField.ArrayDeclaration(IntExpression.integer("1000"), null)),
                new Field("capability", string, "SERVER_JOIN_REQ2ByteKind", Collections.<WeakFlag>emptyList(),
                          new WeakField.ArrayDeclaration(IntExpression.integer("1000"), null)),
                new Field("version_label", string, "SERVER_JOIN_REQ2ByteKind", Collections.<WeakFlag>emptyList(),
                          new WeakField.ArrayDeclaration(IntExpression.integer("1000"), null)),
                new Field("major_version", uint32, "SERVER_JOIN_REQ2ByteKind", Collections.<WeakFlag>emptyList()),
                new Field("minor_version", uint32, "SERVER_JOIN_REQ2ByteKind", Collections.<WeakFlag>emptyList()),
                new Field("patch_version", uint32, "SERVER_JOIN_REQ2ByteKind", Collections.<WeakFlag>emptyList())), targetFolder);
        writePacket(new Packet("TestArray",
                926,
                new TargetClass(Header_2_2.class.getSimpleName()),
                GeneratorDefaults.LOG_TO, Collections.<Annotate>emptyList(),
                new Field("theArray", uint32, "TestArray", Collections.<WeakFlag>emptyList(),
                          new WeakField.ArrayDeclaration(IntExpression.integer("2"), null))), targetFolder);
        writePacket(new Packet("TestArrayTransfer",
                927,
                new TargetClass(Header_2_2.class.getSimpleName()),
                GeneratorDefaults.LOG_TO, Collections.<Annotate>emptyList(),
                new Field("toTransfer", uint8, "TestArrayTransfer", Collections.<WeakFlag>emptyList()),
                new Field("theArray", uint32, "TestArrayTransfer", Collections.<WeakFlag>emptyList(),
                          new WeakField.ArrayDeclaration(IntExpression.integer("4"), "toTransfer"))), targetFolder);
        writePacket(new Packet("TestArrayDouble",
                928,
                new TargetClass(Header_2_2.class.getSimpleName()),
                GeneratorDefaults.LOG_TO, Collections.<Annotate>emptyList(),
                new Field("theArray", uint32, "TestArrayDouble", Collections.<WeakFlag>emptyList(),
                          new WeakField.ArrayDeclaration(IntExpression.integer("2"), null),
                        new WeakField.ArrayDeclaration(IntExpression.integer("3"), null))), targetFolder);
        writePacket(new Packet("TestArrayDoubleTransfer",
                929,
                new TargetClass(Header_2_2.class.getSimpleName()),
                GeneratorDefaults.LOG_TO, Collections.<Annotate>emptyList(),
                new Field("toTransfer", uint8, "TestArrayDoubleTransfer", Collections.<WeakFlag>emptyList()),
                new Field("toTransfer2", uint8, "TestArrayDoubleTransfer", Collections.<WeakFlag>emptyList()),
                new Field("theArray", uint32, "TestArrayDoubleTransfer", Collections.<WeakFlag>emptyList(),
                          new WeakField.ArrayDeclaration(IntExpression.integer("4"), "toTransfer"),
                        new WeakField.ArrayDeclaration(IntExpression.integer("5"), "toTransfer2"))), targetFolder);
        writePacket(new Packet("StringArray",
                930,
                new TargetClass(Header_2_2.class.getSimpleName()),
                GeneratorDefaults.LOG_TO, Collections.<Annotate>emptyList(),
                new Field("notAnArray", string, "StringArray", Collections.<WeakFlag>emptyList(),
                          new WeakField.ArrayDeclaration(IntExpression.integer("15"), null)),
                new Field("theArray", string, "StringArray", Collections.<WeakFlag>emptyList(),
                          new WeakField.ArrayDeclaration(IntExpression.integer("3"), null),
                        new WeakField.ArrayDeclaration(IntExpression.integer("10"), null))), targetFolder);
    }

    @Test
    public void generateRemaining() throws IOException, UndefinedException {
        Parts items = new Parts();
        FieldTypeBasic.FieldTypeAlias uint8 = createFieldTypeUINT8(items);
        FieldTypeBasic.FieldTypeAlias uint32 = createFieldTypeUINT32(items);
        FieldTypeBasic.FieldTypeAlias string = createFieldTypeSTRING(items);
        FieldTypeBasic.FieldTypeAlias bool = createFieldTypeBool(items);
        FieldTypeBasic.FieldTypeAlias connection = createFieldTypeConnection(items);

        remaining(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER, uint8, uint32, string, bool, connection);
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
            (new File(targetFolder + "/" + pack.getName().replace('.', '/'))).mkdirs();
        }
    }

    @Test
    public void writeFieldTypeUINT8() throws IOException, UndefinedException {
        writeFieldTypeUINT8(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER, new Parts());
    }

    private FieldTypeBasic.FieldTypeAlias writeFieldTypeUINT8(String targetFolder, Parts items) throws UndefinedException, IOException {
        FieldTypeBasic.FieldTypeAlias uint8 = createFieldTypeUINT8(items);
        writeJavaFile(uint8, targetFolder);
        return uint8;
    }

    private FieldTypeBasic.FieldTypeAlias createFieldTypeUINT8(Parts items) throws UndefinedException {
        return getPrimitiveFieldType(items, "uint8", "int", "UINT8");
    }

    @Test
    public void writeFieldTypeUINT32() throws IOException, UndefinedException {
        writeFieldTypeUINT32(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER, new Parts());
    }

    private FieldTypeBasic.FieldTypeAlias writeFieldTypeUINT32(String targetFolder, Parts items) throws UndefinedException, IOException {
        FieldTypeBasic.FieldTypeAlias uint32 = createFieldTypeUINT32(items);
        writeJavaFile(uint32, targetFolder);
        return uint32;
    }

    private FieldTypeBasic.FieldTypeAlias createFieldTypeUINT32(Parts items) throws UndefinedException {
        return getPrimitiveFieldType(items, "uint32", "int", "UINT32");
    }

    @Test
    public void writeFieldTypeString() throws IOException, UndefinedException {
        writeFieldTypeString(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER, new Parts());
    }

    private FieldTypeBasic.FieldTypeAlias writeFieldTypeString(String targetFolder, Parts items) throws UndefinedException, IOException {
        FieldTypeBasic.FieldTypeAlias string = createFieldTypeSTRING(items);
        writeJavaFile(string, targetFolder);
        return string;
    }

    private FieldTypeBasic.FieldTypeAlias createFieldTypeSTRING(Parts items) throws UndefinedException {
        return getPrimitiveFieldType(items, "string", "char", "STRING");
    }

    @Test
    public void writeFieldTypeBool() throws IOException, UndefinedException {
        writeFieldTypeBool(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER, new Parts());
    }

    private FieldTypeBasic.FieldTypeAlias writeFieldTypeBool(String targetFolder, Parts items) throws UndefinedException, IOException {
        FieldTypeBasic.FieldTypeAlias bool = createFieldTypeBool(items);
        writeJavaFile(bool, targetFolder);
        return bool;
    }

    private FieldTypeBasic.FieldTypeAlias createFieldTypeBool(Parts items) throws UndefinedException {
        return getPrimitiveFieldType(items, "bool8", "bool", "BOOL");
    }

    @Test
    public void writeFieldTypeConnection() throws IOException, UndefinedException {
        writeFieldTypeConnection(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER, new Parts());
    }

    private FieldTypeBasic.FieldTypeAlias writeFieldTypeConnection(String targetFolder, Parts items) throws UndefinedException, IOException {
        FieldTypeBasic.FieldTypeAlias connection = createFieldTypeConnection(items);
        writeJavaFile(connection, targetFolder);
        return connection;
    }

    private FieldTypeBasic.FieldTypeAlias createFieldTypeConnection(Parts items) throws UndefinedException {
        return getPrimitiveFieldType(items, "sint16", "int", "CONNECTION");
    }

    @Test
    public void writeEnumSimple() throws IOException {
        writeEnumSimple(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER);
    }

    public static void writeEnumSimple(String targetFolder) throws IOException {
        Enum test = Enum.fromArray("test", false,
                newEnumValue("one", 1),
                newEnumValue("two", 2, "\"2nd\""),
                newEnumValue("three", 3),
                newInvalidEnum(-3));

                writeJavaFile(test, targetFolder);
    }

    @Test
    public void writeEnumDefaultInvalid() throws IOException {
        writeEnumDefaultInvalid(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER);
    }

    public static void writeEnumDefaultInvalid(String targetFolder) throws IOException {
        Enum testDefaultInvalid = Enum.fromArray("testDefaultInvalid", false,
                newEnumValue("one", 1),
                newEnumValue("two", 2, "\"2nd\""),
                newEnumValue("three", 3));

        writeJavaFile(testDefaultInvalid, targetFolder);
    }

    @Test
    public void writeEnumNamedCount() throws IOException {
        writeEnumNamedCount(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER);
    }

    public static void writeEnumNamedCount(String targetFolder) throws IOException {
        Enum testCount = Enum.fromArray("testCount", "COUNT", "\"numbers listed\"",
                newEnumValue("zero", 0),
                newEnumValue("one", 1),
                newEnumValue("two", 2, "\"2nd\""),
                newEnumValue("three", 3));

        writeJavaFile(testCount, targetFolder);
    }

    @Test
    public void writeEnumBitwise() throws IOException {
        writeEnumBitwise(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER);
    }

    public static void writeEnumBitwise(String targetFolder) throws IOException {
        Enum bitwise = Enum.fromArray("bitwise", true,
                newEnumValue("one", 1),
                newEnumValue("two", 2),
                newEnumValue("four", 4));

        writeJavaFile(bitwise, targetFolder);
    }

    @Test
    public void writeEnumWithSettableName() throws IOException {
        writeEnumWithSettableName(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER);
    }

    public static void writeEnumWithSettableName(String targetFolder) throws IOException {
        Enum result = new Enum("UserMod", true, false, Arrays.asList(
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
        fields.add(new WeakVarDec("int", "aNumber"));
        fields.add(new WeakVarDec("int", "theArray", new WeakVarDec.ArrayDeclaration(IntExpression.integer("5"))));
        Struct result = new Struct("StructArrayField", fields, Collections.<Requirement>emptySet());

        writeJavaFile(result, targetFolder);
    }

    @Test public void writeTerminatedArrayFieldArray() throws IOException, UndefinedException {
        writeTerminatedArrayFieldArray(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER);
    }

    private void writeTerminatedArrayFieldArray(String targetFolder) throws IOException, UndefinedException {
        writeJavaFile(TerminatedArray.fieldArray("x", "y", createFieldTypeUINT32(new Parts()))
                .createFieldType("UINT32S"), targetFolder);
    }

    @Test
    public void writeConstantClass() throws IOException {
        writeConstantClass(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER);
    }

    private void writeConstantClass(String targetFolder) throws IOException {
        Set<Constant> constants = new HashSet<Constant>();
        for (IDependency stringEnd : Hardcoded.values())
            if (stringEnd instanceof Constant && "STRING_ENDER".equals(((Constant) stringEnd).getName()))
                constants.add((Constant)stringEnd);

        TreeMap<Integer, String> packets = new TreeMap<Integer, String>();
        packets.put(926, "TestArray");
        packets.put(927, "TestArrayTransfer");
        packets.put(928, "TestArrayDouble");
        packets.put(929, "TestArrayDoubleTransfer");
        packets.put(930, "StringArray");

        writeJavaFile(PacketsStore.generateVersionData(packets, constants), targetFolder);
    }

    private static FieldTypeBasic.FieldTypeAlias getPrimitiveFieldType(Parts items,
                String netType, String pType, String alias) throws UndefinedException {
        if (items.primitiveTypes.containsKey(netType + "(" + pType + ")"))
            return items.primitiveTypes.get(netType + "(" + pType + ")").createFieldType(alias);
        else
            return ((FieldTypeBasic)items.generators.get(pType)
                    .produce(new Requirement(netType + "(" + pType + ")", FieldTypeBasic.class),
                            items.network.get(netType)))
                    .createFieldType(alias);
    }

    private void writePacket(Packet packet, String targetFolder) throws IOException {
        writeJavaFile(packet, targetFolder);
    }

    private static void writeJavaFile(ClassWriter content, String targetFolder) throws IOException {
        String packagePath = content.getPackage().replace('.', '/');
        File classFile = new File(targetFolder +
                "/" + packagePath + "/" + content.getName() + ".java");
        System.out.println(classFile.getAbsolutePath());
        classFile.createNewFile();
        FileWriter toClass = new FileWriter(classFile);
        toClass.write(content.toString());
        toClass.close();
    }

    public static class Parts {
        final HashMap<String, FieldTypeBasic> primitiveTypes = new HashMap<String, FieldTypeBasic>();
        final HashMap<String, IDependency.Maker> generators = new HashMap<String, IDependency.Maker>();
        final HashMap<String, NetworkIO> network = new HashMap<String, NetworkIO>();

        public Parts() {
            for (IDependency mayBeNeeded : Hardcoded.values()) {
                if (mayBeNeeded instanceof FieldTypeBasic)
                    primitiveTypes.put(((FieldTypeBasic) mayBeNeeded).getFieldTypeBasic(), (FieldTypeBasic)mayBeNeeded);
                else if (mayBeNeeded instanceof IDependency.Maker)
                    generators.put(mayBeNeeded.getIFulfillReq().getName(),
                            (IDependency.Maker)mayBeNeeded);
                else if (mayBeNeeded instanceof NetworkIO)
                    network.put(mayBeNeeded.getIFulfillReq().getName(), (NetworkIO)mayBeNeeded);
            }
        }
    }
}
