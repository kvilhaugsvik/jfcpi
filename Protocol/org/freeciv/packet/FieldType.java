package org.freeciv.packet;

import java.io.DataOutput;
import java.io.IOException;

public abstract interface FieldType<Javatype> {
    public abstract void encodeTo(DataOutput to) throws IOException;
    public abstract int encodedLength();
    public abstract Javatype getValue();
}
