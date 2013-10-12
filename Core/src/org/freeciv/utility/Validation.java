/*
 * Copyright (c) 2013 Sveinung Kvilhaugsvik.
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

package org.freeciv.utility;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Validation {
    public static void validateNotNull(Object var, String name) {
        if (null == var)
            throw new IllegalArgumentException("No " + name);
    }

    public static void validateMethodIsAConverter(Method converter, Class<?> returns, Class<?> argumentType) {
        if (!returns.isAssignableFrom(converter.getReturnType()))
            throw new IllegalArgumentException(converter + " doesn't convert to a " + returns);

        final Class<?>[] params = converter.getParameterTypes();
        if (1 != params.length)
            throw new IllegalArgumentException(converter + " takes the wrong number of parameters");
        if (!params[0].isAssignableFrom(argumentType))
            throw new IllegalArgumentException(converter + " doesn't convert from a " + argumentType);
    }

    public static void validateMethodIsStatic(Method method) {
        if (!Modifier.isStatic(method.getModifiers()))
            throw new IllegalArgumentException(method + " isn't static");
    }
}
