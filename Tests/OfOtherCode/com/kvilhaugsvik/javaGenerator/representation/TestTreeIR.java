/*
 * Copyright (c) 2013. Sveinung Kvilhaugsvik
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

package com.kvilhaugsvik.javaGenerator.representation;

import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestTreeIR {
    @Test public void ir_noChildren() {
        IR top = new IR(HasAtoms.HAS);

        assertFalse(top.getChildren().hasNext());
    }

    @Test public void ir_aChild() {
        IR dad = new IR(HasAtoms.HAS);
        dad.addChild(new IR(HasAtoms.ADD));

        Iterator<IR> childrenOfParent = dad.getChildren();
        assertTrue("No child", childrenOfParent.hasNext());

        IR child = childrenOfParent.next();
        assertEquals("Wrong child", HasAtoms.ADD, child.getAtom());

        assertFalse("To many children", childrenOfParent.hasNext());
        assertFalse("No grand children", child.getChildren().hasNext());
    }

    @Test public void ir_twoChildren() {
        IR dad = new IR(HasAtoms.HAS);
        dad.addChild(new IR(HasAtoms.ADD));
        dad.addChild(new IR(HasAtoms.SUB));

        final Iterator<IR> irs = dad.getChildren();
        assertEquals(HasAtoms.ADD, irs.next().getAtom());
        assertEquals(HasAtoms.SUB, irs.next().getAtom());
    }

    @Test public void ir_aGrandChild() {
        IR dad = new IR(HasAtoms.HAS);
        IR son = new IR(HasAtoms.ADD);
        dad.addChild(son);
        son.addChild(new IR(HasAtoms.AND));

        assertEquals(son, dad.getChildren().next());
        assertEquals(HasAtoms.AND, son.getChildren().next().getAtom());
    }

    @Test public void ir_3GenerationsSameAtom() {
        IR dad = new IR(HasAtoms.HAS);
        IR son = new IR(HasAtoms.HAS);
        dad.addChild(son);
        son.addChild(new IR(HasAtoms.HAS));

        assertEquals(HasAtoms.HAS, dad.getAtom());
        assertEquals(HasAtoms.HAS, dad.getChildren().next().getAtom());
        assertEquals(HasAtoms.HAS, dad.getChildren().next().getChildren().next().getAtom());
    }

    @Test public void ir_grandChildrenDifferentParents() {
        IR dad = new IR(HasAtoms.HAS);

        IR son = new IR(HasAtoms.ADD);
        dad.addChild(son);
        son.addChild(new IR(HasAtoms.AND));

        IR daughter = new IR(HasAtoms.DIV);
        dad.addChild(daughter);
        daughter.addChild(new IR(HasAtoms.EOL));
        daughter.addChild(new IR(HasAtoms.MUL));

        final Iterator<IR> irsDad = dad.getChildren();
        assertEquals(son, irsDad.next());
        assertEquals(daughter, irsDad.next());

        final Iterator<IR> irsSon = son.getChildren();
        assertEquals(HasAtoms.AND, irsSon.next().getAtom());

        final Iterator<IR> irsDaughter = daughter.getChildren();
        assertEquals(HasAtoms.EOL, irsDaughter.next().getAtom());
        assertEquals(HasAtoms.MUL, irsDaughter.next().getAtom());
    }
}
