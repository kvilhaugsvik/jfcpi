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

package org.freeciv.test;

import org.freeciv.packetgen.Packet;
import org.freeciv.packetgen.Field;
import org.freeciv.packetgen.Hardcoded;
import org.freeciv.packetgen.GeneratorDefaults;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class GenerateTest {
    private static final LinkedList<String> writtenPackets = new LinkedList<String>();

    public static void main(String[] args) throws IOException {
        (new File(GeneratorDefaults.GENERATEDOUT + "/org/freeciv/packet/")).mkdirs();

        writeFieldType("UINT32", "uint32(int)");
        writeFieldType("STRING", "string(char)");
        writeFieldType("BOOL", "bool8(bool)");
        writeFieldType("CONNECTION", "sint16(int)");
        writePacket(new Packet("SERVER_JOIN_REQ",
                4,
                false,
                new Field("username", "STRING", "String"),
                new Field("capability", "STRING", "String"),
                new Field("version_label", "STRING", "String"),
                new Field("major_version", "UINT32", "Long"),
                new Field("minor_version", "UINT32", "Long"),
                new Field("patch_version", "UINT32", "Long")));
        writePacket(new Packet("SERVER_JOIN_REPLY",
                5,
                false,
                new Field("you_can_join", "BOOL", "boolean"),
                new Field("message", "STRING", "String"),
                new Field("capability", "STRING", "String"),
                new Field("challenge_file", "STRING", "String"),
                new Field("conn_id", "CONNECTION", "Short")));
        writePacket(new Packet("CONN_PING", 88, false));
        writePacket(new Packet("CONN_PONG", 89, false));
        writePacket(new Packet("SERVER_JOIN_REQ2ByteKind",
                4,
                true,
                new Field("username", "STRING", "String"),
                new Field("capability", "STRING", "String"),
                new Field("version_label", "STRING", "String"),
                new Field("major_version", "UINT32", "Long"),
                new Field("minor_version", "UINT32", "Long"),
                new Field("patch_version", "UINT32", "Long")));

        FileWriter packetList = new FileWriter(GeneratorDefaults.GENERATEDOUT + "/" + "org/freeciv/packet/" + "packets.txt");
        for (String packet: writtenPackets) {
            packetList.write(packet + "\n");
        }
        packetList.close();
    }

    private static void writePacket(Packet packet) throws IOException {
        writeJavaFile(packet.getName(), packet.toString());
        writtenPackets.add((packet.getNumber() + "\t" + "org/freeciv/packet/" + packet.getName()).replace('/', '.'));
    }

    private static void writeFieldType(String fieldType, String ioType) throws IOException {
        String content = Hardcoded.getJTypeFor(ioType).toString(fieldType);
        writeJavaFile(fieldType, content);
    }

    private static void writeJavaFile(String javaclass, String content) throws IOException {
        File classFile = new File(GeneratorDefaults.GENERATEDOUT + "/org/freeciv/packet/" + javaclass + ".java");
        classFile.createNewFile();
        FileWriter toClass = new FileWriter(classFile);
        toClass.write(content);
        toClass.close();
    }
}
