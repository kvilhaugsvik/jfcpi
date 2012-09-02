package org.freeciv.packetgen.enteties;

import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.supporting.IntExpression;
import org.freeciv.packetgen.enteties.supporting.NetworkIO;
import org.freeciv.packetgen.javaGenerator.*;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class BitVector extends ClassWriter implements IDependency, FieldTypeBasic.Generator {
    private final Collection<Requirement> iRequire;
    private final Requirement iProvide;

    private final boolean knowsSize;
    private final boolean arrayEater;

    public BitVector(String name, IntExpression bits) {
        super(ClassKind.CLASS, new TargetPackage(org.freeciv.types.BitVector.class.getPackage()), null,
              "Freeciv C code", name, "BitVector", null);

        addClassConstant(Visibility.PUBLIC, "int", "size", bits.toString());
        knowsSize = true;

        addConstructorPublic("", "byte[] from", Block.fromStrings("super(size, " + "from)"));
        addConstructorPublic("", "boolean[] from", Block.fromStrings("super(from)"));
        addConstructorPublic("", "boolean setAllTo", Block.fromStrings("super(size, " + "setAllTo)"));

        iRequire = bits.getReqs();
        iProvide = new Requirement(getName(), Requirement.Kind.AS_JAVA_DATATYPE);
        arrayEater = false;
    }

    public BitVector() { // Bit string. Don't convert to string of "1" or "0" just to convert it back later.
        super(ClassKind.CLASS, new TargetPackage(org.freeciv.types.BitVector.class.getPackage()), null,
                "Freeciv C code", "BitString", "BitVector", null);

        addPublicObjectConstant("int", "size");
        knowsSize = false;

        Var size = getField("size");
        addConstructorPublic("", "byte[] from, int sizeInBits", new Block(
                BuiltIn.asAValue("super(sizeInBits, " + "from)"),
                getField("size").assign(BuiltIn.asAValue("sizeInBits"))));
        addConstructorPublic("", "boolean[] from", new Block(
                BuiltIn.asAValue("super(from)"),
                size.assign(BuiltIn.asAValue("from.length"))));
        addConstructorPublic("", "boolean setAllTo, int size", new Block(
                BuiltIn.asAValue("super(size, " + "setAllTo)"),
                size.assign(BuiltIn.asAValue("size"))));

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
    public FieldTypeBasic getBasicFieldTypeOnInput(NetworkIO io) {
        final String bvName = iProvide.getName();
        final String[] size = new String[]{"1 + (", " - 1) / 8"};
        final String realBitVector =  bvName + ".size";
        return new FieldTypeBasic(io.getIFulfillReq().getName(), bvName,
                                  getName(),
                                  (knowsSize ?
                                          io.getRead(size[0] + realBitVector  + size[1]) :
                                          "int size = from.readUnsignedShort();" + "\n" +
                                                  io.getRead(size[0] + "size"  + size[1]))
                                          + "this.value = new " + getName() + "(innBuffer" + (knowsSize ?
                                          "" :
                                          ", size") + ");",
                                  (knowsSize ?
                                          "":
                                          "to.writeShort(" + "this." + "value" + ".size" + ");\n")
                                          + io.getWrite("this.value.getAsByteArray()"),
                                  "return " + (knowsSize ?
                                          size[0] + realBitVector + size[1] :
                                          "2 + " + size[0] + "this." + "value" + ".size" + size[1]) + ";",
                                  arrayEater,
                                  Arrays.asList(iProvide));
    }

    @Override
    public Requirement.Kind needsDataInFormat() {
        return Requirement.Kind.FROM_NETWORK_AMOUNT_OF_BYTES;
    }
}
