/*
 * Copyright (c) 2014. Sveinung Kvilhaugsvik
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

package org.freeciv.packetgen.enteties.supporting;

import com.kvilhaugsvik.javaGenerator.TargetClass;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.typeBridge.Value;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AValue;

/**
 * A simple DataType. Not able to do anything fancy like generating a field type for the type it wraps.
 */
public class SimpleJavaType implements DataType {
    private final TargetClass wrapped;
    private final Value zero;

    /**
     * Constructor from the wrapped type given as a {@see com.kvilhaugsvik.javaGenerator.TargetClass}
     * @param wrapped the type to wrap
     * @param zero the zero value of this data type
     */
    public SimpleJavaType(TargetClass wrapped, Typed<? extends AValue> zero) {
        this.wrapped = wrapped;
        this.zero = wrapped.newInstance(zero);
    }

    /**
     * Constructor from the wrapped type given as a {@see java.lang.Class}
     * @param wrapped the type to wrap
     * @param zero the zero value of this data type
     */
    public SimpleJavaType(Class wrapped, Typed<? extends AValue> zero) {
        this(TargetClass.from(wrapped), zero);
    }

    @Override
    public TargetClass getAddress() {
        return this.wrapped;
    }

    @Override
    public Value getZeroValue() {
        return zero;
    }
}
