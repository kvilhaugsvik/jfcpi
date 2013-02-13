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
import com.kvilhaugsvik.javaGenerator.TargetClass;
import org.freeciv.packet.Header_2_2;
import org.freeciv.packetgen.Hardcoded;
import org.freeciv.packetgen.UndefinedException;
import org.freeciv.packetgen.enteties.supporting.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PacketTest {
    private static final FieldTypeBasic.FieldTypeAlias floatalias = Hardcoded.getFloat("5").createFieldType("AFloat");

    @Test
    public void deltaHeader_shouldExist() throws UndefinedException {
        Packet packet = new Packet("Test", 33, TargetClass.newKnown(Header_2_2.class), "Logger.GLOBAL_LOGGER_NAME",
                Collections.<Annotate>emptyList(), true,
                new Field("aField", floatalias, "Test", Collections.<WeakFlag>emptyList()));
        assertNotNull("Should have a delta field", packet.getField("delta"));
    }

    @Test
    public void deltaHeader_shouldNotExist_noFields() throws UndefinedException {
        Packet packet = new Packet("Test", 33, TargetClass.newKnown(Header_2_2.class), "Logger.GLOBAL_LOGGER_NAME",
                Collections.<Annotate>emptyList(), true);
        assertNull("Shouldn't have delta when there are no other fields", packet.getField("delta"));
    }

    @Test
    public void deltaHeader_shouldNotExist_onlyAKeyField() throws UndefinedException {
        Packet packet = new Packet("Test", 33, TargetClass.newKnown(Header_2_2.class), "Logger.GLOBAL_LOGGER_NAME",
                Collections.<Annotate>emptyList(), true,
                new Field("aField", floatalias, "Test", Arrays.asList(new WeakFlag("key"))));
        assertNull("Shouldn't have delta when there only are key fields", packet.getField("delta"));
    }

    @Test
    public void deltaHeader_shouldNotExist_annotationNoDelta() throws UndefinedException {
        Packet packet = new Packet("Test", 33, TargetClass.newKnown(Header_2_2.class), "Logger.GLOBAL_LOGGER_NAME",
                Arrays.asList(new Annotate("NoDelta")), true,
                new Field("aField", floatalias, "Test", Collections.<WeakFlag>emptyList()));
        assertNull("Shouldn't have delta when packet has the flag no-delta", packet.getField("delta"));
    }

    @Test
    public void deltaHeader_shouldNotExist_deltaOf() throws UndefinedException {
        Packet packet = new Packet("Test", 33, TargetClass.newKnown(Header_2_2.class), "Logger.GLOBAL_LOGGER_NAME",
                Collections.<Annotate>emptyList(), false,
                new Field("aField", floatalias, "Test", Collections.<WeakFlag>emptyList()));
        assertNull("Shouldn't have delta when delta is off", packet.getField("delta"));
    }
}
