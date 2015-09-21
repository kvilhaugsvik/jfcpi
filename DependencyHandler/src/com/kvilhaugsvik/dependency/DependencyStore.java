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

package com.kvilhaugsvik.dependency;

import java.util.*;

public final class DependencyStore {
    private static final String nullNotAllowed = "null is not an allowed argument here";

    private final DepStore<Dependency.Item> resolved = DepStore.forIDependency();
    private final DepStore<Dependency.Item> existing = DepStore.forIDependency();
    private final DepStore<Dependency.Item> dependenciesFulfilled = DepStore.forIDependency();
    private final HashSet<Requirement> dependenciesUnfulfilled = new HashSet<Requirement>();
    private final HashSet<Requirement> wantsOut = new HashSet<Requirement>();
    private final HashMap<Requirement, Collection<Requirement>> blameDeeperWhenNoItem = new HashMap<Requirement, Collection<Requirement>>();
    private final DepStore<Dependency.Maker> makers = DepStore.forMaker();

    /**
     * Make the dependency store aware of the fulfillment of a possible requirement.
     * Any existing fulfillment will be overwritten. A Maker will be added as a Maker as well
     * @param item The dependency to add.
     */
    public void addPossibleRequirement(Dependency.Item item) {
        if (null == item) throw new NullPointerException(nullNotAllowed);
        if (ReqKind.FailHard.class.equals(item.getIFulfillReq().getKind()))
            throw new AssertionError("Tried to fulfill a " + ReqKind.FailHard.class +
                                             " that by definition can't be fulfilled");
        if (item instanceof Dependency.Maker)
            addMaker((Dependency.Maker)item);

        existing.add(item);
        dependenciesUnfulfilled.clear();
    }

    public void addWanted(Dependency.Item item) {
        addPossibleRequirement(item);
        demand(item.getIFulfillReq());
    }

    public void demand(Requirement requirement) {
        wantsOut.add(requirement);
    }

    public void addMaker(Dependency.Maker maker) {
        if  (null == maker) throw new NullPointerException(nullNotAllowed);

        if (maker instanceof Dependency.BlameShifter) {
            Map<Requirement, Collection<Requirement>> suspects = ((Dependency.BlameShifter) maker).blameSuspects();

            for (Requirement req : suspects.keySet()) {
                /* TODO: Don't be so trusting. */
                this.blameMissingOn(req, suspects.get(req));
            }
        }

        makers.add(maker);
    }

    /**
     * Add information on what missing requirements to suspect when a certain requirement is missing. This may be a
     * maker that could have produced it, a parameter to the maker or something the item would have required.
     * @param missing the missing item
     * @param blamed requirements to add when it is missing
     */
    public void blameMissingOn(Requirement missing, Requirement... blamed) {
        blameMissingOn(missing, Arrays.asList(blamed));
    }

    /**
     * Add information on what missing requirements to suspect when a certain requirement is missing. This may be a
     * maker that could have produced it, a parameter to the maker or something the item would have required.
     * @param missing the missing item
     * @param blamed requirements to add when it is missing
     */
    public void blameMissingOn(Requirement missing, Collection<Requirement> blamed) {
        assert blamed.size() != 0 : "No one to blame";
        blameDeeperWhenNoItem.put(missing, new HashSet<Requirement>(blamed));
    }

    @Deprecated
    public Set<Requirement> getMissingRequirements() {
        HashSet<Requirement> knownOrAssumedGuilty = new HashSet<Requirement>();

        for (Requirement req : getMissing()) {
            knownOrAssumedGuilty.add(req);
            knownOrAssumedGuilty.addAll(explainMissing(req).toRequirements());
        }

        return knownOrAssumedGuilty;
    }

    /**
     * Wanted items that don't exist or can't be resolved
     * @return the requirements of items asked for but not resolved
     */
    public Set<Requirement> getMissing() {
        resolve();
        return Collections.unmodifiableSet(wantsOut);
    }

