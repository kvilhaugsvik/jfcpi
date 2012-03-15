package org.freeciv.packetgen;

import java.util.*;

public final class DependencyStore {
    private static final String nullNotAllowed = "null is not an allowed argument here";

    private final HashMap<Requirement, IDependency> resolved = new HashMap<Requirement, IDependency>();
    private final HashMap<Requirement, IDependency> existing = new HashMap<Requirement, IDependency>();
    private final HashMap<Requirement, IDependency> dependenciesFulfilled = new HashMap<Requirement, IDependency>();
    private final HashSet<Requirement> dependenciesUnfulfilled = new HashSet<Requirement>();
    private final HashSet<IDependency> wantsOut = new HashSet<IDependency>();

    public void addPossibleRequirement(IDependency item) {
        if (null == item) throw new NullPointerException(nullNotAllowed);
        existing.put(item.getIFulfillReq(), item);
        dependenciesUnfulfilled.clear();
    }

    public void addWanted(IDependency item) {
        addPossibleRequirement(item);
        wantsOut.add(item);
    }

    public Set<Requirement> getMissingRequirements() {
        resolve();
        return Collections.unmodifiableSet(dependenciesUnfulfilled);
    }

    public Collection<IDependency> getResolved() {
        resolve();
        return resolved.values();
    }

    private boolean declareFulfilled(IDependency item) {
        dependenciesFulfilled.put(item.getIFulfillReq(), item);
        return true;
    }

    private boolean dependenciesFound(IDependency item) {
        assert (null != item) : nullNotAllowed;
        if (dependenciesFulfilled.containsKey(item.getIFulfillReq())) {
            return true;
        } else if (item.getReqs().isEmpty()) {
            return declareFulfilled(item);
        } else if (dependenciesUnfulfilled.contains(item.getIFulfillReq())) {
            return false;
        } else {
            boolean missingReq = false;
            for (Requirement req: item.getReqs()) {
                if (!(existing.containsKey(req) && dependenciesFound(existing.get(req)))) {
                    dependenciesUnfulfilled.add(req);
                    missingReq = true;
                }
            }
            if (missingReq) {
                dependenciesUnfulfilled.add(item.getIFulfillReq());
                return false;
            } else  {
                return declareFulfilled(item);
            }
        }
    }

    private void addWillCrashUnlessAlreadyChecked(IDependency item) {
        assert (null != item) : nullNotAllowed;
        assert (dependenciesFulfilled.containsKey(item.getIFulfillReq())) : "Missing dependency";
        if (!resolved.containsKey(item.getIFulfillReq())) {
            resolved.put(item.getIFulfillReq(), item);
            for (Requirement dependOn: item.getReqs()) {
                addWillCrashUnlessAlreadyChecked(dependenciesFulfilled.get(dependOn));
            }
        }
    }

    private void addToResolvedIfPossible(IDependency item) {
        assert (null != item) : nullNotAllowed;
        if (dependenciesFound(item)) {
            addWillCrashUnlessAlreadyChecked(item);
        }
    }

    private void resolve() {
        for (IDependency toAdd: wantsOut) {
            addToResolvedIfPossible(toAdd);
        }
        wantsOut.removeAll(resolved.keySet());
    }
}