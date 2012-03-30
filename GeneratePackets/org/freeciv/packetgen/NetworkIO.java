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

// TODO: Quick: Split based on kind of fulfillment so a dummy don't have get for the nulls or throw excpetion on null
// TODO: Long term: Split code in a better way between NetworkIO and java type. Some argument passing may fix all.
public class NetworkIO implements IDependency {
    private final Requirement me;
    private final String size;
    private final String read;
    private final String write;

    private NetworkIO(String type, String size, String read, String write, Requirement.Kind kind) {
        this.me = new Requirement(type, kind);
        this.size = size;
        this.read = read;
        this.write = write;
    }

    /**
     * A network reader that uses int as representation
     * @param type the IOType it should match
     * @param size code that returns its size
     * @param read code to get an integer from a DataInput named "from"
     * @param write code to write an integer provided in braces right after to a DataOutput named "to"
     */
    public NetworkIO(String type, String size, String read, String write) {
        this(type, size, read, write, Requirement.Kind.FROM_NETWORK_TO_INT);
    }

    /**
     * A dummy
     * @param type the IOType it should match
     */
    public NetworkIO(String type) {
        this(type, null, null, null, Requirement.Kind.FROM_NETWORK_DUMMY);
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
