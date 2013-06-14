/*
 * Copyright (c) 2012, Sveinung Kvilhaugsvik
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
import com.kvilhaugsvik.javaGenerator.representation.CodeAtoms;
import org.freeciv.utility.Strings;

import java.lang.reflect.Array;
import java.util.regex.Pattern;

public class TargetArray extends TargetClass {
    private final TargetClass of;
    private final int dimensions;

    private TargetArray(Class wrapped) {
        super(wrapped);

        if (!wrapped.isArray())
            throw new IllegalArgumentException("Not an array");

        this.of = TargetClass.from(wrapped.getComponentType());

        lookForDimensions(wrapped.getComponentType().getName());

        this.dimensions = 1;

        registerBuiltIn();
    }

    private TargetArray(TargetClass wrapped, int levels) {
        super(wrapped.getPackage().getFullAddress(), wrapped.getSimpleName() + Strings.repeat("[]", levels), ClassKind.CLASS);
        this.of = wrapped;

        if (null != wrapped.getRepresents())
            this.setRepresents(Array.newInstance(wrapped.getRepresents(), levels).getClass());

        if (levels < 1)
            throw new IllegalArgumentException("Not an array");

        if (!(wrapped instanceof TargetArray))
            lookForDimensions(wrapped.getSimpleName());

        this.dimensions = levels + (wrapped instanceof TargetArray ? ((TargetArray)wrapped).dimensions : 0);

        registerBuiltIn();
    }

    private TargetArray(String inPacket, String inClass, int levels) {
        super(inPacket, inClass + Strings.repeat("[]", levels), ClassKind.CLASS);
        this.of = TargetClass.from(inPacket, inClass);

        if (levels < 1)
            throw new IllegalArgumentException("Not an array");

        lookForDimensions(inClass);

        this.dimensions = levels;

        registerBuiltIn();
    }

    private void registerBuiltIn() {
        register(new TargetMethod(this, "[]", of, TargetMethod.Called.DYNAMIC_ARRAY_GET));
        register(new TargetMethod(this, "length", TargetClass.from(int.class), TargetMethod.Called.DYNAMIC_FIELD));
    }

    private static final Pattern chechName = Pattern.compile("\\[");
    private static void lookForDimensions(String wrapped) {
        if (chechName.matcher(wrapped).find())
            throw new IllegalArgumentException("Levels given in unsupported way");
    }

    @Override
    public MethodCall.HasResult<AValue> newInstance(Typed<? extends AValue>... parameterList) {
        if (dimensions < parameterList.length)
            throw new IllegalArgumentException("To many arguments when creating array instance");

        final int missing = dimensions - parameterList.length;

        return new MethodCall.HasResult<AValue>(TargetMethod.Called.STATIC_ARRAY_INST,
                this, getNewMethod(getDeepestOf()), parameterList) {
            @Override
            public void writeAtoms(CodeAtoms to) {
                super.writeAtoms(to);
                for (int i = 0; i < missing; i++) {
                    to.add(ARRAY_ACCESS_START);
                    to.add(ARRAY_ACCESS_END);
                }
            }
        };
    }

    public TargetClass getOf() {
        return of;
    }

    public TargetClass getDeepestOf() {
        if (of instanceof TargetArray)
            return ((TargetArray) of).getDeepestOf();
        else
            return of;
    }

    public static TargetArray from(TargetClass wrapped, int levels) {
        return new TargetArray(wrapped, levels);
    }

    public static TargetArray from(Class wrapped) {
        return new TargetArray(wrapped);
    }

    public static TargetArray from(String inPacket, String className, int levels) {
        return new TargetArray(inPacket, className, levels);
    }
}
