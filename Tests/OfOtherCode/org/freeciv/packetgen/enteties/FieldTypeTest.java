/*
 * Copyright (c) 2012. Sveinung Kvilhaugsvik
 * Portions are data from Freeciv's common/packets.def. Copyright
 * of those (if copyrightable) belong to their respective copyright
 * holders.
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

package org.freeciv.packetgen.enteties;

import org.freeciv.packetgen.dependency.Requirement;
import com.kvilhaugsvik.javaGenerator.TargetClass;
import com.kvilhaugsvik.javaGenerator.Var;
import com.kvilhaugsvik.javaGenerator.Block;
import com.kvilhaugsvik.javaGenerator.typeBridge.From1;
import com.kvilhaugsvik.javaGenerator.typeBridge.From2;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AnInt;
import org.junit.Test;

import java.util.Collections;

import static com.kvilhaugsvik.javaGenerator.util.BuiltIn.TO_STRING_OBJECT;
import static org.junit.Assert.*;

public class FieldTypeTest {
    @Test
    public void fieldTypeAliasAlias() {
        FieldTypeBasic.FieldTypeAlias base = simpleBasicFT().createFieldType("base");
        FieldTypeBasic.FieldTypeAlias alias = base.aliasUnseenToCode("alias");

        assertFalse("Requirement fulfillment names should differ",
                base.getIFulfillReq().getName().equals(alias.getIFulfillReq().getName()));

        assertEquals("Different names in code between base and alias", base.getName(), alias.getName());
        assertEquals("Different code generated for base and alias", base.toString(), alias.toString());
    }

    private static FieldTypeBasic simpleBasicFT() {
        return new FieldTypeBasic("net", "c", TargetClass.fromClass(Integer.class),
                new From1<Block, Var>() {
                    @Override
                    public Block x(Var arg1) {
                        return new Block();
                    }
                },
                new From2<Block, Var, Var>() {
                    @Override
                    public Block x(Var out, Var inn) {
                        return new Block();
                    }
                },
                new From2<Block, Var, Var>() {
                    @Override
                    public Block x(Var value, Var to) {
                        return new Block();
                    }
                },
                new From1<Typed<AnInt>, Var>() {
                    @Override
                    public Typed<AnInt> x(Var arg1) {
                        return BuiltIn.literal(1);
                    }
                },
                TO_STRING_OBJECT,
                false, Collections.<Requirement>emptySet());
    }
}
