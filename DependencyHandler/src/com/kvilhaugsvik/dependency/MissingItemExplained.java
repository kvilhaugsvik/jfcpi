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

package com.kvilhaugsvik.dependency;

import java.util.Collection;
import java.util.HashSet;

public class MissingItemExplained implements Comparable<MissingItemExplained> {
    private final Requirement missing;
    private final Status how;
    private final Collection<Blame> blames;

    public MissingItemExplained(Requirement missing, Status how, Collection<Blame> blames) {
        this.missing = missing;
        this.how = how;
        this.blames = blames;
    }

    @Override
    public String toString() {
        final StringBuilder out = new StringBuilder();
        out.append("(");
        out.append(missing.toString());
        out.append(" is ");
        out.append(how);

        if (!blames.isEmpty()) {
            out.append(" since");
            for (Blame blamed : blames) {
                out.append(" ");
                out.append(blamed.toString());
            }
        }

        out.append(")");

        return out.toString();
    }

    public Collection<? extends Requirement> toRequirements() {
        HashSet<Requirement> out = new HashSet<Requirement>();
        out.add(missing);
        for (Blame blamed : blames)
            out.addAll(blamed.problem.toRequirements());
        return out;
    }

    @Override
    public int compareTo(MissingItemExplained missingItemExplained) {
        // TODO: When claimed to be same check blames as well
        return missing.compareTo(missingItemExplained.missing);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof MissingItemExplained) {
            MissingItemExplained that = (MissingItemExplained)other;
            return this.missing.equals(that.missing);
        } else {
            return false;
        }
    }

    public static class Blame {
        private final MissingItemExplained problem;
        private final Relation what;

        public Blame(MissingItemExplained problem, Relation what) {
            this.problem = problem;
            this.what = what;
        }

        @Override
        public String toString() {
            return what + " " + problem;
        }
    }

    public enum Status {IS_MISSING, EXIST_BUT}
    public enum Relation {DEPEND_ON, MADE_FROM, BLAMED_ON}
}