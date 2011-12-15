/*
 * Copyright (c) 2011. Sveinung Kvilhaugsvik
 * Portions are data from Freeciv's common/packets.def. Copyright
 * to those (if copyrightable) belong to their respective copyright
 * holders.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package org.freeciv.test;

import org.freeciv.packetgen.DataIO;
import org.junit.Test;

import static org.junit.Assert.*;

public class CodeGenTest {
    @Test public void testFullMethodEverything() {
        String result = DataIO.fullMethod("// comment", "public", "static", "int", "testMethod", "String a",
                "Throwable", "return 5;");

        assertEquals("Generated source not as expected",
                "\t" + "// comment" + "\n" +
                "\t" + "public static int testMethod(String a) throws Throwable {" + "\n" +
                "\t" + "\t" + "return 5;\n" +
                "\t" + "}" + "\n",
                result);
    }

    @Test public void testFullMethodNoComment() {
        String result = DataIO.fullMethod(null, "public", "static", "int", "testMethod", "String a", "Throwable",
                "return 5;");

        assertEquals("Generated source not as expected",
                        "\t" + "public static int testMethod(String a) throws Throwable {" + "\n" +
                        "\t" + "\t" + "return 5;\n" +
                        "\t" + "}" + "\n",
                result);
    }

    @Test public void testFullMethodNoParams() {
        String result = DataIO.fullMethod("// comment", "public", "static", "int", "testMethod", null,
                "Throwable", "return 5;");

        assertEquals("Generated source not as expected",
                "\t" + "// comment" + "\n" +
                        "\t" + "public static int testMethod() throws Throwable {" + "\n" +
                        "\t" + "\t" + "return 5;\n" +
                        "\t" + "}" + "\n",
                result);
    }

    @Test public void testFullMethodEverythingTwoLineComment() {
        String result = DataIO.fullMethod("/** comment\n * more comment\n */", "public", "static", "int", "testMethod",
                "String a", "Throwable", "return 5;");

        assertEquals("Generated source not as expected",
                "\t" + "/** comment" + "\n" +
                        "\t" + " * more comment" + "\n" +
                        "\t" + " */" + "\n" +
                        "\t" + "public static int testMethod(String a) throws Throwable {" + "\n" +
                        "\t" + "\t" + "return 5;\n" +
                        "\t" + "}" + "\n",
                result);
    }

    @Test public void testFullMethodEverythingBodyWithBlanks() {
        String result = DataIO.fullMethod("// comment", "public", "static", "int", "testMethod", "String a",
                "Throwable", "int a = 5;", "", "return a;");

        assertEquals("Generated source not as expected",
                "\t" + "// comment" + "\n" +
                        "\t" + "public static int testMethod(String a) throws Throwable {" + "\n" +
                        "\t" + "\t" + "int a = 5;" + "\n" +
                        "\n" +
                        "\t" + "\t" + "return a;" + "\n" +
                        "\t" + "}" + "\n",
                result);
    }

    // Tests based on real examples

    @Test public void testPublicConstructorNoExceptions() {
        String result = DataIO.publicConstructorNoExceptions(null,
                "PACKET_CITY_NAME_SUGGESTION_REQ", "Integer unit_id",
                "this.unit_id = new UNIT(unit_id);");

        assertEquals("Generated source not as expected",
                "\t" + "public PACKET_CITY_NAME_SUGGESTION_REQ(Integer unit_id) {" + "\n" +
                        "\t" + "\t" + "this.unit_id = new UNIT(unit_id);" + "\n" +
                        "\t" + "}" + "\n",
                result);
    }

    @Test public void testPublicConstructor() {
        String result = DataIO.publicConstructor("/***" + "\n" +
                " * Construct an object from a DataInput" + "\n" +
                " * @param from data stream that is at the start of the package body" + "\n" +
                " * @param headerLen length from header package" + "\n" +
                " * @param packet the number of the packet specified in the header" + "\n" +
                " * @throws IOException if the DataInput has a problem" + "\n" +
                " */",
                "PACKET_CITY_NAME_SUGGESTION_REQ", "DataInput from, int headerLen, int packet", "IOException",
                "this.unit_id = new UNIT(from);",
                "if (getNumber() != packet) {",
                "\tthrow new IOException(\"Tried to create package PACKET_CITY_NAME_SUGGESTION_REQ but packet number was \" + packet);",
                "}",
                "",
                "if (getEncodedSize() != headerLen) {",
                "\tthrow new IOException(\"Package size in header and Java packet not the same. Header: \" + headerLen",
                "\t+ \" Packet: \" + getEncodedSize());",
                "}");

        assertEquals("Generated source not as expected",
                "\t" + "/***" + "\n" +
                        "\t" + " * Construct an object from a DataInput" + "\n" +
                        "\t" + " * @param from data stream that is at the start of the package body" + "\n" +
                        "\t" + " * @param headerLen length from header package" + "\n" +
                        "\t" + " * @param packet the number of the packet specified in the header" + "\n" +
                        "\t" + " * @throws IOException if the DataInput has a problem" + "\n" +
                        "\t" + " */" + "\n" +
                        "\t" + "public PACKET_CITY_NAME_SUGGESTION_REQ(DataInput from, int headerLen, int packet) throws IOException {\n" +
                        "\t" + "\t" + "this.unit_id = new UNIT(from);\n" +
                        "\t" + "\t" + "if (getNumber() != packet) {\n" +
                        "\t" + "\t" + "\t" + "throw new IOException(\"Tried to create package PACKET_CITY_NAME_SUGGESTION_REQ but packet number was \" + packet);\n" +
                        "\t" + "\t" + "}" + "\n" +
                        "\n" +
                        "\t" + "\t" + "if (getEncodedSize() != headerLen) {\n" +
                        "\t" + "\t" + "\t" + "throw new IOException(\"Package size in header and Java packet not the same. Header: \" + headerLen\n" +
                        "\t" + "\t" + "\t" + "+ \" Packet: \" + getEncodedSize());\n" +
                        "\t" + "\t" + "}" + "\n" +
                        "\t" + "}" + "\n",
                result);
    }

    @Test public void testPublicReadObjectState() {
        String result = DataIO.publicReadObjectState(null, "String", "toString", "return value.toString();");

        assertEquals("Generated source not as expected",
                "\tpublic String toString() {\n" +
                        "\t\treturn value.toString();\n" +
                        "\t}\n",
                result);
    }
}
