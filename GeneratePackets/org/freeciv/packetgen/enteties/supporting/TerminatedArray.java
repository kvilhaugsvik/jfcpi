package org.freeciv.packetgen.enteties.supporting;

import org.freeciv.packet.fieldtype.ElementsLimit;
import org.freeciv.packet.fieldtype.FieldTypeException;
import org.freeciv.packet.fieldtype.IllegalNumberOfElementsException;
import org.freeciv.packetgen.Hardcoded;
import com.kvilhaugsvik.dependency.Requirement;
import org.freeciv.packetgen.enteties.Constant;
import org.freeciv.packetgen.enteties.FieldType;
import com.kvilhaugsvik.javaGenerator.*;
import com.kvilhaugsvik.javaGenerator.Block;
import com.kvilhaugsvik.javaGenerator.expression.MethodCall;
import com.kvilhaugsvik.javaGenerator.typeBridge.From1;
import com.kvilhaugsvik.javaGenerator.typeBridge.From2;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.*;

import java.util.*;

import static com.kvilhaugsvik.javaGenerator.util.BuiltIn.cast;
import static org.freeciv.packetgen.Hardcoded.*;
import static com.kvilhaugsvik.javaGenerator.util.BuiltIn.*;

// Perhaps also have the generalized version output an Array of the referenced objects in stead of their number.
public class TerminatedArray extends FieldType {
    public static final TargetArray byteArray = TargetArray.from(byte[].class);

    public static final From1<Typed<AnInt>, Var> arrayLen = new From1<Typed<AnInt>, Var>() {
        @Override
        public Typed<AnInt> x(Var value) {
            return value.ref().callV("length");
        }
    };
    public static final From2<Block, Var, Var> elemIsByteArray = new From2<Block, Var, Var>() {
        @Override
        public Block x(Var a, Var b) {
            return new Block(a.ref().<Returnable>call("writeByte", b.ref()));
        }
    };
    private static final From1<Typed<AValue>, Var> fullIsByteArray = new From1<Typed<AValue>, Var>() {
        @Override
        public Typed<AValue> x(Var everything) {
            return everything.ref();
        }
    };
    private static final From1<Typed<AValue>, Typed<AValue>> valueIsBufferArray =
            new From1<Typed<AValue>, Typed<AValue>>() {
                @Override
                public Typed<AValue> x(Typed<AValue> bytes) {
                    return bytes;
                }
            };
    public static final From1<Typed<? extends AValue>, Var> readByte = new From1<Typed<? extends AValue>, Var>() {
        @Override
        public Typed<? extends AValue> x(Var from) {
            return from.ref().<Returnable>call("readByte");
        }
    };
    public static final From1<Typed<AnInt>, Typed<AnInt>> sameNumberOfBufferElementsAndValueElements =
            new From1<Typed<AnInt>, Typed<AnInt>>() {
                @Override
                public Typed<AnInt> x(Typed<AnInt> maxElements) {
                    return maxElements;
                }
            };
    public static final String SELF_VALIDATOR_NAME = "verifyInsideLimits";

