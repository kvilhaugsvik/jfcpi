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

import com.kvilhaugsvik.dependency.Dependency;
import com.kvilhaugsvik.dependency.ReqKind;
import com.kvilhaugsvik.dependency.Requirement;
import com.kvilhaugsvik.javaGenerator.Var;
import com.kvilhaugsvik.javaGenerator.typeBridge.From1;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AnInt;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.Returnable;

import java.util.Collection;
import java.util.Collections;

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

    public final Typed<AnInt> getRead(Var from) {
        return readNoArgs.x(from);
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
     * @param size the since on the wire in bytes
     * @param readFunction DataInput's read method for this
     * @param castReadTo cast data to this after it is read or null if no casting is needed
     * @param write DataInput's write method for this
     */
    public static NetworkIO simple(String type,
                                   int size,
                                   final String readFunction,
                                   final Class<?> castReadTo,
                                   String write) {
        return new NetworkIO(type, size, write, NetworkIO.class,
                new From1<Typed<AnInt>, Var>() {
                    @Override
                    public Typed<AnInt> x(Var from) {
                        Typed<AnInt> out = from.ref().<Returnable>call(readFunction);
                        if (null != castReadTo)
                            out = BuiltIn.<AnInt>cast(castReadTo, out);
                        return out;
                    }
                });
    }
}
