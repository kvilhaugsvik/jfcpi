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

package com.kvilhaugsvik.javaGenerator.typeBridge;

import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AValue;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.Returnable;

/**
 * Code typed with a value (not void)
 * @param <Kind> the kind of value
 */
public interface Value<Kind extends AValue> extends Typed<Kind> {
    /**
     * Call a method on the value. Will throw an IllegalArgumentException if the method don't exist.
     * @param method Name of the method to call
     * @param params The parameters to pass in the call. The value it self should be the first parameter.
     * @param <Ret> The return type of the call
     * @return Typed code that calls the method on the value giving it the parameters.
     */
    <Ret extends Returnable> Typed<Ret> call(String method, Typed<? extends AValue>... params);

    /**
     * Call a method that returns a Value on the value.
     * @param method Name of the method to call
     * @param params The parameters to pass in the call. The value it self should be the first parameter.
     * @param <Ret> The return type of the call
     * @return Typed code that calls the method on the value giving it the parameters.
     */
    <Ret extends AValue> Value<Ret> callV(String method, Typed<? extends AValue>... params);
}
