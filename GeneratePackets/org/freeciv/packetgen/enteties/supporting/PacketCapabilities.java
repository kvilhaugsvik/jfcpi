/*
 * Copyright (c) 2013, Sveinung Kvilhaugsvik
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

import com.kvilhaugsvik.javaGenerator.Annotate;
import com.kvilhaugsvik.javaGenerator.expression.ArrayLiteral;
import com.kvilhaugsvik.javaGenerator.expression.Reference;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AString;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AValue;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import org.freeciv.packet.Capabilities;

import java.util.SortedSet;

public class PacketCapabilities extends Annotate {
    public PacketCapabilities(SortedSet<String> caps) {
        super(Capabilities.class, Reference.SetTo.strToVal("value", formatArray(caps)));
    }

    public static Typed<AValue> formatArray(SortedSet<String> caps) {
        Typed<AString>[] usedCaps = new Typed[caps.size()];

        int i = 0;
        for (String cap : caps) {
            usedCaps[i] = BuiltIn.literal(cap);
            i++;
        }

        return new ArrayLiteral(usedCaps);
    }
}
