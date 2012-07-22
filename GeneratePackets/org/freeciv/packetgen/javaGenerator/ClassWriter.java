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

import org.freeciv.packetgen.javaGenerator.expression.willReturn.SomeExpr;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassWriter {
    private final TargetPackage where;
    private final String[] imports;
    private final Visibility visibility;
    private final ClassKind kind;
    private final String madeFrom;
    private final String name;
    private final String parent;
    private final String implementsInterface;

    private final LinkedList<VariableDeclaration> constants = new LinkedList<VariableDeclaration>();
    private final LinkedList<VariableDeclaration> stateVars = new LinkedList<VariableDeclaration>();

    private final LinkedList<Method> methods = new LinkedList<Method>();
    protected final LinkedHashMap<String, EnumElement> enums = new LinkedHashMap<String, ClassWriter.EnumElement>();

    private boolean constructorFromAllFields = false;

    public ClassWriter(ClassKind kind, TargetPackage where, String[] imports, String madeFrom, String name,
                       String parent, String implementsInterface) {
        if (null == name) throw new IllegalArgumentException("No name for class to be generated");

        this.where = where;
        this.imports = imports;
        this.madeFrom = madeFrom;
        this.name = name;
        this.parent = parent;
        this.implementsInterface = implementsInterface;
        this.kind = kind;

        visibility = Visibility.PUBLIC;
    }

    public void addClassConstant(String type, String name, String value) {
        constants.add(new VariableDeclaration(Visibility.PRIVATE, Scope.CLASS, Modifiable.NO, type, name, value));
    }

    public void addClassConstant(Visibility visibility, String type, String name, String value) {
        constants.add(new VariableDeclaration(visibility, Scope.CLASS, Modifiable.NO, type, name, value));
    }

    public void addObjectConstant(String type, String name) {
        stateVars.add(new VariableDeclaration(Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO, type, name, null));
    }

    public void addPublicObjectConstant(String type, String name) {
        stateVars.add(new VariableDeclaration(Visibility.PUBLIC, Scope.OBJECT, Modifiable.NO, type, name, null));
    }

    public void addObjectConstantAndGetter(String type, String name) {
        stateVars.add(new VariableDeclaration(Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO, type, name, null));
        addMethodPublicReadObjectState(
                        null,
                        type,
                        "get" + name.substring(0, 1).toUpperCase() + name.substring(1),
                        "return " + "this." + name + ";");
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
                                                   String paramList,
                                                   String exceptionList,
                                                   String... body) {
        methods.add(Method.newPublicConstructorWithException(comment, getName(), paramList, exceptionList, body));
    }

    public void addConstructorPublic(String comment,
                                     String paramList,
                                     String... body) {
        methods.add(Method.newPublicConstructor(comment, getName(), paramList, body));
    }

    public void addConstructorFields() {
        constructorFromAllFields = true;
    }

    protected void addEnumerated(EnumElement element) {
        assert kind.equals(ClassKind.ENUM);

        enums.put(element.getEnumValueName(), element);
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
    protected static String setFieldToVariableSameName(String field) {
        return "this." + field + " = " + field + ";";
    }

    private String formatImports() {
        String out = "";

        for (String anImport : imports) {
            if ((null != anImport) && !anImport.isEmpty())
                out += declareImport(anImport);
            out += "\n";
        }
        if (0 < imports.length) out += "\n";

        return out;
    }

    private String formatEnumeratedElements() {
        String out = "";

        for (EnumElement element : enums.values()) {
            out += "\t" + element + "," + "\n";
        }
        if (!enums.isEmpty())
            out = out.substring(0, out.length() - 2) + ";" + "\n" + "\n";

        return out;
    }

    private static String formatVariableDeclarations(List<VariableDeclaration> variables) {
        String out = "";

        for (VariableDeclaration variable : variables) {
            out += "\t" + variable + "\n";
        }
        if (!variables.isEmpty()) out += "\n";

        return out;
    }

    private static String formatMethods(List<Method> methods) {
        String out = "";

        for (Method method : methods) {
            out += method + "\n";
        }

        return out;
    }

    private String constructorFromFields() {
        LinkedList<String> body = new LinkedList<String>();
        LinkedList<Map.Entry<String, String>> args = new LinkedList<Map.Entry<String, String>>();
        for (VariableDeclaration dec : stateVars) {
            body.add(setFieldToVariableSameName(dec.name));
            args.add(new AbstractMap.SimpleImmutableEntry<String, String>(dec.type, dec.name));
        }
        return Method.newPublicConstructor(null, name, createParameterList(args), body.toArray(new String[0])) + "\n";
    }

    public String toString() {
        String out = "";

        if (null != where) out = packageDeclaration(where) + "\n" +
                "\n";

        if (null != imports) out += formatImports();

        if (null != madeFrom) out += commentAutoGenerated(madeFrom) + "\n";
        out += visibility + " " + kind + " " + name + ifIs(" extends ", parent, "") + ifIs(" implements ",
                                                                                           implementsInterface,
                                                                                           "") + " {" + "\n";

        if (ClassKind.ENUM == kind)
            out += formatEnumeratedElements();

        out += formatVariableDeclarations(constants);
        out += formatVariableDeclarations(stateVars);

        if (constructorFromAllFields)
            out += constructorFromFields();

        out += formatMethods(methods);

        out = removeBlankLine(out);

        out += "}" + "\n";

        return out;
    }

    public String getName() {
        return name;
    }

    public String getPackage() {
        return where.getJavaCode();
    }

    public VariableDeclaration getField(String name) {
        HashSet<VariableDeclaration> allFields = new HashSet<VariableDeclaration>(constants);
        allFields.addAll(stateVars);
        for (VariableDeclaration field : allFields) {
            if (field.getName().equals(name)) return field;
        }
        return null;
    }

    public boolean hasConstant(String name) {
        VariableDeclaration field = getField(name);
        return null != field && field.modifiable.equals(Modifiable.NO);
    }

    static String commentAutoGenerated(String from) {
        return "// This code was auto generated from " + from;
    }

    static String packageDeclaration(TargetPackage inPackage) {
        return "package " + inPackage.getJavaCode() + ";";
    }

    static String declareImport(String toImport) {
        return "import " + toImport + ";";
    }

    protected static String allInPackageOf(Class thisClass) {
        return thisClass.getPackage().getName() + ".*";
    }

    private static String indent(String code) {
        return "\t" + code.replace("\n", "\n\t");
    }

    private static final Pattern scopeEndFirst = Pattern.compile("\\A\\}+");
    private static String indent(String[] lines, String startAt) {
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
        return (null == element ? "" : before + element + after);
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

        public Method(String comment, Visibility visibility, Scope scope, String type, String name, String paramList,
                      String exceptionList, SomeExpr... body) {
            this(comment, visibility, scope, type, name, paramList, exceptionList, exprsToString(body));
        }

        private static String[] exprsToString(SomeExpr[] inn) {
            String[] raw = new String[inn.length];
            for (int i = 0; i < inn.length; i++) {
                raw[i] = inn[i].getJavaCode();
            }
            return raw;
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

    public static class VariableDeclaration {
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

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public String toString() {
            return ifIs(visibility.toString(), " ") +
                    ifIs(scope.toString(), " ") +
                    ifIs(modifiable.toString(), " ") +
                    type + " " + name + ifIs(" = ", value, "") + ";";
        }
    }

    public static class EnumElement {
        private final String comment;
        private final String elementName;
        private final String paramlist;

        protected EnumElement(String comment, String elementName, String params) {
            if (null == elementName)
                throw new IllegalArgumentException("All elements of enums must have names");

            this.comment = comment;
            this.elementName = elementName;
            this.paramlist = params;
        }

        public String getEnumValueName() {
            return elementName;
        }

        public String toString() {
            return elementName + " (" + paramlist + ")" + ifIs(" /* ", comment, " */");
        }

        public static EnumElement newEnumValue(String enumValueName) {
            return newEnumValue(null, enumValueName, null);
        }

        public static EnumElement newEnumValue(String enumValueName, String params) {
            return newEnumValue(null, enumValueName, params);
        }

        public static EnumElement newEnumValue(String comment, String enumValueName, String params) {
            return new EnumElement(comment, enumValueName, params);
        }
    }

    public enum Visibility {
        PUBLIC,
        PACKAGE(null),
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
        CLASS("static"),
        OBJECT(null);

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
        YES(null),
        NO("final");

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
        CLASS,
        ENUM,
        INTERFACE;

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
