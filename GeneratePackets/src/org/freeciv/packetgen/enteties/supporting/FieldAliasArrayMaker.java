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

import com.kvilhaugsvik.dependency.*;
import org.freeciv.packetgen.enteties.Constant;
import org.freeciv.packetgen.enteties.FieldType;

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
            out.add(new Requirement(underlying, FieldType.class));

        if (diff) {
            out.add(new Requirement("DIFF_ARRAY_ENDER", Constant.class));
            out.add(new Requirement(underlying + "_DIFF", FieldType.class));
        }

        return out;
    }

    /**
     * Does the name of the requested field array indicates that it is a diff array.
     * At the moment this is true if the letters DIFF appear between _ and the number of dimensions.
     * @param asParts the name of the requested field array split using splitRequest
     * @return true iff the name of the requested field array indicates that it is a diff array.
     */
    private static boolean isDiff(Matcher asParts) {
        return null != asParts.group(3);
    }

    private static Requirement theDimensionBelow(String underlying, int dimensions, boolean diff) {
        if (diff)
            throw new UnsupportedOperationException("Only 1 dimension supported for diff arrays");

        return new Requirement(underlying + "_" + (dimensions - 1), FieldType.class);
    }

    /**
     * Split the name of the requested field array so it can be understood.
     * The name of the requested field type array has information about
     * its wanted functionality. Split the name in understandable parts.
     * @param toProduce the required field array type.
     * @return information about what should be made.
     */
    private static Matcher splitRequest(Requirement toProduce) {
        Matcher add = arrayRequest.matcher(toProduce.getName());

        /* Validate that the request is understood. Unless it is understood
         * it probably shouldn't have been sent here. */
        if (!add.matches())
            throw new IllegalArgumentException("Can't produce " + toProduce.toString());

        return add;
    }

    /**
     * Return the proper field type for handling the role in a field type array as specified by the name.
     * That may be a regular array, a diff array or even the field type the array is made of.
     * @param toProduce the Requirement of the field type that should be produced. Its name determines its functions.
     * @param wasRequired the items required to produce it in the order given by {@link #neededInput(Requirement)}
     * @return the proper field type for handling the specified role in a field type array.
     * @throws UndefinedException
     */
    @Override
    public Dependency.Item produce(Requirement toProduce, Dependency.Item... wasRequired) throws UndefinedException {
        /* The name of the requested field type array has information about
         * its wanted functionality. Split the name in understandable
         * parts. */
        final Matcher asParts = splitRequest(toProduce);

        /* This should be an array of fields of this type. */
        final FieldType madeOf = (FieldType) wasRequired[0];

        if ("1".equals(asParts.group(4)) && madeOf.isArrayEater()) {
            /* The remaining array dimension is meant for the field type it
             * self. Its field type is therefore considered as the first
             * array dimension. */
            return madeOf.aliasUnseenToCode(toProduce.getName());
        }

        /* Is this field type array a diff array? */
        final boolean isDiffArray = isDiff(asParts);

        /* Diff arrays are the only field type arrays that have a stop element.
         * Regular field type arrays read a known number of elements. */
        final Constant stopElem = (Constant) (isDiffArray ? wasRequired[1] : null);

        /* Diff array elements on the line has the array index and the new value. */
        final FieldType diffElem = (FieldType) (isDiffArray ? wasRequired[2] : null);

        /* The newly created field type array must be renamed since naming it during creation currently is
         * impossible. (A newly created field type is expected to be named like a basic field type) */
        return TerminatedArray.fieldArray("n", "a", madeOf, stopElem, diffElem, isDiffArray).createFieldType(toProduce.getName());
    }
}
