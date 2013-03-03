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

import org.freeciv.packetgen.dependency.Dependency;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.supporting.DataType;
import org.freeciv.packetgen.enteties.supporting.WeakVarDec;
import com.kvilhaugsvik.javaGenerator.*;
import com.kvilhaugsvik.javaGenerator.Block;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AValue;
import org.freeciv.types.FCEnum;

import java.util.*;

import static com.kvilhaugsvik.javaGenerator.util.BuiltIn.*;

public class Struct extends ClassWriter implements Dependency.Item, DataType {
    private final Set<Requirement> iRequire;
    private final Requirement iProvide;

    public Struct(String name, List<WeakVarDec> fields, List<TargetClass> partTypes) {
        super(ClassKind.CLASS,
                TargetPackage.from(FCEnum.class.getPackage()),
                Imports.are(), "Freeciv C code", Collections.<Annotate>emptyList(), name,
                DEFAULT_PARENT, Collections.<TargetClass>emptyList());

        addConstructorFields();

        for (int i = 0; i < fields.size(); i++) {
            final WeakVarDec field = fields.get(i);
            final TargetClass type = partTypes.get(i);

            addObjectConstant(type, field.getName());
            addMethod(Method.newPublicReadObjectState(Comment.no(),
                    type, "get" + field.getName(),
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
                TargetClass.newKnown(String.class), "toString",
                new Block(RETURN(varsToString))));

        HashSet<Requirement> neededByFields = new HashSet<Requirement>();
        for (WeakVarDec field : fields) {
            assert (null != field.getTypeRequirement()) : "Type can't be null";
            neededByFields.add(field.getTypeRequirement());
            for (WeakVarDec.ArrayDeclaration dec : field.getDeclarations())
                neededByFields.addAll(dec.maxSize.getReqs());
        }

        this.iRequire = Collections.unmodifiableSet(neededByFields);
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
