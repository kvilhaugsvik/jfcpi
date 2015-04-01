/*
 * Copyright (c) 2012 - 2014. Sveinung Kvilhaugsvik
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

import java.util.*;

public interface Dependency {
    /**
     * An Item can depend on other items and have other items depend on it.
     */
    interface Item extends Dependency {
        /**
         * Other items that this item requires.
         * @return the required items.
         */
        Collection<Requirement> getReqs();

        /**
         * The requirement this item fulfills.
         * @return the requirement this item fulfills.
         */
        Requirement getIFulfillReq();
    }

    /**
     * A Maker can produce a {@link com.kvilhaugsvik.dependency.Dependency.Item} from other items. A Maker may be
     * limited to produce a specific item. It may also produce different items depending on the input.
     */
    interface Maker extends Dependency {
        /**
         * Get a list of the items needed to produce the requested item. The required items should be given as
         * arguments to the {@link #produce(Requirement, com.kvilhaugsvik.dependency.Dependency.Item...)} method in the
         * same order as they were requested.
         *
         * @param toProduce the wanted item.
         * @return a list of the items needed to produce the wanted item.
         */
        List<Requirement> neededInput(Requirement toProduce);

        /**
         * Get a Required that matches each Requirement this Maker can produce. If a Requirement matches the method
         * {@link #neededInput(Requirement)} should give a list of items required to produce it.
         * @return a Required that matches each Requirement this Maker can produce.
         */
        Required getICanProduceReq();

        /**
         * Produce the requested item from the input items.
         * @param toProduce the Requirement of the item that should be produced
         * @param wasRequired the items required to produce it in the order given by {@link #neededInput(Requirement)}
         * @return the requested Dependency.Item.
         * @throws UndefinedException when the requested item couldn't be created.
         */
        Dependency.Item produce(Requirement toProduce, Dependency.Item... wasRequired) throws UndefinedException;
    }

    /**
     * This Maker has (potentially unreliable) information about why a requirement is missing.
     *
     * Limited to makers for now since they are more likely to have reliable information.
     */
    interface BlameShifter extends Maker {
        /**
         * Collection of what unresolved requirements the BlameShifter suspects is to blame if a requirement is missing.
         * The information is only a suspicion / hint and could be wrong.
         *
         * @return a map mapping from the missing requirement to all blamed suspects
         */
        Map<Requirement, Collection<Requirement>> blameSuspects();
    }
}
