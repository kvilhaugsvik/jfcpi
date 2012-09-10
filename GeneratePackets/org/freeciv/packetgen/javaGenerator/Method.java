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
import org.freeciv.packetgen.javaGenerator.expression.util.Formatted;

import java.util.List;

public class Method extends Formatted implements HasAtoms {
    private final Comment comment;
    private final Visibility visibility;
    private final Scope scope;
    private final TargetClass type;
    private final String name;
    // TODO: Make typed
    private final String paramList;
    private final List<TargetClass> exceptionList;
    private final Block body;

    @Deprecated
    public Method(String comment, Visibility visibility, Scope scope, TargetClass type, String name, String paramList,
                  String exceptionList, Block body) {
        this(Comment.oldCompat(comment), visibility, scope, type, name, paramList, exceptionList, body);
    }

    public Method(Comment comment, Visibility visibility, Scope scope, TargetClass type, String name, String paramList,
                  String exceptionList, Block body) {
        this.comment = comment;
        this.visibility = visibility;
        this.scope = scope;
        this.type = type;
        this.name = name;
        this.paramList = paramList;
        this.exceptionList = (null == exceptionList ? null : ClassWriter.oldClassList2newClassList(exceptionList));
        this.body = body;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.getJavaCodeIndented("\t") + "\n";
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        comment.writeAtoms(to);
        visibility.writeAtoms(to);
        scope.writeAtoms(to);
        if (null != type) type.writeAtoms(to);
        to.add(new IR.CodeAtom(name));
        to.add(HasAtoms.LPR);
        if (null != paramList) to.add(new IR.CodeAtom(paramList));
        to.add(HasAtoms.RPR);
        if (null != exceptionList) {
            to.add(new IR.CodeAtom("throws"));
            to.joinSep(SEP, exceptionList.toArray(new HasAtoms[exceptionList.size()]));
        }
        body.writeAtoms(to);
    }

    @Deprecated
    public static Method newPublicConstructorWithException(String comment,
                                                           String name,
                                                           String paramList,
                                                           String exceptionList,
                                                           Block body) {
        return newPublicConstructorWithException(Comment.oldCompat(comment), name, paramList, exceptionList, body);
    }

    public static Method newPublicConstructorWithException(Comment comment,
                                                    String name,
                                                    String paramList,
                                                    String exceptionList,
                                                    Block body) {
        return newPublicDynamicMethod(comment, TargetClass.fromName(null), name, paramList, exceptionList, body);
    }

    @Deprecated
    public static Method newPublicConstructor(String comment,
                                              String name,
                                              String paramList,
                                              Block body) {
        return newPublicConstructor(Comment.oldCompat(comment), name, paramList, body);
    }

    public static Method newPublicConstructor(Comment comment,
                                       String name,
                                       String paramList,
                                       Block body) {
        return newPublicConstructorWithException(comment, name, paramList, null, body);
    }

    @Deprecated
    public static Method newPublicReadObjectState(String comment,
                                                  TargetClass type,
                                                  String name,
                                                  Block body) {
        return newPublicReadObjectState(Comment.oldCompat(comment), type, name, body);
    }

    public static Method newPublicReadObjectState(Comment comment,
                                           TargetClass type,
                                           String name,
                                           Block body) {
        return newPublicDynamicMethod(comment, type, name, null, null, body);
    }

    @Deprecated
    public static Method newPublicDynamicMethod(String comment,
                                                            TargetClass type,
                                                            String name,
                                                            String paramList,
                                                            String exceptionList,
                                                            Block body) {
        return newPublicDynamicMethod(Comment.oldCompat(comment), type, name, paramList, exceptionList, body);
    }

    public static Method newPublicDynamicMethod(Comment comment,
                                         TargetClass type,
                                         String name,
                                         String paramList,
                                         String exceptionList,
                                         Block body) {
        return new Method(comment, Visibility.PUBLIC, Scope.OBJECT, type, name, paramList, exceptionList, body);
    }

    @Deprecated
    public static Method newReadClassState(String comment, TargetClass type, String name, Block body) {
        return newReadClassState(Comment.oldCompat(comment), type, name, body);
    }

    public static Method newReadClassState(Comment comment, TargetClass type, String name, Block body) {
        return new Method(comment, Visibility.PUBLIC, Scope.CLASS, type, name, null, null, body);
    }
}
