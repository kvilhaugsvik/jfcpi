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

package org.freeciv;

import org.freeciv.connection.ProtocolData;
import org.freeciv.connection.ProtocolVariant;
import org.freeciv.connection.ProtocolVariantManually;
import org.freeciv.packet.CONN_PING;
import org.freeciv.packet.DeltaKey;
import org.freeciv.packet.PACKET_ONE_FIELD;
import org.freeciv.packet.Packet;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;

import static org.junit.Assert.*;

public class ProtoData {
    @Test
    public void hasSettableCaps() {
        ProtocolData protoData = new ProtocolData();

        final Set<String> allSettableCaps = protoData.getAllSettableCaps();

        assertTrue("Missing settable capability", allSettableCaps.contains("cap1"));
        assertTrue("Missing settable capability", allSettableCaps.contains("cap2"));
        assertTrue("Missing settable capability", allSettableCaps.contains("isAdded"));
        assertTrue("Missing settable capability", allSettableCaps.contains("isRemoved"));
        assertTrue("Missing settable capability", allSettableCaps.contains("updated"));
    }

    @Test
    public void cap_disabledAsStandard() {
        ProtocolVariant map = new ProtocolData().getNewPacketMapper();

        assertFalse(map.isCapabilityEnabled("isAdded"));
    }

    @Test
    public void cap_canEnable() {
        ProtocolVariantManually map = new ProtocolData().getNewPacketMapper();

        assertFalse(map.isCapabilityEnabled("isAdded"));
        map.enableCapability("isAdded");
        assertTrue(map.isCapabilityEnabled("isAdded"));
    }

    @Test
    public void cap_canDisable() {
        ProtocolVariantManually map = new ProtocolData().getNewPacketMapper();

        map.enableCapability("isAdded");
        assertTrue(map.isCapabilityEnabled("isAdded"));
        map.disableCapability("isAdded");
        assertFalse(map.isCapabilityEnabled("isAdded"));
    }

    /**
     * A capability could have been added to Freeciv after the protocol was
     * generated. Enabling it shouldn't be legal.
     */
    @Test(expected = IllegalArgumentException.class)
    public void cap_unsupportedByData_enable() {
        ProtocolVariantManually map = new ProtocolData().getNewPacketMapper();

        map.enableCapability("thingInANewVersionNotSupported");
    }

    /**
     * A capability could have been added to Freeciv after the protocol was
     * generated. Enabling it shouldn't be legal. Disabling on the other
     * hand should be allowed.
     */
    @Test public void cap_canDisable_unsupported() {
        ProtocolVariantManually map = new ProtocolData().getNewPacketMapper();

        map.disableCapability("thingInANewVersionNotSupported");
    }

    /**
     * A capability could have been added to Freeciv after the protocol was
     * generated. Enabling it shouldn't be legal. Querying if it is enabled
     * or not should be allwed.
     */
    @Test public void cap_canQuery_unsupported() {
        ProtocolVariantManually map = new ProtocolData().getNewPacketMapper();

        assertFalse(map.isCapabilityEnabled("thingInANewVersionNotSupported"));
    }

    /* Check that it is possible to instantiate a packet of the current
     * protocol variant from the values its fields are supposed to have.
     * This tests the case where the packet don't have a body. */
    @Test
    public void canCreatePacketFromFields_noBody() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        /* Set up */
        final ProtocolData protoData = new ProtocolData();
        final ProtocolVariantManually map = protoData.getNewPacketMapper();
        final HashMap<DeltaKey, Packet> old = new HashMap<DeltaKey, Packet>();

        /* Creation */
        final Packet packet = map.newPacketFromValues(88, protoData.getNewPacketHeaderData(), old);

        /* Check the result */
        assertTrue("Wrong packet kind", CONN_PING.class.isInstance(packet));
    }

    /* Check that it is possible to instantiate a packet of the current
     * protocol variant from the values its fields are supposed to have.
     * This tests the case where the packet has a field in its body. */
    @Test
    public void canCreatePacketFromFields_hasBody() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        /* Set up */
        final ProtocolData protoData = new ProtocolData();
        final ProtocolVariantManually map = protoData.getNewPacketMapper();
        final HashMap<DeltaKey, Packet> old = new HashMap<DeltaKey, Packet>();

        /* Creation */
        final Packet packet = map.newPacketFromValues(1002, protoData.getNewPacketHeaderData(), old, 42);

        /* Check the result */
        assertTrue("Wrong packet kind", PACKET_ONE_FIELD.class.isInstance(packet));
        assertEquals("Wrong field value", 42, ((PACKET_ONE_FIELD)packet).getOneValue().intValue());
    }
}
