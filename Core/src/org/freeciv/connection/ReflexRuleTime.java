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
 * A situation a ReflexRule applies to.
 */
public enum ReflexRuleTime {
    /**
     * This rule may fire after a packet is sent.
     */
    POST_SEND,

    /**
     * This rule may fire after a packet is received.
     */
    POST_RECEIVE
}