    public TerminatedArray(final String dataIOType, final String publicType, final TargetClass javaType,
                           final Constant<?> terminator,
                           final MaxArraySize maxArraySizeKind,
                           final TransferArraySize transferArraySizeKind,
                           final TargetArray buffertype,
                           final From1<Typed<AnInt>, Var> numberOfElements,
                           final From1<Typed<AValue>, Var> convertAllElementsToByteArray,
                           final From1<Typed<AValue>, Typed<AValue>> convertBufferArrayToValue,
                           final From2<Block, Var, Var> writeElementTo,
                           final From1<Typed<? extends AValue>, Var> readElementFrom,
                           final From1<Typed<AString>, Var> toString,
                           final Collection<Requirement> uses,
                           final Typed<AnInt> fullArraySizeLocation,
                           final NetworkIO transferSizeSerialize,
                           final From1<Typed<AnInt>, Var> valueGetByteLen,
                           final From1<Typed<AnInt>, Typed<AnInt>> numberOfValueElementToNumberOfBufferElements,
                           final List<Method.Helper> helperMethods,
                           boolean elementTypeCanLimitVerify,
                           boolean alwaysIncludeStopValue
    ) {
        super(dataIOType, publicType, javaType,
                createConstructorBody(javaType, maxArraySizeKind, transferArraySizeKind, numberOfElements, !notTerminatable(terminator), fullArraySizeLocation, new MethodCall<Returnable>(SELF_VALIDATOR_NAME, fMaxSize.ref()), elementTypeCanLimitVerify),
                createDecode(terminator, maxArraySizeKind, transferArraySizeKind, buffertype, convertBufferArrayToValue, readElementFrom, fullArraySizeLocation, transferSizeSerialize, numberOfValueElementToNumberOfBufferElements, elementTypeCanLimitVerify, alwaysIncludeStopValue),
                createEncode(terminator, transferArraySizeKind, numberOfElements, convertAllElementsToByteArray, writeElementTo, transferSizeSerialize, javaType, alwaysIncludeStopValue),
                createEnocedSize(transferArraySizeKind, numberOfElements, !notTerminatable(terminator), transferSizeSerialize, valueGetByteLen, alwaysIncludeStopValue),
                toString,
                eatsArrayLimitInformation(maxArraySizeKind, transferArraySizeKind),
                uses,
                Arrays.asList(Var.field(Collections.<Annotate>emptyList(), Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO, TargetClass.from(ElementsLimit.class), "maxArraySize", null)),
                addValidate(helperMethods, maxArraySizeKind, transferArraySizeKind, numberOfElements,
                        Var.field(Collections.<Annotate>emptyList(), Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO,
                                javaType, "value", null),
                        notTerminatable(terminator), elementTypeCanLimitVerify, buffertype)
        );
    }

    private static List<? extends Method> addValidate(List<? extends Method> helperMethods,
                                                      MaxArraySize maxArraySizeKind,
                                                      TransferArraySize transferArraySizeKind,
                                                      From1<Typed<AnInt>, Var> numberOfElements,
                                                      Var<?> fValue,
                                                      boolean unterminatable,
                                                      boolean elementTypeCanLimitVerify,
                                                      TargetArray buffertype) {
        if (eatsArrayLimitInformation(maxArraySizeKind, transferArraySizeKind)) {
            List<Method> e = new ArrayList<Method>();
            final Method insideLimits = getValidateInsideLimits(maxArraySizeKind, transferArraySizeKind, numberOfElements, fValue, unterminatable, elementTypeCanLimitVerify, buffertype);
            e.addAll(helperMethods);
            e.add(insideLimits);
            return e;
        } else {
            return helperMethods;
        }
    }

    private static boolean eatsArrayLimitInformation(MaxArraySize maxArraySizeKind, TransferArraySize transferArraySizeKind) {
        return MaxArraySize.CONSTRUCTOR_PARAM.equals(maxArraySizeKind)
                || TransferArraySize.CONSTRUCTOR_PARAM.equals(transferArraySizeKind);
    }

    private static boolean notTerminatable(Constant<?> terminator) {
        return null == terminator;
    }

    private static From1<Typed<AnInt>, Var> createEnocedSize(final TransferArraySize transferArraySizeKind, final From1<Typed<AnInt>, Var> numberOfElements, final boolean terminatorShouldBeAdded, final NetworkIO transferSizeSerialize, final From1<Typed<AnInt>, Var> valueGetByteLen, final boolean alwaysIncludeStopValue) {
        return new From1<Typed<AnInt>, Var>() {
            @Override
            public Typed<AnInt> x(Var value) {
                Typed<AnInt> length = valueGetByteLen.x(value);
                if (TransferArraySize.SERIALIZED.equals(transferArraySizeKind))
                    length = sum(transferSizeSerialize.getSize().x(value), length);
                if (terminatorShouldBeAdded) {
                    final Typed<AnInt> terminatorExtra = alwaysIncludeStopValue ?
                            literal(1) :
                            R_IF(addTerminatorUnlessFull(numberOfElements.x(value)),
                                    literal(1),
                                    literal(0));
                    length = BuiltIn.<AnInt>sum(length, terminatorExtra);
                }
                return length;
            }
        };
    }

