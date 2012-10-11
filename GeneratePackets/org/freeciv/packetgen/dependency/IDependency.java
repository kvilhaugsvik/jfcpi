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

package org.freeciv.packetgen.dependency;

import org.freeciv.packetgen.UndefinedException;

import java.util.*;

public interface IDependency {
    public Collection<Requirement> getReqs();
    public Requirement getIFulfillReq();

    /**
     * Fulfill more than one thing.
     */
    public static interface ManyFulfiller extends IDependency {
        /**
         * Extra requirements fulfilled in addition to the primary fulfillment in getIFulfillReq.
         * @return the additional requirements fulfilled.
         */
        public Collection<Requirement> getIAlsoFulfillReqs();
    }

    public static interface Maker {
        public Collection<Requirement> getReqs();
        public Requirement getICanProduceReq();
        public IDependency produce(IDependency... wasRequired) throws UndefinedException;
    }

}
