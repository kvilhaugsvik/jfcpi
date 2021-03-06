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

import org.freeciv.packet.Cancel;
import com.kvilhaugsvik.javaGenerator.*;
import com.kvilhaugsvik.javaGenerator.expression.ArrayLiteral;
import com.kvilhaugsvik.javaGenerator.expression.Reference;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AValue;

import java.util.List;

public class Canceler extends Annotate {
    public Canceler(List<String> canceled) {
        super(Cancel.class, Reference.SetTo.strToVal("value", formatArray(canceled)));
    }

    public static Typed<AValue> formatArray(List<String> canceled) {
        Typed<AValue>[] toCancel = new Typed[canceled.size()];

        for (int i = 0; i < canceled.size(); i++)
            toCancel[i] = (TargetClass.from("org.freeciv.packet", canceled.get(i))).callV("class");

        return new ArrayLiteral(toCancel);
    }
}
