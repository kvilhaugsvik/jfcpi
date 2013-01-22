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

package org.freeciv.packetgen.javaGenerator.representation;

import java.util.Iterator;
import java.util.LinkedList;

class IRHolder {
    private final LinkedList<IR> writeTo = new LinkedList<IR>();
    private final LinkedList<IR> peers = new LinkedList<IR>();
    private final Position start = Position.first();
    private Position current = start;

    public void add(IR elem) {
        if (isNotForwarding())
            peers.add(elem);
        else
            writeTo.peekLast().addChild(elem);
        current.use(elem);
        current = current.next();
    }

    public void childrenFollows(IR elem) {
        add(elem);
        writeTo.add(elem);
    }

    public void stopForwarding(IR.CodeAtom to) {
        if (isNotForwarding())
            throw new IllegalStateException("Wasn't writing children");

            if (writeTo.peekLast().getAtom().equals(to))
                writeTo.removeLast();
            else
                throw new IllegalArgumentException("Writing children of " + writeTo.peekLast().getAtom().get() +
                        " but asked to stop writing children of " + to.get());
    }

    public boolean isNotForwarding() {
        return writeTo.isEmpty();
    }

    public boolean isEmpty() {
        return !start.isUsed();
    }

    public Position getCurrent() {
        return current;
    }

    public IR[] flatArray() {
        if (!isNotForwarding())
            throw new IllegalStateException("Not finished writing children");

        LinkedList<IR> flattened = new LinkedList<IR>();
        for (Position elem : start)
            flattened.add(elem.get());

        return flattened.toArray(new IR[flattened.size()]);
    }

    public Iterator<IR> asTree() {
        return peers.iterator();
    }
}