    public static Typed<ABool> addTerminatorUnlessFull(Typed<AnInt> size) {
        return isSmallerThan(size, fMaxSize.ref().callV("full_array_size"));
    }

    private static From2<Block, Var, Var> createEncode(final Constant<?> terminator, final TransferArraySize transferArraySizeKind, final From1<Typed<AnInt>, Var> numberOfElements, final From1<Typed<AValue>, Var> convertAllElementsToByteArray, final From2<Block, Var, Var> writeElementTo, final NetworkIO transferSizeSerialize, final TargetClass javaType, final boolean alwaysIncludeStopValue) {
        return new From2<Block, Var, Var>() {
            @Override
            public Block x(Var val, Var to) {
                Block out = new Block();
                if (TransferArraySize.SERIALIZED.equals(transferArraySizeKind))
                    out.addStatement(to.ref().<Returnable>call(transferSizeSerialize.getWrite(), numberOfElements.x(val)));
                if (null == convertAllElementsToByteArray) {
                    Var element = Var.param(((TargetArray)javaType).getOf(), "element");
                    out.addStatement(FOR(element, val.ref(), writeElementTo.x(to, element)));
                } else {
                    out.addStatement(to.ref().<Returnable>call("write", convertAllElementsToByteArray.x(val)));
                }
                if (!notTerminatable(terminator))
                    if (alwaysIncludeStopValue)
                        out.addStatement(IF(TRUE, writeElementTo.x(to, terminator)));
                    else
                        out.addStatement(IF(addTerminatorUnlessFull(numberOfElements.x(val)),
                                writeElementTo.x(to, terminator)));
                return out;
            }
        };
    }

    private static From2<Block, Var, Var> createDecode(final Constant<?> terminator, final MaxArraySize maxArraySizeKind, final TransferArraySize transferArraySizeKind, final TargetArray buffertype, final From1<Typed<AValue>, Typed<AValue>> convertBufferArrayToValue, final From1<Typed<? extends AValue>, Var> readElementFrom, final Typed<AnInt> fullArraySizeLocation, final NetworkIO transferSizeSerialize, final From1<Typed<AnInt>, Typed<AnInt>> numberOfValueElementToNumberOfBufferElements, final boolean elementTypeCanLimitVerify, final boolean alwaysIncludeStopValue) {
        return new From2<Block, Var, Var>() {
            @Override
            public Block x(Var to, Var from) {
                Var buf = Var.local(buffertype, "buffer",
                        buffertype.newInstance(numberOfValueElementToNumberOfBufferElements.x(fMaxSize.ref().callV("elements_to_transfer"))));
                Var current = Var.local(buffertype.getOf(), "current", readElementFrom.x(from));
                Var pos = Var.local(int.class, "pos", literal(0));

                Typed<ABool> noTerminatorFound;
                if (!notTerminatable(terminator))
                    if ("byte".equals(current.getTType().getName()))
                        noTerminatorFound = isNotSame(cast(byte.class, terminator.ref()), current.ref());
                    else if (current.getTType().getName().endsWith("_DIFF"))
                        noTerminatorFound = isNotSame(terminator.ref(),
                                current.ref().callV("getValue").callV("getIndex").callV("intValue"));
                    else
                        throw new IllegalArgumentException("Don't know how to compare terminator to current value");
                else
                    noTerminatorFound = TRUE;

                final Block limitReached = new Block();
                if (alwaysIncludeStopValue)
                    limitReached.addStatement(readElementFrom.x(from));
                limitReached.addStatement(BuiltIn.BREAK());

                Block out = new Block();

                writeLimitsReading(out, maxArraySizeKind, fullArraySizeLocation, transferArraySizeKind, null == transferSizeSerialize ? null : transferSizeSerialize.getRead(from), elementTypeCanLimitVerify);

                out.addStatement(buf);
                out.addStatement(IF(isSame(literal(0), buf.ref().callV("length")),
                        new Block(to.assign(convertBufferArrayToValue.x(buf.ref())),
                                BuiltIn.RETURN())));
                out.addStatement(current);
                out.addStatement(pos);
                out.addStatement(
                        WHILE(noTerminatorFound,
                                new Block(arraySetElement(buf, pos.ref(), current.ref()),
                                        inc(pos),
                                        IF(isSmallerThan(pos.ref(), buf.ref().callV("length")),
                                                new Block(current.assign(readElementFrom.x(from))),
                                                limitReached))));
                out.addStatement(to.assign(convertBufferArrayToValue.x(TargetClass.from(java.util.Arrays.class)
                        .callV("copyOf", buf.ref(), pos.ref()))));

                return out;
            }
        };
    }

