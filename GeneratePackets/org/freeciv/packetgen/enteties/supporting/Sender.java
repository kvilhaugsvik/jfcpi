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

import com.kvilhaugsvik.javaGenerator.TargetClass;
import org.freeciv.packet.Sent;
import com.kvilhaugsvik.javaGenerator.Annotate;
import com.kvilhaugsvik.javaGenerator.expression.Reference;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AValue;

public class Sender extends Annotate {
    public Sender(int number) {
        super(Sent.class, Reference.SetTo.strToVal("value", sender(number)));
    }

    private static Typed<AValue> sender(int number) {
        switch (number) {
            case 0:
                return TargetClass.from(Sent.From.class).callV("UNKNOWN");
            case 1:
                return TargetClass.from(Sent.From.class).callV("CLIENT");
            case 2:
                return TargetClass.from(Sent.From.class).callV("SERVER");
            case 3:
                return TargetClass.from(Sent.From.class).callV("BOTH");
        }
        throw new Error("This code should not be reachable");
    }
}
