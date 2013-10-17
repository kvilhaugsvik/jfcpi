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

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class TestTreeCodeAtoms {
    @Test public void codeAtoms_children_sameAsFlat_oneChild() {
        CodeAtoms atoms = new CodeAtoms();

        IR.CodeAtom parent = new IR.CodeAtom("Father");
        IR.CodeAtom child = new IR.CodeAtom("Son");

        atoms.childrenFollows(parent);
        atoms.add(child);
        atoms.childrenAdded(parent);

        assertEquals(parent, atoms.toArray()[0].getAtom());
        assertEquals(child, atoms.toArray()[1].getAtom());
        assertEquals("Wrong size", 2, atoms.toArray().length);
    }

    @Test public void codeAtoms_children_sameAsFlat_oneChildAndOneGrandChild() {
        CodeAtoms atoms = new CodeAtoms();

        IR.CodeAtom grandparent = new IR.CodeAtom("Grand father");
        IR.CodeAtom parent = new IR.CodeAtom("Father");
        IR.CodeAtom child = new IR.CodeAtom("Son");

        atoms.childrenFollows(grandparent);
        atoms.childrenFollows(parent);
        atoms.add(child);
        atoms.childrenAdded(parent);
        atoms.childrenAdded(grandparent);

        assertEquals(grandparent, atoms.toArray()[0].getAtom());
        assertEquals(parent, atoms.toArray()[1].getAtom());
        assertEquals(child, atoms.toArray()[2].getAtom());
        assertEquals("Wrong size", 3, atoms.toArray().length);
    }

    @Test public void codeAtoms_children_sameAsFlat_oneChild_peerBefore() {
        CodeAtoms atoms = new CodeAtoms();

        IR.CodeAtom peer = new IR.CodeAtom("Spinster");

        IR.CodeAtom parent = new IR.CodeAtom("Father");
        IR.CodeAtom child = new IR.CodeAtom("Son");

        atoms.add(peer);
        atoms.childrenFollows(parent);
        atoms.add(child);
        atoms.childrenAdded(parent);

        assertEquals(peer, atoms.toArray()[0].getAtom());
        assertEquals(parent, atoms.toArray()[1].getAtom());
        assertEquals(child, atoms.toArray()[2].getAtom());
        assertEquals("Wrong size", 3, atoms.toArray().length);
    }

    @Test public void codeAtoms_children_sameAsFlat_oneChild_peerAfter() {
        CodeAtoms atoms = new CodeAtoms();

        IR.CodeAtom peer = new IR.CodeAtom("Spinster");

        IR.CodeAtom parent = new IR.CodeAtom("Father");
        IR.CodeAtom child = new IR.CodeAtom("Son");

        atoms.childrenFollows(parent);
        atoms.add(child);
        atoms.childrenAdded(parent);
        atoms.add(peer);

        assertEquals(parent, atoms.toArray()[0].getAtom());
        assertEquals(child, atoms.toArray()[1].getAtom());
        assertEquals(peer, atoms.toArray()[2].getAtom());
        assertEquals("Wrong size", 3, atoms.toArray().length);
    }

    @Test public void codeAtoms_children_sameAsFlat_oneChild_betweenPeers() {
        CodeAtoms atoms = new CodeAtoms();

        IR.CodeAtom peer = new IR.CodeAtom("Spinster");

        IR.CodeAtom parent = new IR.CodeAtom("Father");
        IR.CodeAtom child = new IR.CodeAtom("Son");

        atoms.add(peer);
        atoms.childrenFollows(parent);
        atoms.add(child);
        atoms.childrenAdded(parent);
        atoms.add(peer);

        assertEquals(peer, atoms.toArray()[0].getAtom());
        assertEquals(parent, atoms.toArray()[1].getAtom());
        assertEquals(child, atoms.toArray()[2].getAtom());
        assertEquals(peer, atoms.toArray()[3].getAtom());
        assertEquals("Wrong size", 4, atoms.toArray().length);
    }

    @Test public void codeAtoms_children_sameAsFlat_twoChildren() {
        CodeAtoms atoms = new CodeAtoms();

        IR.CodeAtom parent = new IR.CodeAtom("Father");
        IR.CodeAtom child = new IR.CodeAtom("Son");
        IR.CodeAtom child2 = new IR.CodeAtom("Daughter");

        atoms.childrenFollows(parent);
        atoms.add(child);
        atoms.add(child2);
        atoms.childrenAdded(parent);

        assertEquals(parent, atoms.toArray()[0].getAtom());
        assertEquals(child, atoms.toArray()[1].getAtom());
        assertEquals(child2, atoms.toArray()[2].getAtom());
        assertEquals("Wrong size", 3, atoms.toArray().length);
    }

    @Test public void codeAtoms_children_sameAsFlat_peersWithChildren() {
        CodeAtoms atoms = new CodeAtoms();

        IR.CodeAtom parent = new IR.CodeAtom("Father");
        IR.CodeAtom child = new IR.CodeAtom("Son");

        IR.CodeAtom parent2 = new IR.CodeAtom("Mother");
        IR.CodeAtom child2 = new IR.CodeAtom("Daughter");

        atoms.childrenFollows(parent);
        atoms.add(child);
        atoms.childrenAdded(parent);

        atoms.childrenFollows(parent2);
        atoms.add(child2);
        atoms.childrenAdded(parent2);

        assertEquals(parent, atoms.toArray()[0].getAtom());
        assertEquals(child, atoms.toArray()[1].getAtom());
        assertEquals(parent2, atoms.toArray()[2].getAtom());
        assertEquals(child2, atoms.toArray()[3].getAtom());
        assertEquals("Wrong size", 4, atoms.toArray().length);
    }

    @Test(expected = IllegalStateException.class)
    public void codeAtoms_children_wrongState_notClosed() {
        CodeAtoms atoms = new CodeAtoms();

        IR.CodeAtom parent = new IR.CodeAtom("Father");
        IR.CodeAtom child = new IR.CodeAtom("Son");

        atoms.childrenFollows(parent);
        atoms.add(child);

        atoms.toArray();
    }

    @Test(expected = IllegalStateException.class)
    public void codeAtoms_children_wrongState_closesNotOpened() {
        CodeAtoms atoms = new CodeAtoms();

        IR.CodeAtom parent = new IR.CodeAtom("Father");
        IR.CodeAtom child = new IR.CodeAtom("Son");

        atoms.childrenFollows(parent);
        atoms.add(child);
        atoms.childrenAdded(parent);
        atoms.childrenAdded(parent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void codeAtoms_children_wrongState_closedInTheWrongOrder() {
        CodeAtoms atoms = new CodeAtoms();

        IR.CodeAtom grandparent = new IR.CodeAtom("Grand father");
        IR.CodeAtom parent = new IR.CodeAtom("Father");
        IR.CodeAtom child = new IR.CodeAtom("Son");

        atoms.childrenFollows(grandparent);
        atoms.childrenFollows(parent);
        atoms.add(child);
        atoms.childrenAdded(grandparent);
        atoms.childrenAdded(parent);
    }

    @Test public void tree_single() {
        CodeAtoms atoms = new CodeAtoms();

        IR.CodeAtom alone = new IR.CodeAtom("Spinster");

        atoms.add(alone);

        Iterator<IR> iter = atoms.asTree();
        assertTrue(iter.hasNext());
        assertEquals(alone, iter.next().getAtom());
        assertFalse(iter.hasNext());
    }

    @Test public void tree_oneChild() {
        CodeAtoms atoms = new CodeAtoms();

        IR.CodeAtom parent = new IR.CodeAtom("Father");
        IR.CodeAtom child = new IR.CodeAtom("Son");

        atoms.childrenFollows(parent);
        atoms.add(child);
        atoms.childrenAdded(parent);

        Iterator<IR> inTop = atoms.asTree();
        assertTrue(inTop.hasNext());
        IR parentIR = inTop.next();
        assertEquals(parent, parentIR.getAtom());
        assertFalse(inTop.hasNext());

        Iterator<IR> inParent = parentIR.getChildren();
        assertTrue(inParent.hasNext());
        assertEquals(child, inParent.next().getAtom());
        assertFalse(inParent.hasNext());
    }

    @Test public void tree_oneChildAndOneGrandChild() {
        CodeAtoms atoms = new CodeAtoms();

        IR.CodeAtom grandparent = new IR.CodeAtom("Grand father");
        IR.CodeAtom parent = new IR.CodeAtom("Father");
        IR.CodeAtom child = new IR.CodeAtom("Son");

        atoms.childrenFollows(grandparent);
        atoms.childrenFollows(parent);
        atoms.add(child);
        atoms.childrenAdded(parent);
        atoms.childrenAdded(grandparent);

        IR gpIR = atoms.asTree().next();
        assertEquals(grandparent, gpIR.getAtom());

        IR pIR = gpIR.getChildren().next();
        assertEquals(parent, pIR.getAtom());

        assertEquals(child, pIR.getChildren().next().getAtom());
    }

    @Test public void tree_peer() {
        CodeAtoms atoms = new CodeAtoms();

        IR.CodeAtom peer = new IR.CodeAtom("Spinster");

        IR.CodeAtom parent = new IR.CodeAtom("Father");
        IR.CodeAtom child = new IR.CodeAtom("Son");

        atoms.add(peer);
        atoms.childrenFollows(parent);
        atoms.add(child);
        atoms.childrenAdded(parent);

        Iterator<IR> topIR = atoms.asTree();
        assertEquals(peer, topIR.next().getAtom());

        IR pIR = topIR.next();
        assertEquals(parent, pIR.getAtom());

        assertEquals(child, pIR.getChildren().next().getAtom());
    }

    @Test public void tree_twoChildren() {
        CodeAtoms atoms = new CodeAtoms();

        IR.CodeAtom parent = new IR.CodeAtom("Father");
        IR.CodeAtom child = new IR.CodeAtom("Son");
        IR.CodeAtom child2 = new IR.CodeAtom("Daughter");

        atoms.childrenFollows(parent);
        atoms.add(child);
        atoms.add(child2);
        atoms.childrenAdded(parent);

        IR pIR = atoms.asTree().next();
        assertEquals(parent, pIR.getAtom());

        Iterator<IR> children = pIR.getChildren();
        assertEquals(child, children.next().getAtom());
        assertEquals(child2, children.next().getAtom());
    }

    @Test public void tree_peersWithChildren() {
        CodeAtoms atoms = new CodeAtoms();

        IR.CodeAtom parent = new IR.CodeAtom("Father");
        IR.CodeAtom child = new IR.CodeAtom("Son");

        IR.CodeAtom parent2 = new IR.CodeAtom("Mother");
        IR.CodeAtom child2 = new IR.CodeAtom("Daughter");

        atoms.childrenFollows(parent);
        atoms.add(child);
        atoms.childrenAdded(parent);

        atoms.childrenFollows(parent2);
        atoms.add(child2);
        atoms.childrenAdded(parent2);

        Iterator<IR> topIR = atoms.asTree();

        IR pIR = topIR.next();
        assertEquals(parent, pIR.getAtom());
        assertEquals(child, pIR.getChildren().next().getAtom());

        IR p2IR = topIR.next();
        assertEquals(parent2, p2IR.getAtom());
        assertEquals(child2, p2IR.getChildren().next().getAtom());
    }
}
