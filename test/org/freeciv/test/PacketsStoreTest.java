package org.freeciv.test;

import org.freeciv.packetgen.PacketsStore;
import org.freeciv.packetgen.UndefinedException;
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
}
