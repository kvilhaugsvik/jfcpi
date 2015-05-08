/*
 * Copyright (c) 2012 - 2015. Sveinung Kvilhaugsvik
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
 * An action to take on something that resembles a Connection. Intended to
 * be used as a quick response in situations where waiting for more
 * advanced processing to finish may cause problems.
 * @param <Target> the connectionish thing the action is done to.
 */
public interface ReflexReaction<Target extends ConnectionRelated> {
    /**
     * Do the action to the specified target.
     * @param connection a connection (or something similar to it) to do
     *                   the action to.
     */
    void apply(Target connection);
}
