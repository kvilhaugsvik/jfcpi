/*
 * Copyright (c) 2012, Sveinung Kvilhaugsvik
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

import org.freeciv.packet.Sent;
import org.freeciv.packetgen.javaGenerator.Annotate;
import org.freeciv.packetgen.javaGenerator.Var;
import org.freeciv.packetgen.javaGenerator.typeBridge.Typed;
import org.freeciv.packetgen.javaGenerator.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.typeBridge.willReturn.AValue;

public class Sender extends Annotate {
    public Sender(int number) {
        super(Sent.class.getSimpleName(), Var.SetTo.strToVal("value", sender(number)));
    }

    private static Typed<AValue> sender(int number) {
        switch (number) {
            case 0:
                return BuiltIn.<AValue>toCode("Sent.From.UNKNOWN");
            case 1:
                return BuiltIn.<AValue>toCode("Sent.From.CLIENT");
            case 2:
                return BuiltIn.<AValue>toCode("Sent.From.SERVER");
            case 3:
                return BuiltIn.<AValue>toCode("Sent.From.BOTH");
        }
        throw new Error("This code should not be reachable");
    }
}
