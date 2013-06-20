/*
 * Copyright (c) 2011 - 2013. Sveinung Kvilhaugsvik
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

package com.kvilhaugsvik.javaGenerator;

import com.kvilhaugsvik.javaGenerator.formating.CodeStyleBuilder;
import com.kvilhaugsvik.javaGenerator.formating.ScopeStack;
import com.kvilhaugsvik.javaGenerator.formating.TokensToStringStyle;
import com.kvilhaugsvik.javaGenerator.representation.HasAtoms;
import org.freeciv.utility.Util;

import java.util.LinkedList;

public class DefaultStyle {
    public static final TokensToStringStyle DEFAULT_STYLE_INDENT;

    static {
        final CodeStyleBuilder<DefaultStyleScopeInfo> maker =
                new CodeStyleBuilder<DefaultStyleScopeInfo>(
                        CodeStyleBuilder.<DefaultStyleScopeInfo>INSERT_SPACE(),
                        DefaultStyleScopeInfo.class);

        maker.whenFirst(maker.condAtTheBeginning(), CodeStyleBuilder.DependsOn.ignore_tokens, maker.DO_NOTHING);
        maker.whenFirst(maker.condAtTheEnd(), CodeStyleBuilder.DependsOn.ignore_tokens, maker.DO_NOTHING);
        maker.whenFirst(maker.condLeftIs(HasAtoms.LSC), maker.condRightIs(HasAtoms.RSC), CodeStyleBuilder.DependsOn.token_both, maker.DO_NOTHING);
        maker.whenFirst(maker.condLeftIs(HasAtoms.RSC), maker.condRightIs(HasAtoms.ELSE), CodeStyleBuilder.DependsOn.token_both, maker.INSERT_SPACE);
        maker.whenFirst(maker.condLeftIs(HasAtoms.RSC), maker.condRightIs(HasAtoms.CATCH), CodeStyleBuilder.DependsOn.token_both, maker.INSERT_SPACE);
        maker.whenFirst(maker.condRightIs(HasAtoms.RSC), CodeStyleBuilder.DependsOn.token_right, maker.BREAK_LINE);
        maker.whenFirst(new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override
            public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return !TokensToStringStyle.GROUP.equals(argument.seeTopHint());
            }
        }, maker.condLeftIs(HasAtoms.RSC), CodeStyleBuilder.DependsOn.token_left, maker.BREAK_LINE_BLOCK);
        maker.whenFirst(new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override
            public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return !TokensToStringStyle.GROUP.equals(argument.seeTopHint());
            }
        }, maker.condLeftIs(HasAtoms.EOL), CodeStyleBuilder.DependsOn.token_left, maker.BREAK_LINE_BLOCK);
        maker.whenFirst(maker.condTopHintIs(TokensToStringStyle.OUTER_LEVEL), maker.condLeftIs(HasAtoms.EOL),
                CodeStyleBuilder.DependsOn.token_left, maker.BREAK_LINE_BLOCK);
        maker.whenFirst(
                maker.condRightIs(Annotate.Atom.class),
                maker.condLeftIs(Comment.Word.class),
                new Util.OneCondition<DefaultStyleScopeInfo>() {
                    @Override
                    public boolean isTrueFor(DefaultStyleScopeInfo context) {
                        return context.getLineBreakTry() <= 2;
                    }
                },
                CodeStyleBuilder.DependsOn.token_both,
                new CodeStyleBuilder.Triggered<DefaultStyleScopeInfo>() {
                    @Override
                    public void run(DefaultStyleScopeInfo context) {
                        context.lineBreakTry = 3;
                        context.getRunningFormatting().scopeReset();
                    }
                });
        maker.whenFirst(
                maker.condRightIs(Annotate.Atom.class),
                maker.condLeftIs(Comment.Word.class),
                CodeStyleBuilder.DependsOn.token_both,
                maker.BREAK_COMMENT_LINE);
        maker.whenFirst(maker.condRightIs(Annotate.Atom.class), maker.condTopHintIs(TokensToStringStyle.OUTER_LEVEL),
                CodeStyleBuilder.DependsOn.token_right, maker.BREAK_LINE);
        maker.whenFirst(maker.condRightIs(Visibility.Atom.class), maker.condTopHintIs(TokensToStringStyle.OUTER_LEVEL),
                CodeStyleBuilder.DependsOn.token_right, maker.BREAK_LINE);
        maker.whenFirst(
                maker.condTopHintIs(TokensToStringStyle.OUTER_LEVEL),
                maker.condLeftIs(Visibility.Atom.class),
                CodeStyleBuilder.DependsOn.token_left,
                maker.INSERT_SPACE);
        maker.whenFirst(maker.condLeftIs(HasAtoms.EOL), CodeStyleBuilder.DependsOn.token_left, maker.BREAK_LINE);
        maker.whenFirst(maker.condLeftIs(HasAtoms.LSC), CodeStyleBuilder.DependsOn.token_left, maker.BREAK_LINE);
        maker.whenFirst(maker.condLeftIs(HasAtoms.RSC), CodeStyleBuilder.DependsOn.token_left, maker.BREAK_LINE);
        maker.whenFirst(maker.condRightIs(HasAtoms.EOL), CodeStyleBuilder.DependsOn.token_right, maker.DO_NOTHING);
        maker.whenFirst(maker.condRightIs(HasAtoms.FORSEP), CodeStyleBuilder.DependsOn.token_right, maker.DO_NOTHING);
        maker.whenFirst(maker.condLeftIs(HasAtoms.HAS), CodeStyleBuilder.DependsOn.token_left, maker.DO_NOTHING);
        maker.whenFirst(maker.condRightIs(HasAtoms.HAS), CodeStyleBuilder.DependsOn.token_right, maker.DO_NOTHING);
        maker.whenFirst(maker.condRightIs(HasAtoms.RPR), CodeStyleBuilder.DependsOn.token_right, maker.DO_NOTHING);
        maker.whenFirst(maker.condTopHintIs(EnumElements.class.getName()), maker.condLeftIs(HasAtoms.SEP),
                CodeStyleBuilder.DependsOn.token_left, maker.BREAK_LINE);
        maker.whenFirst(maker.condTopHintIs(EnumElements.class.getName()), maker.condLeftIs(HasAtoms.EOL),
                CodeStyleBuilder.DependsOn.token_left, maker.BREAK_LINE_BLOCK);
        maker.whenFirst(
                maker.condLeftIs(HasAtoms.SEP),
                new Util.OneCondition<DefaultStyleScopeInfo>() {
                    @Override public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                        return 1 < argument.getLineBreakTry() &&
                                argument.approachingTheEdge() &&
                                TokensToStringStyle.ARGUMENTS.equals(argument.seeTopHint());
                    }
                },
                CodeStyleBuilder.DependsOn.token_left,
                maker.BREAK_LINE,
                new CodeStyleBuilder.Triggered<DefaultStyleScopeInfo>() {
                    @Override
                    public void run(DefaultStyleScopeInfo context) {
                        context.statementBroken = true;
                    }
                }
        );
        maker.whenFirst(new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override
            public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return 1 < argument.getLineBreakTry() && argument.approachingTheEdge();
            }
        }, maker.condLeftIs(HasAtoms.SEP), CodeStyleBuilder.DependsOn.token_left, maker.BREAK_LINE);
        maker.whenFirst(
                new Util.OneCondition<DefaultStyleScopeInfo>() {
                    @Override
                    public boolean isTrueFor(DefaultStyleScopeInfo context) {
                        return 2 < context.getLineBreakTry() &&
                                (context.getLeftAtom().equals(HasAtoms.CCommentStart) ||
                                        context.getLeftAtom().equals(HasAtoms.JDocStart));
                    }
                },
                CodeStyleBuilder.DependsOn.token_left,
                maker.BREAK_COMMENT_LINE);
        maker.whenFirst(maker.condRightIs(HasAtoms.CCommentEnd),
                new Util.OneCondition<DefaultStyleScopeInfo>() {
                    @Override
                    public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                        return 2 < argument.getLineBreakTry();
                    }
                },
                CodeStyleBuilder.DependsOn.token_right,
                maker.BREAK_LINE,
                maker.INSERT_SPACE);
        maker.whenFirst(new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override
            public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return 1 < argument.getLineBreakTry();
            }
        }, maker.condLeftIs(HasAtoms.CCommentEnd), CodeStyleBuilder.DependsOn.token_left, maker.BREAK_LINE);
        maker.whenFirst(
                maker.condRightIs(Comment.Word.class),
                new Util.OneCondition<DefaultStyleScopeInfo>() {
                    @Override
                    public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                        return 2 < argument.getLineBreakTry() && argument.approachingTheEdge();
                    }
                },
                CodeStyleBuilder.DependsOn.token_right,
                maker.BREAK_COMMENT_LINE);
        maker.whenFirst(new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override
            public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return 2 < argument.getLineBreakTry() &&
                        !TokensToStringStyle.ARGUMENTS.equals(argument.seeTopHint());
            }
        }, maker.condLeftIs(HasAtoms.SEP), CodeStyleBuilder.DependsOn.token_left, maker.BREAK_LINE);
        maker.whenFirst(
                maker.condRightIs(HasAtoms.ADD),
                new Util.OneCondition<DefaultStyleScopeInfo>() {
                    @Override public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                        return 3 < argument.getLineBreakTry() && argument.approachingTheEdge();
                    }
                },
                CodeStyleBuilder.DependsOn.token_right,
                maker.BREAK_LINE,
                new CodeStyleBuilder.Triggered<DefaultStyleScopeInfo>() {
                    @Override public void run(DefaultStyleScopeInfo context) {
                        context.statementBroken = true;
                    }
                }
        );
        maker.whenFirst(new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override
            public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return 0 < argument.getLineBreakTry();
            }
        }, maker.condLeftIs(HasAtoms.ALS), CodeStyleBuilder.DependsOn.token_left, maker.BREAK_LINE);
        maker.whenFirst(
                maker.condRightIs(HasAtoms.ALE),
                new Util.OneCondition<DefaultStyleScopeInfo>() {
                    @Override public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                        return 0 < argument.getLineBreakTry();
                    }
                },
                CodeStyleBuilder.DependsOn.token_right,
                maker.BREAK_LINE);
        maker.whenFirst(maker.condLeftIs(HasAtoms.ALS), CodeStyleBuilder.DependsOn.token_left, maker.DO_NOTHING);
        maker.whenFirst(maker.condRightIs(HasAtoms.ALE), CodeStyleBuilder.DependsOn.token_right, maker.DO_NOTHING);
        maker.whenFirst(maker.condRightIs(HasAtoms.SEP), CodeStyleBuilder.DependsOn.token_right, maker.DO_NOTHING);
        maker.whenFirst(maker.condLeftIs(HasAtoms.WHILE), maker.condRightIs(HasAtoms.LPR), CodeStyleBuilder.DependsOn.token_both, maker.INSERT_SPACE);
        maker.whenFirst(maker.condLeftIs(HasAtoms.IF), maker.condRightIs(HasAtoms.LPR), CodeStyleBuilder.DependsOn.token_both, maker.INSERT_SPACE);
        maker.whenFirst(maker.condLeftIs(HasAtoms.FOR), maker.condRightIs(HasAtoms.LPR), CodeStyleBuilder.DependsOn.token_both, maker.INSERT_SPACE);
        maker.whenFirst(maker.condLeftIs(HasAtoms.RET), maker.condRightIs(HasAtoms.LPR), CodeStyleBuilder.DependsOn.token_both, maker.INSERT_SPACE);
        maker.whenFirst(maker.condLeftIs(HasAtoms.CATCH), maker.condRightIs(HasAtoms.LPR), CodeStyleBuilder.DependsOn.token_both, maker.INSERT_SPACE);
        maker.whenFirst(maker.condLeftIs(HasAtoms.ADD), maker.condRightIs(HasAtoms.LPR), CodeStyleBuilder.DependsOn.token_both, maker.INSERT_SPACE);
        maker.whenFirst(maker.condLeftIs(HasAtoms.MUL), maker.condRightIs(HasAtoms.LPR), CodeStyleBuilder.DependsOn.token_both, maker.INSERT_SPACE);
        maker.whenFirst(maker.condLeftIs(HasAtoms.DIV), maker.condRightIs(HasAtoms.LPR), CodeStyleBuilder.DependsOn.token_both, maker.INSERT_SPACE);
        maker.whenFirst(maker.condRightIs(HasAtoms.INC), CodeStyleBuilder.DependsOn.token_right, maker.DO_NOTHING);
        maker.whenFirst(maker.condRightIs(HasAtoms.DEC), CodeStyleBuilder.DependsOn.token_right, maker.DO_NOTHING);
        maker.whenFirst(maker.condLeftIs(HasAtoms.INC), CodeStyleBuilder.DependsOn.token_left, maker.DO_NOTHING);
        maker.whenFirst(maker.condLeftIs(HasAtoms.DEC), CodeStyleBuilder.DependsOn.token_left, maker.DO_NOTHING);
        maker.whenFirst(maker.condLeftIs(HasAtoms.ASSIGN), maker.condRightIs(HasAtoms.LPR), CodeStyleBuilder.DependsOn.token_both, maker.INSERT_SPACE);
        maker.whenFirst(maker.condRightIs(HasAtoms.LPR), CodeStyleBuilder.DependsOn.token_right, maker.DO_NOTHING);
        maker.whenFirst(maker.condLeftIs(HasAtoms.LPR), CodeStyleBuilder.DependsOn.token_left, maker.DO_NOTHING);
        maker.whenFirst(maker.condRightIs(HasAtoms.ARRAY_ACCESS_START), CodeStyleBuilder.DependsOn.token_right, maker.DO_NOTHING);
        maker.whenFirst(maker.condLeftIs(HasAtoms.ARRAY_ACCESS_START), CodeStyleBuilder.DependsOn.token_left, maker.DO_NOTHING);
        maker.whenFirst(maker.condRightIs(HasAtoms.ARRAY_ACCESS_END), CodeStyleBuilder.DependsOn.token_right, maker.DO_NOTHING);
        maker.whenFirst(maker.condTopHintIs(TokensToStringStyle.OUTER_LEVEL), maker.condLeftIs(HasAtoms.CCommentEnd),
                CodeStyleBuilder.DependsOn.token_left, maker.BREAK_LINE);
        maker.whenFirst(maker.condRightIs(HasAtoms.OR), CodeStyleBuilder.DependsOn.token_right, maker.DO_NOTHING);
        maker.whenFirst(maker.condRightIs(HasAtoms.AND), CodeStyleBuilder.DependsOn.token_right, maker.DO_NOTHING);
        maker.whenFirst(maker.condLeftIs(HasAtoms.GENERIC_START), CodeStyleBuilder.DependsOn.token_left, maker.DO_NOTHING);
        maker.whenFirst(maker.condRightIs(HasAtoms.GENERIC_START), CodeStyleBuilder.DependsOn.token_right, maker.DO_NOTHING);
        maker.whenFirst(maker.condRightIs(HasAtoms.GENERIC_END), CodeStyleBuilder.DependsOn.token_right, maker.DO_NOTHING);

        maker.alwaysWhen(
                new Util.OneCondition<DefaultStyleScopeInfo>() {
                    @Override public boolean isTrueFor(DefaultStyleScopeInfo info) {
                        return info.approachingTheEdge() && info.getLineBreakTry() < 10;
                    }
                },
                CodeStyleBuilder.DependsOn.ignore_tokens,
                maker.RESET_LINE,
                new CodeStyleBuilder.Triggered<DefaultStyleScopeInfo>() {
                    @Override
                    public void run(DefaultStyleScopeInfo context) {
                        context.lineBreakTry++;
                        context.toFar.add(context.getNowAt());
                        context.statementBroken = false;
                    }
                });
        maker.alwaysWhen(maker.condLeftIs(HasAtoms.EOL),
                CodeStyleBuilder.DependsOn.token_left,
                new CodeStyleBuilder.Triggered<DefaultStyleScopeInfo>() {
                    @Override public void run(DefaultStyleScopeInfo context) {
                        context.statementBroken = false;
                    }
                });
        maker.alwaysWhen(maker.condRightIs(HasAtoms.ALS), CodeStyleBuilder.DependsOn.token_right, maker.SCOPE_ENTER);
        maker.alwaysWhen(maker.condRightIs(HasAtoms.ALE), CodeStyleBuilder.DependsOn.token_right, maker.SCOPE_EXIT);
        maker.alwaysWhen(maker.condRightIs(HasAtoms.LPR), CodeStyleBuilder.DependsOn.token_right, maker.SCOPE_ENTER);
        maker.alwaysWhen(maker.condRightIs(HasAtoms.RPR), CodeStyleBuilder.DependsOn.token_right, maker.SCOPE_EXIT);
        maker.alwaysWhen(maker.condRightIs(HasAtoms.LSC), CodeStyleBuilder.DependsOn.token_right, maker.SCOPE_ENTER);
        maker.alwaysWhen(maker.condRightIs(HasAtoms.RSC), CodeStyleBuilder.DependsOn.token_right, maker.SCOPE_EXIT);
        maker.alwaysWhen(maker.condRightIs(HasAtoms.ALS), CodeStyleBuilder.DependsOn.token_right, maker.INDENT);
        maker.alwaysWhen(maker.condRightIs(HasAtoms.LPR), CodeStyleBuilder.DependsOn.token_right, maker.INDENT);
        maker.alwaysWhen(maker.condRightIs(HasAtoms.LSC), CodeStyleBuilder.DependsOn.token_right, maker.INDENT);
        DEFAULT_STYLE_INDENT = maker.getStyle();
    }

    public static class DefaultStyleScopeInfo extends ScopeStack.ScopeInfo {
        private int lineBreakTry = 0;
        private boolean statementBroken = false;
        private LinkedList<Integer> toFar = new LinkedList<Integer>();

        public DefaultStyleScopeInfo(TokensToStringStyle.FormattingProcess process, ScopeStack inStack,
                                     int beganAt, int beganAtLine, String lineUpToScope) {
            super(process, inStack, beganAt, beganAtLine, lineUpToScope);
        }

        public int getLineBreakTry() {
            return lineBreakTry;
        }

        public boolean approachingTheEdge() {
            return 1000 < getLineLength() + getRLen() + 1; //|| toFar.contains(getNowAt() + 1);
        }

        @Override
        public int getExtraIndent() {
            return super.getExtraIndent() + (statementBroken ? 1 : 0);
        }
    }
}
