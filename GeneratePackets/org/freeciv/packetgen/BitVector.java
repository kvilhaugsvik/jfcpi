package org.freeciv.packetgen;

import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.javaGenerator.ClassWriter;

import java.util.*;

public class BitVector extends ClassWriter implements IDependency, FieldTypeBasic.Generator {
    private final Collection<Requirement> iRequire;
    private final Requirement iProvide;

    public BitVector(String name, IntExpression bits) {
        super(ClassKind.CLASS, new TargetPackage(org.freeciv.types.BitVector.class.getPackage()), null,
              "Freeciv C code", name, "BitVector", null);

        addClassConstant(Visibility.PUBLIC, "int", "size", bits.toString());

        addConstructorPublic("", getName(), "byte[] from", "super(size, " + "from);");
        addConstructorPublic("", getName(), "boolean[] from", "super(from);");
        addConstructorPublic("", getName(), "boolean setAllTo", "super(size, " + "setAllTo);");

        iRequire = bits.getReqs();
        iProvide = new Requirement(getName(), Requirement.Kind.AS_JAVA_DATATYPE);
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
        final String size = "1 + (" + bvName + ".size" + " - 1) / 8";
        return new FieldTypeBasic(io.getIFulfillReq().getName(), bvName,
                                  bvName,
                                  new String[]{"this.value = value;"},
                                  io.getRead(size)
                                          + "value = new " + bvName + "(innBuffer);",
                                  io.getWrite("value.getAsByteArray()"),
                                  "return " + size + ";",
                                  false,
                                  Arrays.asList(iProvide));
    }

    @Override
    public Requirement.Kind needsDataInFormat() {
        return Requirement.Kind.FROM_NETWORK_AMOUNT_OF_BYTES;
    }
}
