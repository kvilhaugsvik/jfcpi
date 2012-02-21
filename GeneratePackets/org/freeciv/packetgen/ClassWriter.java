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

package org.freeciv.packetgen;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassWriter {
    private final Package where;
    private final String[] imports;
    private final Visibility visibility;
    private final ClassKind kind;
    private final String madeFrom;
    private final String name;
    private final String implementsInterface;

    private final LinkedList<VariableDeclaration> constants = new LinkedList<VariableDeclaration>();
    private final LinkedList<VariableDeclaration> stateVars = new LinkedList<VariableDeclaration>();

    private final LinkedList<Method> methods = new LinkedList<Method>();
    protected final HashMap<String, EnumElement> enums = new HashMap<String, ClassWriter.EnumElement>();

    public ClassWriter(ClassKind kind, Package where, String[] imports, String madeFrom, String name, String implementsInterface) {
        if (null == name) throw new IllegalArgumentException("No name for class to be generated");

        this.where = where;
        this.imports = imports;
        this.madeFrom = madeFrom;
        this.name = name;
        this.implementsInterface = implementsInterface;
        this.kind = kind;

        visibility = Visibility.PUBLIC;
    }

    public ClassWriter(Package where, String[] imports, String madeFrom, String name, String implementsInterface) {
        this(ClassKind.CLASS, where, imports, madeFrom, name, implementsInterface);
    }


    public void addClassConstant(String type, String name, String value) {
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

    public void addConstructorPublicWithExceptions(String comment,
                                                   String name,
                                                   String paramList,
                                                   String exceptionList,
                                                   String... body) {
        methods.add(Method.newPublicConstructorWithException(comment, name, paramList, exceptionList, body));
    }

    public void addConstructorPublic(String comment,
                                     String name,
                                     String paramList,
                                     String... body) {
        methods.add(Method.newPublicConstructor(comment, name, paramList, body));
    }

    public void addEnumerated(String comment,
                              String enumName,
                              int number,
                              String toStringName) {
        assert kind.equals(ClassKind.ENUM);

        addEnumerated(EnumElement.newEnumValue(comment, enumName, number, toStringName));
    }

    protected void addEnumerated(EnumElement element) {
        enums.put(element.getEnumValueName(), element);
    }

    private String formatImports() {
        String out = "";

        for (String anImport: imports) {
            if ((null != anImport) && !anImport.isEmpty())
                out += declareImport(anImport);
            out += "\n";
        }
        if (0 < imports.length) out += "\n";

        return out;
    }

    private String formatEnumeratedElements() {
        String out = "";

        LinkedList<EnumElement> elements = new LinkedList<EnumElement>(enums.values());
        Collections.sort(elements);
        for (EnumElement element: elements) {
            out += "\t" + element + "," + "\n";
        }
        if (!enums.isEmpty())
            out = out.substring(0, out.length() - 2) + ";" + "\n" + "\n";

        return out;
    }

    private static String formatVariableDeclarations(List<VariableDeclaration> variables) {
        String out = "";

        for (VariableDeclaration variable: variables) {
            out += "\t" + variable + "\n";
        }
        if (!variables.isEmpty()) out += "\n";

        return out;
    }

    private static String formatMethods(List<Method> methods) {
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

        if (ClassKind.ENUM == kind)
            out += formatEnumeratedElements();

        out += formatVariableDeclarations(constants);
        out += formatVariableDeclarations(stateVars);

        out += formatMethods(methods);

        out = removeBlankLine(out);

        out += "}" + "\n";

        return out;
    }

    public String getName() {
        return name;
    }

    public String getPackage() {
        return where.getName();
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

    static String allInPackageOf(Class thisClass) {
        return thisClass.getPackage().getName() + ".*";
    }

    private static String indent(String code) {
        return "\t" + code.replace("\n", "\n\t");
    }

    private static final Pattern scopeEndFirst = Pattern.compile("\\A\\}+");
    private static String indent(String[] lines, String startAt) {
        String out = "";
        int extraIndention = 0;
        for (String line: lines) {
            if (null != line) {
                char[] addIndention;
                Matcher scopeEndFirstInLine = scopeEndFirst.matcher(line);
                if (scopeEndFirstInLine.find()) {
                    final int numberOfScopes = extraIndention - scopeEndFirstInLine.end();
                    if (0 > numberOfScopes) throw endOfScopeNotBegan(line);
                    addIndention = new char[numberOfScopes];
                }
                else
                    addIndention = new char[extraIndention];
                extraIndention = updateIndention(extraIndention, line);
                Arrays.fill(addIndention, '\t');
                out += !line.isEmpty()? (startAt + new String(addIndention) + line) : "";
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
                case '{': extraIndention++; break;
                case '}': extraIndention--; break;
            }
        }
        return extraIndention;
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

    private static String removeBlankLine(String out) {
        return out.substring(0, out.length() - 1);
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
        private final Visibility visibility;
        private final Scope scope;
        private final Modifiable modifiable;
        private final String type;
        private final String name;
        private final String value;

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

    static class EnumElement implements Comparable<EnumElement> {
        private final String comment;
        private final String elementName;
        private final int number;
        private final String toStringName;
        private final boolean valid;

        EnumElement(String comment, String elementName, int number, String toStringName, boolean valid) {
            if (null == elementName)
                throw new IllegalArgumentException("All elements of enums must have names");

            // Look up numbers in a uniform way
            if (null == toStringName)
                throw new IllegalArgumentException("All elements of enums must have toStringNames");

            this.comment = comment;
            this.elementName = elementName;
            this.number = number;
            this.toStringName = toStringName;
            this.valid = valid;
        }

        public int getNumber() {
            return number;
        }

        public String getEnumValueName() {
            return elementName;
        }

        public String getToStringName() {
            return toStringName;
        }

        public boolean isValid() {
            return valid;
        }

        @Override
        public int compareTo(EnumElement other) {
            if (other.getNumber() == this.getNumber())
                return 0;
            if (0 <= this.getNumber() && 0 > other.getNumber())
                return -1;
            if (0 > this.getNumber() && 0 <= other.getNumber())
                return 1;
            return (this.getNumber() < other.getNumber())? -1: 1;
        }

        public String toString() {
            return elementName + " (" + number + ", " + toStringName +
                    (!valid?", " + valid : "") + ")" + ifIs(" /* ", comment, " */");
        }

        static EnumElement newEnumValue(String enumValueName, int number) {
            return newEnumValue(enumValueName, number, '"' + enumValueName +  '"');
        }

        static EnumElement newEnumValue(String enumValueName, int number, String toStringName) {
            return newEnumValue(null, enumValueName, number, toStringName);
        }

        static EnumElement newEnumValue(String comment, String enumValueName, int number, String toStringName) {
            return new EnumElement(comment, enumValueName, number, toStringName, true);
        }

        public static EnumElement newInvalidEnum(int value) {
            return new EnumElement(null, "INVALID", value,  "\"INVALID\"", false);
        }

        public static EnumElement newInvalidEnum(String nameInCode, String toStringName, int value) {
            return new EnumElement(null, nameInCode, value, toStringName, false);
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
