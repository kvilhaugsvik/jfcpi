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

package com.kvilhaugsvik.javaGenerator.util;

import com.kvilhaugsvik.javaGenerator.TargetClass;
import com.kvilhaugsvik.javaGenerator.typeBridge.Value;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AValue;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.Returnable;

public class ValueHelper {
    private final TargetClass type;
    private final Typed<? extends AValue> value;

    public ValueHelper(TargetClass type, Typed<? extends AValue> value) {
        this.type = type;
        this.value = value;
    }

    public <Ret extends Returnable> Typed<Ret> call(String method, Typed<? extends AValue>... params) {
        return type.<Ret>call(method, addSelfAsFirstParams(params));
    }

    public <Ret extends AValue> Value<Ret> callV(String method, Typed<? extends AValue>... params) {
        return type.<Ret>callV(method, addSelfAsFirstParams(params));
    }

    private Typed<? extends AValue>[] addSelfAsFirstParams(Typed<? extends AValue>... params) {
        Typed<? extends AValue>[] allParams = new Typed[params.length + 1];
        allParams[0] = value;
        System.arraycopy(params, 0, allParams, 1, params.length);
        return allParams;
    }
}
