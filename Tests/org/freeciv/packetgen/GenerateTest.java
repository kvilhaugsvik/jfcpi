/*
 * Copyright (c) 2011. Sveinung Kvilhaugsvik
 * Portions are data from Freeciv's common/packets.def. Copyright
 * to those (if copyrightable) not claimed by Sveinung Kvilhaugsvik
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

import org.freeciv.packetgen.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class GenerateTest {
    private static final LinkedList<String> writtenPackets = new LinkedList<String>();

    public static void main(String[] args) throws IOException {
        (new File(GeneratorDefaults.GENERATEDOUT + "/" +
                org.freeciv.types.FCEnum.class.getPackage().getName().replace('.', '/'))).mkdirs();
        (new File(GeneratorDefaults.GENERATEDOUT + "/" +
                org.freeciv.packet.fieldtype.FieldType.class.getPackage().getName().replace('.', '/'))).mkdirs();

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

        FieldTypeBasic.FieldTypeAlias uint32 =
                Hardcoded.getBasicFieldType("uint32(int)").createFieldType("UINT32");
        FieldTypeBasic.FieldTypeAlias string =
                Hardcoded.getBasicFieldType("string(char)").createFieldType("STRING");
        FieldTypeBasic.FieldTypeAlias bool =
                Hardcoded.getBasicFieldType("bool8(bool)").createFieldType("BOOL");
        FieldTypeBasic.FieldTypeAlias connection =
                Hardcoded.getBasicFieldType("sint16(int)").createFieldType("CONNECTION");

        writeJavaFile(uint32);
        writeJavaFile(string);
        writeJavaFile(bool);
        writeJavaFile(connection);
        writePacket(new Packet("SERVER_JOIN_REQ",
                4,
                false,
                new Field("username", string),
                new Field("capability", string),
                new Field("version_label", string),
                new Field("major_version", uint32),
                new Field("minor_version", uint32),
                new Field("patch_version", uint32)));
        writePacket(new Packet("SERVER_JOIN_REPLY",
                5,
                false,
                new Field("you_can_join", bool),
                new Field("message", string),
                new Field("capability", string),
                new Field("challenge_file", string),
                new Field("conn_id", connection)));
        writePacket(new Packet("CONN_PING", 88, false));
        writePacket(new Packet("CONN_PONG", 89, false));
        writePacket(new Packet("SERVER_JOIN_REQ2ByteKind",
                4,
                true,
                new Field("username", string),
                new Field("capability", string),
                new Field("version_label", string),
                new Field("major_version", uint32),
                new Field("minor_version", uint32),
                new Field("patch_version", uint32)));

        FileWriter packetList = new FileWriter(GeneratorDefaults.GENERATEDOUT + "/" +
                org.freeciv.packet.Packet.class.getPackage().getName().replace('.', '/') + "/" + "packets.txt");
        for (String packet: writtenPackets) {
            packetList.write(packet + "\n");
        }
        packetList.close();
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
