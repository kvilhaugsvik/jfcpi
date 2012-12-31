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

package org.freeciv.packetgen.enteties;

import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.supporting.DataType;
import org.freeciv.packetgen.enteties.supporting.WeakVarDec;
import org.freeciv.packetgen.javaGenerator.*;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;
import org.freeciv.types.FCEnum;

import java.util.*;

import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.*;

public class Struct extends ClassWriter implements IDependency {
    private final Set<Requirement> iRequire;
    private final Requirement iProvide;

    public Struct(String name, List<WeakVarDec> fields, Set<Requirement> willNeed) {
        super(ClassKind.CLASS,
                new TargetPackage(FCEnum.class.getPackage()),
                null, "Freeciv C code", Collections.<Annotate>emptyList(), name,
                DEFAULT_PARENT, Collections.<TargetClass>emptyList());

        addConstructorFields();

        for (WeakVarDec field: fields) {
            addObjectConstant(field.getTypeIncludingArrayBraces(), field.getName());
            addMethod(Method.newPublicReadObjectState(Comment.no(),
                    TargetClass.fromName(field.getTypeIncludingArrayBraces()), "get" + field.getName(),
                    new Block(RETURN(getField(field.getName()).ref()))));
        }

        Typed<? extends AValue> varsToString = literal("(");
        for (int i = 0; i < fields.size(); i++) {
            if (0 != i)
                varsToString = sum(varsToString, literal(", "));
            varsToString = sum(
                    varsToString,
                    literal(fields.get(i).getName() + ": "),
                    getField(fields.get(i).getName()).ref());
        }
        varsToString = sum(varsToString, literal(")"));
        addMethod(Method.newPublicReadObjectState(Comment.no(),
                TargetClass.fromName("String"), "toString",
                new Block(RETURN(varsToString))));

        iRequire = willNeed;
        iProvide = new Requirement("struct" + " " + name, DataType.class);
    }

    @Override
    public Collection<Requirement> getReqs() {
        return iRequire;
    }

    @Override
    public Requirement getIFulfillReq() {
        return iProvide;
    }
}
