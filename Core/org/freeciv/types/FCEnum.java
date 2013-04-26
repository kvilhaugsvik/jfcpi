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

package org.freeciv.types;

public interface FCEnum {
    public int getNumber();

    class Helper {
        public static <Flags extends FCEnum> Flags valueOfUnknownIsIllegal(int number, Flags[] values) {
            for (Flags element : values) {
                if (element.getNumber() == number) {
                    return element;
                }
            }
            throw new IllegalArgumentException(number + " not known");
        }
    }
}
