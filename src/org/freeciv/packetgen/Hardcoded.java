package org.freeciv.packetgen;

//TODO: Move data to file
public class Hardcoded {
        public static final JavaSrc[] out = {
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
                        "return " + "value" + ".length() + 1;\n")
        };
}
