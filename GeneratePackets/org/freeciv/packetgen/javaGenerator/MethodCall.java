/*
 * Copyright (c) 2011, 2012, Sveinung Kvilhaugsvik
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

import org.freeciv.packetgen.javaGenerator.IR.CodeAtom;
import org.freeciv.packetgen.javaGenerator.expression.Value;
import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.util.Formatted;
import org.freeciv.packetgen.javaGenerator.expression.util.ValueHelper;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.Returnable;
import org.freeciv.packetgen.javaGenerator.formating.CodeStyle;

import java.util.Arrays;

public class MethodCall<Returns extends Returnable> extends Formatted implements HasAtoms, Typed<Returns> {
    // Ways to write argument lists
    private static final ArgList argListNormal = new ArgList(LPR, SEP, RPR);
    private static final ArgList argListArray = new ArgList(ARRAY_ACCESS_START, new HasAtoms() {
        @Override public void writeAtoms(CodeAtoms to) {
            to.hintEnd(CodeStyle.ARGUMENTS);
            to.add(ARRAY_ACCESS_END);
            to.add(ARRAY_ACCESS_START);
            to.hintStart(CodeStyle.ARGUMENTS);
        }
    }, ARRAY_ACCESS_END);

    // Were to place the method
    private static final MethodPosition placeMethodAfterTheFirstArgument = new MethodPosition() {
        @Override public boolean hasPList(Typed<? extends AValue>[] parameters) {
            return 1 < parameters.length;
        }

        @Override public Typed<? extends AValue>[] choose(Typed<? extends AValue>[] parameters) {
            return Arrays.copyOfRange(parameters, 1, parameters.length);
        }

        @Override public void call(HasAtoms method, Typed<? extends AValue>[] parameters, CodeAtoms to) {
            parameters[0].writeAtoms(to);
            method.writeAtoms(to);
        }
    };
    private static final MethodPosition placeMethodBeforeTheFirstArgument = new MethodPosition() {
        @Override public boolean hasPList(Typed<? extends AValue>[] parameters) {
            return 0 < parameters.length;
        }

        @Override public Typed<? extends AValue>[] choose(Typed<? extends AValue>[] parameters) {
            return parameters;
        }

        @Override public void call(HasAtoms method, Typed<? extends AValue>[] parameters, CodeAtoms to) {
            method.writeAtoms(to);
        }
    };

    private final HasAtoms writer;
    private final Comment comment;
    protected final HasAtoms method;
    protected final Typed<? extends AValue>[] parameters;

    public MethodCall(String name, Typed<? extends AValue>... params) {
        this(Comment.no(), name, params);
    }

    public MethodCall(Comment comment, String name, Typed<? extends AValue>... params) {
        this(TargetMethod.Called.STATIC, comment, transformName(TargetMethod.Called.STATIC, name), params);
    }

    public MethodCall(TargetMethod.Called kind, String name, Typed<? extends AValue>... params) {
        this(kind, transformName(kind, name), params);
    }

    public MethodCall(TargetMethod.Called kind, HasAtoms name, Typed<? extends AValue>... params) {
        this(kind, Comment.no(), name, params);
    }

    private static HasAtoms transformName(TargetMethod.Called kind, final String name) {
        switch (kind) {
            case STATIC:
            case DYNAMIC:
                return new CodeAtom(name);
            case DYNAMIC_ARRAY_GET:
            case MANUALLY:
                return new NotAWriter("Should be handled manually");
            case STATIC_ARRAY_INST:
                throw new IllegalArgumentException("Two tokens shouldn't be sent as one String");
            default:
                throw new UnsupportedOperationException("Can't formulate call for " + kind);
        }
    }

    private MethodCall(TargetMethod.Called kind, Comment comment, final HasAtoms name, Typed<? extends AValue>... params) {
        if (null == name)
            throw new IllegalArgumentException("No method name given to method call");

        this.comment = comment;
        this.parameters = params;

        switch (kind) {
            case DYNAMIC:
                this.method = new HasAtoms() {
                    @Override
                    public void writeAtoms(CodeAtoms to) {
                        to.add(HAS);
                        name.writeAtoms(to);
                    }
                };
                break;
            case DYNAMIC_ARRAY_GET:
                this.method = new HasAtoms() {
                    @Override public void writeAtoms(CodeAtoms to) {}
                };
                break;
            case STATIC:
            case MANUALLY:
            case STATIC_ARRAY_INST:
                this.method = name;
                break;
            default:
                throw new IllegalArgumentException("Can't formulate call for " + kind);
        }

        switch (kind) {
            case STATIC:
                writer = new AWriter(argListNormal, placeMethodBeforeTheFirstArgument);
                break;
            case DYNAMIC:
                writer =  new AWriter(argListNormal, placeMethodAfterTheFirstArgument);
                break;
            case STATIC_ARRAY_INST:
                writer = new AWriter(argListArray, placeMethodBeforeTheFirstArgument);
                break;
            case DYNAMIC_ARRAY_GET:
                writer = new AWriter(argListArray, placeMethodAfterTheFirstArgument);
                break;
            case MANUALLY:
                writer = new NotAWriter("The call should have been handled manually");
                break;
            default:
                throw new UnsupportedOperationException("Don't know how to write " + kind);
        }
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        writer.writeAtoms(to);
    }

    private static class NotAWriter implements HasAtoms {
        private final String problem;

        private NotAWriter(String problem) {
            this.problem = problem;
        }

        @Override
        public void writeAtoms(CodeAtoms to) {
            throw new UnsupportedOperationException(problem);
        }
    }

    private class AWriter implements HasAtoms {
        private final ArgList argList;
        private final MethodPosition place;

        public AWriter(ArgList argList, MethodPosition orderOfArgs) {
            this.place = orderOfArgs;
            this.argList = argList;
        }

        public void writeAtoms(CodeAtoms to) {
            to.hintStart(MethodCall.class.getCanonicalName());
            comment.writeAtoms(to);
            place.call(method, parameters, to);
            argList.before.writeAtoms(to);
            if (place.hasPList(parameters)) {
                to.hintStart(CodeStyle.ARGUMENTS);
                to.joinSep(argList.between, place.choose(parameters));
                to.hintEnd(CodeStyle.ARGUMENTS);
            }
            argList.after.writeAtoms(to);
            to.hintEnd(MethodCall.class.getCanonicalName());
        }
    }

    private static class ArgList {
        public final HasAtoms before;
        public final HasAtoms between;
        public final HasAtoms after;

        private ArgList(HasAtoms before, HasAtoms between, HasAtoms after) {
            this.before = before;
            this.between = between;
            this.after = after;
        }
    }

    private static interface MethodPosition {
        public abstract boolean hasPList(Typed<? extends AValue>[] parameters);
        public abstract Typed<? extends AValue>[] choose(Typed<? extends AValue>[] parameters);
        public abstract void call(HasAtoms method, Typed<? extends AValue>[] parameters, CodeAtoms to);
    }

    public static class HasResult<Returns extends AValue> extends MethodCall<Returns> implements Value<Returns> {
        private final ValueHelper valueHelper;

        public HasResult(TargetMethod.Called call, TargetClass type, HasAtoms name, Typed<? extends AValue>... params) {
            super(call, Comment.no(), name, params);
            valueHelper = new ValueHelper(type, this);
        }

        @Override
        public <Ret extends Returnable> Typed<Ret> call(String method, Typed<? extends AValue>... params) {
            return valueHelper.call(method, params);
        }
    }
}
