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

import org.freeciv.Util;
import org.freeciv.packetgen.javaGenerator.*;
import org.freeciv.packetgen.javaGenerator.IR.CodeAtom;

import java.util.*;

public class CodeStyleBuilder<ScopeInfoKind extends CodeStyleBuilder.ScopeInfo> {
    private final LinkedList<AtomCheck<ScopeInfoKind>> triggers;
    private final LinkedList<AtomCheck<ScopeInfoKind>> many;
    private final Class<ScopeInfoKind> scopeMaker;
    private final AtomCheck<ScopeInfoKind> stdIns;

    public CodeStyleBuilder(final CodeStyle.Action standard, Class<ScopeInfoKind> scopeMaker) {
        triggers = new LinkedList<AtomCheck<ScopeInfoKind>>();
        this.scopeMaker = scopeMaker;
        this.stdIns = AtomCheck.alwaysTrue(standard);

        this.many = new LinkedList<AtomCheck<ScopeInfoKind>>();
    }

    public void alwaysOnState(Util.OneCondition<ScopeInfoKind> when, CodeStyle.Action doThis) {
        many.add(AtomCheck.<ScopeInfoKind>scopeIs(when, doThis));
    }

    public void alwaysAfter(CodeAtom atom, CodeStyle.Action change) {
        many.add(AtomCheck.<ScopeInfoKind>leftIs(atom, change));
    }

    public void alwaysBefore(CodeAtom atom, CodeStyle.Action change) {
        many.add(AtomCheck.<ScopeInfoKind>rightIs(atom, change));
    }

    public void whenAfter(final CodeAtom atom, CodeStyle.Action toInsert) {
        triggers.add(AtomCheck.<ScopeInfoKind>leftIs(atom, toInsert));
    }

    public void whenAfter(final CodeAtom atom, CodeStyle.Action toInsert, Util.OneCondition<ScopeInfoKind> scopeCond) {
        triggers.add(AtomCheck.<ScopeInfoKind>leftIs(atom, toInsert, scopeCond));
    }

    public void whenBefore(final CodeAtom atom, CodeStyle.Action toInsert) {
        triggers.add(AtomCheck.<ScopeInfoKind>rightIs(atom, toInsert));
    }

    public void whenBefore(final CodeAtom atom, CodeStyle.Action toInsert, Util.OneCondition<ScopeInfoKind> scopeCond) {
        triggers.add(AtomCheck.<ScopeInfoKind>rightIs(atom, toInsert, scopeCond));
    }

    public void whenBetween(final CodeAtom before, final CodeAtom after, CodeStyle.Action toInsert) {
        triggers.add(AtomCheck.<ScopeInfoKind>leftAndRightIs(before, after, toInsert));
    }

    public void whenBetween(final CodeAtom before, final CodeAtom after, CodeStyle.Action toInsert,
                            Util.OneCondition<ScopeInfoKind> scopeCond) {
        triggers.add(AtomCheck.<ScopeInfoKind>leftAndRightIs(before, after, toInsert, scopeCond));
    }

    public void atTheEnd(CodeStyle.Action toInsert) {
        triggers.add(AtomCheck.<ScopeInfoKind>atTheEnd(toInsert));
    }

    public void atTheBeginning(CodeStyle.Action toInsert) {
        triggers.add(AtomCheck.<ScopeInfoKind>atTheBeginning(toInsert));
    }

