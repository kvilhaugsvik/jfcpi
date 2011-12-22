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

package org.freeciv.packetgen;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.freeciv.packetgen.ClassWriter.*;

public class CodeGenTest {
    @Test public void testMethodEverything() {
        String result = (new Method("// comment", Visibility.PUBLIC, Scope.CLASS, "int", "testMethod", "String a",
                "Throwable", "return 5;")).toString();

        assertEquals("Generated source not as expected",
                "\t" + "// comment" + "\n" +
                "\t" + "public static int testMethod(String a) throws Throwable {" + "\n" +
                "\t" + "\t" + "return 5;\n" +
                "\t" + "}" + "\n",
                result);
    }

    @Test public void testMethodNoComment() {
        String result = (new Method(null, Visibility.PUBLIC, Scope.CLASS, "int", "testMethod", "String a", "Throwable",
                "return 5;")).toString();

        assertEquals("Generated source not as expected",
                        "\t" + "public static int testMethod(String a) throws Throwable {" + "\n" +
                        "\t" + "\t" + "return 5;\n" +
                        "\t" + "}" + "\n",
                result);
    }

    @Test public void testMethodNoParams() {
        String result = (new Method("// comment", Visibility.PUBLIC, Scope.CLASS, "int", "testMethod", null,
                "Throwable", "return 5;")).toString();

        assertEquals("Generated source not as expected",
                "\t" + "// comment" + "\n" +
                        "\t" + "public static int testMethod() throws Throwable {" + "\n" +
                        "\t" + "\t" + "return 5;\n" +
                        "\t" + "}" + "\n",
                result);
    }

    @Test public void testMethodEverythingTwoLineComment() {
        String result = (new Method("/** comment\n * more comment\n */",  Visibility.PUBLIC, Scope.CLASS, "int", "testMethod",
                "String a", "Throwable", "return 5;")).toString();

        assertEquals("Generated source not as expected",
                "\t" + "/** comment" + "\n" +
                        "\t" + " * more comment" + "\n" +
                        "\t" + " */" + "\n" +
                        "\t" + "public static int testMethod(String a) throws Throwable {" + "\n" +
                        "\t" + "\t" + "return 5;\n" +
                        "\t" + "}" + "\n",
                result);
    }

    @Test public void testMethodEverythingBodyWithBlanks() {
        String result = (new Method("// comment",  Visibility.PUBLIC, Scope.CLASS, "int", "testMethod", "String a",
                "Throwable", "int a = 5;", "", "return a;")).toString();

        assertEquals("Generated source not as expected",
                "\t" + "// comment" + "\n" +
                        "\t" + "public static int testMethod(String a) throws Throwable {" + "\n" +
                        "\t" + "\t" + "int a = 5;" + "\n" +
                        "\n" +
                        "\t" + "\t" + "return a;" + "\n" +
                        "\t" + "}" + "\n",
                result);
    }

    @Test public void testMethodClassStateReader() {
        Method toTest = Method.newReadClassState(null, "boolean", "isTrue", "return true;");
        assertEquals("Generated Class state reader source code not as espected",
                "\t" + "public static boolean isTrue() {" + "\n" +
                        "\t\t" + "return true;" + "\n" +
                        "\t" + "}" + "\n",
                toTest.toString());
    }

    @Test public void testConstantDeclaration() {
        assertEquals("Generated source not as expected",
                "private static final int integer = 25;",
                (new VariableDeclaration(Visibility.PRIVATE, Scope.CLASS, Modifiable.NO, "int", "integer", "25")).toString());
    }

    @Test public void testObjectConstantDeclaration() {
        assertEquals("Generated source not as expected",
                "private final int integer;",
                (new VariableDeclaration(Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO, "int", "integer", null)).toString());
    }

    @Test public void testStateVarDeclaration() {
        assertEquals("Generated source not as expected",
                "private int integer;",
                (new VariableDeclaration(Visibility.PRIVATE, Scope.OBJECT, Modifiable.YES, "int", "integer", null)).toString());
    }

    @Test public void testPackageImport() {
        assertEquals("Generated source not as expected",
                "import org.freeciv.Connect;",
                ClassWriter.declareImport("org.freeciv.Connect"));
    }

    @Test public void testCommentAutoGenerated() {
        assertEquals("Generated source not as expected",
                "// This code was auto generated from nothing",
                ClassWriter.commentAutoGenerated("nothing"));
    }

    @Test public void testEnumElement() {
        assertEquals("Generated source not as expected",
                "ONE (1, \"one\")",
                EnumElement.newEnumValue("ONE", 1, "\"one\"").toString());
    }

