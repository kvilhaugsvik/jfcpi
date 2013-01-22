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

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class TestPosition {
    @Test
    public void creation_first_isNotOtherFirst() {
        Position first = Position.first();
        assertNotSame("Different chains should have different beginning", first, Position.first());
    }

    @Test
    public void creation_next_isSame() {
        Position first = Position.first();
        assertSame("Should not create a new next position when next already exist", first.next(), first.next());
    }

    @Test
    public void creation_previousOfNext_isSame() {
        Position first = Position.first();
        assertSame("The next value's previous value should be the start", first, first.next().previous());
    }

    @Test(expected = NoSuchElementException.class)
    public void creation_firstHasNoPrevious() {
        Position.first().previous();
    }

    @Test
    public void use_unused_isUnused() {
        Position first = Position.first();
        assertFalse("The position shouldn't be in use", first.isUsed());
    }

    @Test
    public void use_used_isUsed() {
        Position first = Position.first();
        first.use(new IR(HasAtoms.ADD));
        assertTrue("The position should be in use", first.isUsed());
    }

    @Test(expected = IllegalStateException.class)
    public void use_canNotUseTwice() {
        Position first = Position.first();
        first.use(new IR(HasAtoms.ADD));
        first.use(new IR(HasAtoms.SUB));
    }

    @Test(expected = NoSuchElementException.class)
    public void use_unUsedHasNothingToReturn() {
        Position first = Position.first();
        first.get();
    }

    @Test
    public void use_used_returnsIR() {
        Position first = Position.first();
        IR ir = new IR(HasAtoms.ADD);
        first.use(ir);
        assertEquals("Should be what was put in", ir, first.get());
    }
}
