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
import org.freeciv.packetgen.javaGenerator.ClassWriter;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import static org.freeciv.packetgen.enteties.Enum.EnumElementKnowsNumber.newEnumValue;
import static org.freeciv.packetgen.enteties.Enum.EnumElementKnowsNumber.newInvalidEnum;

public class GenerateTest {
    private static final LinkedList<String> writtenPackets = new LinkedList<String>();

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

        remaining(targetFolder);
    }

    public void remaining(String targetFolder) throws IOException, UndefinedException {
        HashMap<String, FieldTypeBasic> primitiveTypes = new HashMap<String, FieldTypeBasic>();
        HashMap<String, FieldTypeBasic.Generator> generators = new HashMap<String, FieldTypeBasic.Generator>();
        HashMap<String, NetworkIO> network = new HashMap<String, NetworkIO>();

        for (IDependency mayBeNeeded : Hardcoded.values()) {
            if (mayBeNeeded instanceof FieldTypeBasic)
                primitiveTypes.put(((FieldTypeBasic) mayBeNeeded).getFieldTypeBasic(), (FieldTypeBasic)mayBeNeeded);
            else if (mayBeNeeded instanceof FieldTypeBasic.Generator)
                generators.put(mayBeNeeded.getIFulfillReq().getName(),
                        (FieldTypeBasic.Generator)mayBeNeeded);
            else if (mayBeNeeded instanceof NetworkIO)
                network.put(mayBeNeeded.getIFulfillReq().getName(), (NetworkIO)mayBeNeeded);
        }

        FieldTypeBasic.FieldTypeAlias uint8 =
                getPrimitiveFieldType(primitiveTypes, generators, network, "uint8", "int", "UINT8");
        FieldTypeBasic.FieldTypeAlias uint32 =
                getPrimitiveFieldType(primitiveTypes, generators, network, "uint32", "int", "UINT32");
        FieldTypeBasic.FieldTypeAlias string =
                getPrimitiveFieldType(primitiveTypes, generators, network, "string", "char", "STRING");
        FieldTypeBasic.FieldTypeAlias bool =
                getPrimitiveFieldType(primitiveTypes, generators, network, "bool8", "bool", "BOOL");
        FieldTypeBasic.FieldTypeAlias connection =
                getPrimitiveFieldType(primitiveTypes, generators, network, "sint16", "int", "CONNECTION");

        writeJavaFile(uint8, targetFolder);
        writeJavaFile(uint32, targetFolder);
        writeJavaFile(string, targetFolder);
        writeJavaFile(bool, targetFolder);
        writeJavaFile(connection, targetFolder);
        writePacket(new Packet("SERVER_JOIN_REQ",
                4,
                Header_2_1.class.getSimpleName(),
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
                Header_2_1.class.getSimpleName(),
                new Field("you_can_join", bool, "you_can_join", Collections.<WeakFlag>emptyList()),
                new Field("message", string, "you_can_join", Collections.<WeakFlag>emptyList(),
                          new WeakField.ArrayDeclaration(IntExpression.integer("1000"), null)),
                new Field("capability", string, "you_can_join", Collections.<WeakFlag>emptyList(),
                          new WeakField.ArrayDeclaration(IntExpression.integer("1000"), null)),
                new Field("challenge_file", string, "you_can_join", Collections.<WeakFlag>emptyList(),
                          new WeakField.ArrayDeclaration(IntExpression.integer("1000"), null)),
                new Field("conn_id", connection, "you_can_join", Collections.<WeakFlag>emptyList())), targetFolder);
        writePacket(new Packet("CONN_PING", 88, Header_2_1.class.getSimpleName()), targetFolder);
        writePacket(new Packet("CONN_PONG", 89, Header_2_1.class.getSimpleName()), targetFolder);
        writePacket(new Packet("SERVER_JOIN_REQ2ByteKind",
                4,
                Header_2_2.class.getSimpleName(),
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
                Header_2_2.class.getSimpleName(),
                new Field("theArray", uint32, "TestArray", Collections.<WeakFlag>emptyList(),
                          new WeakField.ArrayDeclaration(IntExpression.integer("2"), null))), targetFolder);
        writePacket(new Packet("TestArrayTransfer",
                927,
                Header_2_2.class.getSimpleName(),
                new Field("toTransfer", uint8, "TestArrayTransfer", Collections.<WeakFlag>emptyList()),
                new Field("theArray", uint32, "TestArrayTransfer", Collections.<WeakFlag>emptyList(),
                          new WeakField.ArrayDeclaration(IntExpression.integer("4"), "toTransfer"))), targetFolder);
        writePacket(new Packet("TestArrayDouble",
                928,
                Header_2_2.class.getSimpleName(),
                new Field("theArray", uint32, "TestArrayDouble", Collections.<WeakFlag>emptyList(),
                          new WeakField.ArrayDeclaration(IntExpression.integer("2"), null),
                        new WeakField.ArrayDeclaration(IntExpression.integer("3"), null))), targetFolder);
        writePacket(new Packet("TestArrayDoubleTransfer",
                929,
                Header_2_2.class.getSimpleName(),
                new Field("toTransfer", uint8, "TestArrayDoubleTransfer", Collections.<WeakFlag>emptyList()),
                new Field("toTransfer2", uint8, "TestArrayDoubleTransfer", Collections.<WeakFlag>emptyList()),
                new Field("theArray", uint32, "TestArrayDoubleTransfer", Collections.<WeakFlag>emptyList(),
                          new WeakField.ArrayDeclaration(IntExpression.integer("4"), "toTransfer"),
                        new WeakField.ArrayDeclaration(IntExpression.integer("5"), "toTransfer2"))), targetFolder);
        writePacket(new Packet("StringArray",
                930,
                Header_2_2.class.getSimpleName(),
                new Field("notAnArray", string, "StringArray", Collections.<WeakFlag>emptyList(),
                          new WeakField.ArrayDeclaration(IntExpression.integer("15"), null)),
                new Field("theArray", string, "StringArray", Collections.<WeakFlag>emptyList(),
                          new WeakField.ArrayDeclaration(IntExpression.integer("3"), null),
                        new WeakField.ArrayDeclaration(IntExpression.integer("10"), null))), targetFolder);
    }

    @Test
    public void generateRemaining() throws IOException, UndefinedException {
        remaining(GeneratorDefaults.GENERATED_TEST_SOURCE_FOLDER);
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

    private static FieldTypeBasic.FieldTypeAlias getPrimitiveFieldType(HashMap<String, FieldTypeBasic> primitiveTypes,
                HashMap<String, FieldTypeBasic.Generator> generators, HashMap<String, NetworkIO> network,
                String netType, String pType, String alias) {
        if (primitiveTypes.containsKey(netType + "(" + pType + ")"))
            return primitiveTypes.get(netType + "(" + pType + ")").createFieldType(alias);
        else
            return generators.get(pType).getBasicFieldTypeOnInput(network.get(netType)).createFieldType(alias);
    }

    private static void writePacket(Packet packet, String targetFolder) throws IOException {
        writeJavaFile(packet, targetFolder);
        writtenPackets.add((packet.getNumber() + "\t" + packet.getPackage() + "." + packet.getName()));
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
}
