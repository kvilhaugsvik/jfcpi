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

import org.freeciv.connection.PacketsMapping;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class ProtoData {
    @Test
    public void hasSettableCaps() {
        PacketsMapping protoData = new PacketsMapping();

        final Set<String> allSettableCaps = protoData.getAllSettableCaps();

        assertTrue("Missing settable capability", allSettableCaps.contains("cap1"));
        assertTrue("Missing settable capability", allSettableCaps.contains("cap2"));
        assertTrue("Missing settable capability", allSettableCaps.contains("isAdded"));
        assertTrue("Missing settable capability", allSettableCaps.contains("isRemoved"));
        assertTrue("Missing settable capability", allSettableCaps.contains("updated"));
    }
}
