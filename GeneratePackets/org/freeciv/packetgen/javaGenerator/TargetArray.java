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

public class TargetArray extends TargetClass {
    private final TargetClass of;

    public TargetArray(Class wrapped, boolean isInScope) {
        super(wrapped, isInScope);
        this.of = new TargetClass(wrapped.getComponentType(), isInScope);
    }

    public TargetArray(TargetClass wrapped, int levels, boolean isInScope) {
        super(wrapped.getName() + Strings.repeat("[]", levels), isInScope);
        this.of = wrapped;
    }

    public TargetArray(String wrapped, int levels, boolean isInScope) {
        super(wrapped + Strings.repeat("[]", levels), isInScope);
        this.of = new TargetClass(wrapped, isInScope);
    }

    @Override
    public MethodCall.HasResult<AValue> newInstance(Typed<? extends AValue>... parameterList) {
        return new MethodCall.HasResult<AValue>(TargetMethod.Called.STATIC_ARRAY_INST, this, getNewMethod(of), parameterList);
    }

    public TargetClass getOf() {
        return of;
    }
}
