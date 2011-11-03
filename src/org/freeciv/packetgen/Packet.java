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

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    public String toString() {
        String declarations = "";
        String arglist = "";
        String constructorBody = "";
        String javatypearglist = "";
        String constructorBodyJ = "";
        String constructorBodyStream = "";
        String encodeFields = "";
        String encodeFieldsLen = "";
        String getFields = "";
        String getFieldValues = "";
        String getToString = "";
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
                constructorBodyStream += "\t\t" + "this." + field.getVariableName() + " = " +
                        "new " + field.getType() + "(from);\n";
                getFieldValues += "\n" + "\t" + "public " + field.getJType() + " get"
                        + field.getVariableName().substring(0, 1).toUpperCase() + field.getVariableName().substring(1)
                        + "Value() {" + "\n" +
                        "\t\t" + "return " + field.getVariableName() + ".getValue();" + "\n" +
                        "\t" + "}" + "\n";
                getFields += "\n" + "\t" + "public " + field.getType() + " get"
                        + field.getVariableName().substring(0, 1).toUpperCase() + field.getVariableName().substring(1)
                        + "() {" + "\n" +
                        "\t\t" + "return " + field.getVariableName() + ";" + "\n" +
                        "\t" + "}" + "\n";
                getToString += "\t\t" + "out += \"\\n\\t" + field.getVariableName() +
                        " = \" + " + field.getVariableName() + ".getValue();" + "\n";
            }
            arglist = arglist.substring(0, arglist.length() - 2);
            javatypearglist = javatypearglist.substring(0, javatypearglist.length() - 2);
            encodeFieldsLen = "\n\t\t\t" + encodeFieldsLen.trim();
            declarations += "\n";
        }

        return "package org.freeciv.packet;\n" +
                "\n" +
                "import java.io.DataInput;\n" +
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
                "\t/***\n" +
                "\t * Construct an object from a DataInput\n" +
                "\t * @param from data stream that is at the start of the package body  \n" +
                "\t * @param headerLen length from header package\n" +
                "\t * @param packet the number of the packet specified in the header\n" +
                "\t * @throws IOException if the DataInput has a problem\n" +
                "\t */" + "\n" +
                "\t" + "public " + name + "(DataInput from, int headerLen, int packet) throws IOException {\n" +
                constructorBodyStream +
                "\t\t" + "if (getNumber() != packet) {\n" +
                "\t\t\t" + "throw new IOException(\"Tried to create package " +
                name + " but packet number was \" + packet);\n" +
                "\t\t" + "}" + "\n" +
                "\n" +
                "\t\t" + "if (getEncodedSize() != headerLen) {\n" +
                "\t\t\t" + "throw new IOException(\"Package size in header and Java packet not the same. Header: \"" +
                " + headerLen\n" +
                "\t\t\t" + "+ \" Packet: \" + getEncodedSize());\n" +
                "\t\t" + "}" + "\n" +
                "\t" + "}" + "\n" +
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
                "\tpublic String toString() {\n" +
                "\t\t" + "String out = \"" + name + "\" + \"(\" + number + \")\";" + "\n" +
                getToString + "\n" +
                "\t\t" + "return out + \"\\n\";" + "\n" +
                "\t}\n" +
                getFields +
                getFieldValues +
                "}";
    }
}
