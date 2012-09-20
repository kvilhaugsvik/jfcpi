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

import java.util.Arrays;
import java.util.Collections;

import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.*;
import static org.freeciv.packetgen.Hardcoded.fMaxSize;
import static org.freeciv.packetgen.Hardcoded.pMaxSize;
import static org.freeciv.packetgen.Hardcoded.arrayEaterScopeCheck;

// Perhaps also have the generalized version output an Array of the referenced objects in stead of their number.
public class TerminatedArray extends FieldTypeBasic {
    private static final TargetClass byteArray = new TargetArray(byte[].class, true);

    private static final ExprFrom1<Typed<AnInt>, Var> arrayLen = new ExprFrom1<Typed<AnInt>, Var>() {
        @Override
        public Typed<AnInt> x(Var value) {
            return value.read("length");
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

    public TerminatedArray(String dataIOType, String publicType) {
        this(dataIOType, publicType, byteArray, null,
                arrayLen,
                new ExprFrom1<Typed<ABool>, Typed<AnInt>>() {
                    @Override
                    public Typed<ABool> x(Typed<AnInt> size) {
                        return FALSE;
                    }
                },
                new ExprFrom2<Typed<ABool>, Typed<AnInt>, Typed<AnInt>>() {
                    @Override
                    public Typed<ABool> x(Typed<AnInt> max, Typed<AnInt> size) {
                        return isNotSame(max, size);
                    }
                },
                fullIsByteArray,
                byteArrayIsFull,
                TO_STRING_ARRAY);
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
        this(dataIOType, publicType, javaType, terminator, sizeGetter,
                new ExprFrom1<Typed<ABool>, Typed<AnInt>>() {
                    @Override
                    public Typed<ABool> x(Typed<AnInt> size) {
                        return isSmallerThan(size, fMaxSize.ref());
                    }
                },
                new ExprFrom2<Typed<ABool>, Typed<AnInt>, Typed<AnInt>>() {
                    @Override
                    public Typed<ABool> x(Typed<AnInt> max, Typed<AnInt> size) {
                        return isSmallerThan(max, size);
                    }
                }, fullToByteArray, byteArrayToFull, toString);
    }

    public TerminatedArray(String dataIOType, String publicType, final TargetClass javaType,
                           final Requirement terminator,
                           final ExprFrom1<Typed<AnInt>, Var> sizeGetter,
                           final ExprFrom1<Typed<ABool>, Typed<AnInt>> addAfter,
                           final ExprFrom2<Typed<ABool>, Typed<AnInt>, Typed<AnInt>> testSizeWrong,
                           final ExprFrom1<Typed<AValue>, Var> fullToByteArray,
                           final ExprFrom1<Typed<AValue>, Typed<AValue>> byteArrayToFull,
                           ExprFrom1<Typed<AString>, Var> toString) {
        super(dataIOType, publicType, javaType,
                new ExprFrom1<Block, Var>() {
                    @Override
                    public Block x(Var to) {
                        final Var pValue = Var.param(javaType, "value");
                        return new Block(
                                fMaxSize.assign(pMaxSize.ref()),
                                arrayEaterScopeCheck(testSizeWrong.x(pMaxSize.ref(), sizeGetter.x(pValue))),
                                to.assign(pValue.ref()));
                    }
                },
                new ExprFrom2<Block, Var, Var>() {
                    @Override
                    public Block x(Var to, Var from) {
                        Var buf = Var.local(byteArray, "buffer",
                                byteArray.newInstance(pMaxSize.ref()));
                        Var current = Var.local("byte", "current", from.<AValue>call("readByte"));
                        Var pos = Var.local("int", "pos", BuiltIn.<AnInt>toCode("0"));

                        Typed<ABool> noTerminatorFound = (null == terminator ?
                                TRUE :
                                isNotSame(cast(byte.class,
                                        BuiltIn.<AnInt>toCode(Constant.referToInJavaCode(terminator))), current.ref()));

                        return new Block(fMaxSize.assign(pMaxSize.ref()), buf, current, pos,
                                WHILE(noTerminatorFound,
                                        new Block(arraySetElement(buf, pos.ref(), current.ref()),
                                                inc(pos),
                                                IF(isSmallerThan(pos.ref(), pMaxSize.<AnInt>ref()),
                                                        new Block(current.assign(from.<AValue>call("readByte"))),
                                                        new Block(BuiltIn.<NoValue>toCode("break"))))),
                                to.assign(byteArrayToFull.x(new MethodCall<AValue>("java.util.Arrays.copyOf",
                                        buf.ref(), pos.ref()))));
                    }
                },
                new ExprFrom2<Block, Var, Var>() {
                    @Override
                    public Block x(Var val, Var to) {
                        Block out = new Block(to.call("write", fullToByteArray.x(val)));
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
              true,
              (null == terminator ?
                      Collections.<Requirement>emptySet() :
                      Arrays.asList(terminator)));
    }
}
