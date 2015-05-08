/*
 * Copyright (c) 2013 - 2015. Sveinung Kvilhaugsvik
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

package org.freeciv.connection;

/**
 * A rule specifying what to do when dealing with a specified packet kind
 * in a certain situation.
 * The rule has three parts: two conditions and the action to take if they
 * are true.
 * The first condition is the situation when the rule applies like after a
 * packet is received.
 * The second condition is the packet number of the kind of packets it
 * applies to.
 * When the rule's action is performed the rule is said to fire.
 * Both conditions must be true before the rule fires.
 * @param <WorksOn> the connectionish thing the action is done to.
 */
public class ReflexRule<WorksOn extends ConnectionRelated> {
    private final ReflexRuleTime when;
    private final int number;
    private final ReflexReaction<WorksOn> action;

    /**
     * Creates a new rule of what to to when a certein packet kind is
     * encountered in a certain situation.
     * @param when the situation the rule applies to.
     * @param number the packet number of the kind of packets that triggers
     *               the rule.
     * @param action the action to take when the rule is triggered.
     */
    public ReflexRule(ReflexRuleTime when, int number, ReflexReaction<WorksOn> action) {
        this.when = when;
        this.number = number;
        this.action = action;
    }

    /**
     * The kind of situation where the rule may fire.
     * @return the situation the rule applies to.
     */
    public ReflexRuleTime getWhen() {
        return when;
    }

    /**
     * The number of the packet kind that makes the rule fire.
     * @return the packet kind the rule applies to.
     */
    public int getNumber() {
        return number;
    }

    /**
     * Get the action to perform when the packet kind is dealt with in the
     * situation the rule applies to.
     * @return the action to perform when the rule fires.
     */
    public ReflexReaction<WorksOn> getAction() {
        return action;
    }
}
