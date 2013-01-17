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
import org.freeciv.packetgen.javaGenerator.TargetClass;
import org.freeciv.packetgen.javaGenerator.Var;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.typeBridge.From1;
import org.freeciv.packetgen.javaGenerator.typeBridge.From2;
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.typeBridge.willReturn.AValue;
import org.freeciv.packetgen.javaGenerator.typeBridge.willReturn.Returnable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleTypeAlias implements IDependency, IDependency.Maker {
    private final Requirement iProvide;
    private final Collection<Requirement> willRequire;
    private final Pattern fieldTypeBasicForMe;
    private final TargetClass typeInJava;

    public SimpleTypeAlias(String name, TargetClass jType, Collection<Requirement> reqs) {
        this.iProvide = new Requirement(name, DataType.class);
        this.typeInJava = jType;
        this.willRequire = reqs;
        fieldTypeBasicForMe = Pattern.compile("(\\w+)\\((" + getIFulfillReq().getName() + ")\\)");
    }

    public SimpleTypeAlias(String name, Class jType, Collection<Requirement> reqs) {
        this(name, TargetClass.fromClass(jType), reqs);
    }

    public SimpleTypeAlias(String name, String jTypePackage, String jType, Collection<Requirement> reqs) {
        this(name, TargetClass.fromName(jTypePackage, jType), reqs);
    }

    public TargetClass getJavaType() {
        return typeInJava;
    }

    @Override
    public Required getICanProduceReq() {
        return new RequiredMulti(FieldTypeBasic.class, fieldTypeBasicForMe);
    }

    @Override
    public IDependency produce(Requirement toProduce, IDependency... wasRequired) throws UndefinedException {
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
                        return new Block(to.assign(willRequire.isEmpty() ?
                                        io.getRead().x(from) :
                                        typeInJava.call("valueOf", io.getRead().x(from))));
                    }
                },
                new From2<Block, Var, Var>() {
                    @Override
                    public Block x(Var val, Var to) {
                        return new Block(to.ref().<Returnable>call(io.getWrite(), willRequire.isEmpty() ?
                                val.ref() :
                                val.ref().<Returnable>call("getNumber")));
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
