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

package com.kvilhaugsvik.javaGenerator.representation;

import java.util.Iterator;
import java.util.NoSuchElementException;

class Position implements Iterable<Position> {
    private IR contains = null;
    private Position next = null;
    private final Position previous;

    private Position(Position previous) {
        this.previous = previous;
    }

    static Position first() {
        return new Position(null);
    }

    public Position previous() {
        if (null == previous)
            throw new NoSuchElementException("This is the first element");

        return previous;
    }

    public Position next() {
        if (null == next)
            if (isUsed())
                next = new Position(this);
            else
                throw new IllegalStateException("Use this one before requesting a new one.");
        return next;
    }

    public boolean isFirst() {
        return null == previous;
    }

    void use(IR current) {
        if (null == contains)
            this.contains = current;
        else
            throw new IllegalStateException("Can't set twice. Was: " + contains + " Tried to set to: " + current);
    }

    public boolean isUsed() {
        return null != contains;
    }

    public IR get() {
        if (null == contains)
            throw new NoSuchElementException("The position isn't used");

        return contains;
    }

    @Override
    public Iterator<Position> iterator() {
        final Position start = this;
        return new Iterator<Position>() {
            private Position where = start;

            @Override
            public boolean hasNext() {
                return where.isUsed();
            }

            @Override
            public Position next() {
                if (!hasNext())
                    throw new NoSuchElementException("No more elements");

                where = where.next();
                return where.previous();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Can't remove position");
            }
        };
    }
}
