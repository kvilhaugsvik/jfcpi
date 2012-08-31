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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.*;

public class ClassWriter {
    private static final String OUTER_LEVEL = "Outside";

    private final TargetPackage where;
    private final List<Import> imports;
    private final Visibility visibility;
    private final ClassKind kind;
    private final LinkedList<Annotate> classAnnotate;
    private final String name;
    private final String parent;
    private final String implementsInterface;

    private final LinkedList<Var> fields = new LinkedList<Var>();

    private final LinkedList<Method> methods = new LinkedList<Method>();
    protected final EnumElements enums = new EnumElements();

    private boolean constructorFromAllFields = false;

    @Deprecated
    public ClassWriter(ClassKind kind, TargetPackage where, Import[] imports, String madeFrom, String name,
                       String parent, String implementsInterface) {
        this(kind, where, imports, madeFrom, Collections.<Annotate>emptyList(), name, parent, implementsInterface);
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
        this.parent = parent;
        this.implementsInterface = implementsInterface;
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
        addMethodPublicReadObjectState(
                        null,
                        field.getType(),
                        "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1),
                        new Block(RETURN(field.ref())));
    }

    public void addMethod(String comment,
                          Visibility visibility,
                          Scope scope,
                          String type,
                          String name,
                          String paramList,
                          String exceptionList,
                          String... body) {
        methods.add(new Method(comment, visibility, scope, type, name, paramList, exceptionList, body));
    }

    public void addMethod(String comment,
                          Visibility visibility,
                          Scope scope,
                          String type,
                          String name,
                          String paramList,
                          String exceptionList,
                          Block body) {
        methods.add(new Method(comment, visibility, scope, type, name, paramList, exceptionList,
                newToOld(body)));
    }

    public void addMethodReadClassState(String comment,
                                        String type,
                                        String name,
                                        String... body) {
        methods.add(Method.newReadClassState(comment, type, name, body));
    }

    public void addMethodPublicDynamic(String comment,
                                       String type,
                                       String name,
                                       String paramList,
                                       String exceptionList,
                                       String... body) {
        methods.add(Method.newPublicDynamicMethod(comment, type, name, paramList, exceptionList, body));
    }

    public void addMethodPublicReadObjectState(String comment,
                                               String type,
                                               String name,
                                               String... body) {
        methods.add(Method.newPublicReadObjectState(comment, type, name, body));
    }

    public void addMethodPublicReadObjectState(String comment,
                                               String type,
                                               String name,
                                               Block body) {
        methods.add(Method.newPublicReadObjectState(comment, type, name, body));
    }

    public void addConstructorPublicWithExceptions(String comment,
                                                   String paramList,
                                                   String exceptionList,
                                                   String... body) {
        methods.add(Method.newPublicConstructorWithException(comment, getName(), paramList, exceptionList, body));
    }

    public void addConstructorPublicWithExceptions(String comment,
                                                   String paramList,
                                                   String exceptionList,
                                                   Block body) {
        addConstructorPublicWithExceptions(comment, paramList, exceptionList, newToOld(body));
    }

    public void addConstructorPublic(String comment,
                                     String paramList,
                                     String... body) {
        methods.add(Method.newPublicConstructor(comment, getName(), paramList, body));
    }

