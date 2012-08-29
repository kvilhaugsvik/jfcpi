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

package org.freeciv.packetgen.javaGenerator;

import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;

import java.lang.reflect.Method;

public class TargetMethod extends Address {
    private final String name;

    public TargetMethod(String named) {
        name = named;
    }

    public TargetMethod(Method has) {
        this(has.getName());
    }

    public String getName() {
        return name;
    }

    // TODO: Make return <? extends Returnable> when type system is fixed
    public MethodCall.RetAValue call(Typed<AValue>[] parameters) {
        return new MethodCall.RetAValue(null, name, parameters);
    }
}
