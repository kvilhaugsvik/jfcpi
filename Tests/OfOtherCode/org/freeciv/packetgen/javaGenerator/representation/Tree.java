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

package org.freeciv.packetgen.javaGenerator.representation;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class Tree {
    @Test public void ir_flatten_noChildren() {
        IR top = new IR(HasAtoms.HAS);

        final List<IR> irs = top.flattenTree();
        assertEquals(HasAtoms.HAS, irs.get(0).getAtom());
        assertEquals(1, irs.size());
    }

    @Test public void ir_flatten_aChild() {
        IR dad = new IR(HasAtoms.HAS);
        dad.addChild(new IR(HasAtoms.ADD));

        final List<IR> irs = dad.flattenTree();
        assertEquals(HasAtoms.HAS, irs.get(0).getAtom());
        assertEquals(HasAtoms.ADD, irs.get(1).getAtom());
        assertEquals(2, irs.size());
    }

    @Test public void ir_flatten_twoChildren() {
        IR dad = new IR(HasAtoms.HAS);
        dad.addChild(new IR(HasAtoms.ADD));
        dad.addChild(new IR(HasAtoms.SUB));

        final List<IR> irs = dad.flattenTree();
        assertEquals(HasAtoms.HAS, irs.get(0).getAtom());
        assertEquals(HasAtoms.ADD, irs.get(1).getAtom());
        assertEquals(HasAtoms.SUB, irs.get(2).getAtom());
        assertEquals(3, irs.size());
    }

    @Test public void ir_flatten_aGrandChild() {
        IR dad = new IR(HasAtoms.HAS);
        IR son = new IR(HasAtoms.ADD);
        dad.addChild(son);
        son.addChild(new IR(HasAtoms.AND));

        final List<IR> irsDad = dad.flattenTree();
        assertEquals(HasAtoms.HAS, irsDad.get(0).getAtom());
        assertEquals(HasAtoms.ADD, irsDad.get(1).getAtom());
        assertEquals(HasAtoms.AND, irsDad.get(2).getAtom());
        assertEquals(3, irsDad.size());

        final List<IR> irsSon = son.flattenTree();
        assertEquals(HasAtoms.ADD, irsSon.get(0).getAtom());
        assertEquals(HasAtoms.AND, irsSon.get(1).getAtom());
        assertEquals(2, irsSon.size());
    }

    @Test public void ir_flatten_3GenerationsSameAtom() {
        IR dad = new IR(HasAtoms.HAS);
        IR son = new IR(HasAtoms.HAS);
        dad.addChild(son);
        son.addChild(new IR(HasAtoms.HAS));

        final List<IR> irsDad = dad.flattenTree();
        assertEquals(HasAtoms.HAS, irsDad.get(0).getAtom());
        assertEquals(HasAtoms.HAS, irsDad.get(1).getAtom());
        assertEquals(HasAtoms.HAS, irsDad.get(2).getAtom());
        assertEquals(3, irsDad.size());
    }

    @Test public void ir_flatten_grandChildrenDifferentParents() {
        IR dad = new IR(HasAtoms.HAS);

        IR son = new IR(HasAtoms.ADD);
        dad.addChild(son);
        son.addChild(new IR(HasAtoms.AND));

        IR daughter = new IR(HasAtoms.DIV);
        dad.addChild(daughter);
        daughter.addChild(new IR(HasAtoms.EOL));
        daughter.addChild(new IR(HasAtoms.MUL));

        final List<IR> irsDad = dad.flattenTree();
        assertEquals(HasAtoms.HAS, irsDad.get(0).getAtom());
        assertEquals(HasAtoms.ADD, irsDad.get(1).getAtom());
        assertEquals(HasAtoms.AND, irsDad.get(2).getAtom());
        assertEquals(HasAtoms.DIV, irsDad.get(3).getAtom());
        assertEquals(HasAtoms.EOL, irsDad.get(4).getAtom());
        assertEquals(HasAtoms.MUL, irsDad.get(5).getAtom());
        assertEquals(6, irsDad.size());

        final List<IR> irsSon = son.flattenTree();
        assertEquals(HasAtoms.ADD, irsSon.get(0).getAtom());
        assertEquals(HasAtoms.AND, irsSon.get(1).getAtom());
        assertEquals(2, irsSon.size());

        final List<IR> irsDaughter = daughter.flattenTree();
        assertEquals(HasAtoms.DIV, irsDaughter.get(0).getAtom());
        assertEquals(HasAtoms.EOL, irsDaughter.get(1).getAtom());
        assertEquals(HasAtoms.MUL, irsDaughter.get(2).getAtom());
        assertEquals(3, irsDaughter.size());
    }
}
