/*
 * Copyright (c) 2013. Sveinung Kvilhaugsvik
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

/**
 * Capture and wrap a Dependency.Item on demand
 */
public class Wrapper implements Dependency.Maker {
    private final Requirement name;
    private final Requirement wrapped;

    /**
     * Create a new Wrapper. Forces the naming convention that the name of a Wrapped should include a ReqKind
     * @param name the name part of the Wrapped requirements name
     * @param kind the kind part of the Wrapped requirements name
     * @param wrapped the Dependency.Item to be captured and wrapped
     */
    public Wrapper(String name, Class<? extends ReqKind> kind, Requirement wrapped) {
        this.name = new Requirement(kind.getSimpleName() + ":" + name, Wrapped.class);
        this.wrapped = wrapped;
    }

    @Override
    public List<Requirement> neededInput(Requirement toProduce) {
        return new ArrayList<Requirement>(){{
            add(wrapped);
        }};
    }

    @Override
    public Required getICanProduceReq() {
        return name;
    }

    @Override
    public Item produce(Requirement toProduce, Item... wasRequired) throws UndefinedException {
        return new Wrapped(name, wasRequired[0]);
    }

    public static class Wrapped implements ReqKind, Dependency.Item {
        private final Requirement req;
        private final Dependency.Item wrapped;

        private Wrapped(Requirement synonymId, Item wrapped) {
            this.req = synonymId;
            this.wrapped = wrapped;
        }

        public Item getWrapped() {
            return wrapped;
        }

        @Override
        public Collection<Requirement> getReqs() {
            return Collections.emptySet();
        }

        @Override
        public Requirement getIFulfillReq() {
            return req;
        }

    }
}
