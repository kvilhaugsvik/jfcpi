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

import java.util.regex.Pattern;

public class TargetArray extends TargetClass {
    private final TargetClass of;
    private final int dimensions;

    private TargetArray(Class wrapped) {
        super(wrapped, true);

        if (!wrapped.isArray())
            throw new IllegalArgumentException("Not an array");

        this.of = newKnown(wrapped.getComponentType());

        lookForDimensions(wrapped.getComponentType().getName());

        this.dimensions = 1;

        registerBuiltIn();
    }

    private TargetArray(TargetClass wrapped, int levels) {
        super(wrapped.getName() + Strings.repeat("[]", levels), true);
        this.of = wrapped;

        if (levels < 1)
            throw new IllegalArgumentException("Not an array");

        if (!(wrapped instanceof TargetArray))
            lookForDimensions(wrapped.getName());

        this.dimensions = levels + (wrapped instanceof TargetArray ? ((TargetArray)wrapped).dimensions : 0);

        registerBuiltIn();
    }

    private TargetArray(String wrapped, int levels) {
        super(wrapped + Strings.repeat("[]", levels), true);
        this.of = new TargetClass(wrapped, true);

        if (levels < 1)
            throw new IllegalArgumentException("Not an array");

        lookForDimensions(wrapped);

        this.dimensions = levels;

        registerBuiltIn();
    }

    private void registerBuiltIn() {
        register(new TargetMethod(this, "[]", of, TargetMethod.Called.DYNAMIC_ARRAY_GET));
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
        return new TargetArray((TargetPackage.TOP_LEVEL_AS_STRING.equals(inPacket) ? "" : inPacket + ".") + className, levels);
    }
}
