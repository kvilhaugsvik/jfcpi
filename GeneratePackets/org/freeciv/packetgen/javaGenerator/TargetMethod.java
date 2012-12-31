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
    private final HasAtoms name;
    private final TargetClass where;
    private final TargetClass returns;
    private final Called kind;

    public TargetMethod(TargetClass where, String named, TargetClass returns, Called kind) {
        this.name = new IR.CodeAtom(named);
        this.where = where;
        this.returns = returns;
        this.kind = kind;
    }

    public TargetMethod(Method has) {
        // TODO: Make TargetClass lazy and use fromClass in stead of fromName
        this(TargetClass.fromName(has.getDeclaringClass().getCanonicalName()),
                has.getName(),
                TargetClass.fromName(has.getReturnType().getCanonicalName()),
                Modifier.isStatic(has.getModifiers()) ? Called.STATIC : Called.DYNAMIC);
    }

    public String getName() {
        return name.toString();
    }

    public <Ret extends Returnable> MethodCall<Ret> call(Typed<? extends AValue>... parameters) {
        return new MethodCall<Ret>(kind, name, parameters);
    }

    public <Ret extends AValue> MethodCall.HasResult<Ret> callV(Typed<? extends AValue>... parameters) {
        if (void.class.getCanonicalName().equals(returns.getFullAddress()))
            throw new IllegalArgumentException(getName() + ": Wrong return type");

        return new MethodCall.HasResult<Ret>(kind, returns, name, parameters);
    }

    public enum Called {
        STATIC, // a class method
        STATIC_ARRAY_INST, // create a new array
        DYNAMIC, // an object method
        DYNAMIC_ARRAY_GET, // get the value of an array
        MANUALLY // handled manually
    }
}
