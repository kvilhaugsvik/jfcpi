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
import org.freeciv.packetgen.javaGenerator.formating.ScopeStack.ScopeInfo;
import org.freeciv.packetgen.javaGenerator.IR.CodeAtom;

import javax.naming.OperationNotSupportedException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class CodeStyleBuilder<ScopeInfoKind extends ScopeInfo> {
    private final LinkedList<FormattingRule<ScopeInfoKind>> triggers;
    private final LinkedList<FormattingRule<ScopeInfoKind>> many;
    private final Class<ScopeInfoKind> scopeMaker;
    private final FormattingRule<ScopeInfoKind> stdIns;

    public CodeStyleBuilder(final Triggered<ScopeInfoKind> standard, Class<ScopeInfoKind> scopeMaker) {
        triggers = new LinkedList<FormattingRule<ScopeInfoKind>>();
        this.scopeMaker = scopeMaker;
        this.stdIns = new FormattingRule<ScopeInfoKind>(Collections.<Util.OneCondition<ScopeInfoKind>>emptyList(),
                EnumSet.<DependsOn>noneOf(DependsOn.class),
                Arrays.asList(standard));

        this.many = new LinkedList<FormattingRule<ScopeInfoKind>>();
    }

    public void alwaysWhen(List<Util.OneCondition<ScopeInfoKind>> isTrue, EnumSet<DependsOn> deps,
                           List<Triggered<ScopeInfoKind>> actions) {
        many.add(new FormattingRule<ScopeInfoKind>(isTrue, deps, actions));
    }

    public void alwaysWhen(Util.OneCondition<ScopeInfoKind> isTrue, EnumSet<DependsOn> deps,
                           Triggered<ScopeInfoKind>... actions) {
        alwaysWhen(Arrays.<Util.OneCondition<ScopeInfoKind>>asList(isTrue), deps,
                Arrays.<Triggered<ScopeInfoKind>>asList(actions));
    }

    public void whenFirst(List<Util.OneCondition<ScopeInfoKind>> isTrue, EnumSet<DependsOn> deps,
                          List<Triggered<ScopeInfoKind>> actions) {
        triggers.add(new FormattingRule<ScopeInfoKind>(isTrue, deps, actions));
    }

    public void whenFirst(Util.OneCondition<ScopeInfoKind> isTrue, EnumSet<DependsOn> deps,
                          Triggered<ScopeInfoKind>... actions) {
        whenFirst(Arrays.<Util.OneCondition<ScopeInfoKind>>asList(isTrue),
                deps,
                Arrays.<Triggered<ScopeInfoKind>>asList(actions));
    }

    public void whenFirst(Util.OneCondition<ScopeInfoKind> cond1,
                          Util.OneCondition<ScopeInfoKind> cond2,
                          EnumSet<DependsOn> deps,
                          Triggered<ScopeInfoKind>... actions) {
        whenFirst(Arrays.<Util.OneCondition<ScopeInfoKind>>asList(cond1, cond2),
                deps,
                Arrays.<Triggered<ScopeInfoKind>>asList(actions));
    }

    public void whenFirst(Util.OneCondition<ScopeInfoKind> cond1,
                          Util.OneCondition<ScopeInfoKind> cond2,
                          Util.OneCondition<ScopeInfoKind> cond3,
                          EnumSet<DependsOn> deps,
                          Triggered<ScopeInfoKind>... actions) {
        whenFirst(Arrays.<Util.OneCondition<ScopeInfoKind>>asList(cond1, cond2, cond3),
                deps,
                Arrays.<Triggered<ScopeInfoKind>>asList(actions));
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

    public static <ScopeInfoKind extends ScopeInfo> Triggered<ScopeInfoKind> INSERT_SPACE() {
        return new Triggered<ScopeInfoKind>() {
            @Override
            public void run(ScopeInfoKind context) {
                context.getRunningFormatting().insertSpace();
            }
        };
    }

    public Triggered<ScopeInfoKind> INSERT_SPACE = new Triggered<ScopeInfoKind>() {
        @Override
        public void run(ScopeInfoKind context) {
            context.getRunningFormatting().insertSpace();
        }
    };
    public Triggered<ScopeInfoKind> BREAK_LINE_BLOCK = new Triggered<ScopeInfoKind>() {
        @Override
        public void run(ScopeInfoKind context) {
            context.getRunningFormatting().breakLineBlock();
        }
    };
    public Triggered<ScopeInfoKind> BREAK_LINE = new Triggered<ScopeInfoKind>() {
        @Override
        public void run(ScopeInfoKind context) {
            context.getRunningFormatting().breakLine();
        }
    };
    public Triggered<ScopeInfoKind> DO_NOTHING = new Triggered<ScopeInfoKind>() {
        @Override
        public void run(ScopeInfoKind context) {
        }
    };
    public Triggered<ScopeInfoKind> SCOPE_ENTER = new Triggered<ScopeInfoKind>() {
        @Override
        public void run(ScopeInfoKind context) {
            context.getRunningFormatting().scopeEnter();
        }
    };
    public Triggered<ScopeInfoKind> SCOPE_EXIT = new Triggered<ScopeInfoKind>() {
        @Override
        public void run(ScopeInfoKind context) {
            context.getRunningFormatting().scopeExit();
        }
    };
    public Triggered<ScopeInfoKind> RESET_LINE = new Triggered<ScopeInfoKind>() {
        @Override
        public void run(ScopeInfoKind context) {
            context.getRunningFormatting().scopeReset();
        }
    };
    public Triggered<ScopeInfoKind> INDENT = new Triggered<ScopeInfoKind>() {
        @Override
        public void run(ScopeInfoKind context) {
            context.getRunningFormatting().indent();
        }
    };

    public TokensToStringStyle getStyle() {
        return new TokensToStringStyle() {
            // Prevent rules added to the builder after style construction from being added to the style
            final ArrayList<FormattingRule<ScopeInfoKind>> firstMatchOnly;
            {
                firstMatchOnly = new ArrayList<FormattingRule<ScopeInfoKind>>(triggers);
                firstMatchOnly.add(stdIns);
            }

            final ArrayList<FormattingRule<ScopeInfoKind>> triggerMany = new ArrayList<FormattingRule<ScopeInfoKind>>(many);

            @Override
            public List<String> asFormattedLines(CodeAtoms from) {
                final IR[] atoms = from.toArray();
                final LinkedList<String> out = new LinkedList<String>();

                FormattingProcess formatting = new FormattingProcess() {
                    private boolean alreadyStarted = false;

                    private StringBuilder line;
                    private String startOfNext = null;
                    private boolean addBreak = false;
                    private boolean addBlank = false;
                    private int pointerAfter = -1;
                    private boolean reset = false;
                    private ScopeStack<ScopeInfoKind> scopeStack = null;

                    @Override
                    public void start() throws OperationNotSupportedException {
                        if (alreadyStarted)
                            throw new OperationNotSupportedException("Can't start alread started formatting");
                        else
                            alreadyStarted = true;

                        try {
                            scopeStack = new ScopeStack<ScopeInfoKind>(this, scopeMaker,
                                    pointerAfter, out.size(), "");
                        } catch (NoSuchMethodException e) {
                            throw new Error("Could not initialize ScopeStack", e);
                        } catch (IllegalAccessException e) {
                            throw new Error("Could not initialize ScopeStack", e);
                        } catch (InstantiationException e) {
                            throw new Error("Could not initialize ScopeStack", e);
                        } catch (InvocationTargetException e) {
                            throw new Error("Could not initialize ScopeStack", e);
                        }

                        ArrayList<ActiveRule<ScopeInfoKind>> firstMatchOnlyKnowStack =
                                new ArrayList<ActiveRule<ScopeInfoKind>>();
                        for (FormattingRule<ScopeInfoKind> rule : firstMatchOnly) {
                            firstMatchOnlyKnowStack.add(rule.forStack(scopeStack));
                        }

                        ArrayList<ActiveRule> allMatchesKnowStack = new ArrayList<ActiveRule>();
                        for (FormattingRule<ScopeInfoKind> rule : triggerMany) {
                            allMatchesKnowStack.add(rule.forStack(scopeStack));
                        }

                        while (pointerAfter < atoms.length) {
                            line = newLine(scopeStack);
                            if (null != startOfNext) {
                                line.append(startOfNext);
                                startOfNext = null;
                            }
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

                                Util.<CodeAtom, CodeAtom, ActiveRule<ScopeInfoKind>>getFirstFound(
                                        firstMatchOnlyKnowStack,
                                        scopeStack.get().getLeftAtom(),
                                        scopeStack.get().getRightAtom()
                                ).run();
                                if (reset) {
                                    reset = false;
                                    continue line;
                                }

                                for (ActiveRule rule : allMatchesKnowStack) {
                                    if (rule.isTrueFor(scopeStack.get().getLeftAtom(), scopeStack.get().getRightAtom())) {
                                        rule.run();
                                        if (reset) {
                                            reset = false;
                                            continue line;
                                        }
                                    }
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
                        write(" ");
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
                        startOfNext = null;
                        scopeStack.get().setLineLength(line.length());
                        scopeStack.get().resetHints();
                        addBreak = false;
                        addBlank = false;
                        reset = true;
                    }

                    @Override
                    public void scopeEnter() {
                        scopeStack.open(pointerAfter + 1, out.size(), line.toString());
                    }

                    @Override
                    public void scopeExit() {
                        scopeStack.close();
                    }

                    @Override
                    public void insertStar() {
                        write(" * ");
                    }

                    private void write(String toWrite) {
                        if (addBreak)
                            startOfNext = (null == startOfNext ? "" : startOfNext) + toWrite;
                        else
                            line.append(toWrite);
                    }
                };

                try {
                    formatting.start();
                } catch (OperationNotSupportedException e) {
                    throw new Error("Bug: A developer didn't check things properly after a change.", e);
                }
                return out;
            }

            private void updateHintsBefore(ScopeStack<ScopeInfoKind> scopeStack, IR element) {
                for (String hint : element.getHintsBegin())
                    scopeStack.get().addHint(hint);
            }

            private void updateHintsAfter(ScopeStack<ScopeInfoKind> scopeStack, IR element) {
                for (String hint : element.getHintsEnd())
                    scopeStack.get().removeTopHint(hint);
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

    public static enum DependsOn {
        LEFT_TOKEN,
        RIGHT_TOKEN;

        public static final EnumSet<DependsOn> ignore_tokens = EnumSet.noneOf(DependsOn.class);
        public static final EnumSet<DependsOn> token_left = EnumSet.of(DependsOn.LEFT_TOKEN);
        public static final EnumSet<DependsOn> token_right = EnumSet.of(DependsOn.RIGHT_TOKEN);
        public static final EnumSet<DependsOn> token_both = EnumSet.of(DependsOn.RIGHT_TOKEN, DependsOn.LEFT_TOKEN);
    }

    private static class ActiveRule<ScopeInfoKind extends ScopeInfo>
            implements Util.TwoConditions<CodeAtom, CodeAtom> {
        private final FormattingRule<ScopeInfoKind> check;
        private final ScopeStack<ScopeInfoKind> stack;
        private final EnumSet<DependsOn> preConds;

        private ActiveRule(FormattingRule<ScopeInfoKind> check, ScopeStack<ScopeInfoKind> stack) {
            this.check = check;
            this.stack = stack;
            this.preConds = check.getPreConds();
        }

        public void run() {
            for (Triggered<ScopeInfoKind> consequenze : check.getToRun())
                consequenze.run(stack.get());
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

    private static class FormattingRule<ScopeInfoKind extends ScopeInfo> {
        private final EnumSet<DependsOn> preConds;
        private final List<Util.OneCondition<ScopeInfoKind>> tests;
        private final List<Triggered<ScopeInfoKind>> toRun;

        public FormattingRule(List<Util.OneCondition<ScopeInfoKind>> tests,
                              EnumSet<DependsOn> reqs,
                              List<Triggered<ScopeInfoKind>> toRun) {
            this.tests = tests;
            this.toRun = toRun;
            this.preConds = reqs;
        }

        public ActiveRule<ScopeInfoKind> forStack(ScopeStack<ScopeInfoKind> scope) {
            return new ActiveRule<ScopeInfoKind>(this, scope);
        }

        public List<Triggered<ScopeInfoKind>> getToRun() {
            return toRun;
        }

        public List<Util.OneCondition<ScopeInfoKind>> getTests() {
            return tests;
        }

        public EnumSet<DependsOn> getPreConds() {
            return preConds;
        }
    }

    public static interface Triggered<ScopeInfoKind extends ScopeInfo> {
        public void run(ScopeInfoKind context);
    }
}
