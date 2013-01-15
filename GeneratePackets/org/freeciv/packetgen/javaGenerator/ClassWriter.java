/*
 * Copyright (c) 2011, 2012. Sveinung Kvilhaugsvik
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

package org.freeciv.packetgen.javaGenerator;

import org.freeciv.Util;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.Import;
import org.freeciv.packetgen.javaGenerator.expression.Statement;
import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.expression.util.Formatted;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.Returnable;
import org.freeciv.packetgen.javaGenerator.formating.CodeStyle;
import org.freeciv.packetgen.javaGenerator.formating.CodeStyleBuilder;
import org.freeciv.packetgen.javaGenerator.formating.CodeStyleBuilder.*;
import org.freeciv.packetgen.javaGenerator.formating.ScopeStack;

import java.util.*;

import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.*;

public class ClassWriter extends Formatted implements HasAtoms {
    public static final TargetClass DEFAULT_PARENT = TargetClass.fromClass(Object.class);

    private final TargetClass myAddress;
    private final Imports.ScopeDataForJavaFile scopeData;
    private final Visibility visibility;
    private final ClassKind kind;
    private final LinkedList<Annotate> classAnnotate;
    private final TargetClass parent;
    private final List<TargetClass> implementsInterface;

    private final LinkedList<Var> fields = new LinkedList<Var>();

    private final LinkedList<Method> methods = new LinkedList<Method>();
    protected final EnumElements enums = new EnumElements();

    private boolean constructorFromAllFields = false;

    public ClassWriter(ClassKind kind, TargetPackage where, Imports imports,
                       String madeFrom, List<Annotate> classAnnotate,
                       String name,
                       TargetClass parent, List<TargetClass> implementsInterface) {
        if (null == name) throw new IllegalArgumentException("No name for class to be generated");
        if (null == where)
            throw new IllegalArgumentException("null given as package. (Did you mean TargetPackage.TOP_LEVEL?)");

        this.myAddress = new TargetClass(where, new ClassWriter.Atom(name), false);
        myAddress.setParent(parent);

        this.classAnnotate = new LinkedList<Annotate>(classAnnotate);
        this.parent = parent;
        this.implementsInterface = implementsInterface;
        this.kind = kind;

        visibility = Visibility.PUBLIC;

        if (null != madeFrom) {
            imports.add(Import.classIn(javax.annotation.Generated.class));
            this.classAnnotate.addFirst(commentAutoGenerated(madeFrom));
        }

        this.scopeData = imports.getScopeData(this.myAddress);
    }

    public void addField(Var field) {
        if (field.getScope().equals(Scope.CODE_BLOCK))
            throw new IllegalArgumentException("Can't add a local variable declaration as a field");

        fields.add(field);
    }

    public void addClassConstant(Visibility visibility, TargetClass type, String name, Typed<? extends AValue> value) {
        addField(Var.field(Collections.<Annotate>emptyList(), visibility, Scope.CLASS, Modifiable.NO, type, name, value));
    }

    public void addClassConstant(Visibility visibility, Class type, String name, Typed<? extends AValue> value) {
        addClassConstant(visibility, TargetClass.fromClass(type), name, value);
    }

    public void addObjectConstant(TargetClass type, String name) {
        addField(Var.field(Collections.<Annotate>emptyList(), Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO, type, name, null));
    }

    public void addObjectConstant(Class type, String name) {
        addObjectConstant(TargetClass.fromClass(type), name);
    }

    public void addPublicObjectConstant(TargetClass type, String name) {
        addField(Var.field(Collections.<Annotate>emptyList(), Visibility.PUBLIC, Scope.OBJECT, Modifiable.NO, type, name, null));
    }

    public void addPublicObjectConstant(Class type, String name) {
        addPublicObjectConstant(TargetClass.fromClass(type), name);
    }

    public void addObjectConstantAndGetter(Var field) {
        addField(field);
        addMethod(Method.newPublicReadObjectState(Comment.no(),
                field.getTType().scopeKnown(),
                "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1),
                new Block(RETURN(field.ref()))));
    }

    public void addMethod(Method toAdd) {
        methods.add(toAdd);
        myAddress.register(toAdd.getAddressOn(this.getAddress()));
    }

    public void addConstructorFields() {
        constructorFromAllFields = true;
    }

    protected void addEnumerated(EnumElement element) {
        assert kind.equals(ClassKind.ENUM);

        enums.add(element);
    }

    /**
     * Get a line that sets a field's value to the value of the variable of the same name.
     * @param field Name of the field (and variable)
     * @return a line of Java setting the field's value to the value of the variable with the same name
     */
    protected Typed<? extends Returnable> setFieldToVariableSameName(String field) {
        return getField(field).assign(BuiltIn.<AValue>toCode(field));
    }

    private static void formatVariableDeclarations(CodeAtoms to, final List<Var> fields) {
        if (!fields.isEmpty()) {
            to.hintStart(CodeStyle.GROUP);
            Scope scopeOfPrevious = fields.get(0).getScope();

            for (Var variable : fields) {
                if (!variable.getScope().equals(scopeOfPrevious)) {
                    to.hintEnd(CodeStyle.GROUP);
                    to.hintStart(CodeStyle.GROUP);
                }
                new Statement(variable).writeAtoms(to);
                scopeOfPrevious = variable.getScope();
            }

            to.hintEnd(CodeStyle.GROUP);
        }
    }

    private void constructorFromFields(CodeAtoms to) {
        Block body = new Block();
        LinkedList<Var<? extends AValue>> args = new LinkedList<Var<? extends AValue>>();
        for (Var dec : fields) {
            body.addStatement(setFieldToVariableSameName(dec.getName()));
            args.add(Var.param(dec.getTType(), dec.getName()));
        }
        Method.newPublicConstructor(Comment.no(), args, body).writeAtoms(to);
    }

    @Override
    public String toString() {
        return super.toString() + "\n";
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        to.hintStart(CodeStyle.OUTER_LEVEL);

        to.rewriteRule(new Util.OneCondition<IR.CodeAtom>() {
            @Override
            public boolean isTrueFor(IR.CodeAtom argument) {
                return HasAtoms.SELF.equals(argument);
            }
        }, myAddress.getCName());

        scopeData.writeAtoms(to);

        for (Annotate ann : classAnnotate)
            ann.writeAtoms(to);

        visibility.writeAtoms(to);
        kind.writeAtoms(to);
        to.add(myAddress.getCName());
        if (!DEFAULT_PARENT.equals(parent)) {
            to.add(EXTENDS);
            parent.writeAtoms(to);
        }
        if (!implementsInterface.isEmpty()) {
            to.add(IMPLEMENTS);
            to.joinSep(HasAtoms.SEP, implementsInterface);
        }

        to.add(HasAtoms.LSC);

        if (ClassKind.ENUM == kind && !enums.isEmpty())
            enums.writeAtoms(to);

        formatVariableDeclarations(to, fields);

        if (constructorFromAllFields)
            constructorFromFields(to);

        LinkedList<Method> constructors = new LinkedList<Method>();
        LinkedList<Method> other = new LinkedList<Method>();
        for (Method toSort : methods)
            if (toSort instanceof Method.Constructor)
                constructors.add(toSort);
            else
                other.add(toSort);

        for (Method method : constructors)
            method.writeAtoms(to);
        for (Method method : other)
            method.writeAtoms(to);

        to.add(HasAtoms.RSC);

        to.hintEnd(CodeStyle.OUTER_LEVEL);
    }

    public String getName() {
        return myAddress.getName();
    }

    public TargetClass getAddress() {
        return myAddress;
    }

    public String getPackage() {
        return myAddress.getPackage().getFullAddress();
    }

    public Var getField(String name) {
        HashSet<Var> allFields = new HashSet<Var>(fields);
        allFields.addAll(fields);
        for (Var field : allFields) {
            if (field.getName().equals(name)) return field;
        }
        return null;
    }

    public boolean hasConstant(String name) {
        Var field = getField(name);
        return null != field && field.getModifiable().equals(Modifiable.NO);
    }

    static Annotate commentAutoGenerated(String from) {
        Var.SetTo value =
                Var.SetTo.strToVal("value", BuiltIn.literal(ClassWriter.class.getCanonicalName()));
        Var.SetTo comments =
                Var.SetTo.strToVal("comments", BuiltIn.literal("Auto generated from " + from));
        return new Annotate("Generated", comments, value);
    }

    public static final CodeStyle DEFAULT_STYLE_INDENT;

    static {
        final CodeStyleBuilder<DefaultStyleScopeInfo> maker =
                new CodeStyleBuilder<DefaultStyleScopeInfo>(
                        CodeStyleBuilder.<DefaultStyleScopeInfo>INSERT_SPACE(),
                        DefaultStyleScopeInfo.class);

        maker.whenFirst(maker.condAtTheBeginning(), DependsOn.ignore_tokens, maker.DO_NOTHING);
        maker.whenFirst(maker.condAtTheEnd(), DependsOn.ignore_tokens, maker.DO_NOTHING);
        maker.whenFirst(maker.condLeftIs(HasAtoms.LSC), maker.condRightIs(HasAtoms.RSC), DependsOn.token_both, maker.DO_NOTHING);
        maker.whenFirst(maker.condLeftIs(HasAtoms.RSC), maker.condRightIs(HasAtoms.ELSE), DependsOn.token_both, maker.INSERT_SPACE);
        maker.whenFirst(maker.condLeftIs(HasAtoms.RSC), maker.condRightIs(HasAtoms.CATCH), DependsOn.token_both, maker.INSERT_SPACE);
        maker.whenFirst(maker.condRightIs(HasAtoms.RSC), DependsOn.token_right, maker.BREAK_LINE);
        maker.whenFirst(new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override
            public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return !CodeStyle.GROUP.equals(argument.seeTopHint());
            }
        }, maker.condLeftIs(HasAtoms.RSC), DependsOn.token_left, maker.BREAK_LINE_BLOCK);
        maker.whenFirst(new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override
            public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return !CodeStyle.GROUP.equals(argument.seeTopHint());
            }
        }, maker.condLeftIs(HasAtoms.EOL), DependsOn.token_left, maker.BREAK_LINE_BLOCK);
        maker.whenFirst(maker.condTopHintIs(CodeStyle.OUTER_LEVEL), maker.condLeftIs(HasAtoms.EOL),
                DependsOn.token_left, maker.BREAK_LINE_BLOCK);
        maker.whenFirst(
                maker.condRightIs(Annotate.Atom.class),
                maker.condLeftIs(Comment.Word.class),
                new Util.OneCondition<DefaultStyleScopeInfo>() {
                    @Override
                    public boolean isTrueFor(DefaultStyleScopeInfo context) {
                        return context.getLineBreakTry() <= 2;
                    }
                },
                DependsOn.token_both,
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
                DependsOn.token_both,
                maker.BREAK_LINE,
                new CodeStyleBuilder.Triggered<DefaultStyleScopeInfo>() {
                    @Override
                    public void run(DefaultStyleScopeInfo context) {
                        context.getRunningFormatting().insertStar();
                    }
                });
        maker.whenFirst(maker.condRightIs(Annotate.Atom.class), maker.condTopHintIs(CodeStyle.OUTER_LEVEL),
                DependsOn.token_right, maker.BREAK_LINE);
        maker.whenFirst(maker.condRightIs(Visibility.Atom.class), maker.condTopHintIs(CodeStyle.OUTER_LEVEL),
                DependsOn.token_right, maker.BREAK_LINE);
        maker.whenFirst(
                maker.condTopHintIs(CodeStyle.OUTER_LEVEL),
                maker.condLeftIs(Visibility.Atom.class),
                DependsOn.token_left,
                maker.INSERT_SPACE);
        maker.whenFirst(maker.condRightIs(ClassKind.Atom.class), maker.condTopHintIs(CodeStyle.OUTER_LEVEL),
                DependsOn.token_right, maker.BREAK_LINE);
        maker.whenFirst(maker.condLeftIs(HasAtoms.EOL), DependsOn.token_left, maker.BREAK_LINE);
        maker.whenFirst(maker.condLeftIs(HasAtoms.LSC), DependsOn.token_left, maker.BREAK_LINE);
        maker.whenFirst(maker.condLeftIs(HasAtoms.RSC), DependsOn.token_left, maker.BREAK_LINE);
        maker.whenFirst(maker.condRightIs(HasAtoms.EOL), DependsOn.token_right, maker.DO_NOTHING);
        maker.whenFirst(maker.condRightIs(HasAtoms.FORSEP), DependsOn.token_right, maker.DO_NOTHING);
        maker.whenFirst(maker.condLeftIs(HasAtoms.HAS), DependsOn.token_left, maker.DO_NOTHING);
        maker.whenFirst(maker.condRightIs(HasAtoms.HAS), DependsOn.token_right, maker.DO_NOTHING);
        maker.whenFirst(maker.condRightIs(HasAtoms.RPR), DependsOn.token_right, maker.DO_NOTHING);
        maker.whenFirst(maker.condTopHintIs(EnumElements.class.getName()), maker.condLeftIs(HasAtoms.SEP),
                DependsOn.token_left, maker.BREAK_LINE);
        maker.whenFirst(maker.condTopHintIs(EnumElements.class.getName()), maker.condLeftIs(HasAtoms.EOL),
                DependsOn.token_left, maker.BREAK_LINE_BLOCK);
        maker.whenFirst(
                maker.condLeftIs(HasAtoms.SEP),
                new Util.OneCondition<DefaultStyleScopeInfo>() {
                    @Override public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                        return 1 < argument.getLineBreakTry() &&
                                argument.approachingTheEdge() &&
                                CodeStyle.ARGUMENTS.equals(argument.seeTopHint());
                    }
                },
                DependsOn.token_left,
                maker.BREAK_LINE,
                new Triggered<DefaultStyleScopeInfo>() {
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
        }, maker.condLeftIs(HasAtoms.SEP), DependsOn.token_left, maker.BREAK_LINE);
        maker.whenFirst(
                new Util.OneCondition<DefaultStyleScopeInfo>() {
                    @Override
                    public boolean isTrueFor(DefaultStyleScopeInfo context) {
                        return 2 < context.getLineBreakTry() &&
                                (context.getLeftAtom().equals(HasAtoms.CCommentStart) ||
                                        context.getLeftAtom().equals(HasAtoms.JDocStart));
                    }
                },
                DependsOn.token_left,
                maker.BREAK_LINE,
                new CodeStyleBuilder.Triggered<DefaultStyleScopeInfo>() {
                    @Override
                    public void run(DefaultStyleScopeInfo context) {
                        context.getRunningFormatting().insertStar();
                    }
                });
        maker.whenFirst(maker.condRightIs(HasAtoms.CCommentEnd),
                new Util.OneCondition<DefaultStyleScopeInfo>() {
                    @Override
                    public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                        return 2 < argument.getLineBreakTry();
                    }
                },
                DependsOn.token_right,
                maker.BREAK_LINE,
                maker.INSERT_SPACE);
        maker.whenFirst(new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override
            public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return 1 < argument.getLineBreakTry();
            }
        }, maker.condLeftIs(HasAtoms.CCommentEnd), DependsOn.token_left, maker.BREAK_LINE);
        maker.whenFirst(
                maker.condRightIs(Comment.Word.class),
                new Util.OneCondition<DefaultStyleScopeInfo>() {
                    @Override
                    public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                        return 2 < argument.getLineBreakTry() && argument.approachingTheEdge();
                    }
                },
                DependsOn.token_right,
                maker.BREAK_LINE,
                new CodeStyleBuilder.Triggered<DefaultStyleScopeInfo>() {
                    @Override
                    public void run(DefaultStyleScopeInfo context) {
                        context.getRunningFormatting().insertStar();
                    }
                });
        maker.whenFirst(new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override
            public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return 2 < argument.getLineBreakTry() &&
                        !CodeStyle.ARGUMENTS.equals(argument.seeTopHint());
            }
        }, maker.condLeftIs(HasAtoms.SEP), DependsOn.token_left, maker.BREAK_LINE);
        maker.whenFirst(
                maker.condRightIs(HasAtoms.ADD),
                new Util.OneCondition<DefaultStyleScopeInfo>() {
                    @Override public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                        return 3 < argument.getLineBreakTry() && argument.approachingTheEdge();
                    }
                },
                DependsOn.token_right,
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
        }, maker.condLeftIs(HasAtoms.ALS), DependsOn.token_left, maker.BREAK_LINE);
        maker.whenFirst(
                maker.condRightIs(HasAtoms.ALE),
                new Util.OneCondition<DefaultStyleScopeInfo>() {
                    @Override public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                        return 0 < argument.getLineBreakTry();
                    }
                },
                DependsOn.token_right,
                maker.BREAK_LINE);
        maker.whenFirst(maker.condLeftIs(HasAtoms.ALS), DependsOn.token_left, maker.DO_NOTHING);
        maker.whenFirst(maker.condRightIs(HasAtoms.ALE), DependsOn.token_right, maker.DO_NOTHING);
        maker.whenFirst(maker.condRightIs(HasAtoms.SEP), DependsOn.token_right, maker.DO_NOTHING);
        maker.whenFirst(maker.condLeftIs(HasAtoms.WHILE), maker.condRightIs(HasAtoms.LPR), DependsOn.token_both, maker.INSERT_SPACE);
        maker.whenFirst(maker.condLeftIs(HasAtoms.IF), maker.condRightIs(HasAtoms.LPR), DependsOn.token_both, maker.INSERT_SPACE);
        maker.whenFirst(maker.condLeftIs(HasAtoms.FOR), maker.condRightIs(HasAtoms.LPR), DependsOn.token_both, maker.INSERT_SPACE);
        maker.whenFirst(maker.condLeftIs(HasAtoms.RET), maker.condRightIs(HasAtoms.LPR), DependsOn.token_both, maker.INSERT_SPACE);
        maker.whenFirst(maker.condLeftIs(HasAtoms.CATCH), maker.condRightIs(HasAtoms.LPR), DependsOn.token_both, maker.INSERT_SPACE);
        maker.whenFirst(maker.condLeftIs(HasAtoms.ADD), maker.condRightIs(HasAtoms.LPR), DependsOn.token_both, maker.INSERT_SPACE);
        maker.whenFirst(maker.condLeftIs(HasAtoms.MUL), maker.condRightIs(HasAtoms.LPR), DependsOn.token_both, maker.INSERT_SPACE);
        maker.whenFirst(maker.condLeftIs(HasAtoms.DIV), maker.condRightIs(HasAtoms.LPR), DependsOn.token_both, maker.INSERT_SPACE);
        maker.whenFirst(maker.condRightIs(HasAtoms.INC), DependsOn.token_right, maker.DO_NOTHING);
        maker.whenFirst(maker.condLeftIs(HasAtoms.ASSIGN), maker.condRightIs(HasAtoms.LPR), DependsOn.token_both, maker.INSERT_SPACE);
        maker.whenFirst(maker.condRightIs(HasAtoms.LPR), DependsOn.token_right, maker.DO_NOTHING);
        maker.whenFirst(maker.condLeftIs(HasAtoms.LPR), DependsOn.token_left, maker.DO_NOTHING);
        maker.whenFirst(maker.condRightIs(HasAtoms.ARRAY_ACCESS_START), DependsOn.token_right, maker.DO_NOTHING);
        maker.whenFirst(maker.condLeftIs(HasAtoms.ARRAY_ACCESS_START), DependsOn.token_left, maker.DO_NOTHING);
        maker.whenFirst(maker.condRightIs(HasAtoms.ARRAY_ACCESS_END), DependsOn.token_right, maker.DO_NOTHING);
        maker.whenFirst(maker.condTopHintIs(CodeStyle.OUTER_LEVEL), maker.condLeftIs(HasAtoms.CCommentEnd),
                DependsOn.token_left, maker.BREAK_LINE);
        maker.whenFirst(maker.condRightIs(HasAtoms.OR), DependsOn.token_right, maker.DO_NOTHING);
        maker.whenFirst(maker.condRightIs(HasAtoms.AND), DependsOn.token_right, maker.DO_NOTHING);

        maker.alwaysWhen(
                new Util.OneCondition<DefaultStyleScopeInfo>() {
                    @Override public boolean isTrueFor(DefaultStyleScopeInfo info) {
                        return 100 <= info.getLineLength() && info.getLineBreakTry() < 10;
                    }
                },
                DependsOn.ignore_tokens,
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
                DependsOn.token_left,
                new CodeStyleBuilder.Triggered<DefaultStyleScopeInfo>() {
                    @Override public void run(DefaultStyleScopeInfo context) {
                        context.statementBroken = false;
                    }
                });
        maker.alwaysWhen(maker.condRightIs(HasAtoms.ALS), DependsOn.token_right, maker.SCOPE_ENTER);
        maker.alwaysWhen(maker.condRightIs(HasAtoms.ALE), DependsOn.token_right, maker.SCOPE_EXIT);
        maker.alwaysWhen(maker.condRightIs(HasAtoms.LPR), DependsOn.token_right, maker.SCOPE_ENTER);
        maker.alwaysWhen(maker.condRightIs(HasAtoms.RPR), DependsOn.token_right, maker.SCOPE_EXIT);
        maker.alwaysWhen(maker.condRightIs(HasAtoms.LSC), DependsOn.token_right, maker.SCOPE_ENTER);
        maker.alwaysWhen(maker.condRightIs(HasAtoms.RSC), DependsOn.token_right, maker.SCOPE_EXIT);
        maker.alwaysWhen(maker.condRightIs(HasAtoms.ALS), DependsOn.token_right, maker.INDENT);
        maker.alwaysWhen(maker.condRightIs(HasAtoms.LPR), DependsOn.token_right, maker.INDENT);
        maker.alwaysWhen(maker.condRightIs(HasAtoms.LSC), DependsOn.token_right, maker.INDENT);
        DEFAULT_STYLE_INDENT = maker.getStyle();
    }

    public static class DefaultStyleScopeInfo extends ScopeStack.ScopeInfo {
        private int lineBreakTry = 0;
        private boolean statementBroken = false;
        private LinkedList<Integer> toFar = new LinkedList<Integer>();

        public DefaultStyleScopeInfo(CodeStyle.FormattingProcess process, ScopeStack inStack,
                                     int beganAt, int beganAtLine, String lineUpToScope) {
            super(process, inStack, beganAt, beganAtLine, lineUpToScope);
        }

        public int getLineBreakTry() {
            return lineBreakTry;
        }

        public boolean approachingTheEdge() {
            return 100 < getLineLength() + getRLen() + 1 || toFar.contains(getNowAt() + 1);
        }

        @Override
        public int getExtraIndent() {
            return super.getExtraIndent() + (statementBroken ? 1 : 0);
        }
    }

    public static class Atom extends IR.CodeAtom {
        public Atom(String atom) {
            super(atom);
        }
    }
}
