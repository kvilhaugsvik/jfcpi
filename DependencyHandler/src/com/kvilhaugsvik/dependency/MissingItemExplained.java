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

/**
 * How and why an item wasn't resolved.
 */
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

    /**
     * Recursively get all missing items as requirements
     * @return a collection containing all missing requirements
     */
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

    /**
     * How a MissingItemExplained blames another for not being resolved.
     */
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

    /**
     * How wasn't the item resolved?
     */
    public enum Status {
        /**
         * The item isn't there.
         */
        IS_MISSING,

        /**
         * The item is there but another problem makes it useless
         */
        EXIST_BUT
    }

    /**
     * In what way did the MissingItemExplained depend on the other?
     */
    public enum Relation {
        /**
         * It required the other item directly
         */
        DEPEND_ON,

        /**
         * It could have been made from the other item
         */
        MADE_FROM,

        /**
         * It blames the other item for some other reason
         */
        BLAMED_ON
    }
}
