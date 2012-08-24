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

package org.freeciv.packetgen.javaGenerator.formating;

import org.freeciv.packetgen.javaGenerator.CodeAtoms;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

public interface CodeStyle {
    public List<String> asFormattedLines(CodeAtoms from);

    enum Action {
        DO_NOTHING,
        INSERT_SPACE,
        BREAK_LINE_BLOCK,
        BREAK_LINE,
        RESET_LINE,
        SCOPE_ENTER,
        SCOPE_EXIT
    }

    public static class ScopeStack<Scope extends CodeStyleBuilder.ScopeInfo> {
        private final Constructor<Scope> kind;
        private final LinkedList<Scope> stack;

        public ScopeStack(Class<Scope> kind)
                throws NoSuchMethodException, IllegalAccessException, InstantiationException {
            this.kind = kind.getConstructor();
            this.stack = new LinkedList<Scope>();
            this.stack.push(kind.newInstance());
        }

        public Scope get() {
            return stack.peekLast();
        }

        public void open() {
            try {
                this.stack.push(kind.newInstance());
            } catch (InstantiationException e) {
                throw new Error("Exception thrown after initialization but not during?", e);
            } catch (IllegalAccessException e) {
                throw new Error("Exception thrown after initialization but not during?", e);
            } catch (InvocationTargetException e) {
                throw new Error("Exception thrown after initialization but not during?", e);
            }
        }

        public void close() {
            if (this.stack.size() < 2)
                throw new IllegalStateException("Tried to close newer opened scope");

            this.stack.pop();
        }
    }
}
