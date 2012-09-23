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

    /**
     * Impose a total order on some objects of the type IDependency that contains everything required by all of its
     * elements and that has no circular requirements.
     *
     * First the number of requirements are compared. Since all requirements of requirements are counted as a
     * requirement anything that require another element will be larger than it as it require the element and the
     * element's dependencies. This way the partial order from required to requires is preserved. The next level is to
     * compare the requirement provided by the IDependency.
     *
     */
    public static class TotalOrderNoCircles implements Comparator<IDependency> {
        private final HashMap<Requirement, Collection<Requirement>> seed;
        private final HashMap<Requirement, Set<Requirement>> found;
        private final boolean strict;

        /**
         * Construct a comparator for some objects of the type IDependency
         * @param othersCanBeFoundIn should contain the elements each element in it requires without circles
         * @param beStrict look for circular references and missing requirements
         */
        private TotalOrderNoCircles(Iterable<IDependency> othersCanBeFoundIn, boolean beStrict) {
            this.strict = beStrict;
            this.found = new HashMap<Requirement, Set<Requirement>>();

            this.seed = new HashMap<Requirement, Collection<Requirement>>();
            for (IDependency dep : othersCanBeFoundIn) {
                seed.put(dep.getIFulfillReq(), dep.getReqs());
            }
        }

        /**
         * Construct a comparator for objects of the type IDependency in othersCanBeFoundIn
         * @param othersCanBeFoundIn where to look for requirements.
         */
        public TotalOrderNoCircles(Iterable<IDependency> othersCanBeFoundIn) {
            this(othersCanBeFoundIn, true);
        }

        private void findTransitiveRequirements(Requirement req) {
            if (!found.containsKey(req)) {
                HashSet<Requirement> myRequirements = new HashSet<Requirement>(seed.get(req));
                found.put(req, myRequirements);
                if (!myRequirements.isEmpty()) {
                    if (strict && !seed.containsKey(req)) //TODO: Test with JUnit
                        throw new Error(req + " not provided during construction of ordering");
                    for (Requirement subReq : seed.get(req)) {
                        findTransitiveRequirements(subReq);
                        Set<Requirement> itsRequirements = found.get(subReq);
                        if (strict && itsRequirements.contains(req)) //TODO: Test with JUnit
                            throw new Error(req + " seems to be a part of a circular reference");
                        myRequirements.addAll(itsRequirements);
                    }
                }
            }
        }

        @Override
        public int compare(IDependency left, IDependency right) {
            findTransitiveRequirements(left.getIFulfillReq());
            findTransitiveRequirements(right.getIFulfillReq());

            int leftSize = found.get(left.getIFulfillReq()).size();
            int rigthSize = found.get(right.getIFulfillReq()).size();

            if (leftSize < rigthSize)
                return -1;
            else if (rigthSize < leftSize)
                return 1;
            else
                return left.getIFulfillReq().compareTo(right.getIFulfillReq());
        }
    }
}
