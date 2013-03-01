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

package org.freeciv.packetgen.enteties.supporting;

import org.freeciv.packetgen.dependency.Dependency;
import org.freeciv.packetgen.dependency.ReqKind;
import org.freeciv.packetgen.dependency.Requirement;
import com.kvilhaugsvik.javaGenerator.Var;
import com.kvilhaugsvik.javaGenerator.typeBridge.From1;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AnInt;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.Returnable;

import java.util.Collection;
import java.util.Collections;

// TODO: Quick: Split based on kind of fulfillment so a dummy don't have get for the nulls or throw excpetion on null
// TODO: Long term: Split code in a better way between NetworkIO and java type. Some argument passing may fix all.
public class NetworkIO implements Dependency.Item, ReqKind {
    private final Requirement me;
    private final int size;
    private final From1<Typed<AnInt>, Var> readNoArgs;
    private final String write;

    private NetworkIO(String type, int size, String write, Class<? extends ReqKind> kind,
                      final From1<Typed<AnInt>, Var> readNoArgs) {
        this.me = new Requirement(type, kind);
        this.size = size;
        this.readNoArgs = readNoArgs;
        this.write = write;
    }

    public From1<Typed<AnInt>, Var> getSize() {
        return new From1<Typed<AnInt>, Var>() {
            @Override
            public Typed<AnInt> x(Var arg1) {
                return BuiltIn.literal(size);
            }
        };
    }

    public final From1<Typed<AnInt>, Var> getRead() {
        return readNoArgs;
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

    /**
     * A network reader that uses int as intermediate representation
     * @param type the IOType it should match
     * @param size expression returning the since on the wire in bytes
     * @param readFunction code to read an integer from a DataInput named "from"
     * @param noCastNeeded does readFunction return an int?
     * @param write code to write an integer provided in braces right after to a DataOutput named "to"
     */
    public static NetworkIO witIntAsIntermediate(String type,
                                                 int size,
                                                 final String readFunction, final boolean noCastNeeded,
                                                 String write) {
        return new NetworkIO(type, size, write, NetworkIO.class,
                new From1<Typed<AnInt>, Var>() {
                    @Override
                    public Typed<AnInt> x(Var from) {
                        Typed<AnInt> out = from.ref().<Returnable>call(readFunction);
                        if (!noCastNeeded)
                            out = BuiltIn.<AnInt>cast(int.class, out);
                        return out;
                    }
                });
    }
}
