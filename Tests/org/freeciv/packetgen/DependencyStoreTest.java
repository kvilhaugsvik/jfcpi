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

package org.freeciv.packetgen;

import org.freeciv.packetgen.dependency.*;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertTrue;

public class DependencyStoreTest {
    private static Requirement reqFor(String has) {
        return new Requirement(has, Requirement.Kind.VALUE);
    }

    private DependencyStore depRoofOnTwoWallsOnTwoFloorsEachOneOverlapping() {
        DependencyStore store = new DependencyStore();

        store.addWanted(new OnlyRequire("Roof", reqFor("Wall1"), reqFor("Wall2")));
        store.addPossibleRequirement(new OnlyRequire("Floor1"));
        store.addPossibleRequirement(new OnlyRequire("Wall1", reqFor("Floor1"), reqFor("Floor2")));
        store.addPossibleRequirement(new OnlyRequire("Floor2"));
        store.addPossibleRequirement(new OnlyRequire("Wall2", reqFor("Floor2"), reqFor("Floor3")));
        store.addPossibleRequirement(new OnlyRequire("Floor3"));

        return store;
    }

    @Test
    public void testResolvedIsOrderedOnDependency() throws Exception {
        DependencyStore store = depRoofOnTwoWallsOnTwoFloorsEachOneOverlapping();
        HashSet<Requirement> seen = new HashSet<Requirement>();

        for (IDependency item: store.getResolved()) {
            if (!item.getReqs().isEmpty()) {
                Collection<Requirement> found = new HashSet<Requirement>(item.getReqs());
                found.removeAll(seen);
                assertTrue("The item " + item + " came before it's requirement(s)", found.isEmpty());
            }
            seen.add(item.getIFulfillReq());
        }
    }

    IDependency impossible = new IDependency() {
        @Override public Collection<Requirement> getReqs() {
            return Collections.<Requirement>emptySet();
        }

        @Override public Requirement getIFulfillReq() {
            return new Requirement("Impossible", Requirement.Kind.HARD_FAILURE);
        }
    };

    @Test(expected = AssertionError.class)
    public void hardFailuresCanNotBeFulfilled() {
        DependencyStore store = new DependencyStore();
        store.addPossibleRequirement(impossible);
    }

    @Test(expected = AssertionError.class)
    public void hardFailuresCanNotBeFulfilledSoItCanNotBeAWantedFulfillment() {
        DependencyStore store = new DependencyStore();
        store.addWanted(impossible);
    }

    public static class OnlyRequire implements IDependency {
        private final HashSet<Requirement> want;
        private final Requirement has;

        public OnlyRequire(String has, Requirement... wants) {
            this.want = new HashSet<Requirement>();
            for (Requirement wanted : wants)
                want.add(wanted);
            this.has = reqFor(has);
        }

        @Override
        public Collection<Requirement> getReqs() {
            return Collections.unmodifiableSet(want);
        }

        @Override
        public Requirement getIFulfillReq() {
            return has;
        }

        @Override
        public String toString() {
            return has.getName();
        }
    }
}
