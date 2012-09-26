package org.freeciv.packetgen.enteties.supporting;

import org.freeciv.packetgen.Hardcoded;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.Constant;
import org.freeciv.packetgen.enteties.FieldTypeBasic;
import org.freeciv.packetgen.javaGenerator.*;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.creators.ExprFrom1;
import org.freeciv.packetgen.javaGenerator.expression.creators.ExprFrom2;
import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.*;

import java.util.*;

import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.*;
import static org.freeciv.packetgen.Hardcoded.fMaxSize;
import static org.freeciv.packetgen.Hardcoded.pMaxSize;
import static org.freeciv.packetgen.Hardcoded.arrayEaterScopeCheck;

// Perhaps also have the generalized version output an Array of the referenced objects in stead of their number.
public class TerminatedArray extends FieldTypeBasic {
    public static final TargetArray byteArray = new TargetArray(byte[].class, true);

    public static final ExprFrom1<Typed<AnInt>, Var> arrayLen = new ExprFrom1<Typed<AnInt>, Var>() {
        @Override
        public Typed<AnInt> x(Var value) {
            return value.read("length");
        }
    };
    public static final ExprFrom2<Block, Var, Var> elemIsByteArray = new ExprFrom2<Block, Var, Var>() {
        @Override
        public Block x(Var a, Var b) {
            return new Block(a.call("writeByte", b.ref()));
        }
    };
    private static final ExprFrom1<Typed<AValue>, Var> fullIsByteArray = new ExprFrom1<Typed<AValue>, Var>() {
        @Override
        public Typed<AValue> x(Var everything) {
            return everything.ref();
        }
    };
    private static final ExprFrom1<Typed<AValue>, Typed<AValue>> byteArrayIsFull =
            new ExprFrom1<Typed<AValue>, Typed<AValue>>() {
                @Override
                public Typed<AValue> x(Typed<AValue> bytes) {
                    return bytes;
                }
            };
    public static final ExprFrom1<Typed<ABool>, Typed<AnInt>> neverAnythingAfter =
            new ExprFrom1<Typed<ABool>, Typed<AnInt>>() {
                @Override
                public Typed<ABool> x(Typed<AnInt> size) {
                    return FALSE;
                }
            };
    public static final ExprFrom2<Typed<ABool>, Typed<AnInt>, Typed<AnInt>> lenShouldBeEqual =
            new ExprFrom2<Typed<ABool>, Typed<AnInt>, Typed<AnInt>>() {
                @Override
                public Typed<ABool> x(Typed<AnInt> max, Typed<AnInt> size) {
                    return isNotSame(max, size);
                }
            };
    public static final ExprFrom1<Typed<? extends AValue>, Var> readByte = new ExprFrom1<Typed<? extends AValue>, Var>() {
        @Override
        public Typed<? extends AValue> x(Var from) {
            return from.call("readByte");
        }
    };
    public static final ExprFrom1<Typed<ABool>, Typed<AnInt>> addAfterIfSmallerThanMaxSize =
            new ExprFrom1<Typed<ABool>, Typed<AnInt>>() {
                @Override
                public Typed<ABool> x(Typed<AnInt> size) {
                    return isSmallerThan(size, fMaxSize.ref());
                }
            };
    public static final ExprFrom2<Typed<ABool>, Typed<AnInt>, Typed<AnInt>> wrongSizeIfToBig =
            new ExprFrom2<Typed<ABool>, Typed<AnInt>, Typed<AnInt>>() {
                @Override
                public Typed<ABool> x(Typed<AnInt> max, Typed<AnInt> size) {
                    return isSmallerThan(max, size);
                }
            };

    public TerminatedArray(String dataIOType, String publicType) {
        this(dataIOType, publicType, byteArray, null,
                true,
                byteArray,
                arrayLen,
                null,
                null,
                neverAnythingAfter,
                lenShouldBeEqual,
                fullIsByteArray,
                byteArrayIsFull,
                elemIsByteArray,
                readByte,
                TO_STRING_ARRAY,
                Collections.<Requirement>emptySet());
    };

    public TerminatedArray(String dataIOType, String publicType, final Requirement terminator) {
        this(dataIOType, publicType, byteArray, terminator, true, byteArray,
                arrayLen, null, null, addAfterIfSmallerThanMaxSize, wrongSizeIfToBig,
                fullIsByteArray, byteArrayIsFull, elemIsByteArray, readByte,
                TO_STRING_ARRAY, Arrays.asList(terminator));
    }

