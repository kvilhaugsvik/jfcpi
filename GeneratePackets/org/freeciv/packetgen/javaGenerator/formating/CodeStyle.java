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
import org.freeciv.packetgen.javaGenerator.IR;

import javax.naming.OperationNotSupportedException;
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

    public interface FormattingProcess {
        void start() throws OperationNotSupportedException;
        void insertSpace();
        void breakLineBlock();
        void breakLine();
        void indent();
        void scopeReset();
        void scopeEnter();
        void scopeExit();
    }

    public static class ScopeStack<Scope extends ScopeStack.ScopeInfo> {
        private final FormattingProcess process;
        private final Constructor<Scope> kind;
        private final LinkedList<Scope> stack;
        private IR leftToken = null;
        private IR rightToken = null;

        public ScopeStack(FormattingProcess process, Class<Scope> kind,
                          int beganAt, int beganAtLine, String lineUpToScope)
                throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
            this.kind = kind.getConstructor(FormattingProcess.class, this.getClass(),
                    int.class, int.class, String.class);
            this.stack = new LinkedList<Scope>();
            this.process = process;
            addNewScopeFrame(beganAt, beganAtLine, lineUpToScope);
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

        public void open(int beganAt, int beganAtLine, String lineUpToScope) {
            try {
                addNewScopeFrame(beganAt, beganAtLine, lineUpToScope);
            } catch (InstantiationException e) {
                throw new Error("Exception thrown after initialization but not during?", e);
            } catch (IllegalAccessException e) {
                throw new Error("Exception thrown after initialization but not during?", e);
            } catch (InvocationTargetException e) {
                throw new Error("Exception thrown after initialization but not during?", e);
            }
        }

        private void addNewScopeFrame(int beganAt, int beganAtLine, String lineUpToScope) throws InstantiationException, IllegalAccessException, InvocationTargetException {
            this.stack.addFirst(kind.newInstance(process, this, beganAt, beganAtLine, lineUpToScope));
        }

        public void close() {
            if (this.stack.size() < 2)
                throw new IllegalStateException("Tried to close newer opened scope");

            this.stack.pop();
        }

        public static class ScopeInfo {
            private final ScopeStack<? extends ScopeInfo> inStack;
            private final int beganAt;
            private final int beganAtLine;
            private final String lineUpToScope;

            private int nowAt = 0;
            private int lineLength = 0;

            private int extraIndent = 0;
            private final LinkedList<String> hints = new LinkedList<String>();
            private final FormattingProcess runningFormatting;

            public ScopeInfo(FormattingProcess runningFormatting, ScopeStack inStack,
                             int beganAt, int beganAtLine, String lineUpToScope) {
                this.inStack = inStack;
                this.beganAt = beganAt;
                this.beganAtLine = beganAtLine;
                this.lineUpToScope = lineUpToScope;
                this.runningFormatting = runningFormatting;
            }

            public int getLineLength() {
                return lineLength;
            }

            void setLineLength(int length) {
                lineLength = length;
            }

            public int getBeganAt() {
                return beganAt;
            }

            public String getLineUpToScope() {
                return lineUpToScope;
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

            final void resetHints() {
                hints.clear();
            }

            public int getExtraIndent() {
                return extraIndent;
            }

            public void setExtraIndent(int add) {
                extraIndent = add;
            }

            public int getRLen() {
                return (null == inStack.rightToken ? 0 : inStack.rightToken.getAtom().get().length());
            }

            void setRightToken(IR rightToken) {
                inStack.rightToken = rightToken;
            }

            public IR getRight() {
                return inStack.rightToken;
            }

            public IR.CodeAtom getRightAtom() {
                return (null == inStack.rightToken ? null : inStack.rightToken.getAtom());
            }

            public int getLLen() {
                return (null == inStack.leftToken ? 0 : inStack.leftToken.getAtom().get().length());
            }

            void setLeftToken(IR leftToken) {
                inStack.leftToken = leftToken;
            }

            public IR getLeft() {
                return inStack.leftToken;
            }

            public IR.CodeAtom getLeftAtom() {
                return (null == inStack.leftToken ? null : inStack.leftToken.getAtom());
            }

            void setNowAt(int at) {
                nowAt = at;
            }

            public int getNowAt() {
                return nowAt;
            }

            public FormattingProcess getRunningFormatting() {
                return runningFormatting;
            }
        }
    }
}
