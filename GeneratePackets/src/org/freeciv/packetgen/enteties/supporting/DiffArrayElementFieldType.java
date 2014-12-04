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

package org.freeciv.packetgen.enteties.supporting;

import com.kvilhaugsvik.dependency.*;
import com.kvilhaugsvik.javaGenerator.*;
import com.kvilhaugsvik.javaGenerator.typeBridge.From1;
import com.kvilhaugsvik.javaGenerator.typeBridge.From2;
import com.kvilhaugsvik.javaGenerator.typeBridge.From3;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.ABool;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AValue;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AnInt;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import org.freeciv.packetgen.Hardcoded;
import org.freeciv.packetgen.enteties.Constant;
import org.freeciv.packetgen.enteties.FieldType;
import org.freeciv.packetgen.enteties.Struct;
import org.freeciv.utility.Validation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.kvilhaugsvik.javaGenerator.util.BuiltIn.*;

public class DiffArrayElementFieldType implements Dependency.Maker {
    private static final Pattern DIFF_ELEMENT_REQUEST = Pattern.compile("(\\w+)_DIFF");

    @Override
    public Required getICanProduceReq() {
        return new RequiredMulti(FieldType.class, DIFF_ELEMENT_REQUEST);
    }

    @Override
    public List<Requirement> neededInput(Requirement toProduce) {
        String underlying = splitRequest(toProduce).group(1);

        return Arrays.asList(new Requirement("uint8(int)", FieldType.class),
                new Requirement(underlying, FieldType.class),
                new Requirement(underlying + "_diff", DataType.class),
                new Requirement("DIFF_ARRAY_ENDER", Constant.class));
    }

    private static Matcher splitRequest(Requirement toProduce) {
        Matcher add = DIFF_ELEMENT_REQUEST.matcher(toProduce.getName());
        if (!add.matches())
            throw new IllegalArgumentException("Can't produce " + toProduce.toString());
        return add;
    }

    @Override
    public Item produce(Requirement toProduce, Item... wasRequired) throws UndefinedException {
        final FieldType ftIndex = (FieldType) wasRequired[0];
        Validation.validateNotNull(ftIndex, "ftIndex");
        final FieldType ftValue = (FieldType) wasRequired[1];
        Validation.validateNotNull(ftValue, "ftValue");
        final Struct aggregatedIn = (Struct) wasRequired[2];
        Validation.validateNotNull(aggregatedIn, "aggregatedIn");
        final Constant stopValue = (Constant) wasRequired[3];
        Validation.validateNotNull(stopValue, "stopValue");

        final String underlying = splitRequest(toProduce).group(1);

        final Item basic = new FieldType(underlying, underlying, aggregatedIn,
                new From1<Block, Var>() {
                    @Override
                    public Block x(Var arg1) {
                        return new Block(arg1.assign(Hardcoded.pValue.ref()));
                    }
                },
                new From3<Block, Var, Var, Var>() {
                    @Override
                    public Block x(Var to, Var from, Var old) {
                        final Var<AValue> index = Var.local(ftIndex.getUnderType(), "index",
                                ftIndex.getAddress().newInstance(from.ref(), Hardcoded.noLimit, NULL).callV("getValue"));
                        final Var<AValue> newValue = Var.local(ftValue.getUnderType(), "newValue",
                                BuiltIn.<AValue>R_IF(index.ref().<ABool>callV("equals", stopValue.ref()),
                                        NULL,
                                        ftValue.getAddress().newInstance(from.ref(), Hardcoded.noLimit, NULL)
                                                .callV("getValue")));

                        return new Block(
                                index,
                                newValue,
                                to.assign(aggregatedIn.getAddress().newInstance(index.ref(), newValue.ref()))
                        );
                    }
                },
                new From2<Block, Var, Var>() {
                    @Override
                    public Block x(Var val, Var to) {
                        return new Block(
                                ftIndex.getAddress().newInstance(val.ref().callV("getIndex"), Hardcoded.noLimit).call("encodeTo", to.ref()),
                                IF(isNotSame(val.ref().callV("getIndex").callV("intValue"), stopValue.ref()),
                                        new Block(ftValue.getAddress().newInstance(val.ref().callV("getNewValue"), Hardcoded.noLimit).call("encodeTo", to.ref()))));
                    }
                },
                new From1<Typed<AnInt>, Var>() {
                    @Override
                    public Typed<AnInt> x(Var val) {
                        return BuiltIn.sum(
                                ftIndex.getAddress().newInstance(val.ref().callV("getIndex"), Hardcoded.noLimit).callV("encodedLength"),
                                BuiltIn.R_IF(isSame(val.ref().callV("getIndex").callV("intValue"), stopValue.ref()),
                                        literal(0),
                                        ftValue.getAddress().newInstance(val.ref().callV("getNewValue"), Hardcoded.noLimit).<AnInt>callV("encodedLength")));
                    }
                },
                TO_STRING_OBJECT,
                false,
                Arrays.asList(new Requirement("uint8(int)", FieldType.class),
                        new Requirement(underlying, FieldType.class),
                        new Requirement(underlying + "_diff", DataType.class),
                        new Requirement("DIFF_ARRAY_ENDER", Constant.class)),
                Collections.<Var<?>>emptyList(),
                Collections.<Method>emptyList(),
                FieldType.UNSIZED_ZERO
        );

        return ((FieldType)basic).createFieldType(toProduce.getName());
    }
}
