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

import static org.freeciv.packetgen.Hardcoded.*;
import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.*;

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
    private static final ExprFrom1<Typed<AValue>, Typed<AValue>> valueIsBufferArray =
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
    public static final ExprFrom1<Typed<AnInt>, Typed<AnInt>> sameNumberOfBufferElementsAndValueElements =
                                new ExprFrom1<Typed<AnInt>, Typed<AnInt>>() {
                                    @Override
                                    public Typed<AnInt> x(Typed<AnInt> maxElements) {
                                        return maxElements;
                                    }
                                };
    public static final String SELF_VALIDATOR_NAME = "verifyInsideLimits";

    private final boolean unterminatable;
    private final TransferArraySize transferArraySizeKind;
    private final MaxArraySize maxArraySizeKind;
    private final ExprFrom1<Typed<AnInt>, Var> numberOfElements;
    private final boolean elementTypeCanLimitVerify;
    private final TargetArray buffertype;
    private final HashSet<Method.Helper> helpers;
    private final Method validateInsideLimits;

    public TerminatedArray(final String dataIOType, final String publicType, final TargetClass javaType,
                           final Requirement terminator,
                           final MaxArraySize maxArraySizeKind,
                           final TransferArraySize transferArraySizeKind,
                           final TargetArray buffertype,
                           final ExprFrom1<Typed<AnInt>, Var> numberOfElements,
                           final ExprFrom1<Typed<ABool>, Typed<AnInt>> testIfTerminatorShouldBeAdded,
                           final ExprFrom1<Typed<AValue>, Var> convertAllElementsToByteArray,
                           final ExprFrom1<Typed<AValue>, Typed<AValue>> convertBufferArrayToValue,
                           final ExprFrom2<Block, Var, Var> writeElementTo,
                           final ExprFrom1<Typed<? extends AValue>, Var> readElementFrom,
                           final ExprFrom1<Typed<AString>, Var> toString,
                           final Collection<Requirement> uses,
                           final Typed<AnInt> fullArraySizeLocation,
                           final NetworkIO transferSizeSerialize,
                           final ExprFrom1<Typed<AnInt>, Var> valueGetByteLen,
                           final ExprFrom1<Typed<AnInt>, Typed<AnInt>> numberOfValueElementToNumberOfBufferElements,
                           final Collection<Method.Helper> helperMethods,
                           boolean elementTypeCanLimitVerify
    ) {
        super(dataIOType, publicType, javaType,
                createConstructorBody(javaType, maxArraySizeKind, transferArraySizeKind, numberOfElements, !notTerminatable(terminator), fullArraySizeLocation, new MethodCall<Returnable>(SELF_VALIDATOR_NAME, pLimits.ref())),
                createDecode(terminator, maxArraySizeKind, transferArraySizeKind, buffertype, convertBufferArrayToValue, readElementFrom, fullArraySizeLocation, transferSizeSerialize, numberOfValueElementToNumberOfBufferElements),
                createEncode(terminator, transferArraySizeKind, buffertype, numberOfElements, testIfTerminatorShouldBeAdded, convertAllElementsToByteArray, writeElementTo, transferSizeSerialize),
                createEnocedSize(transferArraySizeKind, numberOfElements, testIfTerminatorShouldBeAdded, transferSizeSerialize, valueGetByteLen),
                toString,
                eatsArrayLimitInformation(maxArraySizeKind, transferArraySizeKind),
                uses
        );

        this.unterminatable = notTerminatable(terminator);
        this.maxArraySizeKind = maxArraySizeKind;
        this.transferArraySizeKind = transferArraySizeKind;
        this.numberOfElements = numberOfElements;
        this.elementTypeCanLimitVerify = elementTypeCanLimitVerify;
        this.buffertype = buffertype;

        this.validateInsideLimits = eatsArrayLimitInformation(maxArraySizeKind, transferArraySizeKind) ?
                getValidateInsideLimits() :
                null;

        helpers = new HashSet<Method.Helper>(helperMethods);
    }

    private static boolean eatsArrayLimitInformation(MaxArraySize maxArraySizeKind, TransferArraySize transferArraySizeKind) {
        return MaxArraySize.CONSTRUCTOR_PARAM.equals(maxArraySizeKind)
                || TransferArraySize.CONSTRUCTOR_PARAM.equals(transferArraySizeKind);
    }

    private static boolean notTerminatable(Requirement terminator) {
        return null == terminator;
    }

    private static ExprFrom1<Typed<AnInt>, Var> createEnocedSize(final TransferArraySize transferArraySizeKind, final ExprFrom1<Typed<AnInt>, Var> numberOfElements, final ExprFrom1<Typed<ABool>, Typed<AnInt>> testIfTerminatorShouldBeAdded, final NetworkIO transferSizeSerialize, final ExprFrom1<Typed<AnInt>, Var> valueGetByteLen) {
        return new ExprFrom1<Typed<AnInt>, Var>() {
            @Override
            public Typed<AnInt> x(Var value) {
                Typed<AnInt> length = valueGetByteLen.x(value);
                if (TransferArraySize.SERIALIZED.equals(transferArraySizeKind))
                    length = sum(transferSizeSerialize.getSize().x(value), length);
                Typed<ABool> addAfterResult = testIfTerminatorShouldBeAdded.x(numberOfElements.x(value));
                if (!FALSE.equals(addAfterResult))
                    length = BuiltIn.<AnInt>sum(
                            length,
                            R_IF(addAfterResult,
                                    literal(1),
                                    literal(0)));
                return length;
            }
        };
    }

    private static ExprFrom2<Block, Var, Var> createEncode(final Requirement terminator, final TransferArraySize transferArraySizeKind, final TargetArray buffertype, final ExprFrom1<Typed<AnInt>, Var> numberOfElements, final ExprFrom1<Typed<ABool>, Typed<AnInt>> testIfTerminatorShouldBeAdded, final ExprFrom1<Typed<AValue>, Var> convertAllElementsToByteArray, final ExprFrom2<Block, Var, Var> writeElementTo, final NetworkIO transferSizeSerialize) {
        return new ExprFrom2<Block, Var, Var>() {
            @Override
            public Block x(Var val, Var to) {
                Block out = new Block();
                if (TransferArraySize.SERIALIZED.equals(transferArraySizeKind))
                    out.addStatement(to.call(transferSizeSerialize.getWrite(), numberOfElements.x(val)));
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
        };
    }

    private static ExprFrom2<Block, Var, Var> createDecode(final Requirement terminator, final MaxArraySize maxArraySizeKind, final TransferArraySize transferArraySizeKind, final TargetArray buffertype, final ExprFrom1<Typed<AValue>, Typed<AValue>> convertBufferArrayToValue, final ExprFrom1<Typed<? extends AValue>, Var> readElementFrom, final Typed<AnInt> fullArraySizeLocation, final NetworkIO transferSizeSerialize, final ExprFrom1<Typed<AnInt>, Typed<AnInt>> numberOfValueElementToNumberOfBufferElements) {
        return new ExprFrom2<Block, Var, Var>() {
            @Override
            public Block x(Var to, Var from) {
                Var buf = Var.local(buffertype, "buffer",
                        buffertype.newInstance(numberOfValueElementToNumberOfBufferElements.x(fMaxSize.ref())));
                Var current = Var.local(buffertype.getOf(), "current", readElementFrom.x(from));
                Var pos = Var.local("int", "pos", literal(0));

                Typed<ABool> noTerminatorFound = (notTerminatable(terminator) ?
                        TRUE :
                        isNotSame(cast(byte.class,
                                BuiltIn.<AnInt>toCode(Constant.referToInJavaCode(terminator))), current.ref()));

                Block out = new Block();

                Typed<AnInt> relativeMaxArray = maxArraySizeVar(maxArraySizeKind, fullArraySizeLocation);
                out.addStatement(fMaxSize.assign(setFMaxSize(relativeMaxArray,
                        transferArraySizeKind, null == transferSizeSerialize ? null : transferSizeSerialize.getRead().x(from))));

                theLimitIsSane(out, relativeMaxArray, fMaxSize.ref(), transferArraySizeKind, maxArraySizeKind);

                out.addStatement(buf);
                out.addStatement(current);
                out.addStatement(pos);
                out.addStatement(
                        WHILE(noTerminatorFound,
                                new Block(arraySetElement(buf, pos.ref(), current.ref()),
                                        inc(pos),
                                        IF(isSmallerThan(pos.ref(), buf.<AnInt>read("length")),
                                                new Block(current.assign(readElementFrom.x(from))),
                                                new Block(BuiltIn.<NoValue>toCode("break"))))));
                out.addStatement(to.assign(convertBufferArrayToValue.x(new MethodCall<AValue>("java.util.Arrays.copyOf",
                        buf.ref(), pos.ref()))));

                return out;
            }
        };
    }

    private static void theLimitIsSane(Block out, Typed<AnInt> relativeMaxArray, Typed<AnInt> absoluteMaxArray,
                                       TransferArraySize transferArraySizeKind, MaxArraySize maxArraySizeKind) {
        if (shouldValidateLimits(transferArraySizeKind, maxArraySizeKind))
            out.addStatement(Hardcoded.arrayEaterScopeCheck(isSmallerThan(absoluteMaxArray, relativeMaxArray)));
    }

    private static boolean shouldValidateLimits(TransferArraySize transferArraySizeKind, MaxArraySize maxArraySizeKind) {
        return !(TransferArraySize.MAX_ARRAY_SIZE.equals(transferArraySizeKind)
                || noUpperLimitOnTheNumberOfElements(maxArraySizeKind));
    }

    private static ExprFrom1<Block, Var> createConstructorBody(final TargetClass javaType, final MaxArraySize maxArraySizeKind, final TransferArraySize transferArraySizeKind, final ExprFrom1<Typed<AnInt>, Var> numberOfElements, final boolean terminatable, final Typed<AnInt> fullArraySizeLocation, final MethodCall<Returnable> validateLimitsCall) {
        return new ExprFrom1<Block, Var>() {
            @Override
            public Block x(Var to) {
                final Var pValue = Var.param(javaType, "value");
                Block fromJavaTyped = new Block();
                Typed<AnInt> relativeMaxArray = maxArraySizeVar(maxArraySizeKind, fullArraySizeLocation);
                fromJavaTyped.addStatement(fMaxSize.assign(setFMaxSize(relativeMaxArray,
                        transferArraySizeKind, null == numberOfElements ? null : numberOfElements.x(pValue))));
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
        out.addStatement(arrayEaterScopeCheck(check));
    }

    private static boolean noUpperLimitOnTheNumberOfElements(MaxArraySize maxArraySizeKind) {
        return MaxArraySize.NO_LIMIT.equals(maxArraySizeKind);
    }

    private static Typed<AnInt> maxArraySizeVar(MaxArraySize maxArraySizeKind, Typed<AnInt> fullArraySizeLocation) {
        switch (maxArraySizeKind) {
            case CONSTRUCTOR_PARAM:
                return Hardcoded.pLimits.read("elements_to_transfer");
            case LIMITED_BY_TYPE:
                return fullArraySizeLocation;
            case NO_LIMIT:
                return null;
            default:
                throw new UnsupportedOperationException("Source of max array size is not known");
        }
    }

    private static Typed<AnInt> setFMaxSize(Typed<AnInt> relativeMaxArray, TransferArraySize transferArraySizeKind,
                                            Typed<AnInt> numberOfElements) {
        switch (transferArraySizeKind) {
            case MAX_ARRAY_SIZE:
                return relativeMaxArray;
            case CONSTRUCTOR_PARAM:
                return pLimits.read("full_array_size");
            case SERIALIZED:
                return numberOfElements;
            default:
                throw new UnsupportedOperationException("Source of transfer array size is not known");
        }
    }

    private Method getValidateInsideLimits() {
        Block verifyInsideLimits = new Block();
        theLimitIsSane(verifyInsideLimits,
                Hardcoded.pLimits.<AnInt>read("elements_to_transfer"),
                Hardcoded.pLimits.<AnInt>read("full_array_size"),
                transferArraySizeKind, maxArraySizeKind);
        sizeIsInsideTheLimit(verifyInsideLimits,
                maxArraySizeKind, transferArraySizeKind,
                numberOfElements.x(fValue),
                Hardcoded.pLimits.<AnInt>read("elements_to_transfer"),
                !unterminatable);
        if (elementTypeCanLimitVerify) {
            TargetClass elemtype = buffertype.getOf(); // arrayEater's are read element by element
            Var element = Var.local(elemtype, "element", null);
            verifyInsideLimits.addStatement(FOR(element, fValue.ref(), new Block(
                    element.call(SELF_VALIDATOR_NAME, pLimits.<TargetClass>call("next"))
            )));
        }
        return Method.newPublicDynamicMethod(Comment.no(),
                new TargetClass("void", true), SELF_VALIDATOR_NAME,
                Arrays.asList(Hardcoded.pLimits),
                Collections.<TargetClass>emptyList(),
                verifyInsideLimits);
    }

    @Override
    public FieldTypeAlias createFieldType(String name) {
        return new FieldTypeAliasToTerminatedArray(name);
    }

    private class FieldTypeAliasToTerminatedArray extends FieldTypeAlias {
        private FieldTypeAliasToTerminatedArray(String name) {
            this(name, name);
        }

        private FieldTypeAliasToTerminatedArray(String name, String alias) {
            super(name, alias);

            addObjectConstant("int", "maxArraySize");

            if (!helpers.isEmpty()) {
                for (Method helper : helpers) {
                    addMethod(helper);
                }
            }

            if (eatsArrayLimitInformation(maxArraySizeKind, transferArraySizeKind)) {
                addMethod(validateInsideLimits);
            }
        }

        @Override
        protected FieldTypeAlias invisibleAliasCreation(String alias) {
            return new FieldTypeAliasToTerminatedArray(getName(), alias);
        }
    }

    public static TerminatedArray xBytes(String dataIOType, String publicType) {
        return new TerminatedArray(dataIOType, publicType, byteArray, null,
                        MaxArraySize.CONSTRUCTOR_PARAM,
                        TransferArraySize.CONSTRUCTOR_PARAM,
                        byteArray,
                        arrayLen,
                        neverAnythingAfter,
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
                        Collections.<Method.Helper>emptySet(),
                false
        );
    }

    public static TerminatedArray maxSizedTerminated(String dataIOType, String publicType, final Requirement terminator) {
        return new TerminatedArray(dataIOType, publicType, byteArray, terminator,
                        MaxArraySize.CONSTRUCTOR_PARAM,
                        TransferArraySize.CONSTRUCTOR_PARAM,
                        byteArray,
                        arrayLen, addAfterIfSmallerThanMaxSize,
                fullIsByteArray, valueIsBufferArray, elemIsByteArray, readByte,
                        TO_STRING_ARRAY,
                        Arrays.asList(terminator),
                        null,
                        null,
                        arrayLen,
                        sameNumberOfBufferElementsAndValueElements,
                        Collections.<Method.Helper>emptySet(),
                false
        );
    }

    public static TerminatedArray fieldArray(final String dataIOType, final String publicType,
                                             final FieldTypeAlias kind) {
        return fieldArray(dataIOType, publicType, new TargetArray(kind.getAddress(), 1, true),
                kind.getBasicType().isArrayEater());
    }

    public static TerminatedArray fieldArray(final String dataIOType, final String publicType, final TargetArray type,
                                             final boolean arrayEater) {
        Var<AValue> helperParamValue = Var.param(type, "value");
        final Method.Helper lenInBytesHelper = Method.newHelper(Comment.no(), new TargetClass(int.class), "lengthInBytes",
                Arrays.<Var<?>>asList(helperParamValue), new Block(RETURN(helperParamValue.read("length"))));
        return new TerminatedArray(dataIOType, publicType, type, null,
                MaxArraySize.CONSTRUCTOR_PARAM,
                TransferArraySize.CONSTRUCTOR_PARAM,
                type,
                arrayLen,
                neverAnythingAfter,
                null,
                valueIsBufferArray,
                new ExprFrom2<Block, Var, Var>() {
                    @Override
                    public Block x(Var to, Var elem) {
                        return new Block(elem.call("encodeTo", to.ref()));
                    }
                },
                new ExprFrom1<Typed<? extends AValue>, Var>() {
                    @Override
                    public Typed<? extends AValue> x(Var from) {
                        TargetClass elemType = type.getOf();
                        if (arrayEater)
                            return elemType.newInstance(from.ref(), Hardcoded.pLimits.<TargetClass>call("next"));
                        else
                            return elemType.newInstance(from.ref(), new MethodCall<AValue>("ElementsLimit.noLimit"));
                    }
                },
                TO_STRING_ARRAY,
                Arrays.asList(new Requirement(type.getOf().getName(), FieldTypeAlias.class)),
                null,
                null,
                new ExprFrom1<Typed<AnInt>, Var>() {
                    @Override
                    public Typed<AnInt> x(Var value) {
                        return lenInBytesHelper.getAddress().call(value.ref());
                    }
                },
                sameNumberOfBufferElementsAndValueElements,
                Arrays.<Method.Helper>asList(lenInBytesHelper),
                arrayEater
        );
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
