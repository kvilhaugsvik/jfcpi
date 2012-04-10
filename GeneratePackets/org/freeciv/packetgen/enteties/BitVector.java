package org.freeciv.packetgen.enteties;

import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.supporting.IntExpression;
import org.freeciv.packetgen.enteties.supporting.NetworkIO;
import org.freeciv.packetgen.javaGenerator.ClassWriter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class BitVector extends ClassWriter implements IDependency, FieldTypeBasic.Generator {
    private final Collection<Requirement> iRequire;
    private final Requirement iProvide;

    private final boolean knowsSize;

    public BitVector(String name, IntExpression bits) {
        super(ClassKind.CLASS, new TargetPackage(org.freeciv.types.BitVector.class.getPackage()), null,
              "Freeciv C code", name, "BitVector", null);

        addClassConstant(Visibility.PUBLIC, "int", "size", bits.toString());
        knowsSize = true;

        addConstructorPublic("", getName(), "byte[] from", "super(size, " + "from);");
        addConstructorPublic("", getName(), "boolean[] from", "super(from);");
        addConstructorPublic("", getName(), "boolean setAllTo", "super(size, " + "setAllTo);");

        iRequire = bits.getReqs();
        iProvide = new Requirement(getName(), Requirement.Kind.AS_JAVA_DATATYPE);
    }

    public BitVector() { // Bit string. Don't convert to string of "1" or "0" just to convert it back later.
        super(ClassKind.CLASS, new TargetPackage(org.freeciv.types.BitVector.class.getPackage()), null,
                "Freeciv C code", "BitString", "BitVector", null);

        addPublicObjectConstant("int", "size");
        knowsSize = false;

        final String setSize = "this.size = ";
        addConstructorPublic("", getName(), "byte[] from, int sizeInBits",
                "super(sizeInBits, " + "from);", setSize + "sizeInBits;");
        addConstructorPublic("", getName(), "boolean[] from", "super(from);",
                setSize + "from.length;");
        addConstructorPublic("", getName(), "boolean setAllTo, int size",
                "super(size, " + "setAllTo);", setSize + "size;");

        iRequire = Collections.<Requirement>emptySet();
        iProvide = new Requirement("char", Requirement.Kind.AS_JAVA_DATATYPE);
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
                                  new String[]{"this.value = value;"},
                                  (knowsSize ?
                                          io.getRead(size[0] + realBitVector  + size[1]) :
                                          "int size = from.readUnsignedShort();" + "\n" +
                                                  io.getRead(size[0] + "size"  + size[1]))
                                          + "value = new " + getName() + "(innBuffer" + (knowsSize ?
                                          "" :
                                          ", size") + ");",
                                  (knowsSize ?
                                          "":
                                          "to.writeShort(" + "value" + ".size" + ");\n")
                                          + io.getWrite("value.getAsByteArray()"),
                                  "return " + (knowsSize ?
                                          size[0] + realBitVector + size[1] :
                                          "2 + " + size[0] + "value" + ".size" + size[1]) + ";",
                                  false,
                                  Arrays.asList(iProvide));
    }

    @Override
    public Requirement.Kind needsDataInFormat() {
        return Requirement.Kind.FROM_NETWORK_AMOUNT_OF_BYTES;
    }
}
