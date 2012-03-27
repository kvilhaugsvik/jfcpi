/*
 * Copyright (c) 2012. Sveinung Kvilhaugsvik
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

import java.util.*;

public class NetworkIO implements IDependency {
    private final Requirement me;
    private final String size;
    private final String read;
    private final String write;

    public NetworkIO(String type, String size, String read, String write) {
        this.me = new Requirement(type, Requirement.Kind.FROM_NETWORK_TO_INT);
        this.size = size;
        this.read = read;
        this.write = write;
    }

    public String getSize() {
        return size;
    }

    public String getRead() {
        return read;
    }

    public String getWrite() {
        return write;
    }

    @Override
    public Collection<Requirement> getReqs() {
        return Collections.<Requirement>emptySet();
    }

    @Override
    public Requirement getIFulfillReq() {
        return me;
    }
}
