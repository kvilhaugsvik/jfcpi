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
    LinkedList<RewriteRule> rewrites;
    private List<IR.Hint> onNext;

    public CodeAtoms(HasAtoms... start) {
        atoms = new LinkedList<IR>();
        rewrites = new LinkedList<RewriteRule>();
        onNext = new LinkedList<IR.Hint>();

        for (HasAtoms owner : start)
            owner.writeAtoms(this);
    }

    public void add(CodeAtom atom) {
        int atomsAtTheBeginning = atoms.size();

        addAtomOrRewrite(atom);
        cleanRewriteRules();

        if (atomsAtTheBeginning < atoms.size()) {
            hintsAddStoredBeginnings(atomsAtTheBeginning);
        }
    }

    private void hintsAddStoredBeginnings(int atomsAtTheBeginning) {
        for (IR.Hint hint : onNext)
            atoms.get(atomsAtTheBeginning).addHint(hint);
        onNext = new LinkedList<IR.Hint>();
    }

    private void addAtomOrRewrite(CodeAtom atom) {
        HasAtoms toAdd = atom;

        for (RewriteRule test : rewrites)
            if (test.isTrueFor(atom))
                toAdd = test.getReplacement();

        if (toAdd instanceof CodeAtom)
            atoms.add(new IR((CodeAtom)toAdd));
        else
            toAdd.writeAtoms(this);
    }

    private void cleanRewriteRules() {
        int elementToLookAt = 0;
        while (elementToLookAt < rewrites.size())
            if (rewrites.get(elementToLookAt).removeMeNow())
                rewrites.remove(elementToLookAt);
            else
                elementToLookAt++;
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
        rewrites.add(new RefuseNextIf(reason));
    }

    /**
     * Rewrite an incoming code atom using a HasAtoms when the trigger is active. If many triggers fire the newest one
     * will be used.
     * @param trigger The condition that must be true for the rule to trigger.
     * @param replacement the replacement for the atom
     */
    public void rewriteRule(Util.OneCondition<CodeAtom> trigger, HasAtoms replacement) {
        rewrites.add(new Rewrite(trigger, replacement));
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

    private interface RewriteRule {
        public boolean isTrueFor(CodeAtom atom);
        public HasAtoms getReplacement();
        public boolean removeMeNow();
    }

    private static class RefuseNextIf implements RewriteRule {
        private final Util.OneCondition<CodeAtom> reason;

        boolean used = false;

        private RefuseNextIf(Util.OneCondition<CodeAtom> reason) {
            this.reason = reason;
        }

        @Override
        public boolean isTrueFor(CodeAtom atom) {
            used = true;
            return reason.isTrueFor(atom);
        }

        @Override
        public HasAtoms getReplacement() {
            return new HasAtoms() {
                @Override
                public void writeAtoms(CodeAtoms to) {
                }
            };
        }

        @Override
        public boolean removeMeNow() {
            return used;
        }
    }

    private static class Rewrite implements RewriteRule {
        private final Util.OneCondition<CodeAtom> condition;
        private final HasAtoms replacement;

        private Rewrite(Util.OneCondition<CodeAtom> condition, HasAtoms replacement) {
            this.condition = condition;
            this.replacement = replacement;
        }

        @Override
        public boolean isTrueFor(CodeAtom atom) {
            return condition.isTrueFor(atom);
        }

        @Override
        public HasAtoms getReplacement() {
            return replacement;
        }

        @Override
        public boolean removeMeNow() {
            return false;
        }
    }
}
