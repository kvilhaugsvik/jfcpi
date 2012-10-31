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

public final class DependencyStore {
    private static final String nullNotAllowed = "null is not an allowed argument here";

    private final DepStore<IDependency> resolved = DepStore.forIDependency();
    private final DepStore<IDependency> existing = DepStore.forIDependency();
    private final DepStore<IDependency> dependenciesFulfilled = DepStore.forIDependency();
    private final HashSet<Requirement> dependenciesUnfulfilled = new HashSet<Requirement>();
    private final HashSet<Requirement> wantsOut = new HashSet<Requirement>();
    private final HashMap<Requirement, Collection<Requirement>> blameDeeperWhenNoItem = new HashMap<Requirement, Collection<Requirement>>();
    private final DepStore<IDependency.Maker> makers = DepStore.forMaker();

    /**
     * Make the dependency store aware of the fulfillment of a possible requirement.
     * Any existing fulfillment will be overwritten. A Maker will be added as a Maker as well
     * @param item The dependency to add.
     */
    public void addPossibleRequirement(IDependency item) {
        if (null == item) throw new NullPointerException(nullNotAllowed);
        if (ReqKind.FailHard.class.equals(item.getIFulfillReq().getKind()))
            throw new AssertionError("Tried to fulfill a " + ReqKind.FailHard.class +
                                             " that by definition can't be fulfilled");
        if (item instanceof IDependency.Maker)
            addMaker((IDependency.Maker)item);

        existing.add(item);
        dependenciesUnfulfilled.clear();
    }

    public void addWanted(IDependency item) {
        addPossibleRequirement(item);
        demand(item.getIFulfillReq());
    }

    public void demand(Requirement requirement) {
        wantsOut.add(requirement);
    }

    public void addMaker(IDependency.Maker maker) {
        if  (null == maker) throw new NullPointerException(nullNotAllowed);
        makers.add(maker);
    }

    /**
     * Add information on what missing requirements to suspect when a certain requirement is missing. This may be a
     * maker that could have produced it, a parameter to the maker or something the item would have required.
     * @param missing the missing item
     * @param blamed requirements to add when it is missing
     */
    public void blameMissingOn(Requirement missing, Requirement... blamed) {
        assert blamed.length != 0 : "No one to blame";
        blameDeeperWhenNoItem.put(missing, new HashSet(Arrays.asList(blamed)));
    }

    public Set<Requirement> getMissingRequirements() {
        resolve();
        purgeBlameDeeper();

        HashSet<Requirement> knownOrAssumedGuilty = new HashSet<Requirement>(dependenciesUnfulfilled);
        for (Requirement blameHint : blameDeeperWhenNoItem.keySet())
            if (dependenciesUnfulfilled.contains(blameHint))
                knownOrAssumedGuilty.addAll(blameDeeperWhenNoItem.get(blameHint));
        return knownOrAssumedGuilty;
    }

    private void purgeBlameDeeper() {
        for (Requirement purgeKey : new HashSet<Requirement>(blameDeeperWhenNoItem.keySet()))
            if (existing.hasFulfillmentOf(purgeKey)) {
                blameDeeperWhenNoItem.remove(purgeKey);
            } else {
                for (Requirement suspect : new HashSet<Requirement>(blameDeeperWhenNoItem.get(purgeKey)))
                    if (isAwareOfProvider(suspect))
                        blameDeeperWhenNoItem.get(blameDeeperWhenNoItem.get(purgeKey).remove(suspect));
                if (blameDeeperWhenNoItem.get(purgeKey).isEmpty())
                    blameDeeperWhenNoItem.remove(purgeKey);
            }
    }

    /**
     * Get the wanted items that has their dependencies in order and their dependencies
     * @return the items sorted so no item comes before its requirements
     */
    public List<IDependency> getResolved() {
        resolve();
        return new LinkedList<IDependency>(resolved.values());
    }

    /**
     * Returns true if a potential provider for a requirement is known
     * @param item the requirement to find a provider for
     * @return true if a potential provider for a requirement is known EVEN IF IT WON'T RESOLVE
     */
    public boolean isAwareOfPotentialProvider(Requirement item) {
        return existing.hasFulfillmentOf(item) || (makers.hasFulfillmentOf(item) && creationWorked(item));
    }

    /**
     * Returns true if a provider for a requirement is known
     *
     * @param item the requirement to find a provider for
     * @return true if a provider for a requirement is known AND it resolves
     */
    public boolean isAwareOfProvider(Requirement item) {
        return isAwareOfPotentialProvider(item) && dependenciesFound(existing.getFulfillmentOf(item));
    }

