package org.freeciv.test;

import org.freeciv.packetgen.*;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class PacketsStoreTest {
    @Test public void registerType() throws UndefinedException {
        PacketsStore storage = new PacketsStore(false);
        storage.registerTypeAlias("UINT32", "uint32(int)");

        assertTrue(storage.hasTypeAlias("UINT32"));
    }

    @Test public void registerTypeAlias() throws UndefinedException {
        PacketsStore storage = new PacketsStore(false);
        storage.registerTypeAlias("UINT32", "uint32(int)");
        storage.registerTypeAlias("UNSIGNEDINT32", "UINT32");

        assertTrue(storage.hasTypeAlias("UNSIGNEDINT32"));
    }

    @Test public void registerTypeNotExistingDevMode() throws UndefinedException {
        PacketsStore storage = new PacketsStore(true);
        storage.registerTypeAlias("THISSHOULDNOTEXIST", "UINT32");

        assertFalse(storage.hasTypeAlias("THISSHOULDNOTEXIST"));
    }

    @Test(expected = UndefinedException.class)
    public void registerTypeNotExisting() throws UndefinedException {
        PacketsStore storage = new PacketsStore(false);
        storage.registerTypeAlias("THISSHOULDNOTEXIST", "UINT32");
    }

    @Test public void codeIsThere() throws UndefinedException {
        PacketsStore storage = new PacketsStore(false);

        storage.registerTypeAlias("UINT32", "uint32(int)");
        storage.registerTypeAlias("UNSIGNEDINT32", "UINT32");

        HashMap results = storage.getJavaCode();

        assertTrue(results.containsKey("UINT32"));
        assertTrue(results.containsKey("UNSIGNEDINT32"));
        assertNotNull(results.get("UINT32"));
    }

    @Test public void registerPacketWithoutFields() throws UndefinedException, PacketCollisionException {
        PacketsStore storage = new PacketsStore(false);
        storage.registerPacket(new Packet("PACKET_HELLO", 25));

        assertTrue(storage.hasPacket(25));
        assertTrue(storage.hasPacket("PACKET_HELLO"));
    }

    @Test(expected = PacketCollisionException.class)
    public void registerTwoPacketsWithTheSameNumber() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = new PacketsStore(false);
        storage.registerPacket(new Packet("PACKET_HELLO", 25));
        storage.registerPacket(new Packet("PACKET_HI", 25));
    }

    @Test(expected = PacketCollisionException.class)
    public void registerTwoPacketsWithTheSameName() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = new PacketsStore(false);
        storage.registerPacket(new Packet("PACKET_HELLO", 25));
        storage.registerPacket(new Packet("PACKET_HELLO", 50));
    }

    @Test public void registerPacketWithFields() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = new PacketsStore(false);
        storage.registerTypeAlias("STRING", "string(char)");
        storage.registerPacket(new Packet("PACKET_HELLO", 25, new Field("myNameIs", "STRING", "String")));

        assertTrue(storage.hasTypeAlias("STRING"));
        assertTrue(storage.hasPacket(25));
        assertTrue(storage.hasPacket("PACKET_HELLO"));
    }

    @Test public void registerPacketWithFieldsStoresField() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = new PacketsStore(false);
        Field field = new Field("myNameIs", "STRING", "String");

        storage.registerTypeAlias("STRING", "string(char)");
        storage.registerPacket(new Packet("PACKET_HELLO", 25, field));

        assertTrue(storage.hasPacket("PACKET_HELLO"));
        assertTrue(storage.getPacket("PACKET_HELLO").getFields().contains(field));
    }

    @Test public void registerPacketWithoutFieldsHasNoFields() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = new PacketsStore(false);
        storage.registerPacket(new Packet("PACKET_HELLO", 25));

        assertTrue(storage.hasPacket("PACKET_HELLO"));
        assertTrue(storage.getPacket("PACKET_HELLO").getFields().isEmpty());
    }

    @Test public void registerPacketWithUndefinedFieldsDevMode() throws UndefinedException, PacketCollisionException {
        PacketsStore storage = new PacketsStore(true);
        storage.registerPacket(new Packet("PACKET_HELLO", 25, new Field("myNameIs", "STRING", "String")));

        assertFalse(storage.hasPacket(25));
        assertFalse(storage.hasPacket("PACKET_HELLO"));
    }

    @Test(expected = UndefinedException.class)
    public void registerPacketWithUndefinedFields() throws PacketCollisionException, UndefinedException {
        PacketsStore storage = new PacketsStore(false);
        storage.registerPacket(new Packet("PACKET_HELLO", 25, new Field("myNameIs", "STRING", "String")));
    }
}
