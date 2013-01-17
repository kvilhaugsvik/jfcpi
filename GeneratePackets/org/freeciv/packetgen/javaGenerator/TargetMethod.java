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
import org.freeciv.packetgen.javaGenerator.representation.CodeAtoms;
import org.freeciv.packetgen.javaGenerator.representation.HasAtoms;
import org.freeciv.packetgen.javaGenerator.representation.IR;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class TargetMethod extends Address<TargetClass> {
    private final HasAtoms name;
    private final TargetClass returns;
    private final Called kind;

    public TargetMethod(TargetClass where, String named, TargetClass returns, Called kind) {
        super(where, new IR.CodeAtom(named));
        this.name = super.components[super.components.length - 1];
        this.returns = returns;
        this.kind = kind;
    }

    public TargetMethod(Method has) {
        this(TargetClass.fromClass(has.getDeclaringClass()),
                has.getName(),
                TargetClass.fromClass(has.getReturnType()),
                Modifier.isStatic(has.getModifiers()) ? Called.STATIC : Called.DYNAMIC);
    }

    public TargetMethod(Field has) {
        this(TargetClass.fromClass(has.getDeclaringClass()).scopeUnknown(),
                has.getName(),
                TargetClass.fromClass(has.getType()).scopeUnknown(),
                Modifier.isStatic(has.getModifiers()) ? Called.STATIC_FIELD : Called.DYNAMIC_FIELD);
    }

    public String getName() {
        return name.toString();
    }

    public <Ret extends Returnable> MethodCall<Ret> call(Typed<? extends AValue>... parameters) {
        return new MethodCall<Ret>(kind, (Called.STATIC_FIELD.equals(kind) ? this : name), parameters);
    }

    public <Ret extends AValue> MethodCall.HasResult<Ret> callV(Typed<? extends AValue>... parameters) {
        if (void.class.getCanonicalName().equals(returns.getFullAddress()))
            throw new IllegalArgumentException(getName() + ": Wrong return type");

        return new MethodCall.HasResult<Ret>(kind, returns, (Called.STATIC_FIELD.equals(kind) ? this : name), parameters);
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        to.hintStart(TargetMethod.class.getCanonicalName());
        super.writeAtoms(to);
        to.hintEnd(TargetMethod.class.getCanonicalName());
    }

    public enum Called {
        STATIC, // a class method
        STATIC_ARRAY_INST, // create a new array
        DYNAMIC, // an object method
        DYNAMIC_ARRAY_GET, // get the value of an array
        STATIC_FIELD, // get the field value
        DYNAMIC_FIELD, // get the field value
        MANUALLY // handled manually
    }
}
