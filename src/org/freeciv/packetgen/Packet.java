package org.freeciv.packetgen;

public class Packet {
    private String name;
    private int number;
    private Field[] fields;

    public Packet(String name, int number, Field... fields) {
        this.name = name;
        this.number = number;
        this.fields = fields;
    }

    public String toString() {
        String declarations = "";
        String arglist = "";
        String constructorBody = "";
        String javatypearglist = "";
        String constructorBodyJ = "";
        String encodeFields = "";
        String encodeFieldsLen = "";
        if (fields.length > 0) {
        encodeFields = "\n" +
                "\t\t// body\n";
            for (Field field: fields) {
                declarations += "\t" + field.getType() + " " + field.getVariableName() + ";\n";
                constructorBody += "\t\t" + "this." + field.getVariableName() + " = " + field.getVariableName() + ";\n";
                constructorBodyJ += "\t\t" + "this." + field.getVariableName() + " = " +
                        "new " + field.getType() + "(" + field.getVariableName() + ")" + ";\n";
                arglist += field.getType() + " " + field.getVariableName() + ", ";
                javatypearglist += field.getJType() + " " + field.getVariableName() + ", ";
                encodeFields += "\t\t" + field.getVariableName() + ".encodeTo(to);\n";
                encodeFieldsLen += "\t\t\t+ " + field.getVariableName() + ".encodedLength()\n";
            }
            arglist = arglist.substring(0, arglist.length() - 2);
            javatypearglist = javatypearglist.substring(0, javatypearglist.length() - 2);
            encodeFieldsLen = "\n\t\t\t" + encodeFieldsLen.trim();
            declarations += "\n";
        }

        return "package org.freeciv.packet;\n" +
                "\n" +
                "import java.io.DataOutputStream;\n" +
                "import java.io.IOException;" + "\n" +
                "\n" +
                "// This code was auto generated from Freeciv's protocol definition" + "\n" +
                "public class " + name + " implements Packet {" + "\n" +
                "\t" + "private static final int number = " + number + ";" + "\n" +
                "\n" +
                declarations +
                "\t" + "public " + name + "(" + arglist + ") {\n" +
                constructorBody +
                "\t" + "}" + "\n" +
                ((fields.length > 0) ?
                        "\n" +
                        "\t" + "public " + name + "(" + javatypearglist + ") {\n" +
                        constructorBodyJ +
                        "\t" + "}" + "\n" :
                        "") +
                "\n" +
                "\t" + "public short getNumber() {\n" +
                "\t" + "\treturn number;\n" +
                "\t" + "}" + "\n" +
                "\n" +
                "\t" + "public void encodeTo(DataOutputStream to) throws IOException {\n" +
                "\t\t// header\n" +
                "\t\t// length is 2 unsigned bytes\n" +
                "\t\tto.writeChar(getEncodedSize());\n" +
                "\t\t// type\n" +
                "\t\tto.writeByte(number);\n" +
                encodeFields +
                "\t}" + "\n" +
                "\n" +
                "\tpublic int getEncodedSize() {\n" +
                "\t\treturn 3" + encodeFieldsLen + ";\n" +
                "\t}\n" +
                "}";
    }
}
