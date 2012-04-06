/*
 * Copyright (c) 2011, 2012. Sveinung Kvilhaugsvik
 * Portions are data from Freeciv's common/packets.def. Copyright
 * of those (if copyrightable) belong to their respective copyright
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

package org.freeciv.packetgen.javaGenerator;

import org.freeciv.packet.fieldtype.FieldType;
import org.junit.Test;

import static org.freeciv.packetgen.javaGenerator.ClassWriter.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    @Test public void testMethodManyLevelsOfIndention() {
        String result = (new Method("// comment", Visibility.PUBLIC, Scope.CLASS, "int", "testMethod", null, null,
                "while(true) {",
                "while(true) {",
                "while(true) {",
                "while(true) {",
                "return 5;",
                "}",
                "}",
                "}",
                "}")).toString();

        assertEquals("Generated source not as expected",
                "\t" + "// comment" + "\n" +
                        "\t" + "public static int testMethod() {" + "\n" +
                        "\t" + "\t" + "while(true) {" + "\n" +
                        "\t" + "\t" + "\t" + "while(true) {" + "\n" +
                        "\t" + "\t" + "\t" + "\t" + "while(true) {" + "\n" +
                        "\t" + "\t" + "\t" + "\t" + "\t" + "while(true) {" + "\n" +
                        "\t" + "\t" + "\t" + "\t" + "\t" + "\t" + "return 5;\n" +
                        "\t" + "\t" + "\t" + "\t" + "\t" + "}" + "\n" +
                        "\t" + "\t" + "\t" + "\t" + "}" + "\n" +
                        "\t" + "\t" + "\t" + "}" + "\n" +
                        "\t" + "\t" + "}" + "\n" +
                        "\t" + "}" + "\n",
                result);
    }

    @Test public void testMethodManyLevelsOfIndentionScopeEndTogether() {
        String result = (new Method("// comment", Visibility.PUBLIC, Scope.CLASS, "int", "testMethod", null, null,
                "while(true) {",
                "while(true) {",
                "while(true) {",
                "while(true) {",
                "return 5;",
                "}}}}")).toString();

        assertEquals("Generated source not as expected",
                "\t" + "// comment" + "\n" +
                        "\t" + "public static int testMethod() {" + "\n" +
                        "\t" + "\t" + "while(true) {" + "\n" +
                        "\t" + "\t" + "\t" + "while(true) {" + "\n" +
                        "\t" + "\t" + "\t" + "\t" + "while(true) {" + "\n" +
                        "\t" + "\t" + "\t" + "\t" + "\t" + "while(true) {" + "\n" +
                        "\t" + "\t" + "\t" + "\t" + "\t" + "\t" + "return 5;\n" +
                        "\t" + "\t" + "}}}}" + "\n" +
                        "\t" + "}" + "\n",
                result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void methodShouldNotEscapeToClassScope() {
        String result = (new Method("// comment", Visibility.PUBLIC, Scope.CLASS, "int", "testMethod", null, null,
                "while(true) {",
                "return 5;",
                "}",
                "}")).toString();
    }

    @Test(expected = IllegalArgumentException.class)
    public void methodShouldNotEscapeToClassScopeEvenIfTextBeforeIt() {
        String result = (new Method("// comment", Visibility.PUBLIC, Scope.CLASS, "int", "testMethod", null, null,
                "int i = 0;",
                "while(i < 10) {",
                "i = i + 1;",
                "}",
                "return(i);}")).toString();
    }

    @Test(expected = IllegalArgumentException.class)
    public void methodShouldFinishScope() {
        String result = (new Method("// comment", Visibility.PUBLIC, Scope.CLASS, "int", "testMethod", null, null,
                "while(true) {",
                "return 5;")).toString();
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

    @Test public void testMethodEverythingBodyWithBlankSymbolEmptyString() {
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

    @Test public void testMethodEverythingBodyWithBlankSymbolNull() {
        String result = (new Method("// comment",  Visibility.PUBLIC, Scope.CLASS, "int", "testMethod", "String a",
                "Throwable", "int a = 5;", null, "return a;")).toString();

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

    @Test public void testImportAllInPackageOf() {
        assertEquals("Generated source not as expected",
                "org.freeciv.packet.fieldtype.*",
                ClassWriter.allInPackageOf(FieldType.class));
    }

    @Test public void testCommentAutoGenerated() {
        assertEquals("Generated source not as expected",
                "// This code was auto generated from nothing",
                ClassWriter.commentAutoGenerated("nothing"));
    }

    @Test public void testClassWriterEmptyClass() {
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, new TargetPackage("org.freeciv.packetgen"),
                                              new String[]{"org.freeciv.packet.Packet"}, "nothing", "NameOfClass", null,
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
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, (TargetPackage)null,
                                              new String[]{"org.freeciv.packet.Packet"}, "nothing", "NameOfClass", null,
                                              "Packet");
        assertEquals("Generated source not as expected",
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "\n" +
                        "// This code was auto generated from nothing" + "\n" +
                        "public class NameOfClass implements Packet {" + "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterEmptyNoImports() {
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, new TargetPackage("org.freeciv.packetgen"), null,
                                              "nothing", "NameOfClass", null, "Packet");
        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "// This code was auto generated from nothing" + "\n" +
                        "public class NameOfClass implements Packet {" + "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterEmptyNoSourceGiven() {
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, new TargetPackage("org.freeciv.packetgen"),
                                              new String[]{"org.freeciv.packet.Packet"}, null, "NameOfClass", null,
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
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, new TargetPackage("org.freeciv.packetgen"),
                                              new String[]{"org.freeciv.packet.Packet"}, "nothing", "NameOfClass", null,
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
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, new TargetPackage("org.freeciv.packetgen"),
                                              new String[]{"org.freeciv.packet.Packet"}, "nothing", null, null,
                                              "Packet");
    }

    @Test public void testClassWriterEmptyEnum() {
        ClassWriter toWrite = new ClassWriter(ClassKind.ENUM, new TargetPackage("org.freeciv.packetgen"),
                                              new String[]{"org.freeciv.packet.Packet"}, "nothing", "NameOfClass", null,
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
        ClassWriter toWrite = new ClassWriter(ClassKind.ENUM, new TargetPackage("org.freeciv.packetgen"),
                                              new String[]{"org.freeciv.packet.Packet"}, "nothing", "NameOfClass", null,
                                              "Packet");
        toWrite.addEnumerated(ClassWriter.EnumElement.newEnumValue(null, "ONE", "1, \"one\""));
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
        ClassWriter toWrite = new ClassWriter(ClassKind.ENUM, new TargetPackage("org.freeciv.packetgen"),
                                              new String[]{"org.freeciv.packet.Packet"}, "nothing", "NameOfClass", null,
                                              "Packet");
        toWrite.addEnumerated(ClassWriter.EnumElement.newEnumValue(null, "ONE", "1"));
        toWrite.addEnumerated(ClassWriter.EnumElement.newEnumValue(null, "TWO", "2"));
        toWrite.addEnumerated(ClassWriter.EnumElement.newEnumValue(null, "THREE", "3"));
        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "\n" +
                        "// This code was auto generated from nothing" + "\n" +
                        "public enum NameOfClass implements Packet {" + "\n" +
                        "\t" + "ONE (1)," + "\n" +
                        "\t" + "TWO (2)," + "\n" +
                        "\t" + "THREE (3);" + "\n" +
                        "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterEnumWithFiveElementsSomeNegative() {
        ClassWriter toWrite = new ClassWriter(ClassKind.ENUM, new TargetPackage("org.freeciv.packetgen"),
                                              new String[]{"org.freeciv.packet.Packet"}, "nothing", "NameOfClass", null,
                                              "Packet");
        toWrite.addEnumerated(ClassWriter.EnumElement.newEnumValue(null, "ONE", "1"));
        toWrite.addEnumerated(ClassWriter.EnumElement.newEnumValue(null, "TWO", "2"));
        toWrite.addEnumerated(ClassWriter.EnumElement.newEnumValue(null, "THREE", "3"));
        toWrite.addEnumerated(ClassWriter.EnumElement.newEnumValue("SMALLEST", "-2"));
        toWrite.addEnumerated(ClassWriter.EnumElement.newEnumValue(null, "INVALID", "-1"));
        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "\n" +
                        "// This code was auto generated from nothing" + "\n" +
                        "public enum NameOfClass implements Packet {" + "\n" +
                        "\t" + "ONE (1)," + "\n" +
                        "\t" + "TWO (2)," + "\n" +
                        "\t" + "THREE (3)," + "\n" +
                        "\t" + "SMALLEST (-2)," + "\n" +
                        "\t" + "INVALID (-1);" + "\n" +
                        "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterEnumWithThreeElementsOneIsCommented() {
        ClassWriter toWrite = new ClassWriter(ClassKind.ENUM, new TargetPackage("org.freeciv.packetgen"),
                                              new String[]{"org.freeciv.packet.Packet"}, "nothing", "NameOfClass", null,
                                              "Packet");
        toWrite.addEnumerated(ClassWriter.EnumElement.newEnumValue(null, "ONE", "1"));
        toWrite.addEnumerated(ClassWriter.EnumElement.newEnumValue("Not a prime number", "TWO", "2"));
        toWrite.addEnumerated(ClassWriter.EnumElement.newEnumValue(null, "THREE", "3"));
        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "\n" +
                        "// This code was auto generated from nothing" + "\n" +
                        "public enum NameOfClass implements Packet {" + "\n" +
                        "\t" + "ONE (1)," + "\n" +
                        "\t" + "TWO (2) /* Not a prime number */," + "\n" +
                        "\t" + "THREE (3);" + "\n" +
                        "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterEnumWithThreeElementsTwoAreTheSame() {
        ClassWriter toWrite = new ClassWriter(ClassKind.ENUM, new TargetPackage("org.freeciv.packetgen"),
                                              new String[]{"org.freeciv.packet.Packet"}, "nothing", "NameOfClass", null,
                                              "Packet");
        toWrite.addEnumerated(ClassWriter.EnumElement.newEnumValue(null, "ONE", "1, \"one\""));
        toWrite.addEnumerated(ClassWriter.EnumElement.newEnumValue(null, "2nd", "2, \"2nd\""));
        toWrite.addEnumerated(ClassWriter.EnumElement.newEnumValue(null, "TWO", "2, \"two\""));
        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "\n" +
                        "// This code was auto generated from nothing" + "\n" +
                        "public enum NameOfClass implements Packet {" + "\n" +
                        "\t" + "ONE (1, \"one\")," + "\n" +
                        "\t" + "2nd (2, \"2nd\")," + "\n" +
                        "\t" + "TWO (2, \"two\");" + "\n" +
                        "}" + "\n",
                toWrite.toString());
    }

    @Test(expected = AssertionError.class)
    public void testNotEnumAddsEnumerated() {
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, new TargetPackage("org.freeciv.packetgen"),
                                              new String[]{"org.freeciv.packet.Packet"}, "nothing", "NameOfClass", null,
                                              "Packet");
        toWrite.addEnumerated(ClassWriter.EnumElement.newEnumValue(null, "One", "1"));
    }

    @Test public void testClassWriterEmptyTwoBlocksOfImports() {
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, new TargetPackage("org.freeciv.packetgen"), new String[]{
                                "org.freeciv.packet.Packet",
                                null,
                                "java.util.List"
                        }, "nothing", "NameOfClass", null, "Packet");

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
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, new TargetPackage("org.freeciv.packetgen"), new String[]{
                                "org.freeciv.packet.Packet",
                                "",
                                "java.util.List"
                        }, "nothing", "NameOfClass", null, "Packet");

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
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, new TargetPackage("org.freeciv.packetgen"),
                                              new String[]{"java.util.List"}, "nothing", "NameOfClass", null, "Packet");
        toWrite.addClassConstant("int", "five", "5");

        String[] lines = toWrite.toString().split("\n");

        assertTrue("Generated source not as expected " + lines[lines.length - 1].trim(),
                lines[lines.length - 1].trim().endsWith("}"));
        assertTrue("Class has blank line before end " + lines[lines.length - 2].trim(),
                !lines[lines.length - 2].trim().isEmpty());
    }

    @Test public void testClassWriterStyleAlwaysEndWithNewLine() {
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, new TargetPackage("org.freeciv.packetgen"),
                                              new String[]{"java.util.List"}, "nothing", "NameOfClass", null, "Packet");

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
                "throw new IOException(\"Tried to create package PACKET_CITY_NAME_SUGGESTION_REQ but packet number was \" + packet);",
                "}",
                "",
                "if (getEncodedSize() != headerLen) {",
                "throw new IOException(\"Package size in header and Java packet not the same. Header: \" + headerLen",
                "+ \" Packet: \" + getEncodedSize());",
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