    /**
     * Explain why a required item is missing
     * @param missing the requirement of the missing item
     * @return the reason the item is missing
     */
    public MissingItemExplained explainMissing(Requirement missing) {
        if (notAProblem(missing))
            throw new IllegalArgumentException("Asked to explain why a non problem is a problem");

        HashSet<MissingItemExplained.Blame> subExplains = new HashSet<MissingItemExplained.Blame>();

        if (existing.hasFulfillmentOf(missing)) {
            for (Requirement req : existing.getFulfillmentOf(missing).getReqs())
                if (!notAProblem(req))
                    subExplains.add(new MissingItemExplained.Blame(
                            explainMissing(req),
                            MissingItemExplained.Relation.DEPEND_ON));
        } else if (makers.hasFulfillmentOf(missing)) {
            for (Requirement req : makers.getFulfillmentOf(missing).neededInput(missing))
                if (!notAProblem(req))
                    subExplains.add(new MissingItemExplained.Blame(
                            explainMissing(req),
                            MissingItemExplained.Relation.MADE_FROM));
        } else if (blameDeeperWhenNoItem.containsKey(missing)) {
            for (Requirement req : blameDeeperWhenNoItem.get(missing))
                if (!(notAProblem(req) || existing.hasFulfillmentOf(req)))
                    subExplains.add(new MissingItemExplained.Blame(
                            explainMissing(req),
                            MissingItemExplained.Relation.BLAMED_ON));
        } // don't know who or what to blame

        return new MissingItemExplained(missing, getHowItIsAProblem(missing), subExplains);
    }

    private MissingItemExplained.Status getHowItIsAProblem(Requirement missing) {
        return existing.hasFulfillmentOf(missing) ?
                MissingItemExplained.Status.EXIST_BUT :
                MissingItemExplained.Status.IS_MISSING;
    }

    private boolean notAProblem(Requirement missing) {
        return resolved.hasFulfillmentOf(missing) || dependenciesFulfilled.hasFulfillmentOf(missing);
    }

    /**
     * Get the wanted items that has their dependencies in order and their dependencies
     * @return the items sorted so no item comes before its requirements
     */
    public List<Dependency.Item> getResolved() {
        resolve();
        return new LinkedList<Dependency.Item>(resolved.values());
    }

    /**
     * Returns true if a potential provider for a requirement is known
     * @param item the requirement to find a provider for
     * @return true if a potential provider for a requirement is known EVEN IF IT WON'T RESOLVE
     */
    public boolean isAwareOfPotentialProvider(Requirement item) {
        return existing.hasFulfillmentOf(item) ||
                wasAbleToCreateShallowly(item) ||
                wasAbleToCreateDeeply(item);
    }

    private boolean wasAbleToCreateShallowly(Requirement item) {
        return (makers.hasFulfillmentOf(item) && creationWorked(item));
    }