    public TerminatedArray(final String dataIOType, final String publicType, final TargetClass javaType,
                           final Requirement terminator,
                           final boolean absoluteMaxSizeAsParameter,
                           final TargetArray buffertype,
                           final ExprFrom1<Typed<AnInt>, Var> numberOfElements,
                           final ExprFrom1<Typed<AnInt>, Var> readBeforeElements,
                           final ExprFrom2<Typed<ABool>, Var, Var> writeBeforeElements,
                           final ExprFrom1<Typed<ABool>, Typed<AnInt>> testIfTerminatorShouldBeAdded,
                           final ExprFrom2<Typed<ABool>, Typed<AnInt>, Typed<AnInt>> testIfSizeIsWrong,
                           final ExprFrom1<Typed<AValue>, Var> convertAllElementsToByteArray,
                           final ExprFrom1<Typed<AValue>, Typed<AValue>> convertByteArrayToAllElements,
                           final ExprFrom2<Block, Var, Var> writeElementTo,
                           final ExprFrom1<Typed<? extends AValue>, Var> readElementFrom,
                           final ExprFrom1<Typed<AString>, Var> toString,
                           final Collection<Requirement> uses) {
        super(dataIOType, publicType, javaType,
                new ExprFrom1<Block, Var>() {
                    @Override
                    public Block x(Var to) {
                        final Var pValue = Var.param(javaType, "value");
                        Block fromJavaTyped = new Block();
                        if (absoluteMaxSizeAsParameter) {
                            fromJavaTyped.addStatement(fMaxSize.assign(pMaxSize.ref()));
                            fromJavaTyped.addStatement(arrayEaterScopeCheck(testIfSizeIsWrong.x(pMaxSize.ref(),
                                    numberOfElements.x(pValue))));
                        } else {
                            fromJavaTyped.addStatement(fMaxSize.assign(numberOfElements.x(pValue)));
                        }
                        if (absoluteMaxSizeAsParameter)
                            fromJavaTyped.addStatement(Hardcoded.arrayEaterScopeCheck(
                                    isSmallerThan(Var.param(int.class, "maxArraySizeThisTime").ref(), fMaxSize.ref())));
                        fromJavaTyped.addStatement(to.assign(pValue.ref()));
                        return fromJavaTyped;
                    }
                },
                new ExprFrom2<Block, Var, Var>() {
                    @Override
                    public Block x(Var to, Var from) {
                        Var buf = Var.local(buffertype, "buffer",
                                buffertype.newInstance(fMaxSize.ref()));
                        Var current = Var.local(buffertype.getOf(), "current", readElementFrom.x(from));
                        Var pos = Var.local("int", "pos", literal(0));

                        Typed<ABool> noTerminatorFound = (null == terminator ?
                                TRUE :
                                isNotSame(cast(byte.class,
                                        BuiltIn.<AnInt>toCode(Constant.referToInJavaCode(terminator))), current.ref()));

                        Block out = new Block();

                        if (absoluteMaxSizeAsParameter)
                            out.addStatement(fMaxSize.assign(pMaxSize.ref()));

                        if (null != readBeforeElements)
                            out.addStatement(readBeforeElements.x(from));

                        if (absoluteMaxSizeAsParameter)
                            out.addStatement(Hardcoded.arrayEaterScopeCheck(
                                    isSmallerThan(Var.param(int.class, "maxArraySizeThisTime").ref(), fMaxSize.ref())));

                        out.addStatement(buf);
                        out.addStatement(current);
                        out.addStatement(pos);
                        out.addStatement(
                                WHILE(noTerminatorFound,
                                        new Block(arraySetElement(buf, pos.ref(), current.ref()),
                                                inc(pos),
                                                IF(isSmallerThan(pos.ref(), fMaxSize.ref()),
                                                        new Block(current.assign(readElementFrom.x(from))),
                                                        new Block(BuiltIn.<NoValue>toCode("break"))))));
                        out.addStatement(to.assign(convertByteArrayToAllElements.x(new MethodCall<AValue>("java.util.Arrays.copyOf",
                                buf.ref(), pos.ref()))));

                        return out;
                    }
                },
                new ExprFrom2<Block, Var, Var>() {
                    @Override
                    public Block x(Var val, Var to) {
                        Block out = new Block();
                        if (null != writeBeforeElements)
                            out.addStatement(writeBeforeElements.x(val, to));
                        if (null == convertAllElementsToByteArray) {
                            Var element = Var.param(buffertype.getOf(), "element");
                            out.addStatement(FOR(element, val.ref(), writeElementTo.x(to, element)));
                        } else {
                            out.addStatement(to.call("write", convertAllElementsToByteArray.x(val)));
                        }
                        Typed<ABool> addAfterResult = testIfTerminatorShouldBeAdded.x(numberOfElements.x(val));
                        if (!FALSE.equals(addAfterResult))
                            out.addStatement(IF(addAfterResult,
                                    new Block(to.call("writeByte", BuiltIn.<AValue>toCode(Constant.referToInJavaCode(terminator))))));
                        return out;
                    }
                },
              new ExprFrom1<Typed<AnInt>, Var>() {
                    @Override
                    public Typed<AnInt> x(Var value) {
                        Typed<AnInt> length = numberOfElements.x(value);
                        Typed<ABool> addAfterResult = testIfTerminatorShouldBeAdded.x(numberOfElements.x(value));
                        if (!FALSE.equals(addAfterResult))
                            length = BuiltIn.<AnInt>sum(
                                    length,
                                    R_IF(addAfterResult,
                                            literal(1),
                                            literal(0)));
                        return length;
                    }
              },
              toString,
              absoluteMaxSizeAsParameter,
              uses);
    }

    @Override
    public FieldTypeAlias createFieldType(String name) {
        return new FieldTypeAliasToTerminatedArray(name);
    }

    private class FieldTypeAliasToTerminatedArray extends FieldTypeAlias {
        private FieldTypeAliasToTerminatedArray(String name) {
            super(name);
            addObjectConstant("int", "maxArraySize");
        }
    }
}
