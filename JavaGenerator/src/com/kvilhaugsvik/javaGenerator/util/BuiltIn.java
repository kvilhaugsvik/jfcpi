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

package com.kvilhaugsvik.javaGenerator.util;

import com.kvilhaugsvik.javaGenerator.*;
import com.kvilhaugsvik.javaGenerator.expression.MethodCall;
import com.kvilhaugsvik.javaGenerator.representation.CodeAtoms;
import com.kvilhaugsvik.javaGenerator.representation.HasAtoms;
import com.kvilhaugsvik.javaGenerator.representation.IR.CodeAtom;
import com.kvilhaugsvik.javaGenerator.typeBridge.*;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.*;
import org.freeciv.utility.Validation;

public class BuiltIn {
    public static final Typed<ABool> TRUE = BuiltIn.<ABool>toCode("true");
    public static final Typed<ABool> FALSE = BuiltIn.<ABool>toCode("false");
    public static final Typed<AValue> NULL = BuiltIn.<AValue>toCode("null");

    public static final TargetArray byteArray = TargetArray.from(byte[].class);
    public static final TargetArray boolArray = TargetArray.from(boolean[].class);

    public static final Typed<NoValue> superConstr(Typed<? extends AValue>... args) {
        return new MethodCall<NoValue>("super", args);
    }

    public static final Typed<NoValue> thisConstr(Typed<? extends AValue>... args) {
        return new MethodCall<NoValue>("this", args);
    }

    public static Typed<NoValue> THROW(final Class error, Typed<? extends AValue>... parms) {
        return THROW((TargetClass.from(error)).newInstance(parms));
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

    public static Typed<NoValue> RETURN() {
            return new Formatted.Type<NoValue>() {
                @Override
                public void writeAtoms(CodeAtoms to) {
                    to.add(RET);
                }
            };
    }

    public static Typed<NoValue> BREAK() {
            return new Formatted.Type<NoValue>() {
                @Override
                public void writeAtoms(CodeAtoms to) {
                    to.add(BRE);
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

    /**
     * Generate Java code using the ternary operator.
     * A ternary statement is (condition ? ifTrue : ifNotTrue)
     * In Java the ternary operator has a return value while the "if" statement don't.
     * @param cond the condition that decides what to do.
     * @param then what to do if the condition is true.
     * @param ifNot what to do if the condition is false.
     * @param <Kind> the type of the ternary statement's return value.
     * @return Java code for a ternary statement with the specified return type, condition and actions.
     */
    public static <Kind extends AValue> Typed<Kind> R_IF(final Typed<ABool> cond, final Typed<Kind> then,
                                                          final Typed<Kind> ifNot) {
        /* Check that the input is provided. */
        Validation.validateNotNull(cond, "cond");
        Validation.validateNotNull(then, "then");
        Validation.validateNotNull(ifNot, "ifNot");

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

    /**
     * Generate Java code representing a String literal containing the provided value.
     * @param javaCode the content of the String literal.
     * @return the String literal Java code.
     */
    public static Typed<AString> literal(String javaCode) {
        return BuiltIn.<AString>toCode("\"" + javaCode + "\"");
    }

    /**
     * Generate Java code representing an int literal containing the provided value.
     * @param inte the value of the int literal.
     * @return the int literal Java code.
     */
    public static Typed<AnInt> literal(int inte) {
        return BuiltIn.<AnInt>toCode(inte + "");
    }

    /**
     * Generate Java code representing a long literal containing the provided value.
     * @param inte the value of the long literal.
     * @return the long literal Java code.
     */
    public static Typed<AnInt> literal(long inte) {
        return BuiltIn.<AnInt>toCode(inte + "L");
    }

    /**
     * Generate Java code representing a float literal containing the provided value.
     * @param num the value of the float literal.
     * @return the float literal Java code.
     */
    public static Typed<AnInt> literal(float num) {
        return BuiltIn.<AnInt>toCode(num + "f");
    }

    /**
     * Generate Java code representing a double literal containing the provided value.
     * @param num the value of the double literal.
     * @return the double literal Java code.
     */
    public static Typed<AnInt> literal(double num) {
        return BuiltIn.<AnInt>toCode(num + "d");
    }

    /**
     * Generate Java code representing a boolean literal containing the provided value.
     * @param b the value of the boolean literal.
     * @return the boolean literal Java code.
     */
    public static Typed<ABool> literal(boolean b) {
        return b ? TRUE : FALSE;
    }

    public static final From1<Typed<AString>, Var> TO_STRING_OBJECT =
            new From1<Typed<AString>, Var>() {
                @Override
                public Typed<AString> x(Var arg1) {
                    return arg1.ref().<AString>call("toString");
                }
            };


    public static final From1<Typed<AString>, Var> TO_STRING_ARRAY = new From1<Typed<AString>, Var>() {
        @Override
        public Typed<AString> x(Var arg1) {
            return TargetClass.from(org.freeciv.utility.Util.class).<AString>callV("joinStringArray",
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

    /**
     * Create code that invert the specified boolean value using Java's
     * logical complement unary operator ("not" / "!").
     * @param val the boolean value to invert.
     * @return code inverting the specified value.
     */
    public static Formatted.Type<ABool> isNot(final Typed<? extends ABool> val) {
        return new Formatted.Type<ABool>() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                to.add(NOT);
                val.writeAtoms(to);
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

    public static <Ret extends AValue> Value<Ret> cast(final Class newType, final Typed<? extends AValue> val) {
        return cast(TargetClass.from(newType), val);
    }

    public static <Ret extends AValue> Value<Ret> cast(final TargetClass newType, final Typed<? extends AValue> val) {
        return new MethodCall.HasResult<Ret>(TargetMethod.Called.MANUALLY, newType, new CodeAtom("(cast)"), val) {
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
                to.add(LPR);
                a.writeAtoms(to);
                to.add(op);
                b.writeAtoms(to);
                to.add(RPR);
            }
        };
    }
}
