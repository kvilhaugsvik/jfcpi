package org.freeciv.packetgen.enteties.supporting;

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
    private static final TargetArray byteArray = new TargetArray(byte[].class, true);

    public static final ExprFrom1<Typed<AnInt>, Var> arrayLen = new ExprFrom1<Typed<AnInt>, Var>() {
        @Override
        public Typed<AnInt> x(Var value) {
            return value.read("length");
        }
    };
    private static final ExprFrom2<Block, Var, Var> elemIsByteArray = new ExprFrom2<Block, Var, Var>() {
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
    private static final ExprFrom1<Typed<? extends AValue>, Var> readByte = new ExprFrom1<Typed<? extends AValue>, Var>() {
        @Override
        public Typed<? extends AValue> x(Var from) {
            return from.call("readByte");
        }
    };
    private static final ExprFrom1<Typed<AnInt>, Var> currentSizeLimitIsEatenDimension =
            new ExprFrom1<Typed<AnInt>, Var>() {
                @Override
                public Typed<AnInt> x(Var arg1) {
                    return fMaxSize.assign(pMaxSize.ref());
                }
            };
    private static final ExprFrom1<Typed<ABool>, Typed<AnInt>> addAfterIfSmallerThanMaxSize =
            new ExprFrom1<Typed<ABool>, Typed<AnInt>>() {
                @Override
                public Typed<ABool> x(Typed<AnInt> size) {
                    return isSmallerThan(size, fMaxSize.ref());
                }
            };
    private static final ExprFrom2<Typed<ABool>, Typed<AnInt>, Typed<AnInt>> wrongSizeIfToBig =
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
                currentSizeLimitIsEatenDimension,
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
        this(dataIOType, publicType, byteArray, terminator,
                arrayLen, fullIsByteArray, byteArrayIsFull, TO_STRING_ARRAY);
    }

    public TerminatedArray(String dataIOType, String publicType, final TargetClass javaType,
                           final Requirement terminator,
                           final ExprFrom1<Typed<AnInt>, Var> sizeGetter,
                           final ExprFrom1<Typed<AValue>, Var> fullToByteArray,
                           final ExprFrom1<Typed<AValue>, Typed<AValue>> byteArrayToFull,
                           ExprFrom1<Typed<AString>, Var> toString) {
        this(dataIOType, publicType, javaType, terminator, true, byteArray, sizeGetter,
                currentSizeLimitIsEatenDimension, null,
                addAfterIfSmallerThanMaxSize,
                wrongSizeIfToBig,
                fullToByteArray, byteArrayToFull,
                elemIsByteArray, readByte, toString,
                Arrays.asList(terminator));
    }

    public TerminatedArray(final String dataIOType, final String publicType, final TargetClass javaType,
                           final Requirement terminator,
                           final boolean absoluteMaxSizeAsParameter,
                           final TargetArray buffertype,
                           final ExprFrom1<Typed<AnInt>, Var> sizeGetter,
                           final ExprFrom1<Typed<AnInt>, Var> readMaxSize,
                           final ExprFrom2<Typed<ABool>, Var, Var> addBefore,
                           final ExprFrom1<Typed<ABool>, Typed<AnInt>> addAfter,
                           final ExprFrom2<Typed<ABool>, Typed<AnInt>, Typed<AnInt>> testSizeWrong,
                           final ExprFrom1<Typed<AValue>, Var> fullToByteArray,
                           final ExprFrom1<Typed<AValue>, Typed<AValue>> byteArrayToFull,
                           final ExprFrom2<Block, Var, Var> elemToByteArray,
                           final ExprFrom1<Typed<? extends AValue>, Var> readElement,
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
                            fromJavaTyped.addStatement(arrayEaterScopeCheck(testSizeWrong.x(pMaxSize.ref(),
                                    sizeGetter.x(pValue))));
                        }
                        fromJavaTyped.addStatement(to.assign(pValue.ref()));
                        return fromJavaTyped;
                    }
                },
                new ExprFrom2<Block, Var, Var>() {
                    @Override
                    public Block x(Var to, Var from) {
                        Var buf = Var.local(buffertype, "buffer",
                                buffertype.newInstance(pMaxSize.ref()));
                        Var current = Var.local(buffertype.getOf(), "current", readElement.x(from));
                        Var pos = Var.local("int", "pos", BuiltIn.<AnInt>toCode("0"));

                        Typed<ABool> noTerminatorFound = (null == terminator ?
                                TRUE :
                                isNotSame(cast(byte.class,
                                        BuiltIn.<AnInt>toCode(Constant.referToInJavaCode(terminator))), current.ref()));

                        return new Block(readMaxSize.x(from), buf, current, pos,
                                WHILE(noTerminatorFound,
                                        new Block(arraySetElement(buf, pos.ref(), current.ref()),
                                                inc(pos),
                                                IF(isSmallerThan(pos.ref(), pMaxSize.ref()),
                                                        new Block(current.assign(readElement.x(from))),
                                                        new Block(BuiltIn.<NoValue>toCode("break"))))),
                                to.assign(byteArrayToFull.x(new MethodCall<AValue>("java.util.Arrays.copyOf",
                                        buf.ref(), pos.ref()))));
                    }
                },
                new ExprFrom2<Block, Var, Var>() {
                    @Override
                    public Block x(Var val, Var to) {
                        Block out = new Block();
                        if (null != addBefore)
                            out.addStatement(addBefore.x(val, to));
                        if (null == fullToByteArray) {
                            Var element = Var.param(buffertype.getOf(), "element");
                            out.addStatement(FOR(element, val.ref(), elemToByteArray.x(to, element)));
                        } else
                            out.addStatement(to.call("write", fullToByteArray.x(val)));
                        Typed<ABool> addAfterResult = addAfter.x(sizeGetter.x(val));
                        if (!FALSE.equals(addAfterResult))
                            out.addStatement(IF(addAfterResult,
                                    new Block(to.call("writeByte", BuiltIn.<AValue>toCode(Constant.referToInJavaCode(terminator))))));
                        return out;
                    }
                },
              new ExprFrom1<Typed<AnInt>, Var>() {
                    @Override
                    public Typed<AnInt> x(Var value) {
                        Typed<AnInt> length = sizeGetter.x(value);
                        Typed<ABool> addAfterResult = addAfter.x(sizeGetter.x(value));
                        if (!FALSE.equals(addAfterResult))
                            length = BuiltIn.<AnInt>sum(
                                    length,
                                    R_IF(addAfterResult,
                                            BuiltIn.<AnInt>toCode("1"),
                                            BuiltIn.<AnInt>toCode("0")));
                        return length;
                    }
              },
              toString,
              absoluteMaxSizeAsParameter,
              uses);
    }
}
