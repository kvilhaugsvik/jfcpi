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
import org.freeciv.packetgen.javaGenerator.expression.willReturn.Returnable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class TargetMethod extends Address {
    private final String name;
    private final Called kind;

    public TargetMethod(String named, Called kind) {
        this.name = named;
        this.kind = kind;
    }

    public TargetMethod(Method has) {
        this(has.getName(), Modifier.isStatic(has.getModifiers()) ? Called.STATIC : Called.DYNAMIC);
    }

    public String getName() {
        return name;
    }

    // TODO: Make return <? extends Returnable> when type system is fixed
    public <Ret extends Returnable> MethodCall<Ret> call(Typed<? extends AValue>... parameters) {
        return new MethodCall<Ret>(name, parameters);
    }

    public enum Called {
        STATIC, // a class method
        DYNAMIC, // an object method
        MANUALLY // handled manually
    }
}
