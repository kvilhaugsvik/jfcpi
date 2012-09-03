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
import org.freeciv.packetgen.enteties.FieldTypeBasic;
import org.freeciv.packetgen.javaGenerator.MethodCall;
import org.freeciv.packetgen.javaGenerator.TargetClass;
import org.freeciv.packetgen.javaGenerator.Var;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.creators.ExprFrom1;
import org.freeciv.packetgen.javaGenerator.expression.creators.ExprFrom2;
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;

import java.util.*;

import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.asAValue;

public class SimpleTypeAlias implements IDependency, FieldTypeBasic.Generator {
    private final Requirement iProvide;
    private final Collection<Requirement> willRequire;
    private final String typeInJava;

    public SimpleTypeAlias(String name, String jType, Collection<Requirement> reqs) {
        this.iProvide = new Requirement(name, Requirement.Kind.AS_JAVA_DATATYPE);
        this.typeInJava = jType;
        this.willRequire = reqs;
    }

    @Override
    public FieldTypeBasic getBasicFieldTypeOnInput(final NetworkIO io) {
        return new FieldTypeBasic(io.getIFulfillReq().getName(), iProvide.getName(), new TargetClass(typeInJava),
                new ExprFrom1<Block, Var>() {
                    @Override
                    public Block x(Var to) {
                        return new Block(
                                to.assign(asAValue("value")));
                    }
                },
                new ExprFrom2<Block, Var, Var>() {
                    @Override
                    public Block x(Var to, Var from) {
                        return new Block(to.assign(willRequire.isEmpty() ?
                                        io.getRead() :
                                        new MethodCall<AValue>(null, typeInJava + ".valueOf", io.getRead())));
                    }
                },
                new ExprFrom2<Block, Var, Var>() {
                    @Override
                    public Block x(Var val, Var to) {
                        return Block.fromStrings(
                                io.getWrite((willRequire.isEmpty() ? "this.value" : "this.value.getNumber()")));
                    }
                },
                                  io.getSize(),
                BuiltIn.TO_STRING_OBJECT,
                                  false, willRequire);
    }

    @Override
    public Requirement.Kind needsDataInFormat() {
        return Requirement.Kind.FROM_NETWORK_TO_INT;
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
