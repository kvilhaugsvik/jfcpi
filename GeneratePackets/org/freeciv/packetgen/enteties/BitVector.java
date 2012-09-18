package org.freeciv.packetgen.enteties;

import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.supporting.IntExpression;
import org.freeciv.packetgen.enteties.supporting.NetworkIO;
import org.freeciv.packetgen.javaGenerator.*;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.creators.ExprFrom1;
import org.freeciv.packetgen.javaGenerator.expression.creators.ExprFrom2;
import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AnInt;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.Returnable;

import static org.freeciv.packetgen.Hardcoded.fMaxSize;
import static org.freeciv.packetgen.Hardcoded.pMaxSize;

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

        Var pFromByte = Var.param(byteArray, "from");
        Var pFromBits = Var.param(boolArray, "from");
        Var pFromBit = Var.param(boolean.class, "setAllTo");
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

        Var pFromByte = Var.param(byteArray, "from");
        Var pFromBits = Var.param(boolArray, "from");
        Var pFromBit = Var.param(boolean.class, "setAllTo");
        Var pSize = Var.param(int.class, "size");
        Var pSizeB = Var.param(int.class, "sizeInBits");
        Var size = getField("size");

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

    @Override
    public FieldTypeBasic getBasicFieldTypeOnInput(final NetworkIO io) {
        final String bvName = iProvide.getName();
        final String[] size = new String[]{"1 + (", " - 1) / 8"};
        final String realBitVector =  bvName + ".size";
        return new FieldTypeBasic(io.getIFulfillReq().getName(), bvName, new TargetClass(getName()),
                new ExprFrom1<Block, Var>() {
                    @Override
                    public Block x(Var arg1) {
                        Block body = new Block(arg1.assign(asAValue("value")));
                        if (arrayEater)
                            body.addStatement(fMaxSize.assign(pMaxSize.ref()));
                        return body;
                    }
                },
                new ExprFrom2<Block, Var, Var>() {
                    @Override
                    public Block x(Var to, Var from) {
                        Block body = knowsSize ?
                                io.getRead(size[0] + realBitVector + size[1], null,
                                        asAValue("this.value = new " + getName() + "(innBuffer)")) :
                                io.getRead(size[0] + "size" + size[1],
                                        asAValue("int size = from.readUnsignedShort()"),
                                        asAValue("this.value = new " + getName() + "(innBuffer" + ", size)"));
                        if (arrayEater)
                            body.addStatement(fMaxSize.assign(pMaxSize.ref()));
                        return body;
                    }
                },
                new ExprFrom2<Block, Var, Var>() {
                    @Override
                    public Block x(Var val, Var to) {
                        Block out = new Block();
                        if (!knowsSize)
                            out.addStatement(to.call("writeShort", val.read("size")));
                        out.addStatement(asAValue(io.getWrite("this.value.getAsByteArray()")));
                        return out;
                    }
                },
                (knowsSize ?
                        new ExprFrom1<Typed<AnInt>, Var>() {
                            @Override
                            public Typed<AnInt> x(Var value) {
                                return asAnInt(size[0] + realBitVector + size[1]);
                            }
                        } :
                        new ExprFrom1<Typed<AnInt>, Var>() {
                            @Override
                            public Typed<AnInt> x(Var value) {
                                return asAnInt("2 + " + size[0] + "this." + "value" + ".size" + size[1]);
                            }
                        }),
                TO_STRING_OBJECT,
                                  arrayEater,
                                  Arrays.asList(iProvide));
    }

    @Override
    public Requirement.Kind needsDataInFormat() {
        return Requirement.Kind.FROM_NETWORK_AMOUNT_OF_BYTES;
    }
}
