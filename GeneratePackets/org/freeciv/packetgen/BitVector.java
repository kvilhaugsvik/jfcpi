package org.freeciv.packetgen;

import java.util.Collection;

public class BitVector extends ClassWriter implements IDependency {
    private final Collection<Requirement> iRequire;
    private final Requirement iProvide;
    public BitVector(String name, IntExpression bits) {
        super(org.freeciv.types.BitVector.class.getPackage(), new String[]{"org.freeciv.packet.Constants"},
                "Freeciv C code", name, "BitVector", null);

        addClassConstant("int", "size", bits.toString());

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
}
