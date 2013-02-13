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
    private final Collection<Requirement> iRequire;
    private final Requirement iProvide;

    private final boolean knowsSize;

    private final Required bvFieldType;

    public BitVector(String name, IntExpression bits) {
        this(name, bits, true);
    }

    public BitVector() { // Bit string. Don't convert to string of "1" or "0" just to convert it back later.
        this("BitString", null, false);
    }

    private BitVector(String name, IntExpression sizeInBits, boolean knowsSize) {
        super(ClassKind.CLASS, TargetPackage.from(org.freeciv.types.BitVector.class.getPackage()), Imports.are(),
                "Freeciv C code", Collections.<Annotate>emptyList(), name,
                TargetClass.newKnown(org.freeciv.types.BitVector.class), Collections.<TargetClass>emptyList());

        if (knowsSize)
            addClassConstant(Visibility.PUBLIC, int.class, "size", BuiltIn.<AnInt>toCode(sizeInBits.toString()));
        else
            addPublicObjectConstant(int.class, "size");

        this.knowsSize = knowsSize;

        Var<TargetArray> pFromByte = Var.param(byteArray, "from");
        Var<TargetArray> pFromBits = Var.param(boolArray, "from");
        Var<ABool> pFromBit = Var.param(boolean.class, "setAllTo");

        Var<AnInt> pSize = Var.param(int.class, "sizeInBits");

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

        this.iRequire = knowsSize ? sizeInBits.getReqs() : Collections.<Requirement>emptySet();
        this.iProvide = new Requirement(knowsSize ? getName() : "char", DataType.class);
        this.bvFieldType = new Requirement((knowsSize ? "bitvector" : "bit_string") +
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
        return new TerminatedArray(
                knowsSize ? "bitvector" : "bit_string",
                iProvide.getName(),
                me,
                null,
                knowsSize ?
                        TerminatedArray.MaxArraySize.LIMITED_BY_TYPE :
                        TerminatedArray.MaxArraySize.CONSTRUCTOR_PARAM,
                knowsSize ?
                        TerminatedArray.TransferArraySize.MAX_ARRAY_SIZE :
                        TerminatedArray.TransferArraySize.SERIALIZED,
                TerminatedArray.byteArray,
                new From1<Typed<AnInt>, Var>() {
                    @Override
                    public Typed<AnInt> x(Var val) {
                        return val.read("size");
                    }
                },
                TerminatedArray.neverAnythingAfter,
                new From1<Typed<AValue>, Var>() {
                    @Override
                    public Typed<AValue> x(Var buffer) {
                        return buffer.ref().<Returnable>call("getAsByteArray");
                    }
                },
                knowsSize ?
                        new From1<Typed<AValue>, Typed<AValue>>() {
                            @Override
                            public Typed<AValue> x(Typed<AValue> bv) {
                                return me.newInstance(bv);
                            }
                        } :
                        new From1<Typed<AValue>, Typed<AValue>>() {
                            @Override
                            public Typed<AValue> x(Typed<AValue> bv) {
                                return me.newInstance(bv, Hardcoded.fMaxSize.read("elements_to_transfer"));
                            }
                        },
                null,
                TerminatedArray.readByte,
                BuiltIn.TO_STRING_OBJECT,
                Arrays.asList(iProvide),
                me.<AnInt>read("size"),
                knowsSize ?
                        null :
                        NetworkIO.witIntAsIntermediate("uint16", 2, "readUnsignedShort", false, "writeShort"),
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