    private static void writeLimitsReading(Block to, MaxArraySize maxArraySizeKind, Typed<AnInt> fullArraySizeLocation,
                                           TransferArraySize transferArraySizeKind, Typed<AnInt> serialLimit,
                                           boolean elementTypeCanLimitVerify) {
        LinkedList<Typed<? extends AValue>> limits = new LinkedList<Typed<? extends AValue>>();
        switch (maxArraySizeKind) {
            case NO_LIMIT:
                break;
            case CONSTRUCTOR_PARAM:
                limits.add(pLimits.ref().callV("full_array_size"));
                break;
            case LIMITED_BY_TYPE:
                limits.add(fullArraySizeLocation);
                break;
            default:
                throw new UnsupportedOperationException("Source of max array size is not known");
        }
        switch (transferArraySizeKind) {
            case MAX_ARRAY_SIZE:
                break;
            case CONSTRUCTOR_PARAM:
                limits.add(Hardcoded.pLimits.ref().callV("elements_to_transfer"));
                break;
            case SERIALIZED:
                limits.add(serialLimit);
                break;
            default:
                throw new UnsupportedOperationException("Source of transfer array size is not known");
        }
        if (elementTypeCanLimitVerify)
            limits.add(pLimits.ref().<AValue>call("next"));
        to.addStatement(fMaxSize.assign(TargetClass.from(ElementsLimit.class).callV("limit", limits.toArray(new Typed[0]))));
    }

    private static From1<Block, Var> createConstructorBody(final TargetClass javaType, final MaxArraySize maxArraySizeKind, final TransferArraySize transferArraySizeKind, final From1<Typed<AnInt>, Var> numberOfElements, final boolean terminatable, final Typed<AnInt> fullArraySizeLocation, final MethodCall<Returnable> validateLimitsCall, final boolean elementTypeCanLimitVerify) {
        return new From1<Block, Var>() {
            @Override
            public Block x(Var to) {
                final Var pValue = Var.param(javaType, "value");
                Block fromJavaTyped = new Block();
                writeLimitsReading(fromJavaTyped, maxArraySizeKind, fullArraySizeLocation, transferArraySizeKind, null == numberOfElements ? null : numberOfElements.x(pValue), elementTypeCanLimitVerify);
                fromJavaTyped.addStatement(to.assign(pValue.ref()));
                if (eatsArrayLimitInformation(maxArraySizeKind, transferArraySizeKind))
                    fromJavaTyped.addStatement(validateLimitsCall);
                return fromJavaTyped;
            }
        };
    }

    // all array eaters, those that take serialized size limits if serialized constructor
    private static void sizeIsInsideTheLimit(Block out,
                                             MaxArraySize maxArraySizeKind, TransferArraySize transferArraySizeKind,
                                             Typed<AnInt> actualNumberOfElements, Typed<AnInt> limit,
                                             boolean tolerateSmaller) {
        Typed<ABool> check = tolerateSmaller ?
                isSmallerThan(limit, actualNumberOfElements) :
                isNotSame(limit, actualNumberOfElements);
        out.addStatement(IF(check,
                new Block(THROW(IllegalNumberOfElementsException.class, literal("Wrong number of elements")))));
    }

