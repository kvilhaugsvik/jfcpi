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
import org.freeciv.packetgen.javaGenerator.formating.CodeStyle.ScopeStack.ScopeInfo;
import org.freeciv.packetgen.javaGenerator.IR.CodeAtom;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class CodeStyleBuilder<ScopeInfoKind extends ScopeInfo> {
    private final LinkedList<AtomCheck<ScopeInfoKind>> triggers;
    private final LinkedList<AtomCheck<ScopeInfoKind>> many;
    private final Class<ScopeInfoKind> scopeMaker;
    private final AtomCheck<ScopeInfoKind> stdIns;

    public CodeStyleBuilder(final CodeStyle.Action standard, Class<ScopeInfoKind> scopeMaker) {
        triggers = new LinkedList<AtomCheck<ScopeInfoKind>>();
        this.scopeMaker = scopeMaker;
        this.stdIns = AtomCheck.act(standard, EnumSet.<DependsOn>noneOf(DependsOn.class));

        this.many = new LinkedList<AtomCheck<ScopeInfoKind>>();
    }

    public void alwaysOnState(Util.OneCondition<ScopeInfoKind> when, CodeStyle.Action doThis,
                              Triggered<ScopeInfoKind> andRun) {
        many.add(AtomCheck.<ScopeInfoKind>runAndAct(doThis, andRun, EnumSet.<DependsOn>noneOf(DependsOn.class), when));
    }

    public void alwaysAfter(CodeAtom atom, CodeStyle.Action change) {
        many.add(AtomCheck.<ScopeInfoKind>act(change, EnumSet.<DependsOn>of(DependsOn.LEFT_TOKEN),
                condLeftIs(atom)));
    }

    public void alwaysAfter(CodeAtom atom, Triggered<ScopeInfoKind> andRun) {
        many.add(AtomCheck.<ScopeInfoKind>run(andRun, EnumSet.<DependsOn>of(DependsOn.LEFT_TOKEN),
                condLeftIs(atom)));
    }

    public void alwaysBefore(CodeAtom atom, CodeStyle.Action change) {
        many.add(AtomCheck.<ScopeInfoKind>act(change, EnumSet.<DependsOn>of(DependsOn.RIGHT_TOKEN),
                condRightIs(atom)));
    }

    public void whenAfter(final CodeAtom atom, CodeStyle.Action toDo) {
        triggers.add(AtomCheck.<ScopeInfoKind>act(toDo, EnumSet.<DependsOn>of(DependsOn.LEFT_TOKEN),
                condLeftIs(atom)));
    }

    public void whenAfter(final CodeAtom atom, CodeStyle.Action toDo, Util.OneCondition<ScopeInfoKind> scopeCond) {
        triggers.add(AtomCheck.<ScopeInfoKind>act(toDo, EnumSet.<DependsOn>of(DependsOn.LEFT_TOKEN),
                scopeCond, condLeftIs(atom)));
    }

    public void whenAfter(final Class<? extends CodeAtom> kind, CodeStyle.Action toDo,
                           Util.OneCondition<ScopeInfoKind> scopeCond) {
        triggers.add(AtomCheck.<ScopeInfoKind>act(toDo, EnumSet.<DependsOn>of(DependsOn.LEFT_TOKEN),
                scopeCond, condLeftIs(kind)));
    }

    public void whenBefore(final CodeAtom atom, CodeStyle.Action toInsert) {
        triggers.add(AtomCheck.<ScopeInfoKind>act(toInsert, EnumSet.<DependsOn>of(DependsOn.RIGHT_TOKEN),
                condRightIs(atom)));
    }

    public void whenBefore(final CodeAtom atom, CodeStyle.Action toInsert, Util.OneCondition<ScopeInfoKind> scopeCond) {
        triggers.add(AtomCheck.<ScopeInfoKind>act(toInsert, EnumSet.<DependsOn>of(DependsOn.RIGHT_TOKEN),
                condRightIs(atom), scopeCond));
    }

    public void whenBefore(final CodeAtom atom, CodeStyle.Action toInsert,
                           Util.OneCondition<ScopeInfoKind> scopeCond, Triggered<ScopeInfoKind> toRun) {
        triggers.add(AtomCheck.<ScopeInfoKind>runAndAct(toInsert, toRun, EnumSet.<DependsOn>of(DependsOn.RIGHT_TOKEN),
                condRightIs(atom), scopeCond));
    }

    public void whenBefore(final Class<? extends CodeAtom> kind, CodeStyle.Action toDo,
                           Util.OneCondition<ScopeInfoKind> scopeCond) {
        triggers.add(AtomCheck.<ScopeInfoKind>act(toDo, EnumSet.<DependsOn>of(DependsOn.RIGHT_TOKEN),
                condRightIs(kind), scopeCond));
    }

    public void whenBetween(final CodeAtom before, final CodeAtom after, CodeStyle.Action toInsert) {
        triggers.add(AtomCheck.act(toInsert,
                EnumSet.<DependsOn>of(DependsOn.RIGHT_TOKEN, DependsOn.LEFT_TOKEN),
                condLeftIs(before), condRightIs(after)));
    }

    public void whenBetween(final CodeAtom before, final CodeAtom after, CodeStyle.Action toInsert,
                            Util.OneCondition<ScopeInfoKind> scopeCond) {
        triggers.add(AtomCheck.<ScopeInfoKind>act(toInsert,
                EnumSet.<DependsOn>of(DependsOn.RIGHT_TOKEN, DependsOn.LEFT_TOKEN),
                scopeCond, condLeftIs(before), condRightIs(after)));
    }

    public void atTheEnd(CodeStyle.Action toInsert) {
        triggers.add(AtomCheck.<ScopeInfoKind>act(toInsert, EnumSet.<DependsOn>noneOf(DependsOn.class),
                condAtTheEnd()));
    }

    public void atTheBeginning(CodeStyle.Action toInsert) {
        triggers.add(AtomCheck.<ScopeInfoKind>act(toInsert, EnumSet.<DependsOn>noneOf(DependsOn.class),
                condAtTheBeginning()));
    }

    public Util.OneCondition<ScopeInfoKind> condAtTheBeginning() {
        return new Util.OneCondition<ScopeInfoKind>() {
            @Override
            public boolean isTrueFor(ScopeInfoKind argument) {
                return null == argument.getLeft();
            }
        };
    }

    public Util.OneCondition<ScopeInfoKind> condAtTheEnd() {
        return new Util.OneCondition<ScopeInfoKind>() {
            @Override
            public boolean isTrueFor(ScopeInfoKind argument) {
                return null == argument.getRight();
            }
        };
    }

    public Util.OneCondition<ScopeInfoKind> condTopHintIs(final String hint) {
        return new Util.OneCondition<ScopeInfoKind>() {
            @Override
            public boolean isTrueFor(ScopeInfoKind argument) {
                return hint.equals(argument.seeTopHint());
            }
        };
    }

    public Util.OneCondition<ScopeInfoKind> condRightIs(final CodeAtom right) {
        return new Util.OneCondition<ScopeInfoKind>() {
            @Override
            public boolean isTrueFor(ScopeInfoKind argument) {
                return right.equals(argument.getRightAtom());
            }
        };
    }

    public Util.OneCondition<ScopeInfoKind> condRightIs(final Class right) {
        return new Util.OneCondition<ScopeInfoKind>() {
            @Override
            public boolean isTrueFor(ScopeInfoKind argument) {
                return right.isInstance(argument.getRightAtom());
            }
        };
    }

    public Util.OneCondition<ScopeInfoKind> condLeftIs(final CodeAtom left) {
        return new Util.OneCondition<ScopeInfoKind>() {
            @Override
            public boolean isTrueFor(ScopeInfoKind argument) {
                return left.equals(argument.getLeftAtom());
            }
        };
    }

    public Util.OneCondition<ScopeInfoKind> condLeftIs(final Class left) {
        return new Util.OneCondition<ScopeInfoKind>() {
            @Override
            public boolean isTrueFor(ScopeInfoKind argument) {
                return left.isInstance(argument.getLeftAtom());
            }
        };
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
                final IR[] atoms = from.toArray();
                final LinkedList<String> out = new LinkedList<String>();

                FormattingProcess formatting = new FormattingProcess() {
                    private StringBuilder line;
                    private boolean addBreak = false;
                    private boolean addBlank = false;
                    private int pointerAfter = -1;
                    private ScopeStack<ScopeInfoKind> scopeStack = null;

                    @Override
                    public void start() {
                        try {
                            scopeStack = new ScopeStack<ScopeInfoKind>(scopeMaker, pointerAfter, out.size(), "");
                        } catch (NoSuchMethodException e) {
                            throw new Error("Could not initialize ScopeStack", e);
                        } catch (IllegalAccessException e) {
                            throw new Error("Could not initialize ScopeStack", e);
                        } catch (InstantiationException e) {
                            throw new Error("Could not initialize ScopeStack", e);
                        } catch (InvocationTargetException e) {
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

                        while (pointerAfter < atoms.length) {
                            line = newLine(scopeStack);
                            addBreak = false;
                            addBlank = false;
                            line: while (pointerAfter < atoms.length) {
                                scopeStack.get().setNowAt(pointerAfter);
                                scopeStack.get().setLeftToken(getOrNull(atoms, pointerAfter));
                                scopeStack.get().setRightToken(getOrNull(atoms, pointerAfter + 1));
                                if (0 <= pointerAfter) {
                                    line.append(scopeStack.get().getLeftAtom().get());
                                    updateHintsAfter(scopeStack, scopeStack.get().getLeft());
                                }
                                scopeStack.get().setLineLength(line.length());

                                switch (Util.<CodeAtom, CodeAtom, CompiledAtomCheck<ScopeInfoKind>>getFirstFound(
                                        firstMatchOnlyKnowStack,
                                        scopeStack.get().getLeftAtom(),
                                        scopeStack.get().getRightAtom()
                                ).getToInsert()) {
                                    case INSERT_SPACE:
                                        this.insertSpace();
                                        break;
                                    case BREAK_LINE_BLOCK:
                                        this.breakLineBlock();
                                        break;
                                    case BREAK_LINE:
                                        this.breakLine();
                                        break;
                                    case DO_NOTHING:
                                        break;
                                }

                                for (CompiledAtomCheck rule : allMatchesKnowStack)
                                    if (rule.isTrueFor(scopeStack.get().getLeftAtom(), scopeStack.get().getRightAtom()))
                                        switch (rule.getToInsert()) {
                                            case SCOPE_ENTER:
                                                this.scopeEnter();
                                                break;
                                            case SCOPE_EXIT:
                                                this.scopeExit();
                                                break;
                                            case RESET_LINE:
                                                this.scopeReset();
                                                continue line;
                                            case INDENT:
                                                this.indent();
                                                break;
                                        }

                                if (pointerAfter + 1 < atoms.length)
                                    updateHintsBefore(scopeStack, scopeStack.get().getRight());
                                pointerAfter++;
                                if (addBreak)
                                    break line;
                            }
                            out.add(line.toString());
                            if (addBlank) {
                                out.add("");
                            }
                        }
                    }

                    @Override
                    public void insertSpace() {
                        line.append(" ");
                    }

                    @Override
                    public void breakLineBlock() {
                        addBreak = true;
                        addBlank = true;
                    }

                    @Override
                    public void breakLine() {
                        addBreak = true;
                    }

                    @Override
                    public void indent() {
                        scopeStack.get().setExtraIndent(1);
                    }

                    @Override
                    public void scopeReset() {
                        pointerAfter = scopeStack.get().getBeganAt();
                        int lineNumber = scopeStack.get().getBeganAtLine();
                        while(lineNumber < out.size())
                            out.removeLast();
                        line = new StringBuilder(scopeStack.get().getLineUpToScope());
                        scopeStack.get().setLineLength(line.length());
                        scopeStack.get().resetHints();
                        addBreak = false;
                        addBlank = false;
                    }

                    @Override
                    public void scopeEnter() {
                        scopeStack.open(pointerAfter + 1, out.size(), line.toString());
                    }

                    @Override
                    public void scopeExit() {
                        scopeStack.close();
                    }
                };

                formatting.start();
                return out;
            }

            private void updateHintsBefore(ScopeStack<ScopeInfoKind> scopeStack, IR element) {
                for (IR.Hint hint : element.getHintsBefore())
                    scopeStack.get().addHint(hint.get());
            }

            private void updateHintsAfter(ScopeStack<ScopeInfoKind> scopeStack, IR element) {
                for (IR.Hint hint : element.getHintsAfter())
                    scopeStack.get().removeTopHint(hint.get());
            }

            private StringBuilder newLine(ScopeStack<ScopeInfoKind> scopeStack) {
                int toInd = scopeStack.getIndentSum();

                if (toInd < 1)
                    return new StringBuilder();

                char[] indention = new char[toInd];
                Arrays.fill(indention, '\t');
                return new StringBuilder(new String(indention));
            }

            private IR getOrNull(IR[] atoms, int pos) {
                try {
                    return atoms[pos];
                } catch (ArrayIndexOutOfBoundsException e) {
                    return null;
                }
            }

        };
    }

    private enum DependsOn {
        LEFT_TOKEN,
        RIGHT_TOKEN
    }

    private static class CompiledAtomCheck<ScopeInfoKind extends ScopeInfo>
            implements Util.TwoConditions<CodeAtom, CodeAtom> {
        private final AtomCheck<ScopeInfoKind> check;
        private final CodeStyle.ScopeStack<ScopeInfoKind> stack;
        private final EnumSet<DependsOn> preConds;

        private CompiledAtomCheck(AtomCheck<ScopeInfoKind> check, CodeStyle.ScopeStack<ScopeInfoKind> stack) {
            this.check = check;
            this.stack = stack;
            this.preConds = check.getPreConds();
        }

        public CodeStyle.Action getToInsert() {
            if (null != check.getToRun())
                check.getToRun().run(stack.get());
            return check.getToInsert();
        }

        public boolean isTrueFor(CodeAtom before, CodeAtom after) {
            for (DependsOn preCond : preConds)
                switch (preCond) {
                    case LEFT_TOKEN:
                        if (null == before) return false;
                        break;
                    case RIGHT_TOKEN:
                        if (null == after) return false;
                        break;
                }
            for (Util.OneCondition<ScopeInfoKind> cond : check.getTests())
                if (!cond.isTrueFor(stack.get()))
                    return false;

            return true;
        }
    }

    private static class AtomCheck<ScopeInfoKind extends ScopeInfo> {
        private static <ScopeInfoKind extends ScopeInfo> Util.OneCondition<ScopeInfoKind> ignoresScope() {
            return new Util.OneCondition<ScopeInfoKind>() {
                @Override public boolean isTrueFor(ScopeInfoKind argument) {
                    return true;
                }
            };
        }

        private final EnumSet<DependsOn> preConds;
        private final Util.OneCondition<ScopeInfoKind>[] tests;
        private final CodeStyle.Action toInsert;
        private final Triggered<ScopeInfoKind> toRun;

        public AtomCheck(CodeStyle.Action toInsert,
                         Triggered<ScopeInfoKind> toRun,
                         EnumSet<DependsOn> reqs,
                         Util.OneCondition<ScopeInfoKind>... tests) {
            this.tests = tests;
            this.toInsert = toInsert;
            this.toRun = toRun;
            this.preConds = reqs;
        }

        public CompiledAtomCheck<ScopeInfoKind> forStack(CodeStyle.ScopeStack<ScopeInfoKind> scope) {
            return new CompiledAtomCheck<ScopeInfoKind>(this, scope);
        }

        public CodeStyle.Action getToInsert() {
            return toInsert;
        }

        public Triggered<ScopeInfoKind> getToRun() {
            return toRun;
        }

        public Util.OneCondition<ScopeInfoKind>[] getTests() {
            return tests;
        }

        public EnumSet<DependsOn> getPreConds() {
            return preConds;
        }

        public static <ScopeInfoKind extends ScopeInfo> AtomCheck<ScopeInfoKind> act(CodeStyle.Action toInsert,
                                                                                     EnumSet<DependsOn> reqs,
                                                                                     Util.OneCondition<ScopeInfoKind>... tests) {
            return new AtomCheck<ScopeInfoKind>(toInsert, null, reqs, tests);
        }

        public static <ScopeInfoKind extends ScopeInfo> AtomCheck<ScopeInfoKind> run(Triggered<ScopeInfoKind> toRun,
                                                                                     EnumSet<DependsOn> reqs,
                                                                                     Util.OneCondition<ScopeInfoKind>... tests) {
            return new AtomCheck<ScopeInfoKind>(CodeStyle.Action.DO_NOTHING, toRun, reqs, tests);
        }

        public static <ScopeInfoKind extends ScopeInfo> AtomCheck<ScopeInfoKind> runAndAct(CodeStyle.Action toInsert,
                                                                                           Triggered<ScopeInfoKind> toRun,
                                                                                           EnumSet<DependsOn> reqs,
                                                                                           Util.OneCondition<ScopeInfoKind>... tests) {
            return new AtomCheck<ScopeInfoKind>(toInsert, toRun, reqs, tests);
        }
    }

    public static interface Triggered<ScopeInfoKind extends ScopeInfo> {
        public void run(ScopeInfoKind context);
    }
}
