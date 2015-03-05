/*
 * Copyright (c) 2013. Sveinung Kvilhaugsvik
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

package org.freeciv.packetgen.enteties;

import com.kvilhaugsvik.javaGenerator.Annotate;
import org.freeciv.packet.NoDelta;
import org.freeciv.packetgen.Hardcoded;
import com.kvilhaugsvik.dependency.UndefinedException;
import org.freeciv.packetgen.enteties.supporting.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.TreeSet;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PacketTest {
    private static final FieldType floatalias = Hardcoded.getFloat("5").createFieldType("AFloat");

    @Test
    public void deltaHeader_shouldExist() throws UndefinedException {
        Packet packet = new Packet("Test", 33,
                Collections.<Annotate>emptyList(), true, false, Hardcoded.deltaField,
                Arrays.asList(new Field("aField", floatalias, "Test", Collections.<WeakFlag>emptyList())),
                new TreeSet<String>());
        assertNotNull("Should have a delta field", packet.getField("delta"));
    }

    @Test
    public void deltaHeader_shouldNotExist_noFields() throws UndefinedException {
        Packet packet = new Packet("Test", 33,
                Collections.<Annotate>emptyList(), true, false, Hardcoded.deltaField, Collections.<Field>emptyList(),
                new TreeSet<String>());
        assertNull("Shouldn't have delta when there are no other fields", packet.getField("delta"));
    }

    @Test
    public void deltaHeader_shouldNotExist_onlyAKeyField() throws UndefinedException {
        Packet packet = new Packet("Test", 33,
                Collections.<Annotate>emptyList(), true, false, Hardcoded.deltaField,
                Arrays.asList(new Field("aField", floatalias, "Test",
                Arrays.asList(new WeakFlag("key")))),
                new TreeSet<String>());
        assertNull("Shouldn't have delta when there only are key fields", packet.getField("delta"));
    }

    @Test
    public void deltaHeader_shouldNotExist_annotationNoDelta() throws UndefinedException {
        Packet packet = new Packet("Test", 33,
                Arrays.asList(new Annotate(NoDelta.class)), true, false, Hardcoded.deltaField,
                Arrays.asList(new Field("aField", floatalias, "Test", Collections.<WeakFlag>emptyList())),
                new TreeSet<String>());
        assertNull("Shouldn't have delta when packet has the flag no-delta", packet.getField("delta"));
    }

    @Test
    public void deltaHeader_shouldNotExist_deltaOf() throws UndefinedException {
        Packet packet = new Packet("Test", 33,
                Collections.<Annotate>emptyList(), false, false, null,
                Arrays.asList(new Field("aField", floatalias, "Test", Collections.<WeakFlag>emptyList())),
                new TreeSet<String>());
        assertNull("Shouldn't have delta when delta is off", packet.getField("delta"));
    }
}
