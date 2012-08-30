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
        INDENT,
        SCOPE_ENTER,
        SCOPE_EXIT
    }

    public static class ScopeStack<Scope extends ScopeStack.ScopeInfo> {
        private final Constructor<Scope> kind;
        private final LinkedList<Scope> stack;

        public ScopeStack(Class<Scope> kind)
                throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
            this.kind = kind.getConstructor();
            this.stack = new LinkedList<Scope>();
            addNewScopeFrame();
        }

        public Scope get() {
            return stack.peekFirst();
        }

        public int getIndentSum() {
            int toInd = 0;
            for (Scope info : stack)
                toInd += info.getExtraIndent();

            return toInd;
        }

        public void open() {
            try {
                addNewScopeFrame();
            } catch (InstantiationException e) {
                throw new Error("Exception thrown after initialization but not during?", e);
            } catch (IllegalAccessException e) {
                throw new Error("Exception thrown after initialization but not during?", e);
            } catch (InvocationTargetException e) {
                throw new Error("Exception thrown after initialization but not during?", e);
            }
        }

        private void addNewScopeFrame() throws InstantiationException, IllegalAccessException, InvocationTargetException {
            this.stack.addFirst(kind.newInstance());
        }

        public void close() {
            if (this.stack.size() < 2)
                throw new IllegalStateException("Tried to close newer opened scope");

            this.stack.pop();
        }

        public static class ScopeInfo {
            private int beganAt = 0;
            private int beganAtLine = 0;
            private int lineLength = 0;
            private int nextLen = 0;
            private String lineUpToScope = "";

            private int extraIndent = 0;
            private final LinkedList<String> hints = new LinkedList<String>();

            public int getLineLength() {
                return lineLength;
            }

            void setLineLength(int length) {
                lineLength = length;
            }

            public void setBeganAt(int atomNumber) {
                beganAt = atomNumber;
            }

            public int getBeganAt() {
                return beganAt;
            }

            public void setLineUpToScope(String line) {
                lineUpToScope = line;
            }

            public String getLineUpToScope() {
                return lineUpToScope;
            }

            public void setBeganAtLine(int number) {
                beganAtLine = number;
            }

            public int getBeganAtLine() {
                return beganAtLine;
            }

            final public String seeTopHint() {
                return hints.peekFirst();
            }

            final void addHint(String hint) {
                hints.addFirst(hint);
            }

            final void removeTopHint(String hint) {
                if (hints.isEmpty())
                    throw new UnsupportedOperationException("Tried to remove the top hint when no hints are there");

                if (hints.peekFirst().equals(hint))
                    hints.removeFirst();
                else
                    throw new IllegalArgumentException("Asked to remove a different hint than the top one");
            }

            public int getExtraIndent() {
                return extraIndent;
            }

            public void setExtraIndent(int add) {
                extraIndent = add;
            }

            void setNextLen(int length) {
                nextLen = length;
            }

            public int getNextLen() {
                return nextLen;
            }
        }
    }
}
