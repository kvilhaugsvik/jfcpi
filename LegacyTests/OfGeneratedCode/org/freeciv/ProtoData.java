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
import org.junit.Test;

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

    @Test(expected = IllegalArgumentException.class)
    public void cap_unsupportedByData() {
        ProtocolVariant map = new ProtocolData().getNewPacketMapper();

        assertFalse(map.isCapabilityEnabled("longNameToMakeSureItIsNotAdded"));
    }
}
