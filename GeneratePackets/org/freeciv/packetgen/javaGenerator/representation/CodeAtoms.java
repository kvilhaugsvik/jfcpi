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

package org.freeciv.packetgen.javaGenerator.representation;

import org.freeciv.Util;
import org.freeciv.packetgen.javaGenerator.representation.IR.CodeAtom;

import java.util.*;

public class CodeAtoms {
    //TODO: Rename atoms
    private final LinkedList<IR> atoms;
    LinkedList<RewriteRule> rewrites;
    private List<String> onNext;

    public CodeAtoms(HasAtoms... start) {
        atoms = new LinkedList<IR>();
        rewrites = new LinkedList<RewriteRule>();
        onNext = new LinkedList<String>();

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
        for (String hint : onNext)
            atoms.get(atomsAtTheBeginning).hintBegin(hint);
        onNext = new LinkedList<String>();
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

    public void childrenFollows(CodeAtom parent) {
        // TODO: Implement for real
        level.add(parent);
        add(parent);
    }

    private LinkedList<CodeAtom> level = new LinkedList<CodeAtom>();
    public void childrenAdded(CodeAtom parent) {
        // TODO: Implement for real
        if (level.size() - 1 < 0)
            throw new IllegalStateException("Wasn't writing children");
        if (level.peekLast().equals(parent))
            level.removeLast();
        else
            throw new IllegalArgumentException("Writing children of " + level.peekLast().get() +
                    " but asked to stop writing children of " + parent.get());
    }

    public void hintStart(String name) {
        onNext.add(name);
    }

    public void hintEnd(String name) {
        assert !atoms.isEmpty() : "Tried to end a hint before an element was added";
        assert onNext.isEmpty() : "Can't add hint after accepting hints for next element";

        atoms.peekLast().hintEnd(name);
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

        if (0 != level.size())
            throw new IllegalStateException("Not finished writing children");

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
            return HasAtoms.BLANK;
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
