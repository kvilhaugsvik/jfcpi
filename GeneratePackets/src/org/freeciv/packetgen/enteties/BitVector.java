package org.freeciv.packetgen.enteties;

import com.kvilhaugsvik.javaGenerator.expression.Reference;
import com.kvilhaugsvik.javaGenerator.typeBridge.*;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.*;
import org.freeciv.packetgen.Hardcoded;
import com.kvilhaugsvik.dependency.UndefinedException;
import com.kvilhaugsvik.dependency.Dependency;
import com.kvilhaugsvik.dependency.Required;
import com.kvilhaugsvik.dependency.Requirement;
import org.freeciv.packetgen.enteties.supporting.DataType;
import org.freeciv.packetgen.enteties.supporting.IntExpression;
import org.freeciv.packetgen.enteties.supporting.NetworkIO;
import org.freeciv.packetgen.enteties.supporting.TerminatedArray;
import com.kvilhaugsvik.javaGenerator.*;
import com.kvilhaugsvik.javaGenerator.typeBridge.From1;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;

import java.util.*;

import static com.kvilhaugsvik.javaGenerator.util.BuiltIn.*;

/**
 * Generates code for a bit vector type. Can also generate a field type for it.
 */
public class BitVector extends ClassWriter implements Dependency.Item, Dependency.Maker, DataType {
    private static final Var<AnObject> pFromByte = Var.param(byteArray, "from");
    private static final Var<AnObject> pFromBits = Var.param(boolArray, "from");
    private static final Var<AnInt> pSize = Var.param(int.class, "sizeInBits");

    private final Collection<Requirement> iRequire;
    private final Requirement iProvide;

    private final boolean knowsSize;
    private final String dataIOType;
    private final TerminatedArray.MaxArraySize maxArraySizeKind;
    private TerminatedArray.TransferArraySize transferArraySizeKind;

    private final Required bvFieldType;

    /**
     * Constructor for a typed fixed size bit vector.
     * In a typed bit vector each position is understood to correspond to an enumerator of an enumeration. If the
     * corresponding bit vector position is true the enumerator is present.
     * @param name the name to give the understood bit vector type.
     * @param kind the enumeration that should be used to understand the bit vector.
     */
    public BitVector(String name, Enum kind) {
        this(name, IntExpression.readFromOther(kind, kind.getAddress().<AnInt>callV("countValidElements")),
                TerminatedArray.MaxArraySize.LIMITED_BY_TYPE, TerminatedArray.TransferArraySize.MAX_ARRAY_SIZE, name,
                TargetClass.from(org.freeciv.types.UnderstoodBitVector.class)
                        .addGenericTypeArguments(Arrays.asList(kind.getAddress())),
                Arrays.asList(kind.getAddress().callV("class")));
    }

    /**
     * Constructor for a fixed size bit vector.
     * @param name the name to give the bit vector type.
     * @param knownSize the size of the bit vector type.
     */
    public BitVector(String name, IntExpression knownSize) {
        this(name, knownSize,
                TerminatedArray.MaxArraySize.LIMITED_BY_TYPE, TerminatedArray.TransferArraySize.MAX_ARRAY_SIZE, name,
                TargetClass.from(org.freeciv.types.BitVector.class), Collections.<Typed<AValue>>emptyList());
    }

    /**
     * Constructor for a bit vector that reads its size from the network and is called "BitString".
     */
    public BitVector() {
        /* Don't convert to string of "1" or "0" just to convert it back later. */
        this("BitString", null,
                TerminatedArray.MaxArraySize.CONSTRUCTOR_PARAM, TerminatedArray.TransferArraySize.SERIALIZED, "char",
                TargetClass.from(org.freeciv.types.BitVector.class), Collections.<Typed<AValue>>emptyList());
    }

