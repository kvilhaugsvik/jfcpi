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

package org.freeciv.packet.fieldtype;

/**
 * The maximum number of elements the field can have this time is claimed to
 * be larger than the maximum number of elements the field ever can have.
 */
public class IllegalLimitSizeException extends FieldTypeException {
    /**
     * Constructor that sets the exception's error message.
     * @param message the error message.
     */
    public IllegalLimitSizeException(String message) {
        super(message);
    }
}
