package org.freeciv.packet;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract interface Packet {
    public abstract short getNumber();
    public abstract void encodeTo(DataOutputStream to) throws IOException;
    public abstract int getEncodedSize();
}