    /**
     * Constructor for a bit vector where each instance has its size determined at compile time. Used for the delta
     * protocol's field vector.
     * @param name the name to give to the bit  vector.
     */
    public BitVector(String name) {
        this(name, null,
                TerminatedArray.MaxArraySize.CONSTRUCTOR_PARAM, TerminatedArray.TransferArraySize.MAX_ARRAY_SIZE, name,
                TargetClass.from(org.freeciv.types.BitVector.class), Collections.<Typed<AValue>>emptyList());
    }

    private BitVector(String name, IntExpression knownSize, TerminatedArray.MaxArraySize maxArraySizeKind,
                      TerminatedArray.TransferArraySize transferArraySizeKind, String reqName, TargetClass parent,
                      List<? extends Typed<? extends AValue>> appendParam) {
        super(ClassKind.CLASS, TargetPackage.from(org.freeciv.types.BitVector.class.getPackage()), Imports.are(),
                "Freeciv C code", Collections.<Annotate>emptyList(), name,
                parent, Collections.<TargetClass>emptyList());
        this.knowsSize = null != knownSize;

        this.maxArraySizeKind = maxArraySizeKind;
        this.transferArraySizeKind = transferArraySizeKind;

        if (knowsSize)
            addClassConstant(Visibility.PUBLIC, int.class, "size", knownSize);
        else
            addPublicObjectConstant(int.class, "size");

        Reference sizeForNotFromData = knowsSize ? getField("size").ref() : pSize.ref();
        {
            List<? extends Var<? extends AValue>> pList = knowsSize ?
                    Arrays.asList(pFromByte) :
                    Arrays.asList(pFromByte, pSize);

            List<Typed<? extends AValue>> args = new LinkedList<Typed<? extends AValue>>();
            args.add(sizeForNotFromData);
            args.add(pFromByte.ref());
            args.addAll(appendParam);

            Block constructorBody = new Block(BuiltIn.superConstr(args.toArray(new Typed[args.size()])));

            if (!knowsSize)
                constructorBody.addStatement(getField("size").assign(pSize.ref()));

            addMethod(Method.newPublicConstructor(Comment.no(), pList, constructorBody));
        }
        {
            List<? extends Var<? extends AValue>> pList = Arrays.asList(pFromBits);

            List<Typed<? extends AValue>> args = new LinkedList<Typed<? extends AValue>>();
            args.add(pFromBits.ref());
            args.addAll(appendParam);

            Block constructorBody = new Block(BuiltIn.superConstr(args.toArray(new Typed[args.size()])));

            if (!knowsSize)
                constructorBody.addStatement(getField("size").assign(pFromBits.ref().callV("length")));

            addMethod(Method.newPublicConstructor(Comment.no(), pList, constructorBody));
        }
        {
            List<? extends Var<? extends AValue>> pList = knowsSize ?
                    Collections.<Var<? extends AValue>>emptyList() :
                    Arrays.asList(pSize);

            List<Typed<? extends AValue>> args = new LinkedList<Typed<? extends AValue>>();
            args.add(sizeForNotFromData);
            args.addAll(appendParam);

            Block constructorBody = new Block(BuiltIn.superConstr(args.toArray(new Typed[args.size()])));

            if (!knowsSize)
                constructorBody.addStatement(getField("size").assign(pSize.ref()));

            addMethod(Method.newPublicConstructor(Comment.no(), pList, constructorBody));
        }

        this.iRequire = knowsSize ? knownSize.getReqs() : Collections.<Requirement>emptySet();
        this.iProvide = new Requirement(reqName, DataType.class);
        this.dataIOType = knowsSize ? "bitvector" : "bit_string";
        this.bvFieldType = new Requirement(dataIOType +
                "(" + iProvide.getName() + ")", FieldType.class);
    }

    @Override
    public Collection<Requirement> getReqs() {
        return iRequire;
    }

    @Override
    public Requirement getIFulfillReq() {
        return iProvide;
    }

    private static Typed<AnInt> neededBytes(Typed<AnInt> value) {
        return BuiltIn.<AnInt>sum(literal(1),
                divide(GROUP(subtract(value, literal(1))), literal(8)));
    }

