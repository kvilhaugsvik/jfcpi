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

import org.freeciv.packetgen.UndefinedException;
import org.freeciv.packetgen.dependency.*;
import org.freeciv.packetgen.enteties.FieldTypeBasic;
import com.kvilhaugsvik.javaGenerator.TargetClass;
import com.kvilhaugsvik.javaGenerator.Var;
import com.kvilhaugsvik.javaGenerator.Block;
import com.kvilhaugsvik.javaGenerator.typeBridge.From1;
import com.kvilhaugsvik.javaGenerator.typeBridge.From2;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AValue;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.Returnable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleTypeAlias implements Dependency.Item, Dependency.Maker {
    private final Requirement iProvide;
    private final Collection<Requirement> willRequire;
    private final Pattern fieldTypeBasicForMe;
    private final TargetClass typeInJava;

    public SimpleTypeAlias(String name, TargetClass jType, Requirement req) {
        this.iProvide = new Requirement(name, DataType.class);
        this.typeInJava = jType;
        this.willRequire = null == req ? Collections.<Requirement>emptySet() : Arrays.asList(req);
        fieldTypeBasicForMe = Pattern.compile("(\\w+)\\((" + getIFulfillReq().getName() + ")\\)");
    }

    public SimpleTypeAlias(String name, Class jType, Requirement req) {
        this(name, TargetClass.fromClass(jType), req);
    }

    public SimpleTypeAlias(String name, String jTypePackage, String jType, Requirement req) {
        this(name, TargetClass.fromName(jTypePackage, jType), req);
    }

    public TargetClass getAddress() {
        return typeInJava;
    }

    @Override
    public Required getICanProduceReq() {
        return new RequiredMulti(FieldTypeBasic.class, fieldTypeBasicForMe);
    }

    @Override
    public Dependency.Item produce(Requirement toProduce, Dependency.Item... wasRequired) throws UndefinedException {
        final NetworkIO io = (NetworkIO)wasRequired[0];
        return new FieldTypeBasic(io.getIFulfillReq().getName(), iProvide.getName(), typeInJava,
                new From1<Block, Var>() {
                    @Override
                    public Block x(Var to) {
                        return new Block(
                                to.assign(BuiltIn.<AValue>toCode("value")));
                    }
                },
                new From2<Block, Var, Var>() {
                    @Override
                    public Block x(Var to, Var from) {
                        return new Block(to.assign(io.getRead().x(from)));
                    }
                },
                new From2<Block, Var, Var>() {
                    @Override
                    public Block x(Var val, Var to) {
                        return new Block(to.ref().<Returnable>call(io.getWrite(), val.ref()));
                    }
                },
                                  io.getSize(),
                BuiltIn.TO_STRING_OBJECT,
                                  false, willRequire);
    }

    @Override
    public List<Requirement> neededInput(Requirement toProduce) {
        Matcher search = fieldTypeBasicForMe.matcher(toProduce.getName());
        if (search.matches() && toProduce.getKind().equals(FieldTypeBasic.class))
            return Arrays.asList(new Requirement(search.group(1), NetworkIO.class));
        else
            throw new IllegalArgumentException("The requirement " + toProduce +
                    " isn't a basic field type for " + iProvide.getName());
    }

    @Override
    public Collection<Requirement> getReqs() {
        return Collections.<Requirement>emptySet();
    }

    @Override
    public Requirement getIFulfillReq() {
        return iProvide;
    }
}
