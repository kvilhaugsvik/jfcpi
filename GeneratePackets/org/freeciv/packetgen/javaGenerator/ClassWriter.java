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
import org.freeciv.packetgen.javaGenerator.expression.util.Formatted;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.Returnable;
import org.freeciv.packetgen.javaGenerator.formating.CodeStyle;
import org.freeciv.packetgen.javaGenerator.formating.CodeStyleBuilder;
import org.freeciv.packetgen.javaGenerator.formating.CodeStyleBuilder.*;

import java.util.*;

import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.*;

public class ClassWriter extends Formatted implements HasAtoms {
    private final TargetPackage where;
    private final List<Import> imports;
    private final Visibility visibility;
    private final ClassKind kind;
    private final LinkedList<Annotate> classAnnotate;
    private final String name;
    private final TargetClass parent;
    private final List<TargetClass> implementsInterface;

    private final LinkedList<Var> fields = new LinkedList<Var>();

    private final LinkedList<Method> methods = new LinkedList<Method>();
    protected final EnumElements enums = new EnumElements();

    private boolean constructorFromAllFields = false;

    public static List<TargetClass> oldClassList2newClassList(String classes) {
        if (null == classes || "".equals(classes))
            return Collections.<TargetClass>emptyList();
        List<TargetClass> out = new LinkedList<TargetClass>();
        for (String target : classes.split(", "))
            out.add(new TargetClass(target));
        return out;
    }

    public ClassWriter(ClassKind kind, TargetPackage where, Import[] imports,
                       String madeFrom, List<Annotate> classAnnotate,
                       String name,
                       String parent, String implementsInterface) {
        if (null == name) throw new IllegalArgumentException("No name for class to be generated");

        this.where = where;
        this.imports = null == imports ? new LinkedList<Import>() : new ArrayList<Import>(Arrays.asList(imports));
        this.classAnnotate = new LinkedList<Annotate>(classAnnotate);
        this.name = name;
        this.parent = (null == parent ? null : new TargetClass(parent));
        this.implementsInterface = oldClassList2newClassList(implementsInterface);
        this.kind = kind;

        visibility = Visibility.PUBLIC;

        if (null != madeFrom) {
            this.imports.add(Import.classIn(javax.annotation.Generated.class));
            this.classAnnotate.addFirst(commentAutoGenerated(madeFrom));
        }
    }

    public void addField(Var field) {
        if (field.getScope().equals(Scope.CODE_BLOCK))
            throw new IllegalArgumentException("Can't add a local variable declaration as a field");

        fields.add(field);
    }

    public void addClassConstant(String type, String name, String value) {
        fields.add(Var.field(Visibility.PRIVATE, Scope.CLASS, Modifiable.NO, type, name,
                asAValue(value)));
    }

    public void addClassConstant(Visibility visibility, String type, String name, String value) {
        fields.add(Var.field(visibility, Scope.CLASS, Modifiable.NO, type, name, asAValue(value)));
    }

    public void addClassConstant(String type, String name, Typed<AValue> value) {
        fields.add(Var.field(Visibility.PRIVATE, Scope.CLASS, Modifiable.NO, type, name, value));
    }

    public void addClassConstant(Visibility visibility, String type, String name, Typed<AValue> value) {
        fields.add(Var.field(visibility, Scope.CLASS, Modifiable.NO, type, name, value));
    }

    public void addObjectConstant(String type, String name) {
        fields.add(Var.field(Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO, type, name, null));
    }

    public void addPublicObjectConstant(String type, String name) {
        fields.add(Var.field(Visibility.PUBLIC, Scope.OBJECT, Modifiable.NO, type, name, null));
    }

    public void addObjectConstantAndGetter(String type, String name) {
        addObjectConstantAndGetter(Var.field(Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO, type, name, null));
    }

    public void addObjectConstantAndGetter(Var field) {
        addField(field);
        addMethod(Method.newPublicReadObjectState(Comment.no(),
                TargetClass.fromName(field.getType()),
                "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1),
                new Block(RETURN(field.ref()))));
    }

    public void addMethod(Method toAdd) {
        methods.add(toAdd);
    }

