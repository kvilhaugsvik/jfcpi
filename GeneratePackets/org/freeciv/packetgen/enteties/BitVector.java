package org.freeciv.packetgen.enteties;

import com.kvilhaugsvik.javaGenerator.expression.Reference;
import org.freeciv.packetgen.Hardcoded;
import org.freeciv.packetgen.UndefinedException;
import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.Required;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.supporting.DataType;
import org.freeciv.packetgen.enteties.supporting.IntExpression;
import org.freeciv.packetgen.enteties.supporting.NetworkIO;
import org.freeciv.packetgen.enteties.supporting.TerminatedArray;
import com.kvilhaugsvik.javaGenerator.*;
import com.kvilhaugsvik.javaGenerator.Block;
import com.kvilhaugsvik.javaGenerator.expression.MethodCall;
import com.kvilhaugsvik.javaGenerator.typeBridge.From1;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.ABool;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AValue;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AnInt;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.Returnable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.kvilhaugsvik.javaGenerator.util.BuiltIn.*;

public class BitVector extends ClassWriter implements IDependency, IDependency.Maker {
    private static final Var<TargetArray> pFromByte = Var.param(byteArray, "from");
    private static final Var<TargetArray> pFromBits = Var.param(boolArray, "from");
    private static final Var<ABool> pFromBit = Var.param(boolean.class, "setAllTo");
    private static final Var<AnInt> pSize = Var.param(int.class, "sizeInBits");

    private final Collection<Requirement> iRequire;
    private final Requirement iProvide;

    private final boolean knowsSize;
    private final String dataIOType;
    private final TerminatedArray.MaxArraySize maxArraySizeKind;
    private TerminatedArray.TransferArraySize transferArraySizeKind;

    private final Required bvFieldType;

    public BitVector(String name, IntExpression knownSize) {
        this(name, knownSize, TerminatedArray.MaxArraySize.LIMITED_BY_TYPE, TerminatedArray.TransferArraySize.MAX_ARRAY_SIZE, name);
    }

    public BitVector() { // Bit string. Don't convert to string of "1" or "0" just to convert it back later.
        this("BitString", null, TerminatedArray.MaxArraySize.CONSTRUCTOR_PARAM, TerminatedArray.TransferArraySize.SERIALIZED, "char");
    }

    public BitVector(String name) {
        this(name, null, TerminatedArray.MaxArraySize.CONSTRUCTOR_PARAM, TerminatedArray.TransferArraySize.MAX_ARRAY_SIZE, name);
    }

    private BitVector(String name, IntExpression knownSize, TerminatedArray.MaxArraySize maxArraySizeKind, TerminatedArray.TransferArraySize transferArraySizeKind, String reqName) {
        super(ClassKind.CLASS, TargetPackage.from(org.freeciv.types.BitVector.class.getPackage()), Imports.are(),
                "Freeciv C code", Collections.<Annotate>emptyList(), name,
                TargetClass.newKnown(org.freeciv.types.BitVector.class), Collections.<TargetClass>emptyList());
        this.knowsSize = null != knownSize;

        this.maxArraySizeKind = maxArraySizeKind;
        this.transferArraySizeKind = transferArraySizeKind;

        if (knowsSize)
            addClassConstant(Visibility.PUBLIC, int.class, "size", BuiltIn.<AnInt>toCode(knownSize.toString()));
        else
            addPublicObjectConstant(int.class, "size");

        Reference sizeForNotFromData = knowsSize ? getField("size").ref() : pSize.ref();
        {
            List<? extends Var<? extends AValue>> pList = knowsSize ?
                    Arrays.asList(pFromByte) :
                    Arrays.asList(pFromByte, pSize);
            Block constructorBody = new Block(new MethodCall<Returnable>("super", sizeForNotFromData, pFromByte.ref()));
            if (!knowsSize)
                constructorBody.addStatement(getField("size").assign(pSize.ref()));
            addMethod(Method.newPublicConstructor(Comment.no(), pList, constructorBody));
        }
        {
            List<? extends Var<? extends AValue>> pList = Arrays.asList(pFromBits);
            Block constructorBody = new Block(new MethodCall<Returnable>("super", pFromBits.ref()));
            if (!knowsSize)
                constructorBody.addStatement(getField("size").assign(pFromBits.read("length")));
            addMethod(Method.newPublicConstructor(Comment.no(), pList, constructorBody));
        }
        {
            List<? extends Var<? extends AValue>> pList = knowsSize ?
                    Arrays.asList(pFromBit) :
                    Arrays.asList(pFromBit, pSize);
            Block constructorBody = new Block(new MethodCall<Returnable>("super", sizeForNotFromData, pFromBit.ref()));
            if (!knowsSize)
                constructorBody.addStatement(getField("size").assign(pSize.ref()));
            addMethod(Method.newPublicConstructor(Comment.no(), pList, constructorBody));
        }

        this.iRequire = knowsSize ? knownSize.getReqs() : Collections.<Requirement>emptySet();
        this.iProvide = new Requirement(reqName, DataType.class);
        this.dataIOType = knowsSize ? "bitvector" : "bit_string";
        this.bvFieldType = new Requirement(dataIOType +
                "(" + iProvide.getName() + ")", FieldTypeBasic.class);
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
        return Collections.<Requirement>emptyList();
    }

