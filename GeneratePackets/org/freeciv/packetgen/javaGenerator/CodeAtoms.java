/*
 * Copyright (c) 2012. Sveinung Kvilhaugsvik
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

package org.freeciv.packetgen.javaGenerator;

import org.freeciv.Util;
import org.freeciv.packetgen.javaGenerator.IR.CodeAtom;

import java.util.*;

public class CodeAtoms {
    //TODO: Rename atoms
    private final LinkedList<IR> atoms;
    private Util.OneCondition<CodeAtom> reason;
    private List<IR.Hint> onNext;

    public CodeAtoms(HasAtoms... start) {
        atoms = new LinkedList<IR>();
        reason = null;
        onNext = new LinkedList<IR.Hint>();

        for (HasAtoms owner : start)
            owner.writeAtoms(this);
    }

    public void add(CodeAtom atom) {
        if (null == reason || !reason.isTrueFor(atom))
            atoms.add(new IR(atom));
        reason = null;

        for (IR.Hint hint : onNext)
            atoms.peekLast().addHint(hint);
        onNext = new LinkedList<IR.Hint>();
    }

    public void hintStart(String name) {
        onNext.add(IR.Hint.begin(name));
    }

    public void hintEnd(String name) {
        assert !atoms.isEmpty() : "Tried to end a hint before an element was added";
        assert onNext.isEmpty() : "Can't add hint after accepting hints for next element";

        atoms.peekLast().addHint(IR.Hint.end(name));
    }

    public void refuseNextIf(Util.OneCondition<CodeAtom> reason) {
        this.reason = reason;
    }

    public IR get(int number) {
        return atoms.get(number);
    }

    public IR[] toArray() {
        assert onNext.isEmpty() : "Tried to read when the last element was half finished (start hints but no code)";
        return atoms.toArray(new IR[atoms.size()]);
    }

    public void joinSep(HasAtoms separator, Collection<? extends HasAtoms> toJoin) {
        joinSep(separator, toJoin.toArray(new HasAtoms[toJoin.size()]));
    }

    public void joinSep(HasAtoms separator, HasAtoms[] toJoin) {
        if (toJoin.length < 1)
            return;

        toJoin[0].writeAtoms(this);
        for (int index = 1; index < toJoin.length; index++) {
            separator.writeAtoms(this);
            toJoin[index].writeAtoms(this);
        }
    }
}