    public void addConstructorPublic(String comment,
                                     String paramList,
                                     Block body) {
        methods.add(Method.newPublicConstructor(comment, getName(), paramList, newToOld(body)));
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
            to.hintStart("Group");

            for (Import anImport : imports) {
                if (null != anImport)
                    anImport.writeAtoms(to);
                else {
                    to.hintEnd("Group");
                    to.hintStart("Group");
                }
            }
            to.hintEnd("Group");
        }
    }

    private static void formatVariableDeclarations(CodeAtoms to, final List<Var> fields) {
                if (!fields.isEmpty()) {
                    to.hintStart("Group");
                    Scope scopeOfPrevious = fields.get(0).getScope();

                    for (Var variable : fields) {
                        if (!variable.getScope().equals(scopeOfPrevious)) {
                            to.hintEnd("Group");
                            to.hintStart("Group");
                        }
                        new Statement(variable).writeAtoms(to);
                        scopeOfPrevious = variable.getScope();
                    }

                    to.hintEnd("Group");
                }
    }

    private static String formatMethods(List<Method> methods) {
        String out = "";

        for (Method method : methods) {
            out += method + "\n";
        }

        return out;
    }

    private String constructorFromFields() {
        Block body = new Block();
        LinkedList<Map.Entry<String, String>> args = new LinkedList<Map.Entry<String, String>>();
        for (Var dec : fields) {
            body.addStatement(setFieldToVariableSameName(dec.getName()));
            args.add(new AbstractMap.SimpleImmutableEntry<String, String>(dec.getType(), dec.getName()));
        }
        return Method.newPublicConstructor(null, name, createParameterList(args), newToOld(body)) + "\n";
    }

    public String toString() {
        String out = "";
        CodeAtoms typedStart = new CodeAtoms();
        typedStart.hintStart(OUTER_LEVEL);

        if (null != where) {
            typedStart.add(new IR.CodeAtom("package"));
            where.writeAtoms(typedStart);
            typedStart.add(HasAtoms.EOL);
        }

        formatImports(typedStart);

        for (Annotate ann : classAnnotate)
            ann.writeAtoms(typedStart);

        visibility.writeAtoms(typedStart);
        kind.writeAtoms(typedStart);
        typedStart.add(new ClassWriter.Atom(name));
        if (null != parent) {
            typedStart.add(new IR.CodeAtom("extends"));
            typedStart.add(new IR.CodeAtom(parent));
        }
        if (null != implementsInterface) {
            typedStart.add(new IR.CodeAtom("implements"));
            typedStart.add(new IR.CodeAtom(implementsInterface));
        }

        typedStart.hintEnd(OUTER_LEVEL);
        out += Util.joinStringArray(
                DEFAULT_STYLE_INDENT.asFormattedLines(typedStart).toArray(),
                "\n", "", "");
        out += " {\n";

        if ((ClassKind.ENUM == kind && !enums.isEmpty()) || !fields.isEmpty()) {
            CodeAtoms typedBody = new CodeAtoms();
            typedBody.hintStart(OUTER_LEVEL);

            if (ClassKind.ENUM == kind && !enums.isEmpty())
                enums.writeAtoms(typedBody);

            formatVariableDeclarations(typedBody, fields);

            typedBody.hintEnd(OUTER_LEVEL);
            for (String line : DEFAULT_STYLE_INDENT.asFormattedLines(typedBody)) {
                if (0 < line.length())
                    out += "\t" + line;
                out += "\n";
            }

            out += "\n";
        }

        if (constructorFromAllFields)
            out += constructorFromFields();

        LinkedList<Method> constructors = new LinkedList<Method>();
        LinkedList<Method> other = new LinkedList<Method>();
        for (Method toSort : methods)
            if (name.equals(toSort.name))
                constructors.add(toSort);
            else
                other.add(toSort);

        out += formatMethods(constructors);
        out += formatMethods(other);

        out = removeBlankLine(out);

        out += "}" + "\n";

        return out;
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

    private static final Pattern scopeEndFirst = Pattern.compile("\\A\\}+");
    public static String indent(String[] lines, String startAt) {
        String out = "";
        int extraIndention = 0;
        for (String line : lines) {
            if (null != line) {
                char[] addIndention;
                Matcher scopeEndFirstInLine = scopeEndFirst.matcher(line);
                if (scopeEndFirstInLine.find()) {
                    final int numberOfScopes = extraIndention - scopeEndFirstInLine.end();
                    if (0 > numberOfScopes) throw endOfScopeNotBegan(line);
                    addIndention = new char[numberOfScopes];
                } else
                    addIndention = new char[extraIndention];
                extraIndention = updateIndention(extraIndention, line);
                Arrays.fill(addIndention, '\t');
                out += !line.isEmpty() ? (startAt + new String(addIndention) + line) : "";
            }
            out += "\n";
            if (0 > extraIndention)
                throw endOfScopeNotBegan(line);
        }
        if (0 < extraIndention)
            throw new IllegalArgumentException("Code to be indented don't end it's scope");
        return out;
    }

    private static IllegalArgumentException endOfScopeNotBegan(String offendingCode) {
        return new IllegalArgumentException("Code to be indented ends scope it didn't define: " + offendingCode);
    }

    private static int updateIndention(int extraIndention, String line) {
        for (int position = 0; position < line.length(); position++) {
            switch (line.charAt(position)) {
                case '{':
                    extraIndention++;
                    break;
                case '}':
                    extraIndention--;
                    break;
                case '(':
                    extraIndention++;
                    break;
                case ')':
                    extraIndention--;
                    break;
            }
        }
        return extraIndention;
    }

    static String ifIs(String element) {
        return ifIs("", element, "");
    }

    static String ifIs(String element, String after) {
        return ifIs("", element, after);
    }

    static String ifIs(String before, String element, String after) {
        return (null == element ? "" : before + element + after);
    }

    private static String removeBlankLine(String out) {
        return out.substring(0, out.length() - 1);
    }

    public static String[] newToOld(Block body) {
        String[] all = body.getJavaCodeLines();
        return Arrays.copyOfRange(all, 1, all.length - 1);
    }

    static class Method {
        private final String comment;
        private final Visibility visibility;
        private final Scope scope;
        private final String type;
        private final String name;
        private final String paramList;
        private final String exceptionList;
        private final String[] body;

        public Method(String comment, Visibility visibility, Scope scope, String type, String name, String paramList,
                      String exceptionList, String... body) {
            this.comment = comment;
            this.visibility = visibility;
            this.scope = scope;
            this.type = type;
            this.name = name;
            this.paramList = paramList;
            this.exceptionList = exceptionList;
            this.body = body;
        }

        @Override
        public String toString() {
            String out = (null == comment ? "" : "\t" + comment.replace("\n", "\n\t") + "\n");
            out += "\t" + ifIs("", visibility.toString(), " ") + ifIs(scope.toString(), " ") + ifIs(type, " ") +
                    name + "(" + ifIs(paramList) + ") " + ifIs("throws ", exceptionList, " ") + "{" + "\n";
            out += indent(body, "\t" + "\t");
            out += "\t" + "}" + "\n";
            return out;
        }

        static Method newPublicConstructorWithException(String comment,
                                                        String name,
                                                        String paramList,
                                                        String exceptionList,
                                                        String... body) {
            return newPublicDynamicMethod(comment, null, name, paramList, exceptionList, body);
        }

        static Method newPublicConstructor(String comment,
                                           String name,
                                           String paramList,
                                           String... body) {
            return newPublicConstructorWithException(comment, name, paramList, null, body);
        }

        static Method newPublicReadObjectState(String comment,
                                               String type,
                                               String name,
                                               Block body) {
            return newPublicDynamicMethod(comment, type, name, null, null, newToOld(body));
        }

        private static Method newPublicReadObjectState(String comment,
                                               String type,
                                               String name,
                                               String... body) {
            return newPublicDynamicMethod(comment, type, name, null, null, body);
        }

        static Method newPublicDynamicMethod(String comment,
                                             String type,
                                             String name,
                                             String paramList,
                                             String exceptionList,
                                             String... body) {
            return new Method(comment, Visibility.PUBLIC, Scope.OBJECT, type, name, paramList, exceptionList, body);
        }

        public static Method newReadClassState(String comment, String type, String name, String... body) {
            return new Method(comment, Visibility.PUBLIC, Scope.CLASS, type, name, null, null, body);
        }
    }

    public static final CodeStyle DEFAULT_STYLE;
    public static final CodeStyle DEFAULT_STYLE_INDENT;
    static {
        final CodeStyleBuilder<DefaultStyleScopeInfo> maker =
                new CodeStyleBuilder<DefaultStyleScopeInfo>(
                        CodeStyle.Action.INSERT_SPACE,
                        DefaultStyleScopeInfo.class);

        maker.atTheBeginning(CodeStyle.Action.DO_NOTHING);
        maker.atTheEnd(CodeStyle.Action.DO_NOTHING);
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
                return OUTER_LEVEL.equals(argument.seeTopHint());
            }
        });
        maker.whenBefore(Annotate.Atom.class, CodeStyle.Action.BREAK_LINE,
                new Util.OneCondition<DefaultStyleScopeInfo>() {
                    @Override
                    public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                        return OUTER_LEVEL.equals(argument.seeTopHint());
                    }
                });
        maker.whenBefore(Visibility.Atom.class, CodeStyle.Action.BREAK_LINE,
                new Util.OneCondition<DefaultStyleScopeInfo>() {
                    @Override
                    public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                        return OUTER_LEVEL.equals(argument.seeTopHint());
                    }
                });
        maker.whenAfter(Visibility.Atom.class, CodeStyle.Action.INSERT_SPACE,
                new Util.OneCondition<DefaultStyleScopeInfo>() {
                    @Override
                    public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                        return OUTER_LEVEL.equals(argument.seeTopHint());
                    }
                });
        maker.whenBefore(ClassKind.Atom.class, CodeStyle.Action.BREAK_LINE,
                new Util.OneCondition<DefaultStyleScopeInfo>() {
                    @Override
                    public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                        return OUTER_LEVEL.equals(argument.seeTopHint());
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
            @Override public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return EnumElements.class.getName().equals(argument.seeTopHint());
            }
        });
        maker.whenAfter(HasAtoms.EOL, CodeStyle.Action.BREAK_LINE_BLOCK, new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return EnumElements.class.getName().equals(argument.seeTopHint());
            }
        });
        maker.whenAfter(HasAtoms.SEP, CodeStyle.Action.BREAK_LINE, new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return 1 < argument.getLineBreakTry() && argument.approachingTheEdge();
            }
        });
        maker.whenAfter(HasAtoms.SEP, CodeStyle.Action.BREAK_LINE, new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return 2 < argument.getLineBreakTry();
            }
        });
        maker.whenBefore(HasAtoms.ADD, CodeStyle.Action.BREAK_LINE, new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override
            public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return 3 < argument.getLineBreakTry() && argument.approachingTheEdge();
            }
        });
        maker.whenAfter(HasAtoms.ALS, CodeStyle.Action.BREAK_LINE, new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return 0 < argument.getLineBreakTry();
            }
        });
        maker.whenBefore(HasAtoms.ALE, CodeStyle.Action.BREAK_LINE, new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override public boolean isTrueFor(DefaultStyleScopeInfo argument) {
                return 0 < argument.getLineBreakTry();
            }
        });
        maker.whenAfter(HasAtoms.ALS, CodeStyle.Action.DO_NOTHING);
        maker.whenBefore(HasAtoms.ALE, CodeStyle.Action.DO_NOTHING);
        maker.whenBefore(HasAtoms.SEP, CodeStyle.Action.DO_NOTHING);
        maker.whenBetween(HasAtoms.WHILE, HasAtoms.LPR, CodeStyle.Action.INSERT_SPACE);
        maker.whenBetween(HasAtoms.IF, HasAtoms.LPR, CodeStyle.Action.INSERT_SPACE);
        maker.whenBetween(HasAtoms.FOR, HasAtoms.LPR, CodeStyle.Action.INSERT_SPACE);
        maker.whenBefore(HasAtoms.INC, CodeStyle.Action.DO_NOTHING);
        maker.whenBefore(HasAtoms.LPR, CodeStyle.Action.DO_NOTHING);
        maker.whenAfter(HasAtoms.LPR, CodeStyle.Action.DO_NOTHING);
        maker.whenBefore(HasAtoms.ARRAY_ACCESS_START, CodeStyle.Action.DO_NOTHING);
        maker.whenAfter(HasAtoms.ARRAY_ACCESS_START, CodeStyle.Action.DO_NOTHING);
        maker.whenBefore(HasAtoms.ARRAY_ACCESS_END, CodeStyle.Action.DO_NOTHING);

        maker.alwaysOnState(new Util.OneCondition<DefaultStyleScopeInfo>() {
            @Override public boolean isTrueFor(DefaultStyleScopeInfo info) {
                boolean toLong = 100 <= info.getLineLength() && info.getLineBreakTry() < 10;
                //TODO: Stop doing as a side effect
                if (toLong) {
                    info.lineBreakTry++;
                    info.toFar.add(info.getNowAt());
                }
                return toLong;
            }
        }, CodeStyle.Action.RESET_LINE);
        maker.alwaysBefore(HasAtoms.ALS, CodeStyle.Action.SCOPE_ENTER);
        maker.alwaysBefore(HasAtoms.ALE, CodeStyle.Action.SCOPE_EXIT);
        maker.alwaysBefore(HasAtoms.LPR, CodeStyle.Action.SCOPE_ENTER);
        maker.alwaysBefore(HasAtoms.RPR, CodeStyle.Action.SCOPE_EXIT);
        maker.alwaysBefore(HasAtoms.LSC, CodeStyle.Action.SCOPE_ENTER);
        maker.alwaysBefore(HasAtoms.RSC, CodeStyle.Action.SCOPE_EXIT);

        DEFAULT_STYLE = maker.getStyle();

        maker.alwaysBefore(HasAtoms.ALS, CodeStyle.Action.INDENT);
        maker.alwaysBefore(HasAtoms.LPR, CodeStyle.Action.INDENT);
        maker.alwaysBefore(HasAtoms.LSC, CodeStyle.Action.INDENT);
        DEFAULT_STYLE_INDENT = maker.getStyle();
    }

    public static class DefaultStyleScopeInfo extends CodeStyle.ScopeStack.ScopeInfo {
        private int lineBreakTry = 0;
        private LinkedList<Integer> toFar = new LinkedList<Integer>();

        public int getLineBreakTry() {
            return lineBreakTry;
        }

        public boolean approachingTheEdge() {
            return 100 < getLineLength() + getNextLen() + 1 || toFar.contains(getNowAt());
        }
    }

    public static class Atom extends IR.CodeAtom {
        public Atom(String atom) {
            super(atom);
        }
    }
}
