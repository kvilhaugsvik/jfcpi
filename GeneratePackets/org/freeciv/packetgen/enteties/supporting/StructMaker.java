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

import com.kvilhaugsvik.javaGenerator.TargetClass;
import org.freeciv.packetgen.UndefinedException;
import org.freeciv.packetgen.dependency.Dependency;
import org.freeciv.packetgen.dependency.Required;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.Struct;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class StructMaker implements Dependency.Maker {
    private final String name;
    private final List<WeakVarDec> fields;

    private final Requirement iCreate;
    private final List<Requirement> elementTypes;

    public StructMaker(String name, List<WeakVarDec> fields) {
        this.name = name;
        this.fields = fields;

        this.iCreate = new Requirement("struct" + " " + name, DataType.class);

        LinkedList<Requirement> varTyped = new LinkedList<Requirement>();
        for (WeakVarDec field : fields) {
            final int decs = field.getDeclarations().length;
            if (0 == decs)
                varTyped.add(field.getTypeRequirement());
            else
                varTyped.add(new Requirement(field.getTypeRequirement().getName() + "_" + decs, DataType.class));
        }
        this.elementTypes = Collections.unmodifiableList(varTyped);
    }

    @Override
    public List<Requirement> neededInput(Requirement toProduce) {
        validateToProduce(toProduce);

        return elementTypes;
    }

    private void validateToProduce(Requirement toProduce) {
        if (!iCreate.equals(toProduce))
            throw new IllegalArgumentException("Asked to produce wrong thing." +
                    "Can create: " + iCreate + " Asked to created: " + toProduce);
    }

    @Override
    public Required getICanProduceReq() {
        return iCreate;
    }

    @Override
    public Item produce(Requirement toProduce, Item... wasRequired) throws UndefinedException {
        validateToProduce(toProduce);

        LinkedList<TargetClass> fieldKinds = new LinkedList<TargetClass>();
        for (Item item : wasRequired)
            fieldKinds.add(((DataType)item).getAddress().scopeKnown());

        return new Struct(name, fields, fieldKinds);
    }
}
