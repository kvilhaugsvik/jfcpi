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

package com.kvilhaugsvik.javaGenerator;

import com.kvilhaugsvik.javaGenerator.util.Formatted;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AValue;
import com.kvilhaugsvik.javaGenerator.formating.TokensToStringStyle;
import com.kvilhaugsvik.javaGenerator.representation.CodeAtoms;
import com.kvilhaugsvik.javaGenerator.representation.HasAtoms;
import com.kvilhaugsvik.javaGenerator.representation.IR;

import java.util.Collections;
import java.util.List;

public class Method extends Formatted implements HasAtoms {
    private final Comment comment;
    private final Visibility visibility;
    private final Scope scope;
    private final TargetClass type;
    private final IR.CodeAtom name;
    private final List<? extends Var<? extends AValue>> paramList;
    private final List<TargetClass> exceptionList;
    private final Block body;

    protected Method(Comment comment, Visibility visibility, Scope scope,
                     TargetClass type, String name, List<? extends Var<? extends AValue>> paramList,
                     List<TargetClass> exceptionList, Block body) {
        this.comment = comment;
        this.visibility = visibility;
        this.scope = scope;
        this.type = type;
        this.name = new IR.CodeAtom(name);
        this.paramList = paramList;
        this.exceptionList = exceptionList;
        this.body = body;
    }

    public TargetMethod getAddress() {
        return getAddressOn(TargetClass.SELF_TYPED);
    }

    public TargetMethod getAddressOn(TargetClass on) {
        return new TargetMethod(on, name.get(), type, Scope.CLASS.equals(scope) ?
                TargetMethod.Called.STATIC :
                TargetMethod.Called.DYNAMIC);
    }

    @Override
    public String toString() {
        return this.getJavaCodeIndented("\t", ClassWriter.DEFAULT_STYLE_INDENT) + "\n";
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        comment.writeAtoms(to);
        visibility.writeAtoms(to);
        scope.writeAtoms(to);
        type.writeAtoms(to);
        if (!(this instanceof Constructor))
            name.writeAtoms(to);
        to.add(HasAtoms.LPR);
        if (!paramList.isEmpty()) {
            to.hintStart(TokensToStringStyle.ARGUMENTS);
            to.joinSep(SEP, paramList);
            to.hintEnd(TokensToStringStyle.ARGUMENTS);
        }
        to.add(HasAtoms.RPR);
        if (!exceptionList.isEmpty()) {
            to.add(THROWS);
            to.joinSep(SEP, exceptionList);
        }
        body.writeAtoms(to);
    }

    public static Method newConstructor(Comment comment,
                                        Visibility visibility,
                                        List<? extends Var<? extends AValue>> paramList,
                                        List<TargetClass> exceptionList,
                                        Block body) {
        return new Constructor(comment, visibility, paramList, exceptionList, body);
    }

    public static Method newPublicConstructorWithException(Comment comment,
                                                           List<? extends Var<? extends AValue>> paramList,
                                                           List<TargetClass> exceptionList,
                                                           Block body) {
        return newConstructor(comment, Visibility.PUBLIC, paramList, exceptionList, body);
    }

    public static class Constructor extends Method {
        protected Constructor(Comment comment,
                              Visibility visibility,
                              List<? extends Var<? extends AValue>> paramList,
                              List<TargetClass> exceptionList,
                              Block body) {
            super(comment, visibility, Scope.OBJECT, TargetClass.SELF_TYPED, null, paramList, exceptionList, body);
        }
    }

    public static Method newPublicConstructor(Comment comment,
                                              List<? extends Var<? extends AValue>> paramList,
                                              Block body) {
        return newPublicConstructorWithException(comment, paramList, Collections.<TargetClass>emptyList(), body);
    }

    public static Method newConstructor(Comment comment,
                                        Visibility visibility,
                                        List<? extends Var<? extends AValue>> paramList,
                                        Block body) {
        return new Constructor(comment, visibility, paramList, Collections.<TargetClass>emptyList(), body);
    }

    public static Method newPublicReadObjectState(Comment comment,
                                           TargetClass type,
                                           String name,
                                           Block body) {
        return newPublicDynamicMethod(comment, type, name, Collections.<Var<AValue>>emptyList(),
                Collections.<TargetClass>emptyList(), body);
    }

    public static Method newPublicDynamicMethod(Comment comment,
                                         TargetClass type,
                                         String name,
                                         List<? extends Var<? extends AValue>> paramList,
                                         List<TargetClass> exceptionList,
                                         Block body) {
        return custom(comment, Visibility.PUBLIC, Scope.OBJECT, type, name, paramList, exceptionList, body);
    }

    public static Method newReadClassState(Comment comment, TargetClass type, String name, Block body) {
        return custom(comment, Visibility.PUBLIC, Scope.CLASS, type, name, Collections.<Var<AValue>>emptyList(),
                Collections.<TargetClass>emptyList(), body);
    }

    public static Method custom(Comment comment, Visibility visibility, Scope scope,
                                      TargetClass type, String name, List<? extends Var<? extends AValue>> paramList,
                                      List<TargetClass> exceptionList, Block body) {
        return new Method(comment, visibility, scope, type, name, paramList,
                exceptionList, body);
    }

    public static Helper newHelper(Comment comment,
                                   TargetClass type,
                                   String name,
                                   List<? extends Var<? extends AValue>> paramList,
                                   Block body) {
        return new Helper(comment, type, name, paramList, body);
    }

    public static class Helper extends Method {
        protected Helper(Comment comment,
                         TargetClass type,
                         String name,
                         List<? extends Var<? extends AValue>> paramList,
                         Block body) {
            super(comment, Visibility.PRIVATE, Scope.CLASS, type, name, paramList,
                    Collections.<TargetClass>emptyList(), body);
        }
    }
}