    public void addConstructorFields() {
        constructorFromAllFields = true;
    }

    protected void addEnumerated(EnumElement element) {
        assert kind.equals(ClassKind.ENUM);

        enums.add(element);
    }

    /**
     * Create a parameter list
     * @param parameters the parameters given as a list of map entry that maps from type to name
     * @return the parameter list
     */
    protected static String createParameterList(List<Map.Entry<String, String>> parameters) {
        String argumentsList = "";
        if (0 < parameters.size()) {
            for (Map.Entry<String, String> field : parameters) {
                argumentsList += field.getKey() + " " + field.getValue() + ", ";
            }
            argumentsList = argumentsList.substring(0, argumentsList.length() - 2);
        }
        return argumentsList;
    }

    /**
     * Get a line that sets a field's value to the value of the variable of the same name.
     * @param field Name of the field (and variable)
     * @return a line of Java setting the field's value to the value of the variable with the same name
     */
    protected Typed<? extends Returnable> setFieldToVariableSameName(String field) {
        return getField(field).assign(asAValue(field));
    }

    private void formatImports(CodeAtoms to) {
        if (!imports.isEmpty()) {
            to.hintStart(CodeStyle.GROUP);

            for (Import anImport : imports) {
                if (null != anImport)
                    anImport.writeAtoms(to);
                else {
                    to.hintEnd(CodeStyle.GROUP);
                    to.hintStart(CodeStyle.GROUP);
                }
            }
            to.hintEnd(CodeStyle.GROUP);
        }
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
        LinkedList<Map.Entry<String, String>> args = new LinkedList<Map.Entry<String, String>>();
        for (Var dec : fields) {
            body.addStatement(setFieldToVariableSameName(dec.getName()));
            args.add(new AbstractMap.SimpleImmutableEntry<String, String>(dec.getType(), dec.getName()));
        }
        Method.newPublicConstructor(Comment.no(), name, createParameterList(args), body).writeAtoms(to);
    }