    public CodeStyle getStyle() {
        return new CodeStyle() {
            // Prevent rules added to the builder after style construction from being added to the style
            final ArrayList<AtomCheck<ScopeInfoKind>> firstMatchOnly;
            {
                firstMatchOnly = new ArrayList<AtomCheck<ScopeInfoKind>>(triggers);
                firstMatchOnly.add(stdIns);
            }

            final ArrayList<AtomCheck<ScopeInfoKind>> triggerMany = new ArrayList<AtomCheck<ScopeInfoKind>>(many);

            @Override
            public List<String> asFormattedLines(CodeAtoms from) {
                final CodeAtom[] atoms = from.getAtoms();
                LinkedList<String> out = new LinkedList<String>();

                ScopeStack<ScopeInfoKind> scopeStack = null;
                try {
                    scopeStack = new ScopeStack<ScopeInfoKind>(scopeMaker);
                } catch (NoSuchMethodException e) {
                    throw new Error("Could not initialize ScopeStack", e);
                } catch (IllegalAccessException e) {
                    throw new Error("Could not initialize ScopeStack", e);
                } catch (InstantiationException e) {
                    throw new Error("Could not initialize ScopeStack", e);
                }

                ArrayList<CompiledAtomCheck<ScopeInfoKind>> firstMatchOnlyKnowStack =
                        new ArrayList<CompiledAtomCheck<ScopeInfoKind>>();
                for (AtomCheck<ScopeInfoKind> rule : firstMatchOnly) {
                    firstMatchOnlyKnowStack.add(rule.forStack(scopeStack));
                }

                ArrayList<CompiledAtomCheck> allMatchesKnowStack = new ArrayList<CompiledAtomCheck>();
                for (AtomCheck<ScopeInfoKind> rule : triggerMany) {
                    allMatchesKnowStack.add(rule.forStack(scopeStack));
                }

                int pointerAfter = -1;
                int lineBeganAt;
                while (pointerAfter < atoms.length) {
                    StringBuilder line = new StringBuilder();
                    lineBeganAt = pointerAfter;
                    boolean addBreak = false;
                    boolean addBlank = false;
                    line: while (pointerAfter < atoms.length) {
                        for (CompiledAtomCheck rule : allMatchesKnowStack)
                            if (rule.isTrueFor(getOrNull(atoms, pointerAfter), getOrNull(atoms, pointerAfter + 1)))
                                switch (rule.getToInsert()) {
                                    case SCOPE_ENTER:
                                        scopeStack.open();
                                        break;
                                    case SCOPE_EXIT:
                                        scopeStack.close();
                                        break;
                                    case RESET_LINE:
                                        pointerAfter = lineBeganAt;
                                        line = new StringBuilder();
                                        continue line;
                                }

                        if (0 <= pointerAfter)
                            line.append(atoms[pointerAfter].get());
                        scopeStack.get().setLineLength(line.length());

                        switch (Util.<CodeAtom, CodeAtom, CompiledAtomCheck<ScopeInfoKind>>getFirstFound(
                                firstMatchOnlyKnowStack,
                                getOrNull(atoms, pointerAfter),
                                getOrNull(atoms, pointerAfter + 1)
                        ).getToInsert()) {
                            case INSERT_SPACE:
                                line.append(" ");
                                break;
                            case BREAK_LINE_BLOCK:
                                addBreak = true;
                                addBlank = true;
                                break;
                            case BREAK_LINE:
                                addBreak = true;
                                break;
                            case DO_NOTHING:
                                break;
                        }

                        pointerAfter++;
                        if (addBreak)
                            break line;
                    }
                    out.add(line.toString());
                    if (addBlank) {
                        out.add("");
                    }
                }
                return out;
            }

            private CodeAtom getOrNull(CodeAtom[] atoms, int pos) {
                try {
                    return atoms[pos];
                } catch (ArrayIndexOutOfBoundsException e) {
                    return null;
                }
            }

        };
    }

    private static class CompiledAtomCheck<ScopeInfoKind extends CodeStyleBuilder.ScopeInfo>
            implements Util.TwoConditions<CodeAtom, CodeAtom> {
        private final AtomCheck<ScopeInfoKind> check;
        private final CodeStyle.ScopeStack<ScopeInfoKind> stack;

        private CompiledAtomCheck(AtomCheck<ScopeInfoKind> check, CodeStyle.ScopeStack<ScopeInfoKind> stack) {
            this.check = check;
            this.stack = stack;
        }

        public CodeStyle.Action getToInsert() {
            return check.getToInsert();
        }

        public boolean isTrueFor(CodeAtom before, CodeAtom after) {
            return check.getCheck().isTrueFor(before, after) && check.getScopeTest().isTrueFor(stack.get());
        }
    }

    private static class AtomCheck<ScopeInfoKind extends CodeStyleBuilder.ScopeInfo> {
        private static <ScopeInfoKind extends CodeStyleBuilder.ScopeInfo> Util.OneCondition<ScopeInfoKind> ignoresScope() {
            return new Util.OneCondition<ScopeInfoKind>() {
                @Override public boolean isTrueFor(ScopeInfoKind argument) {
                    return true;
                }
            };
        }

        private final Util.TwoConditions<CodeAtom, CodeAtom> test;
        private final CodeStyle.Action toInsert;
        private final Util.OneCondition<ScopeInfoKind> scopeTest;

        public AtomCheck(Util.TwoConditions<CodeAtom, CodeAtom> positionTest, CodeStyle.Action toInsert,
                         Util.OneCondition<ScopeInfoKind> scopeTest) {
            this.test = positionTest;
            this.scopeTest = scopeTest;
            this.toInsert = toInsert;
        }

        public CompiledAtomCheck<ScopeInfoKind> forStack(CodeStyle.ScopeStack<ScopeInfoKind> scope) {
            return new CompiledAtomCheck<ScopeInfoKind>(this, scope);
        }

        public CodeStyle.Action getToInsert() {
            return toInsert;
        }

        public Util.TwoConditions<CodeAtom, CodeAtom> getCheck() {
            return test;
        }

        public Util.OneCondition<ScopeInfoKind> getScopeTest() {
            return scopeTest;
        }

