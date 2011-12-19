/*
 * Copyright (c) 2011. Sveinung Kvilhaugsvik
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

package org.freeciv.packetgen;

import java.util.LinkedList;
import java.util.List;

public class ClassWriter {
    Package where;
    String[] imports;
    Visibility visibility;
    ClassKind kind;
    String madeFrom;
    String name;
    String implementsInterface;

    LinkedList<VariableDeclaration> constants = new LinkedList<VariableDeclaration>();
    LinkedList<VariableDeclaration> stateVars = new LinkedList<VariableDeclaration>();

    LinkedList<Method> methods = new LinkedList<Method>();

    public ClassWriter(Package where, String[] imports, String madeFrom, String name, String implementsInterface) {
        if (null == name) throw new IllegalArgumentException("No name for class to be generated");

        this.where = where;
        this.imports = imports;
        this.madeFrom = madeFrom;
        this.name = name;
        this.implementsInterface = implementsInterface;

        visibility = Visibility.PUBLIC;
        kind = ClassKind.CLASS;
    }

    public void addConstant(String type, String name, String value) {
        constants.add(new VariableDeclaration(Visibility.PRIVATE, Scope.CLASS, Modifiable.NO, type, name, value));
    }

    public void addObjectConstant(String type, String name) {
        stateVars.add(new VariableDeclaration(Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO, type, name, null));
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

    public void addReadClassState(String comment,
                                  String type,
                                  String name,
                                  String... body) {
        methods.add(Method.newReadClassState(comment, type, name, body));
    }

    public void addPublicDynamicMethod(String comment,
                                       String type,
                                       String name,
                                       String paramList,
                                       String exceptionList,
                                       String... body) {
        methods.add(Method.newPublicDynamicMethod(comment, type, name, paramList, exceptionList, body));
    }

    public void addPublicReadObjectState(String comment,
                                         String type,
                                         String name,
                                         String... body) {
        methods.add(Method.newPublicReadObjectState(comment, type, name, body));
    }

    public void addPublicConstructorWithExceptions(String comment,
                               String name,
                               String paramList,
                               String exceptionList,
                               String... body) {
        methods.add(Method.newPublicConstructorWithException(comment, name, paramList, exceptionList, body));
    }

    public void addPublicConstructor(String comment,
                                     String name,
                                     String paramList,
                                     String... body) {
        methods.add(Method.newPublicConstructor(comment, name, paramList, body));
    }

    private String formatImports() {
        String out = "";

        for (String anImport: imports) {
            if ((null != anImport) && !anImport.equals(""))
                out += declareImport(anImport);
            out += "\n";
        }
        if (0 < imports.length) out += "\n";

        return out;
    }

    private String formatVariableDeclarations(List<VariableDeclaration> variables) {
        String out = "";

        for (VariableDeclaration variable: variables) {
            out += "\t" + variable + "\n";
        }
        if (!variables.isEmpty()) out += "\n";

        return out;
    }

    private String formatMethods(List<Method> methods) {
        String out = "";

        for (Method method: methods) {
            out += method + "\n";
        }

        return out;
    }

    public String toString() {
        String out = "";

        if (null != where) out = packageDeclaration(where) + "\n" +
                "\n";

        if (null != imports) out += formatImports();

        if (null != madeFrom) out += commentAutoGenerated(madeFrom) + "\n";
        out += visibility + " " + kind + " " + name + ifIs(" implements ", implementsInterface, "") + " {" + "\n";

        out += formatVariableDeclarations(constants);
        out += formatVariableDeclarations(stateVars);

        out += formatMethods(methods);

        out = removeBlankLine(out);

        out += "}" + "\n";

        return out;
    }

    static String commentAutoGenerated(String from) {
        return "// This code was auto generated from " + from;
    }

    static String packageDeclaration(Package inPackage) {
        return "package " + inPackage.getName() + ";";
    }

    static String declareImport(String toImport) {
        return "import " + toImport + ";";
    }

    private static String indent(String code) {
        return "\t" + code.replace("\n", "\n\t");
    }

    private static String ifIs(String element) {
        return ifIs("", element, "");
    }

    private static String ifIs(String element, String after) {
        return ifIs("", element, after);
    }

    private static String ifIs(String before, String element, String after) {
        return (null == element? "" : before + element + after);
    }

    private String removeBlankLine(String out) {
        return out.substring(0, out.length() - 1);
    }

    static class Method {
        String comment;
        Visibility visibility;
        Scope scope;
        String type;
        String name;
        String paramList;
        String exceptionList;
        String[] body;

        public Method(String comment, Visibility visibility, Scope scope, String type, String name, String paramList, String exceptionList, String... body) {
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
            String out = (null == comment ? "" : indent(comment) + "\n");
            out += ifIs("\t", visibility.toString(), " ") + ifIs(scope.toString(), " ") + ifIs(type, " ") +
                    name + "(" + ifIs(paramList) + ") " + ifIs("throws ", exceptionList, " ") + "{" + "\n";
            for (String line: body) {
                out += (!line.equals("")? "\t" + "\t" + line : "") + "\n";
            }
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

    static class VariableDeclaration {
        Visibility visibility;
        Scope scope;
        Modifiable modifiable;
        String type;
        String name;
        String value;

        public VariableDeclaration(Visibility visibility, Scope scope, Modifiable modifiable,
                                    String type, String name, String value) {
            this.visibility = visibility;
            this.scope = scope;
            this.modifiable = modifiable;
            this.type = type;
            this.name = name;
            this.value = value;
        }

        public String toString() {
            return ifIs(visibility.toString(), " ") +
                    ifIs(scope.toString(), " ") +
                    ifIs(modifiable.toString(), " ") +
                    type + " " + name + ifIs(" = ", value, "") + ";";
        }
    }

    public enum Visibility {
        PUBLIC,
        PACKAGE (null),
        PROTECTED,
        PRIVATE;

        private final String code;

        Visibility() {
            this.code = name().toLowerCase();
        }

        Visibility(String code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return code;
        }
    }

    public enum Scope {
        CLASS ("static"),
        OBJECT (null);

        private final String code;

        Scope(String code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return code;
        }
    }

    public enum Modifiable {
        YES (null),
        NO ("final");

        private final String code;

        Modifiable(String code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return code;
        }
    }

    public enum ClassKind {
        CLASS, ENUM, INTERFACE;

        private final String code;

        ClassKind() {
            this.code = name().toLowerCase();
        }

        @Override
        public String toString() {
            return code;
        }
    }
}
