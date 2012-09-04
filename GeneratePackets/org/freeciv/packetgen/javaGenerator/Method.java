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

import org.freeciv.packetgen.javaGenerator.expression.Block;

class Method {
    private final String comment;
    private final Visibility visibility;
    private final Scope scope;
    private final TargetClass type;
    private final String name;
    private final String paramList;
    private final String exceptionList;
    private final Block body;

    @Deprecated
    public Method(String comment, Visibility visibility, Scope scope, String type, String name, String paramList,
                  String exceptionList, Block body) {
        this(comment, visibility, scope, stringTypeToTargetClass(type), name, paramList, exceptionList, body);
    }

    private static TargetClass stringTypeToTargetClass(String type) {
        return (null == type ? TargetClass.INSTANCE : new TargetClass(type));
    }

    public Method(String comment, Visibility visibility, Scope scope, TargetClass type, String name, String paramList,
                  String exceptionList, Block body) {
        this.comment = comment;
        this.visibility = visibility;
        this.scope = scope;
        this.type = type;
        this.name = name;
        this.paramList = paramList;
        this.exceptionList = exceptionList;
        this.body = body;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        String out = (null == comment || "".equals(comment) ? "" : "\t" + comment.replace("\n", "\n\t") + "\n");
        out += "\t" + ClassWriter.ifIs("", visibility.toString(), " ") + ClassWriter.ifIs(scope.toString(), " ") +
                (null == type ? "" : type.getName() + " ") +
                name + "(" + ClassWriter.ifIs(paramList) + ") " + ClassWriter.ifIs("throws ", exceptionList, " ");
        out += body.getJavaCodeIndented("\t").substring(1);
        out += "\n";
        return out;
    }

    static Method newPublicConstructorWithException(String comment,
                                                    String name,
                                                    String paramList,
                                                    String exceptionList,
                                                    Block body) {
        return newPublicDynamicMethod(comment, TargetClass.INSTANCE, name, paramList, exceptionList, body);
    }

    static Method newPublicConstructor(String comment,
                                       String name,
                                       String paramList,
                                       Block body) {
        return newPublicConstructorWithException(comment, name, paramList, null, body);
    }

    @Deprecated
    static Method newPublicReadObjectState(String comment,
                                           String type,
                                           String name,
                                           Block body) {
        return newPublicReadObjectState(comment, stringTypeToTargetClass(type), name, body);
    }

    static Method newPublicReadObjectState(String comment,
                                           TargetClass type,
                                           String name,
                                           Block body) {
        return newPublicDynamicMethod(comment, type, name, null, null, body);
    }

    @Deprecated
    static Method newPublicDynamicMethod(String comment,
                                         String type,
                                         String name,
                                         String paramList,
                                         String exceptionList,
                                         Block body) {
        return newPublicDynamicMethod(comment, stringTypeToTargetClass(type), name, paramList, exceptionList, body);
    }

    static Method newPublicDynamicMethod(String comment,
                                         TargetClass type,
                                         String name,
                                         String paramList,
                                         String exceptionList,
                                         Block body) {
        return new Method(comment, Visibility.PUBLIC, Scope.OBJECT, type, name, paramList, exceptionList, body);
    }

    @Deprecated
    public static Method newReadClassState(String comment, String type, String name, Block body) {
        return newReadClassState(comment, stringTypeToTargetClass(type), name, body);
    }

    public static Method newReadClassState(String comment, TargetClass type, String name, Block body) {
        return new Method(comment, Visibility.PUBLIC, Scope.CLASS, type, name, null, null, body);
    }
}
