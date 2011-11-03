package org.freeciv.packet;

import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;

public class RawPacket implements Packet {
    int size;
    short kind;
    byte[] content;

    public RawPacket(DataInput in, int size, int kind) throws IOException {
        this.size = size;
        this.kind = (short)kind;
        content = new byte[size - 3];
        in.readFully(content);
    }

    public short getNumber() {
        return kind;
    }

    public void encodeTo(DataOutputStream to) throws IOException {
        to.write(content);
    }

    public int getEncodedSize() {
        return size;
    }

    @Override public String toString() {
        String out = "(" + kind + ")\t";
        for (byte part: content) {
            out += ((int)part) + "\t";
        }
        return out + "\n";
    }
}
