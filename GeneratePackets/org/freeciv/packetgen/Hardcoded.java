package org.freeciv.packetgen;

import java.util.HashMap;

//TODO: Move data to file
public class Hardcoded {
    private HashMap<String, JavaSrc> data = new HashMap<String, JavaSrc>();
    private static Hardcoded instance;

    private Hardcoded() {
        for (JavaSrc src: new JavaSrc[]{
            new JavaSrc("uint32(int)",
                    "Long",
                    DataIO.readUIntCode(4, "Long", "value"),
                    DataIO.writeWriteUInt(4),
                    "return 4;"),
            new JavaSrc("string(char)",
                    "String",
                    "StringBuffer buf = new StringBuffer();\n" +
                            "\t\tbyte letter = from.readByte();\n" +
                            "\t\twhile (letter != 0) {\n" +
                            "\t\t\tbuf.append((char)letter);\n" +
                            "\t\t\tletter = from.readByte();\n" +
                            "\t\t}\n" +
                            "\t\tvalue = buf.toString();",
                    "to.writeBytes(" + "value" + ");\n" +
                        "\t\t" + "to.writeByte(0);" + "\n",
                    "return " + "value" + ".length() + 1;\n"),
            new JavaSrc("bool8(bool)",
                    "Boolean",
                    "value = from.readBoolean();" + "\n",
                    "to.writeBoolean(value);" + "\n",
                    "return 1;"),
            new JavaSrc("sint16(int)",
                    "Short",
                    "value = from.readShort();" + "\n",
                    "to.writeShort(value);" + "\n",
                    "return 2;")
        }) {
            data.put(src.getCSrc(), src);
        }
    }

    public static JavaSrc getJTypeFor(String src) {
        if (null == instance) {
            instance = new Hardcoded();
        }
        return instance.data.get(src);
    }
}
