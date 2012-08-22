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

import java.util.*;

public class CodeStyleBuilder {
    private final LinkedList<AtomCheck> triggers;
    private final HashMap<CodeAtom, CodeStyle.Action> chScopeBefore;
    private final HashMap<CodeAtom, CodeStyle.Action> chScopeAfter;
    private final AtomCheck stdIns;
    private final int tryLineBreakAt;
    private final int maxLineBreakAttempts;

    public CodeStyleBuilder(final CodeStyle.Action standard, int tryLineBreakAt, int maxLineBreakAttempts) {
        this.tryLineBreakAt = tryLineBreakAt;
        this.maxLineBreakAttempts = maxLineBreakAttempts;

        triggers = new LinkedList<AtomCheck>();
        this.stdIns = AtomCheck.alwaysTrue(standard);

        this.chScopeBefore = new HashMap<CodeAtom, CodeStyle.Action>();
        this.chScopeAfter = new HashMap<CodeAtom, CodeStyle.Action>();
    }

    public void changeScopeAfter(CodeAtom atom, CodeStyle.Action change) {
        chScopeAfter.put(atom, change);
    }

    public void changeScopeBefore(CodeAtom atom, CodeStyle.Action change) {
        chScopeBefore.put(atom, change);
    }

    public void whenAfter(final CodeAtom atom, CodeStyle.Action toInsert) {
        triggers.add(AtomCheck.leftIs(atom, toInsert));
    }

    public void whenAfter(final CodeAtom atom, CodeStyle.Action toInsert, Util.OneCondition<ScopeInfo> scopeCond) {
        triggers.add(AtomCheck.leftIs(atom, toInsert, scopeCond));
    }

    public void whenBefore(final CodeAtom atom, CodeStyle.Action toInsert) {
        triggers.add(AtomCheck.rightIs(atom, toInsert));
    }

    public void whenBefore(final CodeAtom atom, CodeStyle.Action toInsert, Util.OneCondition<ScopeInfo> scopeCond) {
        triggers.add(AtomCheck.rightIs(atom, toInsert, scopeCond));
    }

    public void whenBetween(final CodeAtom before, final CodeAtom after, CodeStyle.Action toInsert) {
        triggers.add(AtomCheck.leftAndRightIs(before, after, toInsert));
    }

    public void whenBetween(final CodeAtom before, final CodeAtom after, CodeStyle.Action toInsert,
                            Util.OneCondition<ScopeInfo> scopeCond) {
        triggers.add(AtomCheck.leftAndRightIs(before, after, toInsert, scopeCond));
    }

    public void atTheEnd(CodeStyle.Action toInsert) {
        triggers.add(AtomCheck.atTheEnd(toInsert));
    }

