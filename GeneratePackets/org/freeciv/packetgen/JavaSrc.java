package org.freeciv.packetgen;

public class JavaSrc {
    String CSrc;
    String JavaType;
    String Decode;
    String encode, EncodedSize;

    public JavaSrc(String CSrc, String javaType, String decode, String encode, String encodedSize) {
        this.CSrc = CSrc;
        JavaType = javaType;
        Decode = decode;
        this.encode = encode;
        EncodedSize = encodedSize;
    }

    public String getCSrc() {
        return CSrc;
    }

    public String toString(String name) {
        return "package org.freeciv.packet;" + "\n" +
                "\n" +
                "import java.io.DataInput;" + "\n" +
                "import java.io.DataOutput;" + "\n" +
                "import java.io.IOException;" + "\n" +
                "\n" +
                "// This code was auto generated from Freeciv's protocol definition" + "\n" +
                "public class " + name + " implements FieldType<" + JavaType + "> {" + "\n" +
                "\t" + "private " + JavaType + " value;" + "\n" +
                "\n" +
                "\t" + "public " + name + "(" + JavaType + " value) {" + "\n" +
                "\t" + "\t" + "this.value = value;" + "\n" +
                "\t" + "}" + "\n" +
                "\n" +
                "\t" + "public " + name + "(DataInput from) throws IOException {" + "\n" +
                "\t" + "\t" + Decode +
                "\t" + "}" + "\n" +
                "\n" +
                "\t" + "public void encodeTo(DataOutput to) throws IOException {" + "\n" +
                "\t" + "\t" + encode +
                "\t" + "}" + "\n" +
                "\n" +
                "\t" + "public int encodedLength() {" + "\n" +
                "\t" + "\t" + EncodedSize +
                "\t" + "}" + "\n" +
                "\n" +
                "\t" + "public " + JavaType + " getValue() {" + "\n" +
                "\t" + "\t" + "return value;" + "\n" +
                "\t" + "}" + "\n" +
                "}";
    }
}