        public static <ScopeInfoKind extends ScopeInfo> AtomCheck<ScopeInfoKind> leftIs(final CodeAtom atom,
                                                                        CodeStyle.Action toInsert) {
            return leftIs(atom, toInsert, AtomCheck.<ScopeInfoKind>ignoresScope());
        }

        public static <ScopeInfoKind extends ScopeInfo> AtomCheck<ScopeInfoKind> leftIs(final CodeAtom atom, CodeStyle.Action toInsert,
                                          Util.OneCondition<ScopeInfoKind> scopeCond) {
            return new AtomCheck<ScopeInfoKind>(new Util.TwoConditions<CodeAtom, CodeAtom>() {
                @Override public boolean isTrueFor(CodeAtom before, CodeAtom after) {
                    return null != before && atom.equals(before);
                }
            }, toInsert, scopeCond);
        }

        public static <ScopeInfoKind extends ScopeInfo> AtomCheck<ScopeInfoKind> rightIs(final CodeAtom atom, CodeStyle.Action toInsert) {
            return rightIs(atom, toInsert, AtomCheck.<ScopeInfoKind>ignoresScope());
        }

        public static <ScopeInfoKind extends ScopeInfo> AtomCheck<ScopeInfoKind> rightIs(final CodeAtom atom, CodeStyle.Action toInsert,
                                        Util.OneCondition<ScopeInfoKind> scopeCond) {
            return new AtomCheck<ScopeInfoKind>(new Util.TwoConditions<CodeAtom, CodeAtom>() {
                @Override public boolean isTrueFor(CodeAtom before, CodeAtom after) {
                    return null != after && atom.equals(after);
                }
            }, toInsert, scopeCond);
        }

        public static <ScopeInfoKind extends ScopeInfo> AtomCheck<ScopeInfoKind> leftAndRightIs(final CodeAtom before, final CodeAtom after, CodeStyle.Action toInsert) {
            return leftAndRightIs(before, after, toInsert, AtomCheck.<ScopeInfoKind>ignoresScope());
        }

        public static <ScopeInfoKind extends ScopeInfo> AtomCheck<ScopeInfoKind> leftAndRightIs(final CodeAtom before, final CodeAtom after, CodeStyle.Action toInsert,
                                Util.OneCondition<ScopeInfoKind> scopeCond) {
            return new AtomCheck<ScopeInfoKind>(new Util.TwoConditions<CodeAtom, CodeAtom>() {
                @Override public boolean isTrueFor(CodeAtom left, CodeAtom right) {
                    return null != left && null != right && before.equals(left) && after.equals(right);
                }
            }, toInsert, scopeCond);
        }

        public static <ScopeInfoKind extends ScopeInfo> AtomCheck<ScopeInfoKind> atTheEnd(CodeStyle.Action toInsert) {
            return new AtomCheck<ScopeInfoKind>(new Util.TwoConditions<CodeAtom, CodeAtom>() {
                @Override
                public boolean isTrueFor(CodeAtom before, CodeAtom after) {
                    return null == after;
                }
            }, toInsert, AtomCheck.<ScopeInfoKind>ignoresScope());
        }

        public static <ScopeInfoKind extends ScopeInfo> AtomCheck<ScopeInfoKind> atTheBeginning(CodeStyle.Action toInsert) {
            return new AtomCheck<ScopeInfoKind>(new Util.TwoConditions<CodeAtom, CodeAtom>() {
                @Override
                public boolean isTrueFor(CodeAtom before, CodeAtom after) {
                    return null == before;
                }
            }, toInsert, AtomCheck.<ScopeInfoKind>ignoresScope());
        }

        public static <ScopeInfoKind extends ScopeInfo> AtomCheck<ScopeInfoKind> alwaysTrue(CodeStyle.Action toInsert) {
            return new AtomCheck<ScopeInfoKind>(new Util.TwoConditions<CodeAtom, CodeAtom>() {
                @Override
                public boolean isTrueFor(CodeAtom before, CodeAtom after) {
                    return true;
                }
            }, toInsert, AtomCheck.<ScopeInfoKind>ignoresScope());
        }

        public static <ScopeInfoKind extends ScopeInfo> AtomCheck<ScopeInfoKind> scopeIs(Util.OneCondition<ScopeInfoKind> when,
                                                                       CodeStyle.Action doThis) {
            return new AtomCheck<ScopeInfoKind>(new Util.TwoConditions<CodeAtom, CodeAtom>() {
                @Override
                public boolean isTrueFor(CodeAtom before, CodeAtom after) {
                    return true;
                }
            }, doThis, when);
        }
    }

    public static class ScopeInfo {
        private int lineLength = 0;

        public int getLineLength() {
            return lineLength;
        }

        void setLineLength(int length) {
            lineLength = length;
        }
    }
}
