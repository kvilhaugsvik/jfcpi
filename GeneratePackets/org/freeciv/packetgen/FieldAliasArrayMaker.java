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

import org.freeciv.packetgen.dependency.Dependency;
import org.freeciv.packetgen.dependency.Required;
import org.freeciv.packetgen.dependency.RequiredMulti;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.Constant;
import org.freeciv.packetgen.enteties.FieldType;
import org.freeciv.packetgen.enteties.supporting.TerminatedArray;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FieldAliasArrayMaker implements Dependency.Maker {
    private static final Pattern arrayRequest = Pattern.compile("(\\w+)_((DIFF)?(\\d+))");

    @Override
    public Required getICanProduceReq() {
        return new RequiredMulti(FieldType.class, arrayRequest);
    }

    @Override
    public List<Requirement> neededInput(Requirement toProduce) {
        LinkedList<Requirement> out = new LinkedList<Requirement>();

        Matcher asParts = splitRequest(toProduce);

        String underlying = asParts.group(1);
        boolean diff = isDiff(asParts);
        int dimensions = Integer.parseInt(asParts.group(4));

        if (1 < dimensions)
            out.add(theDimensionBelow(underlying, dimensions, diff));
        else
            out.add(theUnderlying(underlying, diff));

        if (diff)
            out.add(new Requirement("DIFF_ARRAY_ENDER", Constant.class));

        return out;
    }

    private static boolean isDiff(Matcher asParts) {
        return null != asParts.group(3);
    }

    private static Requirement theDimensionBelow(String underlying, int dimensions, boolean diff) {
        if (diff)
            throw new UnsupportedOperationException("Only 1 dimension supported for diff arrays");

        return new Requirement(underlying + "_" + (dimensions - 1), FieldType.class);
    }

    private static Requirement theUnderlying(String underlying, boolean diff) {
        return new Requirement(underlying + (diff ? "_DIFF" : ""), FieldType.class);
    }

    private static Matcher splitRequest(Requirement toProduce) {
        Matcher add = arrayRequest.matcher(toProduce.getName());
        if (!add.matches())
            throw new IllegalArgumentException("Can't produce " + toProduce.toString());
        return add;
    }

    @Override
    public Dependency.Item produce(Requirement toProduce, Dependency.Item... wasRequired) throws UndefinedException {
        final Matcher asParts = splitRequest(toProduce);

        if ("1".equals(asParts.group(4)) && eatsArrays(wasRequired[0]))
            return ((FieldType)wasRequired[0]).aliasUnseenToCode(toProduce.getName());

        final FieldType madeOf = (FieldType) wasRequired[0];
        final Constant stopElem = (Constant) (isDiff(asParts) ? wasRequired[1] : null);

        return TerminatedArray.fieldArray("n", "a", madeOf, stopElem).createFieldType(toProduce.getName());
    }

    private static boolean eatsArrays(Dependency.Item dependencyItem) {
        return ((FieldType) dependencyItem).isArrayEater();
    }
}