    @Override
    public String toString() {
        return super.toString() + "\n";
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        to.hintStart(CodeStyle.OUTER_LEVEL);

        if (null != where) {
            to.add(HasAtoms.PACKAGE);
            where.writeAtoms(to);
            to.add(HasAtoms.EOL);
        }

        formatImports(to);

        for (Annotate ann : classAnnotate)
            ann.writeAtoms(to);

        visibility.writeAtoms(to);
        kind.writeAtoms(to);
        to.add(new ClassWriter.Atom(name));
        if (null != parent) {
            to.add(EXTENDS);
            parent.writeAtoms(to);
        }
        if (!implementsInterface.isEmpty()) {
            to.add(IMPLEMENTS);
            to.joinSep(HasAtoms.SEP, implementsInterface.toArray(new HasAtoms[implementsInterface.size()]));
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
            if (name.equals(toSort.getName()))
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
        return name;
    }

    public String getPackage() {
        return where.getFullAddress();
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
                Var.SetTo.strToVal("value", asAString("\"" + ClassWriter.class.getCanonicalName() + "\""));
        Var.SetTo comments =
                Var.SetTo.strToVal("comments", asAString("\"Auto generated from " + from + "\""));
        return new Annotate("Generated", comments, value);
    }

    public static final CodeStyle DEFAULT_STYLE_INDENT;

    static {
        final CodeStyleBuilder<DefaultStyleScopeInfo> maker =
                new CodeStyleBuilder<DefaultStyleScopeInfo>(
                        CodeStyle.Action.INSERT_SPACE,
                        DefaultStyleScopeInfo.class);

        maker.atTheBeginning(CodeStyle.Action.DO_NOTHING);
        maker.whenFirst(Arrays.<Util.OneCondition<DefaultStyleScopeInfo>>asList(maker.condAtTheEnd()),
                EnumSet.<CodeStyleBuilder.DependsOn>noneOf(CodeStyleBuilder.DependsOn.class),
                Arrays.<Triggered<DefaultStyleScopeInfo>>asList(maker.action2Triggered(CodeStyle.Action.DO_NOTHING)));
        maker.whenBetween(HasAtoms.LSC, HasAtoms.RSC, CodeStyle.Action.DO_NOTHING);
        maker.whenBetween(HasAtoms.RSC, HasAtoms.ELSE, CodeStyle.Action.INSERT_SPACE);
        maker.whenBefore(HasAtoms.RSC, CodeStyle.Action.BREAK_LINE);
        maker.whenAfter(HasAtoms.RSC, CodeStyle.Action.BREAK_LINE_BLOCK, new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override
            public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return null == argument.seeTopHint();
            }
        });
        maker.whenAfter(HasAtoms.EOL, CodeStyle.Action.BREAK_LINE_BLOCK, new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override
            public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return null == argument.seeTopHint();
            }
        });
        maker.whenAfter(HasAtoms.EOL, CodeStyle.Action.BREAK_LINE_BLOCK, new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override
            public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return CodeStyle.OUTER_LEVEL.equals(argument.seeTopHint());
            }
        });
        maker.whenBefore(Annotate.Atom.class, CodeStyle.Action.BREAK_LINE,
                new Util.OneCondition<DefaultStyleScopeInfo>() {
                    @Override
                    public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                        return CodeStyle.OUTER_LEVEL.equals(argument.seeTopHint());
                    }
                });
        maker.whenBefore(Visibility.Atom.class, CodeStyle.Action.BREAK_LINE,
                new Util.OneCondition<DefaultStyleScopeInfo>() {
                    @Override
                    public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                        return CodeStyle.OUTER_LEVEL.equals(argument.seeTopHint());
                    }
                });
        maker.whenFirst(
                Arrays.<Util.OneCondition<DefaultStyleScopeInfo>>asList(
                        new Util.OneCondition<DefaultStyleScopeInfo>() {
                            @Override
                            public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                                return CodeStyle.OUTER_LEVEL.equals(argument.seeTopHint());
                            }
                        },
                        maker.condLeftIs(Visibility.Atom.class)
                ),
                EnumSet.<CodeStyleBuilder.DependsOn>of(CodeStyleBuilder.DependsOn.LEFT_TOKEN),
                Arrays.<CodeStyleBuilder.Triggered<DefaultStyleScopeInfo>>asList(
                        maker.action2Triggered(CodeStyle.Action.INSERT_SPACE)));
        maker.whenBefore(ClassKind.Atom.class, CodeStyle.Action.BREAK_LINE,
                new Util.OneCondition<DefaultStyleScopeInfo>() {
                    @Override
                    public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                        return CodeStyle.OUTER_LEVEL.equals(argument.seeTopHint());
                    }
                });
        maker.whenAfter(HasAtoms.EOL, CodeStyle.Action.BREAK_LINE);
        maker.whenAfter(HasAtoms.LSC, CodeStyle.Action.BREAK_LINE);
        maker.whenAfter(HasAtoms.RSC, CodeStyle.Action.BREAK_LINE);
        maker.whenBefore(HasAtoms.EOL, CodeStyle.Action.DO_NOTHING);
        maker.whenBefore(HasAtoms.FORSEP, CodeStyle.Action.DO_NOTHING);
        maker.whenAfter(HasAtoms.HAS, CodeStyle.Action.DO_NOTHING);
        maker.whenBefore(HasAtoms.HAS, CodeStyle.Action.DO_NOTHING);
        maker.whenBefore(HasAtoms.RPR, CodeStyle.Action.DO_NOTHING);
        maker.whenAfter(HasAtoms.SEP, CodeStyle.Action.BREAK_LINE, new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override
            public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return EnumElements.class.getName().equals(argument.seeTopHint());
            }
        });
        maker.whenAfter(HasAtoms.EOL, CodeStyle.Action.BREAK_LINE_BLOCK, new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override
            public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return EnumElements.class.getName().equals(argument.seeTopHint());
            }
        });
        maker.whenFirst(
                Arrays.asList(
                        maker.condLeftIs(HasAtoms.SEP),
                        new Util.OneCondition<DefaultStyleScopeInfo>() {
                            @Override public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                                return 1 < argument.getLineBreakTry() &&
                                        argument.approachingTheEdge() &&
                                        CodeStyle.ARGUMENTS.equals(argument.seeTopHint());
                            }
                        }
                ),
                EnumSet.<DependsOn>of(DependsOn.LEFT_TOKEN),
                Arrays.asList(
                        maker.action2Triggered(CodeStyle.Action.BREAK_LINE),
                        new Triggered<DefaultStyleScopeInfo>() {
                            @Override
                            public void run(DefaultStyleScopeInfo context) {
                                context.statementBroken = true;
                            }
                        }
                ));
        maker.whenAfter(HasAtoms.SEP, CodeStyle.Action.BREAK_LINE, new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return 1 < argument.getLineBreakTry() && argument.approachingTheEdge();
            }
        });
        maker.whenFirst(
                Arrays.<Util.OneCondition<DefaultStyleScopeInfo>>asList(
                        new Util.OneCondition<DefaultStyleScopeInfo>() {
                            @Override
                            public boolean isTrueFor(DefaultStyleScopeInfo context) {
                                return 2 < context.getLineBreakTry() &&
                                        (context.getLeftAtom().equals(HasAtoms.CCommentStart) ||
                                                context.getLeftAtom().equals(HasAtoms.JDocStart));
                            }
                        }),
                EnumSet.<CodeStyleBuilder.DependsOn>of(DependsOn.LEFT_TOKEN),
                Arrays.<Triggered<DefaultStyleScopeInfo>>asList(
                        maker.action2Triggered(CodeStyle.Action.BREAK_LINE),
                        new CodeStyleBuilder.Triggered<DefaultStyleScopeInfo>() {
                            @Override
                            public void run(DefaultStyleScopeInfo context) {
                                context.getRunningFormatting().insertStar();
                            }
                        }));
        maker.whenFirst(
                Arrays.<Util.OneCondition<DefaultStyleScopeInfo>>asList(
                        maker.condRightIs(Annotate.Atom.class),
                        maker.condLeftIs(Comment.Word.class),
                        new Util.OneCondition<DefaultStyleScopeInfo>() {
                            @Override
                            public boolean isTrueFor(DefaultStyleScopeInfo context) {
                                return context.getLineBreakTry() <= 2;
                            }
                        }),
                EnumSet.<DependsOn>of(DependsOn.LEFT_TOKEN, DependsOn.RIGHT_TOKEN),
                Arrays.<Triggered<DefaultStyleScopeInfo>>asList(
                        new CodeStyleBuilder.Triggered<DefaultStyleScopeInfo>() {
                            @Override
                            public void run(DefaultStyleScopeInfo context) {
                                context.lineBreakTry = 3;
                                context.getRunningFormatting().scopeReset();
                            }
                        }));
        maker.whenFirst(
                Arrays.<Util.OneCondition<DefaultStyleScopeInfo>>asList(
                        maker.condRightIs(Annotate.Atom.class),
                        maker.condLeftIs(Comment.Word.class)),
                EnumSet.<DependsOn>of(DependsOn.LEFT_TOKEN, DependsOn.RIGHT_TOKEN),
                Arrays.<Triggered<DefaultStyleScopeInfo>>asList(
                        maker.action2Triggered(CodeStyle.Action.BREAK_LINE),
                        new CodeStyleBuilder.Triggered<DefaultStyleScopeInfo>() {
                            @Override
                            public void run(DefaultStyleScopeInfo context) {
                                context.getRunningFormatting().insertStar();
                            }
                        }));
        maker.whenFirst(
                Arrays.<Util.OneCondition<DefaultStyleScopeInfo>>asList(maker.condRightIs(HasAtoms.CCommentEnd),
                        new Util.OneCondition<DefaultStyleScopeInfo>() {
                            @Override
                            public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                                return 2 < argument.getLineBreakTry();
                            }
                        }),
                EnumSet.<DependsOn>of(DependsOn.RIGHT_TOKEN),
                Arrays.<Triggered<DefaultStyleScopeInfo>>asList(
                        maker.action2Triggered(CodeStyle.Action.BREAK_LINE),
                        maker.action2Triggered(CodeStyle.Action.INSERT_SPACE)));
        maker.whenAfter(HasAtoms.CCommentEnd, CodeStyle.Action.BREAK_LINE, new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override
            public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return 1 < argument.getLineBreakTry();
            }
        });
        maker.whenFirst(
                Arrays.<Util.OneCondition<DefaultStyleScopeInfo>>asList(
                        maker.condRightIs(Comment.Word.class),
                        new Util.OneCondition<DefaultStyleScopeInfo>() {
                            @Override
                            public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                                return 2 < argument.getLineBreakTry() && argument.approachingTheEdge();
                            }
                        }),
                EnumSet.<CodeStyleBuilder.DependsOn>of(CodeStyleBuilder.DependsOn.RIGHT_TOKEN),
                Arrays.<Triggered<DefaultStyleScopeInfo>>asList(
                        maker.action2Triggered(CodeStyle.Action.BREAK_LINE),
                        new CodeStyleBuilder.Triggered<DefaultStyleScopeInfo>() {
                            @Override
                            public void run(DefaultStyleScopeInfo context) {
                                context.getRunningFormatting().insertStar();
                            }
                        }));
        maker.whenAfter(HasAtoms.SEP, CodeStyle.Action.BREAK_LINE, new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return 2 < argument.getLineBreakTry() &&
                        !CodeStyle.ARGUMENTS.equals(argument.seeTopHint());
            }
        });
        maker.whenFirst(
                Arrays.<Util.OneCondition<DefaultStyleScopeInfo>>asList(
                        maker.condRightIs(HasAtoms.ADD),
                        new Util.OneCondition<DefaultStyleScopeInfo>() {
                            @Override public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                                return 3 < argument.getLineBreakTry() && argument.approachingTheEdge();
                            }
                        }),
                EnumSet.<CodeStyleBuilder.DependsOn>of(CodeStyleBuilder.DependsOn.RIGHT_TOKEN),
                Arrays.<CodeStyleBuilder.Triggered<DefaultStyleScopeInfo>>asList(
                        maker.action2Triggered(CodeStyle.Action.BREAK_LINE),
                        new CodeStyleBuilder.Triggered<DefaultStyleScopeInfo>() {
                            @Override public void run(DefaultStyleScopeInfo context) {
                                context.statementBroken = true;
                            }
                        }
                ));
        maker.whenAfter(HasAtoms.ALS, CodeStyle.Action.BREAK_LINE, new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return 0 < argument.getLineBreakTry();
            }
        });
        maker.whenFirst(
                Arrays.<Util.OneCondition<DefaultStyleScopeInfo>>asList(
                        maker.condRightIs(HasAtoms.ALE),
                        new Util.OneCondition<DefaultStyleScopeInfo>() {
                            @Override public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                                return 0 < argument.getLineBreakTry();
                            }
                        }),
                EnumSet.<CodeStyleBuilder.DependsOn>of(CodeStyleBuilder.DependsOn.RIGHT_TOKEN),
                Arrays.<CodeStyleBuilder.Triggered<DefaultStyleScopeInfo>>asList(
                        maker.action2Triggered(CodeStyle.Action.BREAK_LINE)));
        maker.whenAfter(HasAtoms.ALS, CodeStyle.Action.DO_NOTHING);
        maker.whenBefore(HasAtoms.ALE, CodeStyle.Action.DO_NOTHING);
        maker.whenBefore(HasAtoms.SEP, CodeStyle.Action.DO_NOTHING);
        maker.whenBetween(HasAtoms.WHILE, HasAtoms.LPR, CodeStyle.Action.INSERT_SPACE);
        maker.whenBetween(HasAtoms.IF, HasAtoms.LPR, CodeStyle.Action.INSERT_SPACE);
        maker.whenBetween(HasAtoms.FOR, HasAtoms.LPR, CodeStyle.Action.INSERT_SPACE);
        maker.whenBetween(HasAtoms.RET, HasAtoms.LPR, CodeStyle.Action.INSERT_SPACE);
        maker.whenBetween(HasAtoms.ADD, HasAtoms.LPR, CodeStyle.Action.INSERT_SPACE);
        maker.whenBetween(HasAtoms.MUL, HasAtoms.LPR, CodeStyle.Action.INSERT_SPACE);
        maker.whenBetween(HasAtoms.DIV, HasAtoms.LPR, CodeStyle.Action.INSERT_SPACE);
        maker.whenBefore(HasAtoms.INC, CodeStyle.Action.DO_NOTHING);
        maker.whenBefore(HasAtoms.LPR, CodeStyle.Action.DO_NOTHING);
        maker.whenAfter(HasAtoms.LPR, CodeStyle.Action.DO_NOTHING);
        maker.whenBefore(HasAtoms.ARRAY_ACCESS_START, CodeStyle.Action.DO_NOTHING);
        maker.whenAfter(HasAtoms.ARRAY_ACCESS_START, CodeStyle.Action.DO_NOTHING);
        maker.whenBefore(HasAtoms.ARRAY_ACCESS_END, CodeStyle.Action.DO_NOTHING);
        maker.whenAfter(HasAtoms.CCommentEnd, CodeStyle.Action.BREAK_LINE, new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override
            public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return null == argument.seeTopHint();
            }
        });

        maker.alwaysWhen(
                Arrays.<Util.OneCondition<DefaultStyleScopeInfo>>asList(
                        new Util.OneCondition<DefaultStyleScopeInfo>() {
                            @Override public boolean isTrueFor(DefaultStyleScopeInfo info) {
                                return 100 <= info.getLineLength() && info.getLineBreakTry() < 10;
                            }
                        }),
                EnumSet.<CodeStyleBuilder.DependsOn>noneOf(CodeStyleBuilder.DependsOn.class),
                Arrays.<CodeStyleBuilder.Triggered<DefaultStyleScopeInfo>>asList(
                        maker.action2Triggered(CodeStyle.Action.RESET_LINE),
                        new CodeStyleBuilder.Triggered<DefaultStyleScopeInfo>() {
                            @Override
                            public void run(DefaultStyleScopeInfo context) {
                                context.lineBreakTry++;
                                context.toFar.add(context.getNowAt());
                                context.statementBroken = false;
                            }
                        }));
        maker.alwaysWhen(Arrays.<Util.OneCondition<DefaultStyleScopeInfo>>asList(maker.condLeftIs(HasAtoms.EOL)),
                EnumSet.<CodeStyleBuilder.DependsOn>of(CodeStyleBuilder.DependsOn.LEFT_TOKEN),
                Arrays.<CodeStyleBuilder.Triggered<DefaultStyleScopeInfo>>asList(
                        new CodeStyleBuilder.Triggered<DefaultStyleScopeInfo>() {
                            @Override public void run(DefaultStyleScopeInfo context) {
                                context.statementBroken = false;
                            }
                        }));
        maker.alwaysBefore(HasAtoms.ALS, CodeStyle.Action.SCOPE_ENTER);
        maker.alwaysBefore(HasAtoms.ALE, CodeStyle.Action.SCOPE_EXIT);
        maker.alwaysBefore(HasAtoms.LPR, CodeStyle.Action.SCOPE_ENTER);
        maker.alwaysBefore(HasAtoms.RPR, CodeStyle.Action.SCOPE_EXIT);
        maker.alwaysBefore(HasAtoms.LSC, CodeStyle.Action.SCOPE_ENTER);
        maker.alwaysBefore(HasAtoms.RSC, CodeStyle.Action.SCOPE_EXIT);
        maker.alwaysBefore(HasAtoms.ALS, CodeStyle.Action.INDENT);
        maker.alwaysBefore(HasAtoms.LPR, CodeStyle.Action.INDENT);
        maker.alwaysBefore(HasAtoms.LSC, CodeStyle.Action.INDENT);
        DEFAULT_STYLE_INDENT = maker.getStyle();
    }

    public static class DefaultStyleScopeInfo extends CodeStyle.ScopeStack.ScopeInfo {
        private int lineBreakTry = 0;
        private boolean statementBroken = false;
        private LinkedList<Integer> toFar = new LinkedList<Integer>();

        public DefaultStyleScopeInfo(CodeStyle.FormattingProcess process, CodeStyle.ScopeStack inStack,
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
