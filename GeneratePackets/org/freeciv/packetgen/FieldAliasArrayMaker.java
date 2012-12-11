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

import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.Required;
import org.freeciv.packetgen.dependency.RequiredMulti;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.FieldTypeBasic;
import org.freeciv.packetgen.enteties.supporting.TerminatedArray;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FieldAliasArrayMaker implements IDependency.Maker {
    private static final Pattern arrayRequest = Pattern.compile("(\\w+)_(\\d+)");

    @Override
    public Required getICanProduceReq() {
        return new RequiredMulti(FieldTypeBasic.FieldTypeAlias.class, arrayRequest);
    }

    @Override
    public List<Requirement> neededInput(Requirement toProduce) {
        Matcher asParts = splitRequest(toProduce);

        String underlying = asParts.group(1);
        int dimensions = Integer.parseInt(asParts.group(2));

        if (2 < dimensions)
            return Arrays.asList(theDimensionBelow(underlying, dimensions));
        else if (2 == dimensions)
            return Arrays.asList(theUnderlying(underlying), theDimensionBelow(underlying, dimensions));
        else
            return Arrays.asList(theUnderlying(underlying));
    }

    private static Requirement theDimensionBelow(String underlying, int dimensions) {
        return new Requirement(underlying + "_" + (dimensions - 1), FieldTypeBasic.FieldTypeAlias.class);
    }

    private static Requirement theUnderlying(String underlying) {
        return new Requirement(underlying, FieldTypeBasic.FieldTypeAlias.class);
    }

    private static Matcher splitRequest(Requirement toProduce) {
        Matcher add = arrayRequest.matcher(toProduce.getName());
        if (!add.matches())
            throw new IllegalArgumentException("Can't produce " + toProduce.toString());
        return add;
    }

    @Override
    public IDependency produce(Requirement toProduce, IDependency... wasRequired) throws UndefinedException {
        // if there are two arguments the first one is the underlying type and the other is a 1D array
        if (wasntArrayEaterAfterAll(wasRequired))
            return TerminatedArray.fieldArray("n", "a", (FieldTypeBasic.FieldTypeAlias) wasRequired[1])
                    .createFieldType(toProduce.getName());


        return TerminatedArray.fieldArray("n", "a", (FieldTypeBasic.FieldTypeAlias) wasRequired[0])
                .createFieldType(toProduce.getName());
    }

    private static boolean wasntArrayEaterAfterAll(IDependency[] wasRequired) {
        return 2 == wasRequired.length && !((FieldTypeBasic.FieldTypeAlias)wasRequired[0]).getBasicType().isArrayEater();
    }
}