    private static Method getValidateInsideLimits(MaxArraySize maxArraySizeKind, TransferArraySize transferArraySizeKind, From1<Typed<AnInt>, Var> numberOfElements, Var<?> fValue, boolean unterminatable, boolean elementTypeCanLimitVerify, TargetArray buffertype) {
        Block verifyInsideLimits = new Block();
        sizeIsInsideTheLimit(verifyInsideLimits,
                maxArraySizeKind, transferArraySizeKind,
                numberOfElements.x(fValue),
                Hardcoded.pLimits.ref().<AnInt>callV("elements_to_transfer"),
                !unterminatable);
        if (elementTypeCanLimitVerify) {
            TargetClass elemtype = ((TargetArray)(fValue.getTType())).getOf();
            Var element = Var.local(elemtype, "element", null);
            verifyInsideLimits.addStatement(FOR(element, fValue.ref(), new Block(
                    buffertype.getOf().newInstance(element.ref(), pLimits.ref().<TargetClass>call("next"))
                            .call(SELF_VALIDATOR_NAME, pLimits.ref().<TargetClass>call("next"))
            )));
        }

        return Method.newPublicDynamicMethod(Comment.no(),
                TargetClass.from(void.class), SELF_VALIDATOR_NAME,
                Arrays.asList(Hardcoded.pLimits),
                Arrays.asList(TargetClass.from(FieldTypeException.class)),
                new Block(wrapThrowableInFieldTypeException(verifyInsideLimits)));
    }

    private static Typed<?> wrapThrowableInFieldTypeException(Block verifyInsideLimits) {
        Var<AValue> e = Var.param(Throwable.class, "e");
        TargetClass ft = TargetClass.from(FieldTypeException.class);
        return BuiltIn.tryCatch(
                verifyInsideLimits,
                Var.param(Throwable.class, "e"),
                new Block(
                        THROW(R_IF(isInstanceOf(e.ref(), ft), cast(ft, e.ref()),
                                ft.newInstance(sum(literal("threw "), e.ref().callV("getClass").callV("getName")), e.ref())))
                ));
    }

    public static TerminatedArray xBytes(String dataIOType, String publicType) {
        return new TerminatedArray(dataIOType, publicType, byteArray, null,
                MaxArraySize.CONSTRUCTOR_PARAM,
                TransferArraySize.CONSTRUCTOR_PARAM,
                byteArray,
                arrayLen,
                fullIsByteArray,
                valueIsBufferArray,
                elemIsByteArray,
                readByte,
                TO_STRING_ARRAY,
                Collections.<Requirement>emptySet(),
                null,
                null,
                arrayLen,
                sameNumberOfBufferElementsAndValueElements,
                Collections.<Method.Helper>emptyList(),
                false,
                false
        );
    }

    public static TerminatedArray maxSizedTerminated(String dataIOType, String publicType, final Constant<?> terminator) {
        return new TerminatedArray(dataIOType, publicType, byteArray, terminator,
                MaxArraySize.CONSTRUCTOR_PARAM,
                TransferArraySize.CONSTRUCTOR_PARAM,
                byteArray,
                arrayLen,
                fullIsByteArray, valueIsBufferArray, elemIsByteArray, readByte,
                TO_STRING_ARRAY,
                Arrays.asList(terminator.getIFulfillReq()),
                null,
                null,
                arrayLen,
                sameNumberOfBufferElementsAndValueElements,
                Collections.<Method.Helper>emptyList(),
                false,
                false
        );
    }

    public static TerminatedArray fieldArray(final String dataIOType, final String publicType,
                                             final FieldType kind) {
        return fieldArray(dataIOType, publicType, kind, null);
    }