    private boolean wasAbleToCreateDeeply(Requirement item) {
        final Collection<Requirement> blamed = blameDeeperWhenNoItem.get(item);

        // Give up if no pre requirements are known
        if (blamed == null)
            return false;

        for (Requirement req : blamed)
            isAwareOfPotentialProvider(req);

        return wasAbleToCreateShallowly(item);
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

    /**
     * Try to create the specified item,
     * @param item the item to create.
     * @return true if the creation was successful.
     */
    private boolean creationWorked(Requirement item) {
        /* Try each maker able to create the item. */
        for (Dependency.Maker maker : makers.getFulfillmentsOf(item)) {
            /* Not aware of any missing items yet, */
            boolean gotReqs = true;

            /* The items to feed the maker. */
            LinkedList<Dependency.Item> args = new LinkedList<Dependency.Item>();

            /* Get each item the maker needs before it can create the
             * requested item. */
            for (Requirement req : maker.neededInput(item)) {
                if (isAwareOfPotentialProvider(req)) {
                    args.add(getPotentialProvider(req));
                    assert null != args.peekLast() : "Item claimed to be there don't exist";
                } else {
                    /* Not able to get (or create) the inout item. */
                    gotReqs = false;

                    /* Try the next maker. */
                    break;
                }
            }

            if (gotReqs) {
                try {
                    /* Create the requested item */
                    addPossibleRequirement(maker.produce(item, args.toArray(new Dependency.Item[args.size()])));
                } catch (UndefinedException e) {
                    /* Try the next maker. */
                    continue;
                }

                /* Report the success */
                return true;
            }
        }

        /* Creation failed. Report missing items the first maker needed. */
        Dependency.Maker maker = makers.getFulfillmentOf(item);
        for (Requirement req : maker.neededInput(item)) {
            if (!isAwareOfPotentialProvider(req)) {
                /* Needed by the first maker and missing. */
                dependenciesUnfulfilled.add(req);
            }
        }

        /* Report the failure. */
        return false;
    }

    public Dependency.Item getPotentialProvider(Requirement item) {
        if (!existing.hasFulfillmentOf(item) && makers.hasFulfillmentOf(item))
            creationWorked(item);
        return existing.getFulfillmentOf(item);
    }

    private boolean declareFulfilled(Dependency.Item item) {
        dependenciesFulfilled.add(item);
        return true;
    }

    public boolean dependenciesFound(Dependency.Item item) {
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

    private void addWillCrashUnlessAlreadyChecked(Dependency.Item item) {
        assert (null != item) : nullNotAllowed;
        assert (dependenciesFulfilled.hasFulfillmentOf(item.getIFulfillReq())) : "Missing dependency";
        if (!resolved.hasFulfillmentOf(item.getIFulfillReq())) {
            for (Requirement dependOn : item.getReqs()) {
                addWillCrashUnlessAlreadyChecked(dependenciesFulfilled.getFulfillmentOf(dependOn));
            }
            resolved.add(item);
        }
    }

    private void addToResolvedIfPossible(Dependency.Item item) {
        assert (null != item) : nullNotAllowed;
        if (dependenciesFound(item)) {
            addWillCrashUnlessAlreadyChecked(item);
        }
    }

    private void resolve() {
        int oldResolved = -1;
        while (moreIsAskedFor() && newItemsWereAdded(oldResolved)) {
            oldResolved = resolved.values().size();
            for (Requirement toAdd : wantsOut) {
                if (isAwareOfPotentialProvider(toAdd))
                    addToResolvedIfPossible(getPotentialProvider(toAdd));
                else
                    dependenciesUnfulfilled.add(toAdd);
            }
            for (Dependency.Item item : resolved.values())
                wantsOut.remove(item.getIFulfillReq());
        }
    }

    private boolean moreIsAskedFor() {
        return wantsOut.size() != 0;
    }

    private boolean newItemsWereAdded(int oldResolved) {
        return oldResolved != resolved.values().size();
    }

    private static class DepStore<Of> {
        private final LinkedHashMap<Requirement, Of> store = new LinkedHashMap<Requirement, Of>();
        private final LinkedHashMap<Required, Of> complex = new LinkedHashMap<Required, Of>();
        private final FulfillGetter fulfillGetter;

        public static DepStore forIDependency() {
            return new DepStore(new FulfillGetter<Dependency.Item>() {
                @Override
                public Required getIFulfill(Dependency.Item dep) {
                    return dep.getIFulfillReq();
                }
            });
        }

        public static DepStore forMaker() {
            return new DepStore(new FulfillGetter<Dependency.Maker>() {
                @Override
                public Required getIFulfill(Dependency.Maker make) {
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

        /**
         * Get all dependencies that may fulfill the requirement.
         * @param req the requirement to fulfill
         * @return iterator over all possible fulfillments
         */
        public Iterable<Of> getFulfillmentsOf(Requirement req) {
            /* No candidates exist yet. */
            LinkedList<Of> out = new LinkedList<Of>();

            if (store.containsKey(req)) {
                /* The item it self comes first */
                out.add(store.get(req));
            }

            /* any complex fulfillment follows. */
            for (Required candidate : complex.keySet()) {
                if (candidate.canFulfill(req)) {
                    out.add(complex.get(candidate));
                }
            }

            return out;
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

        private interface FulfillGetter<Of> {
            Required getIFulfill(Of of);
        }
    }
}
