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

import org.junit.Test;

public class TestValidation {
    @Test(expected = IllegalArgumentException.class)
    public void notNull_wrong() {
        Validation.validateNotNull(null, "var");
    }

    @Test
    public void notNull_right() {
        Validation.validateNotNull("not null", "var");
    }

    @Test(expected = IllegalArgumentException.class)
    public void methodIsStatic_wrong() throws NoSuchMethodException {
        Validation.validateMethodIsStatic(Object.class.getMethod("toString"));
    }

    @Test
    public void methodIsStatic_right() throws NoSuchMethodException {
        Validation.validateMethodIsStatic(Integer.class.getMethod("getInteger", String.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void methodIsAConverter_returnType_wrong() throws NoSuchMethodException {
        Validation.validateMethodIsAConverter(Integer.class.getMethod("getInteger", String.class), String.class, String.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void methodIsAConverter_numberOfParams_wrong() throws NoSuchMethodException {
        Validation.validateMethodIsAConverter(Integer.class.getMethod("getInteger", String.class, int.class), Integer.class, String.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void methodIsAConverter_paramType_wrong() throws NoSuchMethodException {
        Validation.validateMethodIsAConverter(Integer.class.getMethod("getInteger", String.class), Integer.class, Integer.class);
    }

    @Test
    public void methodIsAConverter_right() throws NoSuchMethodException {
        Validation.validateMethodIsAConverter(Integer.class.getMethod("getInteger", String.class), Integer.class, String.class);
    }
}
