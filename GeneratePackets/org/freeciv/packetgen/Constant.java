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

package org.freeciv.packetgen;

import java.util.*;

public class Constant implements IDependency {
    private final String name;
    private final String expression;
    private final HashSet<Requirement> reqs = new HashSet<Requirement>();

    public Constant(String name, String expression) {
        this.name = name;
        this.expression = expression;
    }

    public String getName() {
        return name;
    }

    public String getExpression() {
        return expression;
    }

    @Override
    public Collection<Requirement> getReqs() {
        return Collections.unmodifiableCollection(reqs);
    }

    @Override
    public Requirement getIFulfillReq() {
        return new Requirement(name, Requirement.Kind.VALUE);
    }
}
