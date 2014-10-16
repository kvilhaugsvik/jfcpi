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

package org.freeciv.packetgen.enteties.supporting;

import com.kvilhaugsvik.javaGenerator.TargetClass;
import com.kvilhaugsvik.dependency.ReqKind;
import com.kvilhaugsvik.javaGenerator.typeBridge.Value;

/**
 * Represents a data type. The data type may be generated (like enums, bit vectors etc) or pre existing.
 */
public interface DataType extends ReqKind {
    /**
     * Get the address of the type it self.
     * @return the represented type.
     */
    public TargetClass getAddress();

    /**
     * Get the zero value of this data type.
     * @return the zero value of the data type.
     */
    public Value getZeroValue();
}
