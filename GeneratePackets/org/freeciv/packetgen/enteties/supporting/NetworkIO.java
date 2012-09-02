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

import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.javaGenerator.HasAtoms;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.creators.ExprFrom1;
import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;

import java.util.Collection;
import java.util.Collections;

import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.asAValue;

// TODO: Quick: Split based on kind of fulfillment so a dummy don't have get for the nulls or throw excpetion on null
// TODO: Long term: Split code in a better way between NetworkIO and java type. Some argument passing may fix all.
public class NetworkIO implements IDependency {
    private final Requirement me;
    private final String size;
    private final Typed<? extends AValue> readNoArgs;
    private final String write;

    private NetworkIO(String type, String size, String write, Requirement.Kind kind, Typed<? extends AValue> readNoArgs) {
        this.me = new Requirement(type, kind);
        this.size = size;
        this.readNoArgs = readNoArgs;
        this.write = write;
    }

    public String getSize() {
        return "return " + size + ";";
    }

    public Block getRead(String argument, Typed<AValue> pre, Typed<AValue> post) {
        Block out = new Block();
        if (null != pre) out.addStatement(pre);
        out.addStatement(asAValue("byte[] innBuffer = new byte[" + argument + "]"));
        out.addStatement(asAValue("from.readFully(innBuffer)"));
        if (null != post) out.addStatement(post);
        return out;
    }

    public Typed<? extends AValue> getRead() {
        return readNoArgs;
    }

    public String getWrite(String toWrite) {
        return write + "(" + toWrite + ");";
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
     * @param read code to read an integer from a DataInput named "from"
     * @param write code to write an integer provided in braces right after to a DataOutput named "to"
     */
    public static NetworkIO witIntAsIntermediate(String type, String size, String read, String write) {
        return new NetworkIO(type, size, write, Requirement.Kind.FROM_NETWORK_TO_INT, BuiltIn.asAnInt(read));
    }

    /**
     * A network reader that read X bytes into a byte[] called innBuffer
     * @param type the IOType it should match
     */
    public static NetworkIO withBytesAsIntermediate(String type) {
        return new NetworkIO(type,
             null,
             "to.write",
             Requirement.Kind.FROM_NETWORK_AMOUNT_OF_BYTES,
             null);
    }
}