    @Test public void testEnumElementNoToStringButOkEntryPoint() {
        assertEquals("Generated source not as expected",
                "ONE (1, \"ONE\")",
                EnumElement.newEnumValue("ONE", 1).toString());
    }

    @Test public void testEnumElementCommented() {
        assertEquals("Generated source not as expected",
                "ONE (1, \"one\") /* An integer */",
                EnumElement.newEnumValue("An integer", "ONE", 1, "\"one\"").toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnumElementNoName() {
        assertEquals("Generated source not as expected",
                "ONE (1, \"one\")",
                EnumElement.newEnumValue(null, 1, "\"one\"").toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEnumElementNoToString() {
        assertEquals("Generated source not as expected",
                "ONE (1, \"one\")",
                EnumElement.newEnumValue("ONE", 1, null).toString());
    }

    @Test public void testClassWriterEmptyClass() {
        ClassWriter toWrite = new ClassWriter(this.getClass().getPackage(),
                new String[]{"org.freeciv.packet.Packet"},
                "nothing",
                "NameOfClass",
                "Packet");
        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "\n" +
                        "// This code was auto generated from nothing" + "\n" +
                        "public class NameOfClass implements Packet {" + "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterEmptyNoPackage() {
        ClassWriter toWrite = new ClassWriter(null,
                new String[]{"org.freeciv.packet.Packet"},
                "nothing",
                "NameOfClass",
                "Packet");
        assertEquals("Generated source not as expected",
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "\n" +
                        "// This code was auto generated from nothing" + "\n" +
                        "public class NameOfClass implements Packet {" + "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterEmptyNoImports() {
        ClassWriter toWrite = new ClassWriter(this.getClass().getPackage(),
                null,
                "nothing",
                "NameOfClass",
                "Packet");
        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "// This code was auto generated from nothing" + "\n" +
                        "public class NameOfClass implements Packet {" + "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterEmptyNoSourceGiven() {
        ClassWriter toWrite = new ClassWriter(this.getClass().getPackage(),
                new String[]{"org.freeciv.packet.Packet"},
                null,
                "NameOfClass",
                "Packet");
        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "\n" +
                        "public class NameOfClass implements Packet {" + "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterEmptyNoInterfaceGiven() {
        ClassWriter toWrite = new ClassWriter(this.getClass().getPackage(),
                new String[]{"org.freeciv.packet.Packet"},
                "nothing",
                "NameOfClass",
                null);
        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "\n" +
                        "// This code was auto generated from nothing" + "\n" +
                        "public class NameOfClass {" + "}" + "\n",
                toWrite.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testClassWriterEmptyNoNameGiven() {
        ClassWriter toWrite = new ClassWriter(this.getClass().getPackage(),
                new String[]{"org.freeciv.packet.Packet"},
                "nothing",
                null,
                "Packet");
    }

    @Test public void testClassWriterEmptyEnum() {
        ClassWriter toWrite = new ClassWriter(ClassKind.ENUM,
                this.getClass().getPackage(),
                new String[]{"org.freeciv.packet.Packet"},
                "nothing",
                "NameOfClass",
                "Packet");
        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "\n" +
                        "// This code was auto generated from nothing" + "\n" +
                        "public enum NameOfClass implements Packet {" + "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterEnumWithOneElement() {
        ClassWriter toWrite = new ClassWriter(ClassKind.ENUM,
                this.getClass().getPackage(),
                new String[]{"org.freeciv.packet.Packet"},
                "nothing",
                "NameOfClass",
                "Packet");
        toWrite.addEnumerated(null, "ONE", 1, "\"one\"");
        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "\n" +
                        "// This code was auto generated from nothing" + "\n" +
                        "public enum NameOfClass implements Packet {" + "\n" +
                        "\t" + "ONE (1, \"one\");" + "\n" +
                        "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterEnumWithThreeElements() {
        ClassWriter toWrite = new ClassWriter(ClassKind.ENUM,
                this.getClass().getPackage(),
                new String[]{"org.freeciv.packet.Packet"},
                "nothing",
                "NameOfClass",
                "Packet");
        toWrite.addEnumerated(null, "ONE", 1, "\"one\"");
        toWrite.addEnumerated(null, "TWO", 2, "\"two\"");
        toWrite.addEnumerated(null, "THREE", 3, "\"three\"");
        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "\n" +
                        "// This code was auto generated from nothing" + "\n" +
                        "public enum NameOfClass implements Packet {" + "\n" +
                        "\t" + "ONE (1, \"one\")," + "\n" +
                        "\t" + "TWO (2, \"two\")," + "\n" +
                        "\t" + "THREE (3, \"three\");" + "\n" +
                        "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterEnumWithThreeElementsOneIsCommented() {
        ClassWriter toWrite = new ClassWriter(ClassKind.ENUM,
                this.getClass().getPackage(),
                new String[]{"org.freeciv.packet.Packet"},
                "nothing",
                "NameOfClass",
                "Packet");
        toWrite.addEnumerated(null, "ONE", 1, "\"one\"");
        toWrite.addEnumerated("Not a prime number", "TWO", 2, "\"two\"");
        toWrite.addEnumerated(null, "THREE", 3, "\"three\"");
        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "\n" +
                        "// This code was auto generated from nothing" + "\n" +
                        "public enum NameOfClass implements Packet {" + "\n" +
                        "\t" + "ONE (1, \"one\")," + "\n" +
                        "\t" + "TWO (2, \"two\") /* Not a prime number */," + "\n" +
                        "\t" + "THREE (3, \"three\");" + "\n" +
                        "}" + "\n",
                toWrite.toString());
    }

    @Test(expected = AssertionError.class)
    public void testNotEnumAddsEnumerated() {
        ClassWriter toWrite = new ClassWriter(this.getClass().getPackage(),
                new String[]{"org.freeciv.packet.Packet"},
                "nothing",
                "NameOfClass",
                "Packet");
        toWrite.addEnumerated(null, "One", 1, "one");
    }

    @Test public void testClassWriterEmptyTwoBlocksOfImports() {
        ClassWriter toWrite = new ClassWriter(this.getClass().getPackage(),
                new String[]{
                        "org.freeciv.packet.Packet",
                        null,
                        "java.util.List"
                },
                "nothing",
                "NameOfClass",
                "Packet");

        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "\n" +
                        "import java.util.List;" + "\n" +
                        "\n" +
                        "// This code was auto generated from nothing" + "\n" +
                        "public class NameOfClass implements Packet {" + "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterEmptyTwoBlocksOfImportsSeparatedByEmpthy() {
        ClassWriter toWrite = new ClassWriter(this.getClass().getPackage(),
                new String[]{
                        "org.freeciv.packet.Packet",
                        "",
                        "java.util.List"
                },
                "nothing",
                "NameOfClass",
                "Packet");

        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "\n" +
                        "import java.util.List;" + "\n" +
                        "\n" +
                        "// This code was auto generated from nothing" + "\n" +
                        "public class NameOfClass implements Packet {" + "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterStyleConstantNoBlankLineAtTheEndOfTheClassScope() {
        ClassWriter toWrite = new ClassWriter(this.getClass().getPackage(),
                new String[]{"java.util.List"},
                "nothing",
                "NameOfClass",
                "Packet");
        toWrite.addConstant("int", "five", "5");

        String[] lines = toWrite.toString().split("\n");

        assertTrue("Generated source not as expected " + lines[lines.length - 1].trim(),
                lines[lines.length - 1].trim().endsWith("}"));
        assertTrue("Class has blank line before end " + lines[lines.length - 2].trim(),
                !lines[lines.length - 2].trim().isEmpty());
    }

    @Test public void testClassWriterStyleAlwaysEndWithNewLine() {
        ClassWriter toWrite = new ClassWriter(this.getClass().getPackage(),
            new String[]{"java.util.List"},
            "nothing",
            "NameOfClass",
            "Packet");

        assertTrue("File should end with line break", toWrite.toString().endsWith("\n"));
}

    // Tests based on real examples

    @Test public void testPublicConstructorNoExceptions() {
        String result = Method.newPublicConstructor(null,
                "PACKET_CITY_NAME_SUGGESTION_REQ", "Integer unit_id",
                "this.unit_id = new UNIT(unit_id);").toString();

        assertEquals("Generated source not as expected",
                "\t" + "public PACKET_CITY_NAME_SUGGESTION_REQ(Integer unit_id) {" + "\n" +
                        "\t" + "\t" + "this.unit_id = new UNIT(unit_id);" + "\n" +
                        "\t" + "}" + "\n",
                result);
    }

    @Test public void testPublicConstructor() {
        String result = Method.newPublicConstructorWithException("/***" + "\n" +
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
                "}").toString();

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
        String result = Method.newPublicReadObjectState(null, "String", "toString", "return value.toString();").toString();

        assertEquals("Generated source not as expected",
                "\tpublic String toString() {\n" +
                        "\t\treturn value.toString();\n" +
                        "\t}\n",
                result);
    }
}
