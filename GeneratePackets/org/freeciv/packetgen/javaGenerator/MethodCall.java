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
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.expression.util.Formatted;
import org.freeciv.packetgen.javaGenerator.expression.util.ValueHelper;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AString;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.Returnable;
import org.freeciv.packetgen.javaGenerator.formating.CodeStyle;

import java.util.Arrays;

public class MethodCall<Returns extends Returnable> extends Formatted implements HasAtoms, Typed<Returns> {
    private final HasAtoms writer;
    private final Comment comment;
    protected final String method;
    protected final Typed<? extends AValue>[] parameters;

    public MethodCall(String name, Typed<? extends AValue>... params) {
        this(Comment.no(), name, params);
    }

    public MethodCall(Comment comment, String name, Typed<? extends AValue>... params) {
        this(TargetMethod.Called.STATIC, comment, name, params);
    }

    public MethodCall(TargetMethod.Called kind, String name, Typed<? extends AValue>... params) {
        this(kind, Comment.no(), name, params);
    }

    private MethodCall(TargetMethod.Called kind, Comment comment, String name, Typed<? extends AValue>... params) {
        if (null == name)
            throw new IllegalArgumentException("No method name given to method call");

        this.comment = comment;
        this.method = name;
        this.parameters = params;

        switch (kind) {
            case STATIC:
                writer = new StaticWriter();
                break;
            case DYNAMIC:
                writer =  new DynamicWriter();
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

    private class StaticWriter implements HasAtoms {
        public void writeAtoms(CodeAtoms to) {
            to.hintStart(MethodCall.class.getCanonicalName());
            comment.writeAtoms(to);
            to.add(new CodeAtom(method));
            to.add(LPR);
            if (0 < parameters.length) {
                to.hintStart(CodeStyle.ARGUMENTS);
                to.joinSep(SEP, parameters);
                to.hintEnd(CodeStyle.ARGUMENTS);
            }
            to.add(RPR);
            to.hintEnd(MethodCall.class.getCanonicalName());
        }
    }

    private class DynamicWriter implements HasAtoms {
        public void writeAtoms(CodeAtoms to) {
            to.hintStart(MethodCall.class.getCanonicalName());
            comment.writeAtoms(to);
            parameters[0].writeAtoms(to);
            to.add(HAS);
            to.add(new CodeAtom(method));
            to.add(LPR);
            if (1 < parameters.length) {
                to.hintStart(CodeStyle.ARGUMENTS);
                to.joinSep(SEP, Arrays.copyOfRange(parameters, 1, parameters.length));
                to.hintEnd(CodeStyle.ARGUMENTS);
            }
            to.add(RPR);
            to.hintEnd(MethodCall.class.getCanonicalName());
        }
    }

    public static class HasResult<Returns extends AValue> extends MethodCall<Returns> implements Value<Returns> {
        private final ValueHelper valueHelper;

        public HasResult(TargetMethod.Called call, TargetClass type, String name, Typed<? extends AValue>... params) {
            super(call, name, params);
            valueHelper = new ValueHelper(type, this);
        }

        @Override
        public <Ret extends Returnable> Typed<Ret> call(String method, Typed<? extends AValue>... params) {
            return valueHelper.call(method, params);
        }
    }
}
