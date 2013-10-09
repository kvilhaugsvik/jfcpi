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

package org.freeciv.connection;

public class ReflexRule {
    private final ReflexRuleTime when;
    private final int number;
    private final ReflexReaction action;

    public ReflexRule(ReflexRuleTime when, int number, ReflexReaction action) {
        this.when = when;
        this.number = number;
        this.action = action;
    }

    public ReflexRuleTime getWhen() {
        return when;
    }

    public int getNumber() {
        return number;
    }

    public ReflexReaction getAction() {
        return action;
    }
}
