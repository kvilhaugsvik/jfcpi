/*
 * Copyright (c) 2013. Sveinung Kvilhaugsvik
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

import com.kvilhaugsvik.javaGenerator.TargetClass;
import org.freeciv.packetgen.dependency.Dependency;
import org.freeciv.packetgen.dependency.Required;
import org.freeciv.packetgen.dependency.RequiredMulti;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.FieldType;
import org.freeciv.packetgen.enteties.Struct;
import org.freeciv.packetgen.enteties.supporting.DataType;
import org.freeciv.packetgen.enteties.supporting.WeakVarDec;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiffArrayElementDataType implements Dependency.Maker {
    private static final Pattern DIFF_ELEMENT_REQUEST = Pattern.compile("(\\w+)_diff");

    @Override
    public Required getICanProduceReq() {
        return new RequiredMulti(DataType.class, DIFF_ELEMENT_REQUEST);
    }

    @Override
    public List<Requirement> neededInput(Requirement toProduce) {
        String underlying = splitRequest(toProduce).group(1);

        return Arrays.asList(new Requirement(underlying, FieldType.class));
    }

    private static Matcher splitRequest(Requirement toProduce) {
        Matcher add = DIFF_ELEMENT_REQUEST.matcher(toProduce.getName());
        if (!add.matches())
            throw new IllegalArgumentException("Can't produce " + toProduce.toString());
        return add;
    }

    @Override
    public Item produce(Requirement toProduce, Item... wasRequired) throws UndefinedException {
        final FieldType fieldTypeAlias = (FieldType) wasRequired[0];
        final Requirement require = wasRequired[0].getIFulfillReq();
        return new Struct(toProduce.getName(),
                Arrays.asList(
                        new WeakVarDec(new Requirement("int", DataType.class), "index"),
                        new WeakVarDec(require, "newValue")),
                Arrays.asList(
                        TargetClass.from(Integer.class),
                        fieldTypeAlias.getUnderType()),
                Arrays.asList(
                        new Requirement("int", DataType.class),
                        require),
                false);
    }
}
