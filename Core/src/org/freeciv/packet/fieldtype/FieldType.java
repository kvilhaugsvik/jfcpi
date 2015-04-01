/*
 * Copyright (c) 2011. Sveinung Kvilhaugsvik
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

package org.freeciv.packet.fieldtype;

import java.io.DataOutput;
import java.io.IOException;

/**
 * A Freeciv network protocol packet field type.
 * It has a value and can serialize and deserialize it.
 * @param <Javatype> the type of the value the field type can serialize and deserialize.
 */
public interface FieldType<Javatype> {
    /**
     * Serialize the value to the given location.
     * @param to the location to serialize the value to.
     * @throws IOException when something goes wrong during serialization.
     */
    void encodeTo(DataOutput to) throws IOException;

    /**
     * Get the size of the serialized value in bytes.
     * @param previous the previous field.
     * @return the size of the serialized value in bytes.
     */
    int encodedLength(FieldType previous);

    /**
     * Get the value of the field.
     * @return the value of the field.
     */
    Javatype getValue();
}
