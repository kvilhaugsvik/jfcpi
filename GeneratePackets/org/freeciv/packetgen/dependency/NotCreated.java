/*
 * Copyright (c) 2012, Sveinung Kvilhaugsvik
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

import java.util.*;

/**
 * A representation of an item that couldn't be created since hard dependencies was missing.
 *
 * Since the attempted creation is over simply finding the requirement later won't help.
 * The creation process may or may not be repeatable and may or may not fail if the requirements are found.
 */
public class NotCreated implements Dependency.Item {
    private final Requirement wouldHaveProvided;
    private final Collection<Requirement> wasMissingAtCreation;

    private final HashSet<Requirement> totalRequirements;

    /**
     * A representation of an item that couldn't be created since hard dependencies was missing.
     * @param wouldHaveProvided What the item would have provided
     * @param wouldAtLeastHaveRequired Items it would require. May or may not be all those items.
     * @param wasMissingAtCreation Items missing at creation time causing the creation to fail.
     */
    public NotCreated(Requirement wouldHaveProvided, Collection<Requirement> wouldAtLeastHaveRequired,
                      Collection<Requirement> wasMissingAtCreation) {
        this.wouldHaveProvided = wouldHaveProvided;
        this.wasMissingAtCreation = wasMissingAtCreation;

        totalRequirements = new HashSet<Requirement>(wouldAtLeastHaveRequired);
        for (Requirement blamed: wasMissingAtCreation) {
            totalRequirements.add(new Requirement(blamed.toString(), ReqKind.FailHard.class));
        }
    }

    @Override public Collection<Requirement> getReqs() {
        return totalRequirements;
    }

    @Override public Requirement getIFulfillReq() {
        return wouldHaveProvided;
    }

    public Collection<Requirement> getWasMissingAtCreation() {
        return wasMissingAtCreation;
    }
}
