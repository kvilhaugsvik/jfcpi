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

package com.kvilhaugsvik.javaGenerator;

import com.kvilhaugsvik.javaGenerator.expression.MethodCall;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AValue;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.Returnable;
import com.kvilhaugsvik.javaGenerator.representation.CodeAtoms;
import com.kvilhaugsvik.javaGenerator.representation.IR;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;

public class TargetMethod extends Address<TargetClass> {
    private final TargetClass returns;
    private final Called kind;

    public TargetMethod(TargetClass where, String named, TargetClass returns, Called kind) {
        super(where, Arrays.asList(new IR.CodeAtom(named)), Collections.<IR.CodeAtom>emptyList());
        this.returns = returns;
        this.kind = kind;
    }

    public TargetMethod(Method has) {
        this(TargetClass.from(has.getDeclaringClass()),
                has.getName(),
                TargetClass.from(has.getReturnType()),
                Modifier.isStatic(has.getModifiers()) ? Called.STATIC : Called.DYNAMIC);
    }

    public TargetMethod(Field has) {
        this(TargetClass.from(has.getDeclaringClass()),
                has.getName(),
                TargetClass.from(has.getType()),
                Modifier.isStatic(has.getModifiers()) ? Called.STATIC_FIELD : Called.DYNAMIC_FIELD);
    }

    public boolean isDynamic() {
        return Called.DYNAMIC.equals(kind);
    }

    public boolean returnsAValue() {
        return !void.class.getCanonicalName().equals(returns.getFullAddress());
    }

    public <Ret extends Returnable> MethodCall<Ret> call(Typed<? extends AValue>... parameters) {
        return new MethodCall<Ret>(kind, this, parameters);
    }

    public <Ret extends AValue> MethodCall.HasResult<Ret> callV(Typed<? extends AValue>... parameters) {
        if (void.class.getCanonicalName().equals(returns.getFullAddress()))
            throw new IllegalArgumentException(getSimpleName() + ": Wrong return type");

        return new MethodCall.HasResult<Ret>(kind, returns, this, parameters);
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        to.hintStart(TargetMethod.class.getCanonicalName());
        switch (kind) {
            case DYNAMIC:
            case DYNAMIC_ARRAY_GET:
            case DYNAMIC_FIELD:
                getTypedSimpleName().writeAtoms(to);
                break;
            case STATIC:
            case STATIC_FIELD:
            default:
                super.writeAtoms(to);
                break;
        }
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
