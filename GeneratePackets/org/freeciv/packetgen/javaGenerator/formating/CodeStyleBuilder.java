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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CodeStyleBuilder {
    private final LinkedList<AtomCheck> triggers;
    private final AtomCheck stdIns;
    private final int tryLineBreakAt;
    private final int maxLineBreakAttempts;

    private final Status status = new Status();

    public CodeStyleBuilder(final CodeStyle.Insert standard, int tryLineBreakAt, int maxLineBreakAttempts) {
        this.tryLineBreakAt = tryLineBreakAt;
        this.maxLineBreakAttempts = maxLineBreakAttempts;

        triggers = new LinkedList<AtomCheck>();
        this.stdIns = new AtomCheck(standard) {
            @Override
            public boolean isTrueFor(CodeAtom before, CodeAtom after) {
                return true;
            }
        };
    }

    public Status getStatus() {
        return status;
    }

    public void whenAfter(final CodeAtom atom, CodeStyle.Insert toInsert) {
        triggers.add(new AtomCheckBefore(new Util.OneCondition<CodeAtom>() {
            @Override public boolean isTrueFor(CodeAtom argument) {
                return atom.equals(argument);
            }
        }, toInsert));
    }

    public void previousIs(Util.OneCondition<CodeAtom> test, CodeStyle.Insert toInsert) {
        triggers.add(new AtomCheckBefore(test, toInsert));
    }

    public void whenBefore(final CodeAtom atom, CodeStyle.Insert toInsert) {
        triggers.add(new AtomCheckAfter(new Util.OneCondition<CodeAtom>() {
            @Override public boolean isTrueFor(CodeAtom argument) {
                return atom.equals(argument);
            }
        }, toInsert));
    }

    public void nextIs(Util.OneCondition<CodeAtom> test, CodeStyle.Insert toInsert) {
        triggers.add(new AtomCheckAfter(test, toInsert));
    }

    public void whenBetween(final CodeAtom before, final CodeAtom after, CodeStyle.Insert toInsert) {
        triggers.add(new AtomCheckBetween(new Util.TwoConditions<CodeAtom, CodeAtom>() {
            @Override public boolean isTrueFor(CodeAtom left, CodeAtom right) {
                return before.equals(left) && after.equals(right);
            }
        }, toInsert));
    }

    public void atTheEnd(CodeStyle.Insert toInsert) {
        triggers.add(new AtomCheck(toInsert) {
            @Override
            public boolean isTrueFor(CodeAtom before, CodeAtom after) {
                return null == after;
            }
        });
    }

    public CodeStyle getStyle() {
        return new CodeStyle() {
            // Prevent rules added to the builder after style construction from being added to the style
            final ArrayList<AtomCheck> rules;
            {
                rules = new ArrayList<AtomCheck>(triggers);
                rules.add(stdIns);
            }

            @Override
            public List<String> asFormattedLines(CodeAtoms from) {
                final CodeAtom[] atoms = from.getAtoms();
                LinkedList<String> out = new LinkedList<String>();

                int pointerAfter = 0;
                int lineBeganAt;
                while (pointerAfter < atoms.length) {
                    StringBuilder line = new StringBuilder();
                    lineBeganAt = pointerAfter;
                    status.lineBreakTry = 0;
                    line: while (pointerAfter < atoms.length) {
                        line.append(atoms[pointerAfter].get());

                        if (tryLineBreakAt < line.length() && status.lineBreakTry < maxLineBreakAttempts) {
                            pointerAfter = lineBeganAt;
                            status.lineBreakTry++;
                            line = new StringBuilder();
                            continue;
                        }

                        switch (Util.<CodeAtom, CodeAtom, AtomCheck>getFirstFound(
                                rules,
                                getOrNull(atoms, pointerAfter),
                                getOrNull(atoms, pointerAfter + 1)
                        ).getToInsert()) {
                            case SPACE:
                                pointerAfter++;
                                line.append(" ");
                                break;
                            case LINE_BREAK:
                                pointerAfter++;
                                break line;
                            case NOTHING:
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
        };
    }

    private abstract class AtomCheck implements Util.TwoConditions<CodeAtom, CodeAtom> {
        private final CodeStyle.Insert toInsert;

        protected AtomCheck(CodeStyle.Insert toInsert) {
            this.toInsert = toInsert;
        }

        public CodeStyle.Insert getToInsert() {
            return toInsert;
        }

        public abstract boolean isTrueFor(CodeAtom before, CodeAtom after);
    }

    private class AtomCheckBefore extends AtomCheck {
        private final Util.OneCondition<CodeAtom> test;

        private AtomCheckBefore(Util.OneCondition<CodeAtom> test, CodeStyle.Insert toInsert) {
            super(toInsert);
            this.test = test;
        }

        public boolean isTrueFor(CodeAtom before, CodeAtom after) {
            return null != before && test.isTrueFor(before);
        }
    }

    private class AtomCheckAfter extends AtomCheck {
        private final Util.OneCondition<CodeAtom> test;

        private AtomCheckAfter(Util.OneCondition<CodeAtom> test, CodeStyle.Insert toInsert) {
            super(toInsert);
            this.test = test;
        }

        public boolean isTrueFor(CodeAtom before, CodeAtom after) {
            return null != after && test.isTrueFor(after);
        }
    }

    private class AtomCheckBetween extends AtomCheck {
        private final Util.TwoConditions<CodeAtom, CodeAtom> test;

        private AtomCheckBetween(Util.TwoConditions<CodeAtom, CodeAtom> test, CodeStyle.Insert toInsert) {
            super(toInsert);
            this.test = test;
        }

        public boolean isTrueFor(CodeAtom before, CodeAtom after) {
            return null != before && null != after && test.isTrueFor(before, after);
        }
    }

    public static class Status {
        private int lineBreakTry = 0;

        private Status() {
        }

        public int getLineBreakTry() {
            return lineBreakTry;
        }
    }
}
