package org.freeciv.packetgen.enteties;

import org.freeciv.packetgen.Hardcoded;
import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.Requirement;
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

import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.*;

public class BitVector extends ClassWriter implements IDependency, FieldTypeBasic.Generator {
    private final Collection<Requirement> iRequire;
    private final Requirement iProvide;

    private final boolean knowsSize;
    private final boolean arrayEater;

    public BitVector(String name, IntExpression bits) {
        super(ClassKind.CLASS, new TargetPackage(org.freeciv.types.BitVector.class.getPackage()), null,
                "Freeciv C code", Collections.<Annotate>emptyList(), name,
                new TargetClass(BitVector.class, true), Collections.<TargetClass>emptyList());

        addClassConstant(Visibility.PUBLIC, "int", "size", bits.toString());
        knowsSize = true;

        Var<TargetArray> pFromByte = Var.param(byteArray, "from");
        Var<TargetArray> pFromBits = Var.param(boolArray, "from");
        Var<ABool> pFromBit = Var.param(boolean.class, "setAllTo");
        addMethod(Method.newPublicConstructor(Comment.no(),
                getName(), Arrays.asList(pFromByte),
                new Block(new MethodCall<Returnable>("super", getField("size").ref(), pFromByte.ref()))));
        addMethod(Method.newPublicConstructor(Comment.no(),
                getName(), Arrays.asList(pFromBits),
                new Block(new MethodCall<Returnable>("super", pFromBits.ref()))));
        addMethod(Method.newPublicConstructor(Comment.no(),
                getName(), Arrays.asList(pFromBit),
                new Block(new MethodCall<Returnable>("super", getField("size").ref(), pFromBit.ref()))));

        iRequire = bits.getReqs();
        iProvide = new Requirement(getName(), Requirement.Kind.AS_JAVA_DATATYPE);
        arrayEater = false;
    }

    public BitVector() { // Bit string. Don't convert to string of "1" or "0" just to convert it back later.
        super(ClassKind.CLASS, new TargetPackage(org.freeciv.types.BitVector.class.getPackage()), null,
                "Freeciv C code", Collections.<Annotate>emptyList(), "BitString",
                new TargetClass(BitVector.class, true), Collections.<TargetClass>emptyList());

        addPublicObjectConstant("int", "size");
        knowsSize = false;

        Var<TargetArray> pFromByte = Var.param(byteArray, "from");
        Var<TargetArray> pFromBits = Var.param(boolArray, "from");
        Var<ABool> pFromBit = Var.param(boolean.class, "setAllTo");
        Var<AnInt> pSize = Var.param(int.class, "size");
        Var<AnInt> pSizeB = Var.param(int.class, "sizeInBits");
        Var<AnInt> size = getField("size");

        addMethod(Method.newPublicConstructor(Comment.no(),
                getName(), Arrays.asList(pFromByte, pSizeB),
                new Block(
                        new MethodCall("super", pSizeB.ref(), pFromByte.ref()),
                        size.assign(pSizeB.ref()))));
        addMethod(Method.newPublicConstructor(Comment.no(),
                getName(), Arrays.asList(pFromBits),
                new Block(
                        new MethodCall<Returnable>("super", pFromBits.ref()),
                        size.assign(pFromBits.read("length")))));
        addMethod(Method.newPublicConstructor(Comment.no(),
                getName(), Arrays.asList(pFromBit, pSize),
                new Block(
                        new MethodCall<Returnable>("super", pSize.ref(), pFromBit.ref()),
                        size.assign(pSize.ref()))));

        iRequire = Collections.<Requirement>emptySet();
        iProvide = new Requirement("char", Requirement.Kind.AS_JAVA_DATATYPE);
        arrayEater = true;
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
    public FieldTypeBasic getBasicFieldTypeOnInput(final NetworkIO io) {
        final TargetClass me = super.getAddress().scopeKnown();
        me.register(new TargetMethod("getAsByteArray"));
        return new TerminatedArray(io.getIFulfillReq().getName(), iProvide.getName(),
                me,
                null,
                knowsSize ?
                        TerminatedArray.MaxArraySize.STORED_IN :
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
                TerminatedArray.lenShouldBeEqual,
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
                                return me.newInstance(bv, Hardcoded.pMaxSize.ref());
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
                Collections.<Method.Helper>emptySet()
        );
    }

    @Override
    public Requirement.Kind needsDataInFormat() {
        return Requirement.Kind.FROM_NETWORK_AMOUNT_OF_BYTES;
    }
}