    public static TerminatedArray fieldArray(final String dataIOType, final String publicType,
                                             final FieldType kind, final Constant<?> stopElem) {
        final TargetArray type = TargetArray.from(kind.getUnderType(), 1);
        final boolean arrayEater = kind.isArrayEater();

        Var<AValue> helperParamValue = Var.param(type, "values");
        Var<AValue> helperParamLimits = Var.param(ElementsLimit.class, "limits");
        Var<AValue> elem = Var.<AValue>param(kind.getUnderType(), "elem");
        Var<AnInt> outVar = Var.<AnInt>local(int.class, "totalSize", literal(0));
        final Method.Helper lenInBytesHelper = Method.newHelper(Comment.no(), TargetClass.from(int.class), "lengthInBytes",
                Arrays.<Var<?>>asList(helperParamValue, helperParamLimits),
                new Block(outVar,
                        FOR(elem, helperParamValue.ref(),
                                new Block(inc(outVar, kind.getAddress().newInstance(elem.ref(), helperParamLimits.ref()).callV("encodedLength")))),
                        RETURN(outVar.ref())));

        Var<AValue> pBuf = Var.param(TargetArray.from(kind.getAddress(), 1), "buf");
        Var<AValue> oVal = Var.local(type, "out", type.newInstance(pBuf.ref().<AnInt>callV("length")));
        Var<AnInt> count = Var.<AnInt>local(int.class, "i", literal(0));
        final Method.Helper buffer2value = Method.newHelper(Comment.no(), type, "buffer2value",
                Arrays.<Var<?>>asList(pBuf),
                new Block(
                        oVal,
                        FOR(count, isSmallerThan(count.ref(), pBuf.ref().callV("length")), inc(count), new Block(
                                arraySetElement(oVal, count.ref(), pBuf.ref().callV("[]", count.ref()).<AValue>call("getValue"))
                        )),
                        RETURN(oVal.ref())
                ));

        return new TerminatedArray(dataIOType, publicType, type, stopElem,
                MaxArraySize.CONSTRUCTOR_PARAM,
                TransferArraySize.CONSTRUCTOR_PARAM,
                TargetArray.from(kind.getAddress(), 1),
                arrayLen,
                null,
                new From1<Typed<AValue>, Typed<AValue>>() {
                    @Override
                    public Typed<AValue> x(Typed<AValue> bytes) {
                        return buffer2value.getAddress().call(bytes);
                    }
                },
                new From2<Block, Var, Var>() {
                    @Override
                    public Block x(Var to, Var elem) {
                        final Typed<AValue> from = "int".equals(elem.getTType().getName()) ?
                                kind.getUnderType().newInstance(elem.ref(), NULL) :
                                elem.ref();
                        return new Block(kind.getAddress()
                                .newInstance(from, fMaxSize.ref().<Returnable>call("next"))
                                .call("encodeTo", to.ref()));
                    }
                },
                new From1<Typed<? extends AValue>, Var>() {
                    @Override
                    public Typed<? extends AValue> x(Var from) {
                        return kind.getAddress().newInstance(from.ref(), getNext(arrayEater));
                    }
                },
                TO_STRING_ARRAY,
                Arrays.asList(kind.getIFulfillReq()),
                null,
                null,
                new From1<Typed<AnInt>, Var>() {
                    @Override
                    public Typed<AnInt> x(Var value) {
                        return lenInBytesHelper.getAddress().call(value.ref(), fMaxSize.ref().<AValue>call("next"));
                    }
                },
                sameNumberOfBufferElementsAndValueElements,
                Arrays.<Method.Helper>asList(lenInBytesHelper, buffer2value),
                arrayEater,
                TargetArray.from(kind.getAddress(), 1).getOf().getName().endsWith("_DIFF")
        );
    }

    private static Typed<? extends AValue> getNext(boolean arrayEater) {
        return arrayEater ?
                Hardcoded.pLimits.ref().<TargetClass>call("next") :
                Hardcoded.noLimit;
    }

    public enum MaxArraySize {
        NO_LIMIT,
        CONSTRUCTOR_PARAM,
        LIMITED_BY_TYPE
    }

    public enum TransferArraySize {
        MAX_ARRAY_SIZE,
        CONSTRUCTOR_PARAM,
        SERIALIZED
    }
}
