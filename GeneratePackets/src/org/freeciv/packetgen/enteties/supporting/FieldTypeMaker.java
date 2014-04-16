/*
 * Copyright (c) 2014. Sveinung Kvilhaugsvik
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

import com.kvilhaugsvik.dependency.Requirement;
import com.kvilhaugsvik.dependency.SimpleDependencyMaker;
import com.kvilhaugsvik.dependency.UndefinedException;

import org.freeciv.packetgen.enteties.FieldType;

public class FieldTypeMaker extends SimpleDependencyMaker {
    private final String alias;

    public FieldTypeMaker(Requirement from, Requirement to, String alias) {
        super(from, to);
        this.alias = alias;
    }

    @Override
    public Item produce(Requirement toProduce, Item... wasRequired) throws UndefinedException {
        return ((FieldType) wasRequired[0]).createFieldType(alias);
    }
}
