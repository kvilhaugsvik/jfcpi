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

    private final LinkedHashMap<Requirement, IDependency> resolved = new LinkedHashMap<Requirement, IDependency>();
    private final HashMap<Requirement, IDependency> existing = new HashMap<Requirement, IDependency>();
    private final HashMap<Requirement, IDependency> dependenciesFulfilled = new HashMap<Requirement, IDependency>();
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
        if (Requirement.Kind.HARD_FAILURE.equals(item.getIFulfillReq().getKind()))
            throw new AssertionError("Tried to fulfill a " + Requirement.Kind.HARD_FAILURE +
                                             " that by definition can't be fulfilled");

        putAllProvdesIn(item, existing);
        dependenciesUnfulfilled.clear();
    }

    private static void putAllProvdesIn(IDependency item, HashMap<Requirement, IDependency> depCategory) {
        depCategory.put(item.getIFulfillReq(), item);
        if (item instanceof IDependency.ManyFulfiller)
            for (Requirement also : ((IDependency.ManyFulfiller)item).getIAlsoFulfillReqs())
                depCategory.put(also, item);
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
        return existing.containsKey(item) || (makers.containsKey(item) && creationWorked(item));
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
            existing.put(item, maker.produce(args.toArray(new IDependency[args.size()])));
            makers.remove(item);
            return true;
        } catch (UndefinedException e) {
            return false;
        }
    }

    public IDependency getPotentialProvider(Requirement item) {
        return existing.get(item);
    }

    private boolean declareFulfilled(IDependency item) {
        putAllProvdesIn(item, dependenciesFulfilled);
        return true;
    }

    public boolean dependenciesFound(IDependency item) {
        assert (null != item) : nullNotAllowed;
        if (dependenciesFulfilled.containsKey(item.getIFulfillReq())) {
            return true;
        } else if (item.getReqs().isEmpty()) {
            return declareFulfilled(item);
        } else if (dependenciesUnfulfilled.contains(item.getIFulfillReq())) {
            return false;
        } else {
            boolean missingReq = false;
            for (Requirement req : item.getReqs()) {
                if (!(isAwareOfPotentialProvider(req) && dependenciesFound(existing.get(req)))) {
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
        assert (dependenciesFulfilled.containsKey(item.getIFulfillReq())) : "Missing dependency";
        if (!resolved.containsKey(item.getIFulfillReq())) {
            for (Requirement dependOn : item.getReqs()) {
                addWillCrashUnlessAlreadyChecked(dependenciesFulfilled.get(dependOn));
            }
            resolved.put(item.getIFulfillReq(), item);
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
}
