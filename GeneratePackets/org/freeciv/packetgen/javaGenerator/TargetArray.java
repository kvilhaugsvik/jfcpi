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

package org.freeciv.packetgen.javaGenerator;

import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;
import org.freeciv.utility.Strings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TargetArray extends TargetClass {
    private final TargetClass of;
    private final int dimensions;

    public TargetArray(Class wrapped, boolean isInScope) {
        super(wrapped, isInScope);

        if (!wrapped.isArray())
            throw new IllegalArgumentException("Not an array");

        this.of = new TargetClass(wrapped.getComponentType(), isInScope);

        lookForDimensions(wrapped.getComponentType().getName());

        this.dimensions = 1;
    }

    public TargetArray(TargetClass wrapped, int levels, boolean isInScope) {
        super(wrapped.getName() + Strings.repeat("[]", levels), isInScope);
        this.of = wrapped;

        if (levels < 1)
            throw new IllegalArgumentException("Not an array");

        if (!(wrapped instanceof TargetArray))
            lookForDimensions(wrapped.getName());

        this.dimensions = levels + (wrapped instanceof TargetArray ? ((TargetArray)wrapped).dimensions : 0);
    }

    public TargetArray(String wrapped, int levels, boolean isInScope) {
        super(wrapped + Strings.repeat("[]", levels), isInScope);
        this.of = new TargetClass(wrapped, isInScope);

        if (levels < 1)
            throw new IllegalArgumentException("Not an array");

        lookForDimensions(wrapped);

        this.dimensions = levels;
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
}
