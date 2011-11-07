package org.freeciv.packetgen;

public class DataIO {
    public static String writeWriteUInt(int bytenumber) {
        String out = "";
        while (bytenumber >= 1) {
            out += "\t\t" + "to.writeByte((int) ((value >>> ((" + bytenumber + " - 1) * 8)) & 0xFF));\n";
            bytenumber--;
        }
        return out;
    }

    public static String readUIntCode(int bytenumber, String Javatype, String var) {
        String out = var + " = ";
        out += "(" + Javatype.toLowerCase() + ")";
        while (bytenumber >= 1) {
            out += "\t\t" + "(from.readUnsignedByte() << 8 * (" + bytenumber + " - 1)) " +
                    (bytenumber > 1 ? "+" : ";") + "\n";
            bytenumber--;
        }

        return out;
    }
}