    private boolean creationWorked(Requirement item) {
        IDependency.Maker maker = makers.getFulfillmentOf(item);
        LinkedList<IDependency> args = new LinkedList<IDependency>();
        boolean parameterMissing = false;
        for (Requirement req : maker.neededInput(item))
            if (isAwareOfPotentialProvider(req)) {
                args.add(getPotentialProvider(req));
            } else {
                dependenciesUnfulfilled.add(req);
                parameterMissing = true;
            }
        if (parameterMissing)
            return false;

        try {
            existing.add(maker.produce(item, args.toArray(new IDependency[args.size()])));
            return true;
        } catch (UndefinedException e) {
            return false;
        }
    }

    public IDependency getPotentialProvider(Requirement item) {
        if (!existing.hasFulfillmentOf(item) && makers.hasFulfillmentOf(item))
            creationWorked(item);
        return existing.getFulfillmentOf(item);
    }

    private boolean declareFulfilled(IDependency item) {
        dependenciesFulfilled.add(item);
        return true;
    }

    public boolean dependenciesFound(IDependency item) {
        assert (null != item) : nullNotAllowed;
        if (dependenciesFulfilled.hasFulfillmentOf(item.getIFulfillReq())) {
            return true;
        } else if (item.getReqs().isEmpty()) {
            return declareFulfilled(item);
        } else if (dependenciesUnfulfilled.contains(item.getIFulfillReq())) {
            return false;
        } else {
            boolean missingReq = false;
            for (Requirement req : item.getReqs()) {
                if (!(isAwareOfProvider(req))) {
                    dependenciesUnfulfilled.add(req);
                    missingReq = true;
                }
            }
            if (missingReq) {
                dependenciesUnfulfilled.add(item.getIFulfillReq());
                return false;
            } else {
                return declareFulfilled(item);
            }
        }
    }

    private void addWillCrashUnlessAlreadyChecked(IDependency item) {
        assert (null != item) : nullNotAllowed;
        assert (dependenciesFulfilled.hasFulfillmentOf(item.getIFulfillReq())) : "Missing dependency";
        if (!resolved.hasFulfillmentOf(item.getIFulfillReq())) {
            for (Requirement dependOn : item.getReqs()) {
                addWillCrashUnlessAlreadyChecked(dependenciesFulfilled.getFulfillmentOf(dependOn));
            }
            resolved.add(item);
        }
    }

    private void addToResolvedIfPossible(IDependency item) {
        assert (null != item) : nullNotAllowed;
        if (dependenciesFound(item)) {
            addWillCrashUnlessAlreadyChecked(item);
        }
    }

    private void resolve() {
        for (Requirement toAdd : wantsOut) {
            if (isAwareOfPotentialProvider(toAdd))
                addToResolvedIfPossible(getPotentialProvider(toAdd));
            else
                dependenciesUnfulfilled.add(toAdd);
        }
        wantsOut.removeAll(resolved.values());
    }

    private static class DepStore<Of> {
        private final LinkedHashMap<Requirement, Of> store = new LinkedHashMap<Requirement, Of>();
        private final LinkedHashMap<Required, Of> complex = new LinkedHashMap<Required, Of>();
        private final FulfillGetter fulfillGetter;

        public static DepStore forIDependency() {
            return new DepStore(new FulfillGetter<IDependency>() {
                @Override
                public Required getIFulfill(IDependency dep) {
                    return dep.getIFulfillReq();
                }
            });
        }

        public static DepStore forMaker() {
            return new DepStore(new FulfillGetter<IDependency.Maker>() {
                @Override
                public Required getIFulfill(IDependency.Maker make) {
                    return make.getICanProduceReq();
                }
            });
        }

        private DepStore(FulfillGetter fulfillGetter) {
            this.fulfillGetter = fulfillGetter;
        }

        public boolean hasFulfillmentOf(Requirement req) {
            return store.containsKey(req) || null != complexGet(req);
        }

        private Of complexGet(Requirement req) {
            for (Required candidate : complex.keySet()) {
                if (candidate.canFulfill(req))
                    return complex.get(candidate);
            }
            return null;
        }

        public Of getFulfillmentOf(Requirement req) {
            Of candidate = store.get(req);
            if (null != candidate)
                return candidate;
            else
                return complexGet(req);
        }

        public void add(Of dep) {
            Required provides = fulfillGetter.getIFulfill(dep);
            if (provides instanceof Requirement)
                store.put((Requirement) provides, dep);
            else
                complex.put(provides, dep);
        }

        public void remove(Requirement item) {
            store.remove(item);
        }

        public Collection<Of> values() {
            return Collections.unmodifiableCollection(store.values());
        }

        private static interface FulfillGetter<Of> {
            public Required getIFulfill(Of of);
        }
    }
}
