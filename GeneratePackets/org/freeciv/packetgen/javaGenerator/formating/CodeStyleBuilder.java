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
import org.freeciv.packetgen.javaGenerator.CodeAtom;
import org.freeciv.packetgen.javaGenerator.CodeAtoms;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CodeStyleBuilder {
    private final LinkedList<AtomCheck> triggers;
    private final AtomCheck stdIns;

    public CodeStyleBuilder(final CodeStyle.Insert standard) {
        triggers = new LinkedList<AtomCheck>();
        this.stdIns = new AtomCheck(standard) {
            @Override
            public boolean isTrueFor(CodeAtom before, CodeAtom after) {
                return true;
            }
        };
    }

    public void previousIs(Util.OneCondition<CodeAtom> test, CodeStyle.Insert toInsert) {
        triggers.add(new AtomCheckBefore(test, toInsert));
    }

    public void nextIs(Util.OneCondition<CodeAtom> test, CodeStyle.Insert toInsert) {
        triggers.add(new AtomCheckAfter(test, toInsert));
    }

    public void isBetween(Util.TwoConditions<CodeAtom, CodeAtom> test, CodeStyle.Insert toInsert) {
        triggers.add(new AtomCheckBetween(test, toInsert));
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
                while (pointerAfter < atoms.length) {
                    StringBuilder line = new StringBuilder();
                    line: while (pointerAfter < atoms.length) {
                        line.append(atoms[pointerAfter].get());

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
}
