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

    public void addStateVar(String type, String name) {
        constants.add(new VariableDeclaration(Visibility.PRIVATE, Scope.OBJECT, Modifiable.YES, type, name, null));
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

    public void addPublicDynamicMethod(String comment,
                                       String type,
                                       String name,
                                       String paramList,
                                       String exceptionList,
                                       String... body) {
        addMethod(comment, Visibility.PUBLIC, Scope.OBJECT, type, name, paramList, exceptionList, body);
    }

    public void addPublicReadObjectState(String comment,
                                         String type,
                                         String name,
                                         String... body) {
        addPublicDynamicMethod(comment, type, name, null, null, body);
    }

    public void addPublicConstructorWithExceptions(String comment,
                               String name,
                               String paramList,
                               String exceptionList,
                               String... body) {
        addPublicDynamicMethod(comment, null, name, paramList, exceptionList, body);
    }

    public void addPublicConstructor(String comment,
                                     String name,
                                     String paramList,
                                     String... body) {
        addPublicConstructorWithExceptions(comment, name, paramList, null, body);
    }

    private String formatImports() {
        String out = "";

        for (String anImport: imports) {
            out += "import " + anImport +";" + "\n";
        }
        if (imports.length < 0) out += "\n";

        return out;
    }

    private String formatVariableDeclarations(List<VariableDeclaration> variables) {
        String out = "";

        for (VariableDeclaration variable: variables) {
            out += "\t" + variable + ";" + "\n";
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
        String out = "package " + where.getName() + ";" + "\n" +
                "\n";

        out += formatImports()
                + "\n";

        out += "// This code was auto generated from " + madeFrom + "\n";
        out += visibility + " " + kind + " " + name + " implements " + implementsInterface + " {" + "\n";

        out += formatVariableDeclarations(constants);
        out += formatVariableDeclarations(stateVars);

        out += formatMethods(methods);

        out += "}";

        return out;
    }

    public static String publicConstructor(String comment,
                                           String name,
                                           String paramList,
                                           String exceptionList,
                                           String... body) {
        return publicDynamicMethod(comment, null, name, paramList, exceptionList, body);
    }

    public static String publicConstructorNoExceptions(String comment,
                                                       String name,
                                                       String paramList,
                                                       String... body) {
        return publicConstructor(comment, name, paramList, null, body);
    }

    public static String publicReadObjectState(String comment,
                                               String type,
                                               String name,
                                               String... body) {
        return publicDynamicMethod(comment, type, name, null, null, body);
    }

    public static String publicDynamicMethod(String comment,
                                             String type,
                                             String name,
                                             String paramList,
                                             String exceptionList,
                                             String... body) {
        return fullMethod(comment, "public", null, type, name, paramList, exceptionList, body);
    }

    public static String fullMethod(String comment,
                                    String access,
                                    String place,
                                    String type,
                                    String name,
                                    String paramList,
                                    String exceptionList,
                                    String... body) {
        String out = (null == comment? "" : indent(comment) + "\n");
        out += ifIs("\t", access, " ") + ifIs(place, " ") + ifIs(type, " ") +
                name + "(" + ifIs(paramList) + ") " + ifIs("throws ", exceptionList, " ") + "{" + "\n";
        for (String line: body) {
            out += (line != ""? "\t" + "\t" + line : "") + "\n";
        }
        out += "\t" + "}" + "\n";
        return out;
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

    private class Method {
        String comment;
        Visibility visibility;
        Scope scope;
        String type;
        String name;
        String paramList;
        String exceptionList;
        String[] body;

        private Method(String comment, Visibility visibility, Scope scope, String type, String name, String paramList, String exceptionList, String[] body) {
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
            return fullMethod(comment, visibility.toString(), scope.toString(), type, name, paramList, exceptionList, body);
        }
    }

    private class VariableDeclaration {
        Visibility visibility;
        Scope scope;
        Modifiable modifiable;
        String type;
        String name;
        String value;

        private VariableDeclaration(Visibility visibility, Scope scope, Modifiable modifiable,
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
                    type + " " + name + ifIs(" = ", value, ";");
        }
    }

    public enum Visibility {
        PUBLIC,
        PACKAGE (null),
        PROTECTED,
        PRIVATE;

        private String code;

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

        private String code;

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

        private String code;

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

        private String code;

        ClassKind() {
            this.code = name().toLowerCase();
        }

        @Override
        public String toString() {
            return code;
        }
    }
}
