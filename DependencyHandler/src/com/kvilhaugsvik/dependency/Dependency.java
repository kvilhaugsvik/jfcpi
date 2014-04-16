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
    public static interface Item extends Dependency {
        public Collection<Requirement> getReqs();
        public Requirement getIFulfillReq();
    }

    public static interface Maker extends Dependency {
        public List<Requirement> neededInput(Requirement toProduce);
        public Required getICanProduceReq();
        public Dependency.Item produce(Requirement toProduce, Dependency.Item... wasRequired) throws UndefinedException;
    }

    /**
     * This Maker has (potentially unreliable) information about why a requirement is missing.
     *
     * Limited to makers for now since they are more likely to have reliable information.
     */
    public static interface BlameShifter extends Maker {
        /**
         * Collection of what unresolved requirements the BlameShifter suspects is to blame if a requirement is missing.
         * The information is only a suspicion / hint and could be wrong.
         *
         * @return a map mapping from the missing requirement to all blamed suspects
         */
        public Map<Requirement, Collection<Requirement>> blameSuspects();
    }
}
