/*
 * Copyright (c) 2014. Sveinung Kvilhaugsvik
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
import com.kvilhaugsvik.dependency.Requirement;
import com.kvilhaugsvik.dependency.SimpleDependencyMaker;
import com.kvilhaugsvik.dependency.UndefinedException;

import org.freeciv.packetgen.enteties.FieldType;

import java.util.*;

public class FieldTypeMaker extends SimpleDependencyMaker implements Dependency.BlameShifter {
    private final String alias;
    private final Map<Requirement, Collection<Requirement>> blame;

    private FieldTypeMaker(Requirement from, Requirement to, String alias,
                           Map<Requirement, Collection<Requirement>> blame) {
        super(from, to);
        this.alias = alias;
        this.blame = Collections.unmodifiableMap(blame);
    }

    @Override
    public Item produce(Requirement toProduce, Item... wasRequired) throws UndefinedException {
        return ((FieldType) wasRequired[0]).createFieldType(alias);
    }

    /**
     * A maker for a field type that is defined by how to read/write it and its C data type
     * @param alias the field type that should be made
     * @param iotype how Freeciv (de)serialize it
     * @param ptype the C data type Freeciv give the deserialized field
     * @return a maker for the field type alias
     */
    public static FieldTypeMaker basic(final String alias, String iotype, String ptype) {
        final Requirement from = new Requirement(alias, FieldType.class);
        final Requirement to = new Requirement(iotype + "(" + ptype + ")", FieldType.class);
        final HashMap<Requirement, Collection<Requirement>> mutableBlame =
                new HashMap<Requirement, Collection<Requirement>>();

        /* Having both will in some cases cause the wanted input to be produced. */
        mutableBlame.put(to,
                Arrays.asList(new Requirement(ptype, DataType.class), new Requirement(iotype, NetworkIO.class)));

        return new FieldTypeMaker(from, to, alias, mutableBlame);
    }

    /**
     * A maker for a field type that is defined as another field type.
     * @param alias the field type that should be made
     * @param aliased the field type it is an alias of
     * @return a maker for the field type alias
     */
    public static FieldTypeMaker alias(final String alias, String aliased) {
        final Requirement from = new Requirement(alias, FieldType.class);
        final Requirement to = new Requirement(aliased, FieldType.class);

        return new FieldTypeMaker(from, to, alias,
                new HashMap<Requirement, Collection<Requirement>>());
    }

    @Override
    public Map<Requirement, Collection<Requirement>> blameSuspects() {
        return blame;
    }
}