    @Override
    public List<Requirement> neededInput(Requirement toProduce) {
        if (TerminatedArray.TransferArraySize.SERIALIZED.equals(transferArraySizeKind))
            return Arrays.asList(new Requirement("uint16", NetworkIO.class));
        else
            return Collections.<Requirement>emptyList();
    }

    @Override
    public Required getICanProduceReq() {
        return bvFieldType;
    }

    @Override
    public Dependency.Item produce(Requirement toProduce, Dependency.Item... wasRequired) throws UndefinedException {
        final TargetClass me = super.getAddress();

        final From2<Typed<AValue>, Typed<AValue>, Var> convertBufferArrayToValue;
        switch (transferArraySizeKind) {
            case MAX_ARRAY_SIZE:
                if (TerminatedArray.MaxArraySize.LIMITED_BY_TYPE.equals(maxArraySizeKind))
                    convertBufferArrayToValue = new From2<Typed<AValue>, Typed<AValue>, Var>() {
                        @Override
                        public Typed<AValue> x(Typed<AValue> bv, Var old) {
                            return me.newInstance(bv);
                        }
                    };
                else
                    convertBufferArrayToValue = new From2<Typed<AValue>, Typed<AValue>, Var>() {
                        @Override
                        public Typed<AValue> x(Typed<AValue> bv, Var old) {
                            return me.newInstance(bv, Hardcoded.fMaxSize.ref().callV("full_array_size"));
                        }
                    };
                break;
            default:
                convertBufferArrayToValue = new From2<Typed<AValue>, Typed<AValue>, Var>() {
                    @Override
                    public Typed<AValue> x(Typed<AValue> bv, Var old) {
                        return me.newInstance(bv, Hardcoded.fMaxSize.ref().callV("elements_to_transfer"));
                    }
                };
                break;
        }

        final NetworkIO transferSizeSerialize;
        switch (transferArraySizeKind) {
            case SERIALIZED:
                assert 1 == wasRequired.length : "Needed argument missing";
                transferSizeSerialize = (NetworkIO)wasRequired[0];
                break;
            default:
                transferSizeSerialize = null;
                break;
        }

        return new TerminatedArray(
                dataIOType,
                iProvide.getName(),
                this,
                null,
                maxArraySizeKind,
                transferArraySizeKind,
                TerminatedArray.byteArray,
                new From1<Typed<AnInt>, Var>() {
                    @Override
                    public Typed<AnInt> x(Var val) {
                        return val.ref().callV("size");
                    }
                },
                new From1<Typed<AValue>, Var>() {
                    @Override
                    public Typed<AValue> x(Var buffer) {
                        return buffer.ref().<Returnable>call("getAsByteArray");
                    }
                },
                convertBufferArrayToValue,
                null,
                TerminatedArray.readByte,
                BuiltIn.TO_STRING_OBJECT,
                Arrays.asList(iProvide),
                me.<AnInt>callV("size"),
                transferSizeSerialize,
                knowsSize ?
                        new From1<Typed<AnInt>, Var>() {
                            @Override
                            public Typed<AnInt> x(Var val) {
                                return neededBytes(me.<AnInt>callV("size"));
                            }
                        } :
                        new From1<Typed<AnInt>, Var>() {
                            @Override
                            public Typed<AnInt> x(Var arg1) {
                                return neededBytes(arg1.ref().callV("size"));
                            }
                        },
                new From1<Typed<AnInt>, Typed<AnInt>>() {
                    @Override
                    public Typed<AnInt> x(Typed<AnInt> val) {
                        return neededBytes(val);
                    }
                },
                Collections.<Method.Helper>emptyList(),
                false,
                false,
                Collections.<Var<? extends AValue>>emptyList(),
                FieldType.UNSIZED_ZERO
        );
    }

    @Override
    public Value getZeroValue() {
        final List<Typed<? extends AValue>> args = new LinkedList<Typed<? extends AValue>>();

        if (!knowsSize) {
            /* The zero size should be 0. */
            args.add(literal(0));
        }

        return getAddress().newInstance(args.toArray(new Typed[args.size()]));
    }
}
