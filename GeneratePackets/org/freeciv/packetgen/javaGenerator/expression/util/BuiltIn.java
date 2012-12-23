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

package org.freeciv.packetgen.javaGenerator.expression.util;

import org.freeciv.packetgen.javaGenerator.*;
import org.freeciv.packetgen.javaGenerator.IR.CodeAtom;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.Statement;
import org.freeciv.packetgen.javaGenerator.expression.creators.*;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.*;

public class BuiltIn {
    public static final Typed<ABool> TRUE = BuiltIn.<ABool>toCode("true");
    public static final Typed<ABool> FALSE = BuiltIn.<ABool>toCode("false");

    public static final TargetArray byteArray = new TargetArray(byte[].class, true);
    public static final TargetArray boolArray = new TargetArray(boolean[].class, true);

    public static Typed<NoValue> THROW(final Class error, Typed<? extends AValue>... parms) {
        return THROW((new TargetClass(error)).newInstance(parms));
    }

    public static Typed<NoValue> THROW(final Typed<? extends AValue> error) {
        return new Formatted.Type<NoValue>() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                to.add(THR);
                error.writeAtoms(to);
            }
        };
    }

    public static Typed<NoValue> tryCatch(final Block toTry, final Var<AValue> toCatch, final Block ifCaught) {
        return new Formatted.Type<NoValue>() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                to.add(TRY);
                toTry.writeAtoms(to);
                to.add(CATCH);
                to.add(LPR);
                toCatch.writeAtoms(to);
                to.add(RPR);
                ifCaught.writeAtoms(to);
            }
        };
    }

    public static Typed<NoValue> RETURN(final Typed<? extends AValue> arg1) {
            return new Formatted.Type<NoValue>() {
                @Override
                public void writeAtoms(CodeAtoms to) {
                    to.add(RET);
                    arg1.writeAtoms(to);
                }
            };
    }

    private static final From2or3<Typed<NoValue>, Typed<ABool>, Block, Block> ifImpl = new If();
    public static Typed<NoValue> IF (Typed<ABool> cond, Block then) {
        return ifImpl.x(cond, then);
    }
    public static Typed<NoValue> IF (Typed<ABool> cond, Block then, Block ifNot) {
        return ifImpl.x(cond, then, ifNot);
    }

    public static <Kind extends AValue> Typed<Kind> R_IF(final Typed<ABool> cond, final Typed<Kind> then,
                                                          final Typed<Kind> ifNot) {
        return new Formatted.Type<Kind>() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                to.add(LPR);
                cond.writeAtoms(to);
                to.add(RIF_THEN);
                then.writeAtoms(to);
                to.add(ELSE2);
                ifNot.writeAtoms(to);
                to.add(RPR);
            }
        };
    }

    public static Typed<NoValue> ASSERT(final Typed<ABool> cond, final Typed<? extends AValue> ifNot) {
        return new Formatted.Type<NoValue>() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                to.add(ASSRT);
                cond.writeAtoms(to);
                to.add(ELSE2);
                ifNot.writeAtoms(to);
            }
        };
    }

    public static Typed<NoValue> WHILE(final Typed<ABool> cond, final Block rep) {
        return new Formatted.Type<NoValue>() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                to.add(WHILE);
                to.add(LPR);
                cond.writeAtoms(to);
                to.add(RPR);
                rep.writeAtoms(to);
            }
        };
    }

    public static Typed<NoValue> FOR(final Var count, final Typed<ABool> cond, final Typed<? extends Returnable> changer,
                              final Block body) {
        return new Formatted.Type<NoValue>() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                to.add(FOR);
                to.add(LPR);
                count.writeAtoms(to);
                to.add(FORSEP);
                cond.writeAtoms(to);
                to.add(FORSEP);
                changer.writeAtoms(to);
                to.add(RPR);
                body.writeAtoms(to);
            }
        };
    }

    public static Typed<NoValue> FOR(final Var element, final Typed<? extends AValue> elements,
                              final Block body) {
        return new Formatted.Type<NoValue>() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                to.add(FOR);
                to.add(LPR);
                element.writeAtoms(to);
                to.add(FOR_EACH_SEP);
                elements.writeAtoms(to);
                to.add(RPR);
                body.writeAtoms(to);
            }
        };
    }

    public static Typed<AString> literal(String javaCode) {
        return BuiltIn.<AString>toCode("\"" + javaCode + "\"");
    }

    public static Typed<AnInt> literal(int inte) {
        return BuiltIn.<AnInt>toCode(inte + "");
    }

    public static final ExprFrom1<Typed<AString>, Var> TO_STRING_OBJECT =
            new ExprFrom1<Typed<AString>, Var>() {
                @Override
                public Typed<AString> x(Var arg1) {
                    return arg1.<AString>call("toString");
                }
            };


    public static final ExprFrom1<Typed<AString>, Var> TO_STRING_ARRAY = new ExprFrom1<Typed<AString>, Var>() {
        @Override
        public Typed<AString> x(Var arg1) {
            return new MethodCall<AString>("org.freeciv.Util.joinStringArray",
                    arg1.ref(), literal(" "));
        }
    };

    public static <Kind extends AValue> Typed<Kind> GROUP(final Typed<Kind> expr) {
        return new Formatted.Type<Kind>() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                to.add(LPR);
                expr.writeAtoms(to);
                to.add(RPR);
            }
        };
    }

    public static <Kind extends AValue> Typed<Kind> sum(final Typed<? extends AValue>... values) {
        return new Formatted.Type<Kind>() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                to.joinSep(ADD, values);
            }
        };
    }

    public static <Kind extends AValue> Typed<Kind> multiply(final Typed<? extends AValue>... values) {
        return new Formatted.Type<Kind>() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                to.joinSep(MUL, values);
            }
        };
    }

    public static <Kind extends ABool> Typed<Kind> and(final Typed<? extends ABool>... values) {
        return new Formatted.Type<Kind>() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                to.joinSep(AND, values);
            }
        };
    }

    public static <Kind extends ABool> Typed<Kind> or(final Typed<? extends ABool>... values) {
        return new Formatted.Type<Kind>() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                to.joinSep(OR, values);
            }
        };
    }

    public static Typed<? extends AValue> subtract(final Typed<? extends AValue> a, final Typed<? extends AValue> b) {
        return binOp(HasAtoms.SUB, a, b);
    }

    public static Typed<? extends AValue> divide(final Typed<? extends AValue> a, final Typed<? extends AValue> b) {
        return binOp(HasAtoms.DIV, a, b);
    }

    public static MethodCall<AValue> inc(final Var var, final Typed<? extends AValue> toAdd) {
        return new MethodCall<AValue>("+=", var.ref()) {
            @Override
            public void writeAtoms(CodeAtoms to) {
                var.ref().writeAtoms(to);
                to.add(INC_USING);
                toAdd.writeAtoms(to);
            }
        };
    }

    public static MethodCall<AValue> inc(final Var var) {
        return new MethodCall<AValue>("++", var.ref()) {
            @Override
            public void writeAtoms(CodeAtoms to) {
                var.ref().writeAtoms(to);
                to.add(INC);
            }
        };
    }

    public static Typed<ABool> isBiggerThan(final Typed<? extends AValue> small,
                                             final Typed<? extends AValue> largerThan) {
        return BuiltIn.<ABool>binOp(HasAtoms.IS_BIGGER, small, largerThan);
    }

    public static Typed<ABool> isSmallerThan(final Typed<? extends AValue> small,
                                             final Typed<? extends AValue> largerThan) {
        return BuiltIn.<ABool>binOp(HasAtoms.IS_SMALLER, small, largerThan);
    }

    public static Typed<ABool> isSmallerThanOrEq(final Typed<? extends AValue> small,
                                             final Typed<? extends AValue> largerThan) {
        return BuiltIn.<ABool>binOp(HasAtoms.IS_SMALLER_OR_EQUAL, small, largerThan);
    }

    public static Typed<ABool> isSame(final Typed<? extends AValue> small,
                                             final Typed<? extends AValue> largerThan) {
        return BuiltIn.<ABool>binOp(HasAtoms.IS_SAME, small, largerThan);
    }

    public static Typed<ABool> isNotSame(final Typed<? extends AValue> small,
                                             final Typed<? extends AValue> largerThan) {
        return BuiltIn.<ABool>binOp(HasAtoms.IS_NOT_SAME, small, largerThan);
    }

    public static Typed<ABool> isInstanceOf(final Typed<? extends AValue> object,
                                            final TargetClass type) {
        return new Formatted.Type<ABool>() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                object.writeAtoms(to);
                to.add(HasAtoms.IS_INSTANCE_OF);
                type.writeAtoms(to);
            }
        };
    }

    public static MethodCall<AValue> arraySetElement(final Var on, final Typed<? extends AValue> number, final Typed<AValue> val) {
        return new MethodCall<AValue>("[]=", on.ref(), val) {
            @Override
            public void writeAtoms(CodeAtoms to) {
                on.ref().writeAtoms(to);
                to.add(ARRAY_ACCESS_START);
                number.writeAtoms(to);
                to.add(ARRAY_ACCESS_END);
                to.add(ASSIGN);
                val.writeAtoms(to);
            }
        };
    }

    public static <Ret extends AValue> Typed<Ret> cast(final Class newType, final Typed<? extends AValue> val) {
        return cast(new TargetClass(newType), val);
    }

    public static <Ret extends AValue> Typed<Ret> cast(final TargetClass newType, final Typed<? extends AValue> val) {
        return new Typed<Ret>() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                to.add(LPR);
                to.add(LPR);
                newType.writeAtoms(to);
                to.add(RPR);
                val.writeAtoms(to);
                to.add(RPR);
            }
        };
    }

    public static <Kind extends Returnable> Typed<Kind> toCode(String javaCode) {
        return new WrapCodeString<Kind>(javaCode);
    }

    /*
     * Internal helpers
     */
    private static <Ret extends AValue> Typed<Ret> binOp(final CodeAtom op,
                                                         final Typed<? extends AValue> a,
                                                         final Typed<? extends AValue> b) {
        return new Formatted.Type<Ret>() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                a.writeAtoms(to);
                to.add(op);
                b.writeAtoms(to);
            }
        };
    }
}
