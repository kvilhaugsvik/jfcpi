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

    private final DepStore resolved = new DepStore();
    private final DepStore existing = new DepStore();
    private final DepStore dependenciesFulfilled = new DepStore();
    private final HashSet<Requirement> dependenciesUnfulfilled = new HashSet<Requirement>();
    private final HashSet<Requirement> wantsOut = new HashSet<Requirement>();
    private final HashMap<Requirement, IDependency.Maker> makers = new HashMap<Requirement, IDependency.Maker>();

    /**
     * Make the dependency store aware of the fulfillment of a possible requirement.
     * Any existing fulfillment will be overwritten. A ManyFulfiller will have every fulfillment added at once. In other
     * words: An earlier registered primary fulfillment will be overwritten by the ManyFulfiller's also-fulfillment.
     * @param item The dependency to add.
     */
    public void addPossibleRequirement(IDependency item) {
        if (null == item) throw new NullPointerException(nullNotAllowed);
        if (ReqKind.FailHard.class.equals(item.getIFulfillReq().getKind()))
            throw new AssertionError("Tried to fulfill a " + ReqKind.FailHard.class +
                                             " that by definition can't be fulfilled");

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
        makers.put(maker.getICanProduceReq(), maker);
    }

    public Set<Requirement> getMissingRequirements() {
        resolve();
        return Collections.unmodifiableSet(dependenciesUnfulfilled);
    }

    /**
     * Get the wanted items that has their dependencies in order and their dependencies
     * @return the items sorted so no item comes before its requirements
     */
    public List<IDependency> getResolved() {
        resolve();
        return new LinkedList<IDependency>(resolved.values());
    }

    public boolean isAwareOfPotentialProvider(Requirement item) {
        return existing.hasFulfillmentOf(item) || (makers.containsKey(item) && creationWorked(item));
    }

    private boolean creationWorked(Requirement item) {
        IDependency.Maker maker = makers.get(item);
        LinkedList<IDependency> args = new LinkedList<IDependency>();
        for (Requirement req : maker.getReqs())
            if (isAwareOfPotentialProvider(req))
                args.add(getPotentialProvider(req));
            else
                return false;

        try {
            existing.add(maker.produce(args.toArray(new IDependency[args.size()])));
            makers.remove(item);
            return true;
        } catch (UndefinedException e) {
            return false;
        }
    }

    public IDependency getPotentialProvider(Requirement item) {
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
                if (!(isAwareOfPotentialProvider(req) && dependenciesFound(existing.getFulfillmentOf(req)))) {
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

    private static class DepStore {
        private final LinkedHashMap<Requirement, IDependency> store = new LinkedHashMap<Requirement, IDependency>();

        public boolean hasFulfillmentOf(Requirement req) {
            return store.containsKey(req);
        }

        public IDependency getFulfillmentOf(Requirement req) {
            return store.get(req);
        }

        public void add(IDependency dep) {
            store.put(dep.getIFulfillReq(), dep);
        }

        public Collection<IDependency> values() {
            return Collections.unmodifiableCollection(store.values());
        }
    }
}
