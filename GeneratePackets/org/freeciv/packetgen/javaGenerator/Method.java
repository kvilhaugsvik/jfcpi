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
import org.freeciv.packetgen.javaGenerator.formating.CodeStyle;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Method extends Formatted implements HasAtoms {
    private final Comment comment;
    private final Visibility visibility;
    private final Scope scope;
    private final TargetClass type;
    private final String name;
    private final List<Var> paramList;
    private final List<TargetClass> exceptionList;
    private final Block body;

    @Deprecated
    public static List<Var> oldParmanList2newParamList(String classes) {
        if (null == classes || "".equals(classes))
            return Collections.<Var>emptyList();
        List<Var> out = new LinkedList<Var>();
        for (String target : classes.split(", ")) {
            String[] typeAndName = target.split(" ");
            out.add(Var.param(typeAndName[0], typeAndName[1]));
        }
        return out;
    }

    protected Method(Comment comment, Visibility visibility, Scope scope,
                     TargetClass type, String name, List<Var> paramList,
                     String exceptionList, Block body) {
        this.comment = comment;
        this.visibility = visibility;
        this.scope = scope;
        this.type = type;
        this.name = name;
        this.paramList = paramList;
        this.exceptionList = ClassWriter.oldClassList2newClassList(exceptionList);
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
        if (!paramList.isEmpty()) {
            to.hintStart(CodeStyle.ARGUMENTS);
            to.joinSep(SEP, paramList.toArray(new HasAtoms[paramList.size()]));
            to.hintEnd(CodeStyle.ARGUMENTS);
        }
        to.add(HasAtoms.RPR);
        if (!exceptionList.isEmpty()) {
            to.add(THROWS);
            to.joinSep(SEP, exceptionList.toArray(new HasAtoms[exceptionList.size()]));
        }
        body.writeAtoms(to);
    }

    public static Method newPublicConstructorWithException(Comment comment,
                                                    String name,
                                                    List<Var> paramList,
                                                    String exceptionList,
                                                    Block body) {
        return newPublicDynamicMethod(comment, TargetClass.fromName(null), name, paramList, exceptionList, body);
    }

    @Deprecated
    public static Method newPublicConstructorWithException(Comment comment,
                                                    String name,
                                                    String paramList,
                                                    String exceptionList,
                                                    Block body) {
        return newPublicConstructorWithException(comment, name, oldParmanList2newParamList(paramList),
                exceptionList, body);
    }

    public static Method newPublicConstructor(Comment comment,
                                       String name,
                                       List<Var> paramList,
                                       Block body) {
        return newPublicConstructorWithException(comment, name, paramList, null, body);
    }

    @Deprecated
    public static Method newPublicConstructor(Comment comment,
                                       String name,
                                       String paramList,
                                       Block body) {
        return newPublicConstructor(comment, name, oldParmanList2newParamList(paramList), body);
    }

    public static Method newPublicReadObjectState(Comment comment,
                                           TargetClass type,
                                           String name,
                                           Block body) {
        return newPublicDynamicMethod(comment, type, name, Collections.<Var>emptyList(), null, body);
    }

    @Deprecated
    public static Method newPublicDynamicMethod(Comment comment,
                                         TargetClass type,
                                         String name,
                                         String paramList,
                                         String exceptionList,
                                         Block body) {
        return newPublicDynamicMethod(comment,
                type, name, oldParmanList2newParamList(paramList),
                exceptionList, body);
    }

    public static Method newPublicDynamicMethod(Comment comment,
                                         TargetClass type,
                                         String name,
                                         List<Var> paramList,
                                         String exceptionList,
                                         Block body) {
        return custom(comment, Visibility.PUBLIC, Scope.OBJECT, type, name, paramList, exceptionList, body);
    }

    public static Method newReadClassState(Comment comment, TargetClass type, String name, Block body) {
        return custom(comment, Visibility.PUBLIC, Scope.CLASS, type, name, Collections.<Var>emptyList(), null, body);
    }

    @Deprecated
    public static Method custom(Comment comment, Visibility visibility, Scope scope,
                                TargetClass type, String name, String paramList,
                                String exceptionList, Block body) {
        return custom(comment, visibility, scope, type, name, oldParmanList2newParamList(paramList),
                exceptionList, body);
    }

    public static Method custom(Comment comment, Visibility visibility, Scope scope,
                                      TargetClass type, String name, List<Var> paramList,
                                      String exceptionList, Block body) {
        return new Method(comment, visibility, scope, type, name, paramList,
                exceptionList, body);
    }
}
