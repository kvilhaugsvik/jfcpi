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

import org.freeciv.Connect;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

public class GenerateTest {
    private static final LinkedList<String> writtenPackets = new LinkedList<String>();

    public static void main(String[] args) throws IOException {
        for (Package pack: new Package[]{
                org.freeciv.types.FCEnum.class.getPackage(),
                org.freeciv.packet.Packet.class.getPackage(),
                org.freeciv.packet.fieldtype.FieldType.class.getPackage()
        }) {
            (new File(GeneratorDefaults.GENERATEDOUT + "/" + pack.getName().replace('.', '/'))).mkdirs();
        }

        Enum test = new Enum("test", false,
                ClassWriter.EnumElement.newEnumValue("one", 1),
                ClassWriter.EnumElement.newEnumValue("two", 2, "\"2nd\""),
                ClassWriter.EnumElement.newEnumValue("three", 3),
                ClassWriter.EnumElement.newInvalidEnum(-3));
        Enum testDefaultInvalid = new Enum("testDefaultInvalid", false,
                ClassWriter.EnumElement.newEnumValue("one", 1),
                ClassWriter.EnumElement.newEnumValue("two", 2, "\"2nd\""),
                ClassWriter.EnumElement.newEnumValue("three", 3));
        Enum testCount = new Enum("testCount", "COUNT", "\"numbers listed\"",
                ClassWriter.EnumElement.newEnumValue("zero", 0),
                ClassWriter.EnumElement.newEnumValue("one", 1),
                ClassWriter.EnumElement.newEnumValue("two", 2, "\"2nd\""),
                ClassWriter.EnumElement.newEnumValue("three", 3));
        Enum bitwise = new Enum("bitwise", true,
                ClassWriter.EnumElement.newEnumValue("one", 1),
                ClassWriter.EnumElement.newEnumValue("two", 2),
                ClassWriter.EnumElement.newEnumValue("four", 4));


        writeJavaFile(test);
        writeJavaFile(testDefaultInvalid);
        writeJavaFile(bitwise);
        writeJavaFile(testCount);

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

        writeJavaFile(uint8);
        writeJavaFile(uint32);
        writeJavaFile(string);
        writeJavaFile(bool);
        writeJavaFile(connection);
        writePacket(new Packet("SERVER_JOIN_REQ",
                4,
                false,
                new Field("username", string,
                        new Field.ArrayDeclaration(IntExpression.integer("1000"), null)),
                new Field("capability", string,
                        new Field.ArrayDeclaration(IntExpression.integer("1000"), null)),
                new Field("version_label", string,
                        new Field.ArrayDeclaration(IntExpression.integer("1000"), null)),
                new Field("major_version", uint32),
                new Field("minor_version", uint32),
                new Field("patch_version", uint32)));
        writePacket(new Packet("SERVER_JOIN_REPLY",
                5,
                false,
                new Field("you_can_join", bool),
                new Field("message", string,
                        new Field.ArrayDeclaration(IntExpression.integer("1000"), null)),
                new Field("capability", string,
                        new Field.ArrayDeclaration(IntExpression.integer("1000"), null)),
                new Field("challenge_file", string,
                        new Field.ArrayDeclaration(IntExpression.integer("1000"), null)),
                new Field("conn_id", connection)));
        writePacket(new Packet("CONN_PING", 88, false));
        writePacket(new Packet("CONN_PONG", 89, false));
        writePacket(new Packet("SERVER_JOIN_REQ2ByteKind",
                4,
                true,
                new Field("username", string,
                        new Field.ArrayDeclaration(IntExpression.integer("1000"), null)),
                new Field("capability", string,
                        new Field.ArrayDeclaration(IntExpression.integer("1000"), null)),
                new Field("version_label", string,
                        new Field.ArrayDeclaration(IntExpression.integer("1000"), null)),
                new Field("major_version", uint32),
                new Field("minor_version", uint32),
                new Field("patch_version", uint32)));
        writePacket(new Packet("TestArray",
                926,
                true,
                new Field("theArray", uint32,
                        new Field.ArrayDeclaration(IntExpression.integer("2"), null))));
        writePacket(new Packet("TestArrayTransfer",
                927,
                true,
                new Field("toTransfer", uint8),
                new Field("theArray", uint32,
                        new Field.ArrayDeclaration(IntExpression.integer("4"), "toTransfer"))));
        writePacket(new Packet("TestArrayDouble",
                928,
                true,
                new Field("theArray", uint32,
                        new Field.ArrayDeclaration(IntExpression.integer("2"), null),
                        new Field.ArrayDeclaration(IntExpression.integer("3"), null))));
        writePacket(new Packet("TestArrayDoubleTransfer",
                929,
                true,
                new Field("toTransfer", uint8),
                new Field("toTransfer2", uint8),
                new Field("theArray", uint32,
                        new Field.ArrayDeclaration(IntExpression.integer("4"), "toTransfer"),
                        new Field.ArrayDeclaration(IntExpression.integer("5"), "toTransfer2"))));
        writePacket(new Packet("StringArray",
                930,
                true,
                new Field("notAnArray", string,
                        new Field.ArrayDeclaration(IntExpression.integer("15"), null)),
                new Field("theArray", string,
                        new Field.ArrayDeclaration(IntExpression.integer("3"), null),
                        new Field.ArrayDeclaration(IntExpression.integer("10"), null))));

        FileWriter packetList = new FileWriter(GeneratorDefaults.GENERATEDOUT + Connect.packetsList);
        for (String packet: writtenPackets) {
            packetList.write(packet + "\n");
        }
        packetList.close();
    }

    private static FieldTypeBasic.FieldTypeAlias getPrimitiveFieldType(HashMap<String, FieldTypeBasic> primitiveTypes,
                HashMap<String, FieldTypeBasic.Generator> generators, HashMap<String, NetworkIO> network,
                String netType, String pType, String alias) {
        if (primitiveTypes.containsKey(netType + "(" + pType + ")"))
            return primitiveTypes.get(netType + "(" + pType + ")").createFieldType(alias);
        else
            return generators.get(pType).getBasicFieldTypeOnInput(network.get(netType)).createFieldType(alias);
    }

    private static void writePacket(Packet packet) throws IOException {
        writeJavaFile(packet);
        writtenPackets.add((packet.getNumber() + "\t" + packet.getPackage() + "." + packet.getName()));
    }

    private static void writeJavaFile(ClassWriter content) throws IOException {
        String packagePath = content.getPackage().replace('.', '/');
        File classFile = new File(GeneratorDefaults.GENERATEDOUT +
                "/" + packagePath + "/" + content.getName() + ".java");
        System.out.println(classFile.getAbsolutePath());
        classFile.createNewFile();
        FileWriter toClass = new FileWriter(classFile);
        toClass.write(content.toString());
        toClass.close();
    }
}