    public CodeStyle getStyle() {
        return new CodeStyle() {
            // Prevent rules added to the builder after style construction from being added to the style
            final ArrayList<AtomCheck> firstMatchOnly;
            {
                firstMatchOnly = new ArrayList<AtomCheck>(triggers);
                firstMatchOnly.add(stdIns);
            }

            final ArrayList<AtomCheck> triggerMany = new ArrayList<AtomCheck>(); {
                for (CodeAtom atom : chScopeAfter.keySet())
                    triggerMany.add(AtomCheck.leftIs(atom, chScopeAfter.get(atom)));
                for (CodeAtom atom : chScopeBefore.keySet())
                    triggerMany.add(AtomCheck.rightIs(atom, chScopeBefore.get(atom)));
            }

            @Override
            public List<String> asFormattedLines(CodeAtoms from) {
                final CodeAtom[] atoms = from.getAtoms();
                LinkedList<String> out = new LinkedList<String>();

                ScopeStack<ScopeInfo> scopeStack = null;
                try {
                    scopeStack = new ScopeStack<ScopeInfo>(ScopeInfo.class);
                } catch (NoSuchMethodException e) {
                    throw new Error("Could not initialize ScopeStack", e);
                } catch (IllegalAccessException e) {
                    throw new Error("Could not initialize ScopeStack", e);
                } catch (InstantiationException e) {
                    throw new Error("Could not initialize ScopeStack", e);
                }

                ArrayList<CompiledAtomCheck> firstMatchOnlyKnowStack = new ArrayList<CompiledAtomCheck>();
                for (AtomCheck rule : firstMatchOnly) {
                    firstMatchOnlyKnowStack.add(rule.forStack(scopeStack));
                }

                ArrayList<CompiledAtomCheck> allMatchesKnowStack = new ArrayList<CompiledAtomCheck>();
                for (AtomCheck rule : triggerMany) {
                    allMatchesKnowStack.add(rule.forStack(scopeStack));
                }

                int pointerAfter = 0;
                int lineBeganAt;
                while (pointerAfter < atoms.length) {
                    StringBuilder line = new StringBuilder();
                    lineBeganAt = pointerAfter;
                    line: while (pointerAfter < atoms.length) {
                        for (CompiledAtomCheck rule : allMatchesKnowStack)
                            if (rule.isTrueFor(getOrNull(atoms, pointerAfter), getOrNull(atoms, pointerAfter + 1)))
                                doAction(rule.getToInsert(), scopeStack);

                        line.append(atoms[pointerAfter].get());

                        if (tryLineBreakAt < line.length() && scopeStack.get().lineBreakTry < maxLineBreakAttempts) {
                            pointerAfter = lineBeganAt;
                            scopeStack.get().lineBreakTry++;
                            line = new StringBuilder();
                            continue;
                        }

                        switch (Util.<CodeAtom, CodeAtom, CompiledAtomCheck>getFirstFound(
                                firstMatchOnlyKnowStack,
                                getOrNull(atoms, pointerAfter),
                                getOrNull(atoms, pointerAfter + 1)
                        ).getToInsert()) {
                            case INSERT_SPACE:
                                pointerAfter++;
                                line.append(" ");
                                break;
                            case BREAK_LINE:
                                pointerAfter++;
                                break line;
                            case DO_NOTHING:
                                pointerAfter++;
                                break;
                        }
                    }
                    out.add(line.toString());
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

            private void doAction(Action action, CodeStyle.ScopeStack<ScopeInfo> scopeStack) {
                switch (action) {
                    case SCOPE_ENTER:
                        scopeStack.open();
                        break;
                    case SCOPE_EXIT:
                        scopeStack.close();
                        break;
                }
            }
        };
    }

    private static class CompiledAtomCheck implements Util.TwoConditions<CodeAtom, CodeAtom> {
        private final AtomCheck check;
        private final CodeStyle.ScopeStack<ScopeInfo> stack;

        private CompiledAtomCheck(AtomCheck check, CodeStyle.ScopeStack stack) {
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

    private static class AtomCheck {
        private static final Util.OneCondition<ScopeInfo> ignoresScope = new Util.OneCondition<ScopeInfo>() {
            @Override public boolean isTrueFor(ScopeInfo argument) {
                return true;
            }
        };

        private final Util.TwoConditions<CodeAtom, CodeAtom> test;
        private final CodeStyle.Action toInsert;
        private final Util.OneCondition<ScopeInfo> scopeTest;

        public AtomCheck(Util.TwoConditions<CodeAtom, CodeAtom> positionTest, CodeStyle.Action toInsert,
                         Util.OneCondition<ScopeInfo> scopeTest) {
            this.test = positionTest;
            this.scopeTest = scopeTest;
            this.toInsert = toInsert;
        }

        public CompiledAtomCheck forStack(CodeStyle.ScopeStack scope) {
            return new CompiledAtomCheck(this, scope);
        }

        public CodeStyle.Action getToInsert() {
            return toInsert;
        }

        public Util.TwoConditions<CodeAtom, CodeAtom> getCheck() {
            return test;
        }

        public Util.OneCondition<ScopeInfo> getScopeTest() {
            return scopeTest;
        }

        public static AtomCheck leftIs(final CodeAtom atom, CodeStyle.Action toInsert) {
            return leftIs(atom, toInsert, ignoresScope);
        }

        public static AtomCheck leftIs(final CodeAtom atom, CodeStyle.Action toInsert,
                                          Util.OneCondition<ScopeInfo> scopeCond) {
            return new AtomCheck(new Util.TwoConditions<CodeAtom, CodeAtom>() {
                @Override public boolean isTrueFor(CodeAtom before, CodeAtom after) {
                    return null != before && atom.equals(before);
                }
            }, toInsert, scopeCond);
        }

        public static AtomCheck rightIs(final CodeAtom atom, CodeStyle.Action toInsert) {
            return rightIs(atom, toInsert, ignoresScope);
        }

        public static AtomCheck rightIs(final CodeAtom atom, CodeStyle.Action toInsert,
                                        Util.OneCondition<ScopeInfo> scopeCond) {
            return new AtomCheck(new Util.TwoConditions<CodeAtom, CodeAtom>() {
                @Override public boolean isTrueFor(CodeAtom before, CodeAtom after) {
                    return null != after && atom.equals(after);
                }
            }, toInsert, scopeCond);
        }

        public static AtomCheck leftAndRightIs(final CodeAtom before, final CodeAtom after, CodeStyle.Action toInsert) {
            return leftAndRightIs(before, after, toInsert, ignoresScope);
        }

        public static AtomCheck leftAndRightIs(final CodeAtom before, final CodeAtom after, CodeStyle.Action toInsert,
                                Util.OneCondition<ScopeInfo> scopeCond) {
            return new AtomCheck(new Util.TwoConditions<CodeAtom, CodeAtom>() {
                @Override public boolean isTrueFor(CodeAtom left, CodeAtom right) {
                    return null != left && null != right && before.equals(left) && after.equals(right);
                }
            }, toInsert, scopeCond);
        }

        public static AtomCheck atTheEnd(CodeStyle.Action toInsert) {
            return new AtomCheck(new Util.TwoConditions<CodeAtom, CodeAtom>() {
                @Override
                public boolean isTrueFor(CodeAtom before, CodeAtom after) {
                    return null == after;
                }
            }, toInsert, ignoresScope);
        }

        public static AtomCheck alwaysTrue(CodeStyle.Action toInsert) {
            return new AtomCheck(new Util.TwoConditions<CodeAtom, CodeAtom>() {
                @Override
                public boolean isTrueFor(CodeAtom before, CodeAtom after) {
                    return true;
                }
            }, toInsert, ignoresScope);
        }
    }

    public static class ScopeInfo {
        private int lineBreakTry = 0;

        public int getLineBreakTry() {
            return lineBreakTry;
        }
    }
}
