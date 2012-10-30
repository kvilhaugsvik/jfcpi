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
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DependencyStoreTest {
    private static Requirement reqFor(String has) {
        return new Requirement(has, Constant.class);
    }

    @Test public void isAwareOfPotentialProviderNoProvider() {
        DependencyStore store = new DependencyStore();
        assertFalse(store.isAwareOfPotentialProvider(reqFor("Is")));
    }

    @Test public void isAwareOfPotentialProviderAProvider() {
        DependencyStore store = new DependencyStore();
        store.addPossibleRequirement(new OnlyRequire("Is"));
        assertTrue(store.isAwareOfPotentialProvider(reqFor("Is")));
    }

    @Test public void isAwareOfPotentialProviderAProviderThatDependOnNotExisting() {
        DependencyStore store = new DependencyStore();
        store.addPossibleRequirement(new OnlyRequire("Is", reqFor("isn't")));
        assertTrue("The provider exist even if it don't resolve so it should return true",
                store.isAwareOfPotentialProvider(reqFor("Is")));
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
            return new Requirement("Impossible", ReqKind.FailHard.class);
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

    @Test public void makerWorksNoDependencies() {
        final Constant<AString> made = Constant.isString("Value", BuiltIn.literal("a value"));
        final Requirement req = new Requirement("Value", Constant.class);
        IDependency.Maker valueGen = new IDependency.Maker.Simple(req) {
            @Override
            public IDependency produce(Requirement toProduce, IDependency... wasRequired) throws UndefinedException {
                return made;
            }
        };

        DependencyStore store = new DependencyStore();
        store.addMaker(valueGen);
        OnlyRequire lookedFor = new OnlyRequire("ValueUser", req);
        store.addWanted(lookedFor);

        List<IDependency> result = store.getResolved();

        assertTrue("Demanded item should be in output", result.contains(lookedFor));
        assertTrue("Dependency of demanded item should be in output", result.contains(made));
    }

    private static final Constant<AString> made = Constant.isString("Value", BuiltIn.literal("a value"));
    private static final Requirement req = new Requirement("Value", Constant.class);
    private static final OnlyRequire lookedFor = new OnlyRequire("ValueUser", req);

    private static final OnlyRequire one = new OnlyRequire("one");
    private static final OnlyRequire two = new OnlyRequire("two");
    private static final OnlyRequire three = new OnlyRequire("three");

    private static DependencyStore makerThreeDependencies() {
        IDependency.Maker valueGen = new IDependency.Maker.Simple(req,
                one.getIFulfillReq(), two.getIFulfillReq(), three.getIFulfillReq()) {
            @Override
            public IDependency produce(Requirement toProduce, IDependency... wasRequired) throws UndefinedException {
                if (one.equals(wasRequired[0]) && two.equals(wasRequired[1]) && three.equals(wasRequired[2]))
                    return made;
                throw new UndefinedException("Parameters missing or in wrong order");
            }
        };

        DependencyStore store = new DependencyStore();
        store.addMaker(valueGen);
        store.addWanted(lookedFor);

        return store;
    }

    @Test public void makerThreeDependenciesNoneAreThere() {
        DependencyStore store = makerThreeDependencies();

        assertFalse("Shouldn't generate without all requirements", store.getResolved().contains(made));
        assertTrue("Should complain about missing argument",
                store.getMissingRequirements().contains(one.getIFulfillReq()));
        assertTrue("Should complain about missing argument",
                store.getMissingRequirements().contains(two.getIFulfillReq()));
        assertTrue("Should complain about missing argument",
                store.getMissingRequirements().contains(three.getIFulfillReq()));
    }

    @Test public void makerThreeDependenciesAllAreThere() {
        DependencyStore store = makerThreeDependencies();

        // Now add the requirements
        store.addPossibleRequirement(one);
        store.addPossibleRequirement(two);
        store.addPossibleRequirement(three);

        List<IDependency> result = store.getResolved();
        assertTrue("Demanded item should be in output", result.contains(lookedFor));
        assertTrue("Dependency of demanded item should be in output", result.contains(made));
    }

    private static final RequiredMulti wordThenWordInParen =
            new RequiredMulti(Constant.class, Pattern.compile("\\w+\\(\\w+\\)"));

    @Test public void requiredMultiFalseNegative() {
        assertTrue("Should match", wordThenWordInParen.canFulfill(new Requirement("word(other)", Constant.class)));
    }

    @Test public void requiredMultiFalseNegativeUpperCaseAndNumber() {
        assertTrue("Should match", wordThenWordInParen.canFulfill(new Requirement("WORD(other7)", Constant.class)));
    }

    @Test public void requiredMultiFalsePositive() {
        assertFalse("Shouldn't match", wordThenWordInParen.canFulfill(new Requirement("word", Constant.class)));
    }

    @Test public void requiredMultiFalsePositiveTwice() {
        assertFalse("Shouldn't match",
                wordThenWordInParen.canFulfill(new Requirement("word(other)word(other)", Constant.class)));
    }

    @Test public void multiReqMakerMakesTwoDifferent() {
        final OnlyRequire produced1 = new OnlyRequire("product1");
        final OnlyRequire produced2 = new OnlyRequire("product2");

        final OnlyRequire rawMaterial1 = new OnlyRequire("one");
        final OnlyRequire rawMaterial2 = new OnlyRequire("two");
        final OnlyRequire rawMaterial3 = new OnlyRequire("three");

        IDependency.Maker valueGen = new IDependency.Maker() {
            @Override
            public List<Requirement> neededInput(Requirement toProduce) {
                if (produced1.getIFulfillReq().canFulfill(toProduce))
                    return Arrays.asList(rawMaterial1.getIFulfillReq(), rawMaterial2.getIFulfillReq());
                else
                    return Arrays.asList(rawMaterial2.getIFulfillReq(), rawMaterial3.getIFulfillReq());
            }

            @Override
            public Required getICanProduceReq() {
                return new RequiredMulti(Constant.class, Pattern.compile("product."));
            }

            @Override
            public IDependency produce(Requirement toProduce, IDependency... wasRequired) throws UndefinedException {
                if (produced1.getIFulfillReq().equals(toProduce)) {
                    if (rawMaterial1.equals(wasRequired[0]) && rawMaterial2.equals(wasRequired[1]))
                        return produced1;
                } else if (produced2.getIFulfillReq().equals(toProduce)) {
                    if (rawMaterial2.equals(wasRequired[0]) && rawMaterial3.equals(wasRequired[1]))
                        return produced2;
                }
                throw new UndefinedException("Parameters missing or in wrong order");
            }
        };

        DependencyStore store = new DependencyStore();
        store.addMaker(valueGen);
        store.addPossibleRequirement(rawMaterial1);
        store.addPossibleRequirement(rawMaterial2);
        store.addPossibleRequirement(rawMaterial3);

        store.demand(produced1.getIFulfillReq());
        store.demand(produced2.getIFulfillReq());

        assertEquals("Didn't create number one.",
                produced1, store.getPotentialProvider(produced1.getIFulfillReq()));
        assertEquals("Didn't create number two.",
                produced2, store.getPotentialProvider(produced2.getIFulfillReq()));
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
