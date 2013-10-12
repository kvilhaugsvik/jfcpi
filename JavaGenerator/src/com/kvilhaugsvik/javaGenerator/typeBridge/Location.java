/*
 * Copyright (c) 2013. Sveinung Kvilhaugsvik
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

/**
 * A location that can be written to
 * @param <Kind> the kind of value this location can hold
 */
public interface Location<Kind extends AValue> extends Value<Kind> {
    public Value<Kind> assign(final Value<Kind> value);
}
