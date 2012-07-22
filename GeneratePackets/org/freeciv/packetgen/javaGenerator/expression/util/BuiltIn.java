/*
 * Copyright (c) 2011, 2012. Sveinung Kvilhaugsvik
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

package org.freeciv.packetgen.javaGenerator.expression.util;

import org.freeciv.packetgen.javaGenerator.expression.creators.*;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.*;
import static org.freeciv.packetgen.javaGenerator.expression.util.WrapCodeString.*;

public class BuiltIn {
    public static final ExprFrom1<NoValue, AValue> RETURN = new ExprFrom1<NoValue, AValue>() {
        @Override
        public NoValue getCodeFor(AValue arg1) {
            return asVoid("return " + arg1);
        }
    };
}
