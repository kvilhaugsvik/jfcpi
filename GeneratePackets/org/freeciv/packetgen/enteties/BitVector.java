package org.freeciv.packetgen.enteties;

import org.freeciv.packetgen.Hardcoded;
import org.freeciv.packetgen.UndefinedException;
import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.Required;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.supporting.DataType;
import org.freeciv.packetgen.enteties.supporting.IntExpression;
import org.freeciv.packetgen.enteties.supporting.NetworkIO;
import org.freeciv.packetgen.enteties.supporting.TerminatedArray;
import org.freeciv.packetgen.javaGenerator.*;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.creators.ExprFrom1;
import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.ABool;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AnInt;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.Returnable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.*;

public class BitVector extends ClassWriter implements IDependency, IDependency.Maker {
    private final Collection<Requirement> iRequire;
    private final Requirement iProvide;

    private final boolean knowsSize;
    private final boolean arrayEater;

    private final Required bvFieldType;

    public BitVector(String name, IntExpression bits) {
        super(ClassKind.CLASS, TargetPackage.from(org.freeciv.types.BitVector.class.getPackage()), null,
                "Freeciv C code", Collections.<Annotate>emptyList(), name,
                TargetClass.newKnown(org.freeciv.types.BitVector.class), Collections.<TargetClass>emptyList());

        addClassConstant(Visibility.PUBLIC, int.class, "size", BuiltIn.<AnInt>toCode(bits.toString()));
        knowsSize = true;

        Var<TargetArray> pFromByte = Var.param(byteArray, "from");
        Var<TargetArray> pFromBits = Var.param(boolArray, "from");
        Var<ABool> pFromBit = Var.param(boolean.class, "setAllTo");
        addMethod(Method.newPublicConstructor(Comment.no(),
                Arrays.asList(pFromByte),
                new Block(new MethodCall<Returnable>("super", getField("size").ref(), pFromByte.ref()))));
        addMethod(Method.newPublicConstructor(Comment.no(),
                Arrays.asList(pFromBits),
                new Block(new MethodCall<Returnable>("super", pFromBits.ref()))));
        addMethod(Method.newPublicConstructor(Comment.no(),
                Arrays.asList(pFromBit),
                new Block(new MethodCall<Returnable>("super", getField("size").ref(), pFromBit.ref()))));

        iRequire = bits.getReqs();
        iProvide = new Requirement(getName(), DataType.class);
        arrayEater = false;
        bvFieldType = new Requirement((knowsSize ? "bitvector" : "bit_string") +
                "(" + iProvide.getName() + ")", FieldTypeBasic.class);
    }

    public BitVector() { // Bit string. Don't convert to string of "1" or "0" just to convert it back later.
        super(ClassKind.CLASS, TargetPackage.from(org.freeciv.types.BitVector.class.getPackage()), null,
                "Freeciv C code", Collections.<Annotate>emptyList(), "BitString",
                TargetClass.newKnown(org.freeciv.types.BitVector.class), Collections.<TargetClass>emptyList());

        addPublicObjectConstant("int", "size");
        knowsSize = false;

        Var<TargetArray> pFromByte = Var.param(byteArray, "from");
        Var<TargetArray> pFromBits = Var.param(boolArray, "from");
        Var<ABool> pFromBit = Var.param(boolean.class, "setAllTo");
        Var<AnInt> pSize = Var.param(int.class, "size");
        Var<AnInt> pSizeB = Var.param(int.class, "sizeInBits");
        Var<AnInt> size = getField("size");

        addMethod(Method.newPublicConstructor(Comment.no(),
                Arrays.asList(pFromByte, pSizeB),
                new Block(
                        new MethodCall("super", pSizeB.ref(), pFromByte.ref()),
                        size.assign(pSizeB.ref()))));
        addMethod(Method.newPublicConstructor(Comment.no(),
                Arrays.asList(pFromBits),
                new Block(
                        new MethodCall<Returnable>("super", pFromBits.ref()),
                        size.assign(pFromBits.read("length")))));
        addMethod(Method.newPublicConstructor(Comment.no(),
                Arrays.asList(pFromBit, pSize),
                new Block(
                        new MethodCall<Returnable>("super", pSize.ref(), pFromBit.ref()),
                        size.assign(pSize.ref()))));

        iRequire = Collections.<Requirement>emptySet();
        iProvide = new Requirement("char", DataType.class);
        arrayEater = true;
        bvFieldType = new Requirement((knowsSize ? "bitvector" : "bit_string") +
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
        me.register(new TargetMethod(me, "getAsByteArray", TargetClass.fromClass(byte[].class), TargetMethod.Called.DYNAMIC));
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
                new ExprFrom1<Typed<AnInt>, Var>() {
                    @Override
                    public Typed<AnInt> x(Var val) {
                        return val.read("size");
                    }
                },
                TerminatedArray.neverAnythingAfter,
                new ExprFrom1<Typed<AValue>, Var>() {
                    @Override
                    public Typed<AValue> x(Var buffer) {
                        return buffer.call("getAsByteArray");
                    }
                },
                knowsSize ?
                        new ExprFrom1<Typed<AValue>, Typed<AValue>>() {
                            @Override
                            public Typed<AValue> x(Typed<AValue> bv) {
                                return me.newInstance(bv);
                            }
                        } :
                        new ExprFrom1<Typed<AValue>, Typed<AValue>>() {
                            @Override
                            public Typed<AValue> x(Typed<AValue> bv) {
                                return me.newInstance(bv, Hardcoded.pLimits.read("elements_to_transfer"));
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
                        new ExprFrom1<Typed<AnInt>, Var>() {
                            @Override
                            public Typed<AnInt> x(Var val) {
                                return neededBytes(me.<AnInt>read("size"));
                            }
                        } :
                        new ExprFrom1<Typed<AnInt>, Var>() {
                            @Override
                            public Typed<AnInt> x(Var arg1) {
                                return neededBytes(arg1.read("size"));
                            }
                        },
                new ExprFrom1<Typed<AnInt>, Typed<AnInt>>() {
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