    @Override
    public Required getICanProduceReq() {
        return bvFieldType;
    }

    @Override
    public IDependency produce(Requirement toProduce, IDependency... wasRequired) throws UndefinedException {
        final TargetClass me = super.getAddress().scopeKnown();

        final From1<Typed<AValue>, Typed<AValue>> convertBufferArrayToValue;
        switch (transferArraySizeKind) {
            case MAX_ARRAY_SIZE:
                if (TerminatedArray.MaxArraySize.LIMITED_BY_TYPE.equals(maxArraySizeKind))
                    convertBufferArrayToValue = new From1<Typed<AValue>, Typed<AValue>>() {
                        @Override
                        public Typed<AValue> x(Typed<AValue> bv) {
                            return me.newInstance(bv);
                        }
                    };
                else
                    convertBufferArrayToValue = new From1<Typed<AValue>, Typed<AValue>>() {
                        @Override
                        public Typed<AValue> x(Typed<AValue> bv) {
                            return me.newInstance(bv, Hardcoded.fMaxSize.read("full_array_size"));
                        }
                    };
                break;
            default:
                convertBufferArrayToValue = new From1<Typed<AValue>, Typed<AValue>>() {
                    @Override
                    public Typed<AValue> x(Typed<AValue> bv) {
                        return me.newInstance(bv, Hardcoded.fMaxSize.read("elements_to_transfer"));
                    }
                };
                break;
        }

        final NetworkIO transferSizeSerialize;
        switch (transferArraySizeKind) {
            case SERIALIZED:
                transferSizeSerialize = NetworkIO.witIntAsIntermediate("uint16", 2, "readUnsignedShort", false, "writeShort");
                break;
            default:
                transferSizeSerialize = null;
                break;
        }

        return new TerminatedArray(
                dataIOType,
                iProvide.getName(),
                me,
                null,
                maxArraySizeKind,
                transferArraySizeKind,
                TerminatedArray.byteArray,
                new From1<Typed<AnInt>, Var>() {
                    @Override
                    public Typed<AnInt> x(Var val) {
                        return val.read("size");
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
                me.<AnInt>read("size"),
                transferSizeSerialize,
                knowsSize ?
                        new From1<Typed<AnInt>, Var>() {
                            @Override
                            public Typed<AnInt> x(Var val) {
                                return neededBytes(me.<AnInt>read("size"));
                            }
                        } :
                        new From1<Typed<AnInt>, Var>() {
                            @Override
                            public Typed<AnInt> x(Var arg1) {
                                return neededBytes(arg1.read("size"));
                            }
                        },
                new From1<Typed<AnInt>, Typed<AnInt>>() {
                    @Override
                    public Typed<AnInt> x(Typed<AnInt> val) {
                        return neededBytes(val);
                    }
                },
                Collections.<Method.Helper>emptyList(),
                false
        );
    }
}
