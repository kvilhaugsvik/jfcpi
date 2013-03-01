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

package org.freeciv.packetgen.dependency;

import java.util.Arrays;
import java.util.List;

public abstract class SimpleDependencyMaker implements IDependency.Maker {
    private final List<Requirement> params;
    private final Requirement iCanProduce;

    public SimpleDependencyMaker(Requirement iCanProduce, Requirement... neededArguments) {
        this.iCanProduce = iCanProduce;
        this.params = Arrays.asList(neededArguments);
    }

    @Override
    public List<Requirement> neededInput(Requirement toProduce) {
        return params;
    }

    @Override
    public Required getICanProduceReq() {
        return iCanProduce;
    }
}
