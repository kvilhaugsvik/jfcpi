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

package org.freeciv.packetgen.dependency;

import org.freeciv.packetgen.enteties.Constant;
import org.freeciv.packetgen.enteties.FieldTypeBasic;
import org.freeciv.packetgen.enteties.Packet;
import org.freeciv.packetgen.enteties.supporting.DataType;
import org.freeciv.packetgen.enteties.supporting.NetworkIO;

public class Requirement implements Comparable<Requirement> {
    private final String name;
    private final Class<? extends ReqKind> kind;

    public Requirement(String name, Kind kind) {
        this.name = name;
        this.kind = kind.newSystem;
    }

    public Class<? extends ReqKind> getKind() {
        return kind;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object that) {
        if (that instanceof Requirement)
            return name.equals(((Requirement)that).getName()) &&
                    kind.equals(((Requirement)that).getKind());
        else
            return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + kind.hashCode();
    }

    @Override
    public int compareTo(Requirement that) {
        if (this.getKind().hashCode() < that.getKind().hashCode())
            return -1;
        else if (that.getKind().hashCode() < this.getKind().hashCode())
            return 1;
        else
            return this.getName().compareTo(that.getName());
    }

    @Override
    public String toString() {
        return "The " + kind + " " + name;
    }

    public enum Kind {
        VALUE(Constant.class),
        FROM_NETWORK(NetworkIO.class),
        AS_JAVA_DATATYPE(DataType.class),
        PRIMITIVE_FIELD_TYPE(FieldTypeBasic.class),
        FIELD_TYPE(FieldTypeBasic.FieldTypeAlias.class),
        HARD_FAILURE(ReqKind.FailHard.class), // a Requirement that already has failed to be met
        PACKET(Packet.class);
        private final Class<? extends ReqKind> newSystem;

        private Kind(Class<? extends ReqKind> newSystem) {
            this.newSystem = newSystem;
        }
    }
}
