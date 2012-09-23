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
import org.freeciv.packetgen.enteties.Constant;
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AString;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
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

    @Test public void demandExisting() {
        DependencyStore store = new DependencyStore();
        store.demand(reqFor("Alone"));
        OnlyRequire alone = new OnlyRequire("Alone");
        store.addPossibleRequirement(alone);
        assertTrue("Demanded item should be in output", store.getResolved().contains(alone));
    }

    @Test public void demandNotExisting() {
        DependencyStore store = new DependencyStore();
        store.demand(reqFor("Alone"));
        assertTrue("Should be informed about missing item",
                store.getMissingRequirements().contains(reqFor("Alone")));
    }

    @Test public void demandRequireExisting() {
        DependencyStore store = new DependencyStore();
        store.demand(reqFor("notAlone"));
        OnlyRequire notAlone = new OnlyRequire("notAlone", reqFor("Existing"));
        store.addPossibleRequirement(notAlone);
        OnlyRequire company = new OnlyRequire("Existing");
        store.addPossibleRequirement(company);
        assertTrue("Demanded item should be in output", store.getResolved().contains(notAlone));
        assertTrue("Dependency of demanded item should be in output", store.getResolved().contains(company));
    }

    @Test public void demandRequireNonExisting() {
        DependencyStore store = new DependencyStore();
        store.demand(reqFor("Alone"));
        OnlyRequire alone = new OnlyRequire("Alone", reqFor("Non existing"));
        store.addPossibleRequirement(alone);
        assertTrue("Should be informed about missing item", store.getMissingRequirements().contains(reqFor("Alone")));
        assertTrue("Should be informed about missing item", store.getMissingRequirements().contains(reqFor("Non existing")));
    }

    @Test public void makerWorks() {
        final Constant<AString> made = Constant.isString("Value", BuiltIn.literal("a value"));
        IDependency.Maker valueGen = new IDependency.Maker() {
            @Override
            public Collection<Requirement> getReqs() {
                return Collections.emptySet();
            }

            @Override
            public Requirement getICanProduceReq() {
                return new Requirement("Value", Requirement.Kind.VALUE);
            }

            @Override
            public IDependency produce(IDependency... wasRequired) throws UndefinedException {
                return made;
            }
        };

        DependencyStore store = new DependencyStore();
        store.addMaker(valueGen);
        OnlyRequire lookedFor = new OnlyRequire("ValueUser", new Requirement("Value", Requirement.Kind.VALUE));
        store.addWanted(lookedFor);

        List<IDependency> result = store.getResolved();

        assertTrue("Demanded item should be in output", result.contains(lookedFor));
        assertTrue("Dependency of demanded item should be in output", result.contains(made));
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
