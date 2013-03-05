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

import org.freeciv.packetgen.dependency.Dependency;
import org.freeciv.packetgen.dependency.Required;
import org.freeciv.packetgen.dependency.RequiredMulti;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.FieldTypeBasic;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

class BasicFieldTypeAsFieldTypeAlias implements Dependency.Maker {
    Pattern splitter = Pattern.compile("((\\{)?\\w+(;\\w+)*(\\})?)\\(([\\w ]+)\\)");

    @Override
    public Required getICanProduceReq() {
        return new RequiredMulti(FieldTypeBasic.FieldTypeAlias.class, splitter);
    }

    @Override
    public List<Requirement> neededInput(Requirement toProduce) {
        return Arrays.asList(new Requirement(toProduce.getName(), FieldTypeBasic.class));
    }

    @Override
    public Dependency.Item produce(Requirement toProduce, Dependency.Item... wasRequired) throws UndefinedException {
        return ((FieldTypeBasic)wasRequired[0])
                .createFieldType("UNALIASED_" + toProduce.getName().replaceAll("[\\(|\\)|\\s|;|\\{|\\}]", "_"),
                        toProduce.getName());
    }
}
