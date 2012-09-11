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

import org.freeciv.Util;
import org.freeciv.packet.fieldtype.FieldType;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.Import;
import org.junit.Test;

import java.io.DataInput;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.*;

public class CodeGenTest {
    private static final String generatorname = ",\n\tvalue = \"org.freeciv.packetgen.javaGenerator.ClassWriter\")";

    @Test public void testMethodEverything() {
        String result = (Method.custom(Comment.c("comment"), Visibility.PUBLIC, Scope.CLASS,
                new TargetClass("int"), "testMethod", "String a",
                "Throwable", new Block(RETURN(asAnInt("5"))))).toString();

        assertEquals("Generated source not as expected",
                "\t" + "/* comment */" + "\n" +
                "\t" + "public static int testMethod(String a) throws Throwable {" + "\n" +
                "\t" + "\t" + "return 5;\n" +
                "\t" + "}" + "\n",
                result);
    }

    @Test public void testMethodNoComment() {
        String result = (Method.custom(Comment.no(), Visibility.PUBLIC, Scope.CLASS,
                new TargetClass("int"), "testMethod", "String a",
                "Throwable",
                new Block(RETURN(asAnInt("5"))))).toString();

        assertEquals("Generated source not as expected",
                        "\t" + "public static int testMethod(String a) throws Throwable {" + "\n" +
                        "\t" + "\t" + "return 5;\n" +
                        "\t" + "}" + "\n",
                result);
    }

    @Test public void testMethodNoParams() {
        String result = (Method.custom(Comment.c("comment"), Visibility.PUBLIC, Scope.CLASS,
                new TargetClass("int"), "testMethod", Collections.<Var>emptyList(),
                "Throwable", new Block(RETURN(asAnInt("5"))))).toString();

        assertEquals("Generated source not as expected",
                "\t" + "/* comment */" + "\n" +
                        "\t" + "public static int testMethod() throws Throwable {" + "\n" +
                        "\t" + "\t" + "return 5;\n" +
                        "\t" + "}" + "\n",
                result);
    }

    @Test public void testMethodManyLevelsOfIndention() {
        String result = (Method.custom(Comment.c("comment"), Visibility.PUBLIC, Scope.CLASS,
                new TargetClass("int"), "testMethod", Collections.<Var>emptyList(),
                null,
                new Block(WHILE(TRUE,
                        new Block(WHILE(TRUE,
                                new Block(WHILE(TRUE,
                                        new Block(WHILE(TRUE,
                                                new Block(RETURN(asAnInt("5"))))))))))))).toString();

        assertEquals("Generated source not as expected",
                "\t" + "/* comment */" + "\n" +
                        "\t" + "public static int testMethod() {" + "\n" +
                        "\t" + "\t" + "while (true) {" + "\n" +
                        "\t" + "\t" + "\t" + "while (true) {" + "\n" +
                        "\t" + "\t" + "\t" + "\t" + "while (true) {" + "\n" +
                        "\t" + "\t" + "\t" + "\t" + "\t" + "while (true) {" + "\n" +
                        "\t" + "\t" + "\t" + "\t" + "\t" + "\t" + "return 5;\n" +
                        "\t" + "\t" + "\t" + "\t" + "\t" + "}" + "\n" +
                        "\t" + "\t" + "\t" + "\t" + "}" + "\n" +
                        "\t" + "\t" + "\t" + "}" + "\n" +
                        "\t" + "\t" + "}" + "\n" +
                        "\t" + "}" + "\n",
                result);
    }

    @Test(expected = IllegalStateException.class)
    public void methodShouldNotEscapeToClassScope() {
        Block closesScopeNotOpened = new Block(WHILE(TRUE, new Block(RETURN(asAnInt("5"))))) {
            @Override
            public void writeAtoms(CodeAtoms to) {
                super.writeAtoms(to);
                to.add(HasAtoms.RSC);
            }
        };
        String result = (Method.custom(Comment.c("comment"), Visibility.PUBLIC, Scope.CLASS,
                new TargetClass("int"), "testMethod", Collections.<Var>emptyList(),
                null, closesScopeNotOpened)).toString();
    }

    @Test(expected = IllegalStateException.class)
    public void methodShouldFinishScope() {
        Block forgetsToCloseScope = new Block() {
            @Override
            public void writeAtoms(CodeAtoms to) {
                to.add(LSC);
                to.add(HasAtoms.WHILE);
                to.add(HasAtoms.LPR);
                to.add(new IR.CodeAtom("true"));
                to.add(HasAtoms.RPR);
                to.add(HasAtoms.RSC);
                to.add(HasAtoms.RET);
                to.add(new IR.CodeAtom("5"));
                to.add(HasAtoms.EOL);
                to.add(RSC);
                to.refuseNextIf(new Util.OneCondition<IR.CodeAtom>() {
                    @Override
                    public boolean isTrueFor(IR.CodeAtom argument) {
                        return HasAtoms.EOL.equals(argument);
                    }
                });
            }
        };
        String result = (Method.custom(Comment.c("comment"), Visibility.PUBLIC, Scope.CLASS,
                new TargetClass("int"), "testMethod", Collections.<Var>emptyList(),
                null, forgetsToCloseScope)).toString();
    }

    @Test public void testMethodEverythingTwoLineComment() {
        String result = (Method.custom(Comment.c("comment comment comment comment comment comment " +
                "comment comment comment comment comment comment " +
                "more comment"), Visibility.PUBLIC, Scope.CLASS,
                new TargetClass("int"), "testMethod", "String a",
                "Throwable", new Block(RETURN(asAnInt("5"))))).toString();

        assertEquals("Generated source not as expected",
                "\t" + "/*" + "\n" +
                        "\t" + " * comment comment comment comment comment comment comment comment comment comment comment comment" + "\n" +
                        "\t" + " * more comment" + "\n" +
                        "\t" + " */" + "\n" +
                        "\t" + "public static int testMethod(String a) throws Throwable {" + "\n" +
                        "\t" + "\t" + "return 5;\n" +
                        "\t" + "}" + "\n",
                result);
    }

    @Test public void testMethodEverythingBodyWithBlank() {
        Block isSeparated = new Block();
        isSeparated.addStatement(asAValue("int a = 5"));
        isSeparated.groupBoundary();
        isSeparated.addStatement(asAValue("return a"));
        String result = (Method.custom(Comment.c("comment"), Visibility.PUBLIC, Scope.CLASS,
                new TargetClass("int"), "testMethod", "String a",
                "Throwable", isSeparated)).toString();

        assertEquals("Generated source not as expected",
                "\t" + "/* comment */" + "\n" +
                        "\t" + "public static int testMethod(String a) throws Throwable {" + "\n" +
                        "\t" + "\t" + "int a = 5;" + "\n" +
                        "\n" +
                        "\t" + "\t" + "return a;" + "\n" +
                        "\t" + "}" + "\n",
                result);
    }

    @Test public void testMethodClassStateReader() {
        Method toTest = Method.newReadClassState(Comment.no(),
                new TargetClass("boolean"), "isTrue",
                Block.fromStrings("return true"));
        assertEquals("Generated Class state reader source code not as espected",
                "\t" + "public static boolean isTrue() {" + "\n" +
                        "\t\t" + "return true;" + "\n" +
                        "\t" + "}" + "\n",
                toTest.toString());
    }

    @Test public void testConstantDeclaration() {
        assertEquals("Generated source not as expected",
                "private static final int integer = 25;",
                (Var.field(Visibility.PRIVATE, Scope.CLASS, Modifiable.NO, "int", "integer", asAnInt("25"))).toString());
    }

    @Test public void testObjectConstantDeclaration() {
        assertEquals("Generated source not as expected",
                "private final int integer;",
                (Var.field(Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO, "int", "integer", null)).toString());
    }

    @Test public void testStateVarDeclaration() {
        assertEquals("Generated source not as expected",
                "private int integer;",
                (Var.field(Visibility.PRIVATE, Scope.OBJECT, Modifiable.YES, "int", "integer", null)).toString());
    }

    @Test public void testPackageImport() {
        assertEquals("Generated source not as expected",
                "import org.freeciv.Connect;",
                Import.classIn(org.freeciv.Connect.class).toString());
    }

    @Test public void testImportAllInPackageOf() {
        assertEquals("Generated source not as expected",
                "import org.freeciv.packet.fieldtype.*;",
                Import.allIn(new TargetPackage(FieldType.class.getPackage())).toString());
    }

    @Test public void testCommentAutoGenerated() {
        assertEquals("Generated source not as expected",
                "@Generated(comments = \"Auto generated from nothing\"" + generatorname,
                ClassWriter.commentAutoGenerated("nothing").getJavaCodeIndented(""));
    }

    @Test public void testClassWriterEmptyClass() {
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, new TargetPackage("org.freeciv.packetgen"), new Import[]{Import.classIn(org.freeciv.packet.Packet.class)}, "nothing", Collections.<Annotate>emptyList(), "NameOfClass", null, "Packet");
        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "import javax.annotation.Generated;" + "\n" +
                        "\n" +
                        "@Generated(comments = \"Auto generated from nothing\"" + generatorname + "\n" +
                        "public class NameOfClass implements Packet {" + "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterEmptyNoPackage() {
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, (TargetPackage)null, new Import[]{Import.classIn(org.freeciv.packet.Packet.class)}, "nothing", Collections.<Annotate>emptyList(), "NameOfClass", null, "Packet");
        assertEquals("Generated source not as expected",
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "import javax.annotation.Generated;" + "\n" +
                        "\n" +
                        "@Generated(comments = \"Auto generated from nothing\"" + generatorname + "\n" +
                        "public class NameOfClass implements Packet {" + "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterEmptyNoImports() {
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, new TargetPackage("org.freeciv.packetgen"), null, "nothing", Collections.<Annotate>emptyList(), "NameOfClass", null, "Packet");
        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "import javax.annotation.Generated;" + "\n" +
                        "\n" +
                        "@Generated(comments = \"Auto generated from nothing\"" + generatorname + "\n" +
                        "public class NameOfClass implements Packet {" + "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterEmptyNoSourceGiven() {
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, new TargetPackage("org.freeciv.packetgen"), new Import[]{Import.classIn(org.freeciv.packet.Packet.class)}, null, Collections.<Annotate>emptyList(), "NameOfClass", null, "Packet");
        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "\n" +
                        "public class NameOfClass implements Packet {" + "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterEmptyNoInterfaceGiven() {
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, new TargetPackage("org.freeciv.packetgen"), new Import[]{Import.classIn(org.freeciv.packet.Packet.class)}, "nothing", Collections.<Annotate>emptyList(), "NameOfClass", null, null);
        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "import javax.annotation.Generated;" + "\n" +
                        "\n" +
                        "@Generated(comments = \"Auto generated from nothing\"" + generatorname + "\n" +
                        "public class NameOfClass {" + "}" + "\n",
                toWrite.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testClassWriterEmptyNoNameGiven() {
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, new TargetPackage("org.freeciv.packetgen"), new Import[]{Import.classIn(org.freeciv.packet.Packet.class)}, "nothing", Collections.<Annotate>emptyList(), null, null, "Packet");
    }

    @Test public void testClassWriterEmptyEnum() {
        ClassWriter toWrite = new ClassWriter(ClassKind.ENUM, new TargetPackage("org.freeciv.packetgen"), new Import[]{Import.classIn(org.freeciv.packet.Packet.class)}, "nothing", Collections.<Annotate>emptyList(), "NameOfClass", null, "Packet");
        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "import javax.annotation.Generated;" + "\n" +
                        "\n" +
                        "@Generated(comments = \"Auto generated from nothing\"" + generatorname + "\n" +
                        "public enum NameOfClass implements Packet {" + "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterEnumWithOneElement() {
        ClassWriter toWrite = new ClassWriter(ClassKind.ENUM, new TargetPackage("org.freeciv.packetgen"), new Import[]{Import.classIn(org.freeciv.packet.Packet.class)}, "nothing", Collections.<Annotate>emptyList(), "NameOfClass", null, "Packet");
        toWrite.addEnumerated(EnumElement.newEnumValue("ONE", "1, \"one\""));
        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "import javax.annotation.Generated;" + "\n" +
                        "\n" +
                        "@Generated(comments = \"Auto generated from nothing\"" + generatorname + "\n" +
                        "public enum NameOfClass implements Packet {" + "\n" +
                        "\t" + "ONE(1, \"one\");" + "\n" +
                        "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterEnumWithThreeElements() {
        ClassWriter toWrite = new ClassWriter(ClassKind.ENUM, new TargetPackage("org.freeciv.packetgen"), new Import[]{Import.classIn(org.freeciv.packet.Packet.class)}, "nothing", Collections.<Annotate>emptyList(), "NameOfClass", null, "Packet");
        toWrite.addEnumerated(EnumElement.newEnumValue("ONE", "1"));
        toWrite.addEnumerated(EnumElement.newEnumValue("TWO", "2"));
        toWrite.addEnumerated(EnumElement.newEnumValue("THREE", "3"));
        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "import javax.annotation.Generated;" + "\n" +
                        "\n" +
                        "@Generated(comments = \"Auto generated from nothing\"" + generatorname + "\n" +
                        "public enum NameOfClass implements Packet {" + "\n" +
                        "\t" + "ONE(1)," + "\n" +
                        "\t" + "TWO(2)," + "\n" +
                        "\t" + "THREE(3);" + "\n" +
                        "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterEnumWithFiveElementsSomeNegative() {
        ClassWriter toWrite = new ClassWriter(ClassKind.ENUM, new TargetPackage("org.freeciv.packetgen"), new Import[]{Import.classIn(org.freeciv.packet.Packet.class)}, "nothing", Collections.<Annotate>emptyList(), "NameOfClass", null, "Packet");
        toWrite.addEnumerated(EnumElement.newEnumValue("ONE", "1"));
        toWrite.addEnumerated(EnumElement.newEnumValue("TWO", "2"));
        toWrite.addEnumerated(EnumElement.newEnumValue("THREE", "3"));
        toWrite.addEnumerated(EnumElement.newEnumValue("SMALLEST", "-2"));
        toWrite.addEnumerated(EnumElement.newEnumValue("INVALID", "-1"));
        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "import javax.annotation.Generated;" + "\n" +
                        "\n" +
                        "@Generated(comments = \"Auto generated from nothing\"" + generatorname + "\n" +
                        "public enum NameOfClass implements Packet {" + "\n" +
                        "\t" + "ONE(1)," + "\n" +
                        "\t" + "TWO(2)," + "\n" +
                        "\t" + "THREE(3)," + "\n" +
                        "\t" + "SMALLEST(-2)," + "\n" +
                        "\t" + "INVALID(-1);" + "\n" +
                        "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterEnumWithThreeElementsOneIsCommented() {
        ClassWriter toWrite = new ClassWriter(ClassKind.ENUM, new TargetPackage("org.freeciv.packetgen"), new Import[]{Import.classIn(org.freeciv.packet.Packet.class)}, "nothing", Collections.<Annotate>emptyList(), "NameOfClass", null, "Packet");
        toWrite.addEnumerated(EnumElement.newEnumValue("ONE", "1"));
        toWrite.addEnumerated(EnumElement.newEnumValue(Comment.c("Not a prime number"), "TWO", "2"));
        toWrite.addEnumerated(EnumElement.newEnumValue("THREE", "3"));
        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "import javax.annotation.Generated;" + "\n" +
                        "\n" +
                        "@Generated(comments = \"Auto generated from nothing\"" + generatorname + "\n" +
                        "public enum NameOfClass implements Packet {" + "\n" +
                        "\t" + "ONE(1)," + "\n" +
                        "\t" + "/* Not a prime number */ TWO(2)," + "\n" +
                        "\t" + "THREE(3);" + "\n" +
                        "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterEnumWithThreeElementsTwoAreTheSame() {
        ClassWriter toWrite = new ClassWriter(ClassKind.ENUM, new TargetPackage("org.freeciv.packetgen"), new Import[]{Import.classIn(org.freeciv.packet.Packet.class)}, "nothing", Collections.<Annotate>emptyList(), "NameOfClass", null, "Packet");
        toWrite.addEnumerated(EnumElement.newEnumValue("ONE", "1, \"one\""));
        toWrite.addEnumerated(EnumElement.newEnumValue("2nd", "2, \"2nd\""));
        toWrite.addEnumerated(EnumElement.newEnumValue("TWO", "2, \"two\""));
        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "import javax.annotation.Generated;" + "\n" +
                        "\n" +
                        "@Generated(comments = \"Auto generated from nothing\"" + generatorname + "\n" +
                        "public enum NameOfClass implements Packet {" + "\n" +
                        "\t" + "ONE(1, \"one\")," + "\n" +
                        "\t" + "2nd(2, \"2nd\")," + "\n" +
                        "\t" + "TWO(2, \"two\");" + "\n" +
                        "}" + "\n",
                toWrite.toString());
    }

    @Test(expected = AssertionError.class)
    public void testNotEnumAddsEnumerated() {
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, new TargetPackage("org.freeciv.packetgen"), new Import[]{Import.classIn(org.freeciv.packet.Packet.class)}, "nothing", Collections.<Annotate>emptyList(), "NameOfClass", null, "Packet");
        toWrite.addEnumerated(EnumElement.newEnumValue("One", "1"));
    }

    @Test public void testClassWriterEmptyTwoBlocksOfImports() {
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, new TargetPackage("org.freeciv.packetgen"), new Import[]{
                                        Import.classIn(org.freeciv.packet.Packet.class),
                                        null,
                                        Import.classIn(List.class)
                                }, "nothing", Collections.<Annotate>emptyList(), "NameOfClass", null, "Packet");

        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "\n" +
                        "import java.util.List;" + "\n" +
                        "import javax.annotation.Generated;" + "\n" +
                        "\n" +
                        "@Generated(comments = \"Auto generated from nothing\"" + generatorname + "\n" +
                        "public class NameOfClass implements Packet {" + "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterEmptyTwoBlocksOfImportsSeparatedByEmpthy() {
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, new TargetPackage("org.freeciv.packetgen"), new Import[]{
                                        Import.classIn(org.freeciv.packet.Packet.class),
                                        null,
                                        Import.classIn(List.class)
                                }, "nothing", Collections.<Annotate>emptyList(), "NameOfClass", null, "Packet");

        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "\n" +
                        "import java.util.List;" + "\n" +
                        "import javax.annotation.Generated;" + "\n" +
                        "\n" +
                        "@Generated(comments = \"Auto generated from nothing\"" + generatorname + "\n" +
                        "public class NameOfClass implements Packet {" + "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterStyleConstantNoBlankLineAtTheEndOfTheClassScope() {
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, new TargetPackage("org.freeciv.packetgen"), new Import[]{Import.classIn(List.class)}, "nothing", Collections.<Annotate>emptyList(), "NameOfClass", null, "Packet");
        toWrite.addClassConstant("int", "five", "5");

        String[] lines = toWrite.toString().split("\n");

        assertTrue("Generated source not as expected " + lines[lines.length - 1].trim(),
                lines[lines.length - 1].trim().endsWith("}"));
        assertTrue("Class has blank line before end " + lines[lines.length - 2].trim(),
                !lines[lines.length - 2].trim().isEmpty());
    }

    @Test public void testClassWriterStyleAlwaysEndWithNewLine() {
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, new TargetPackage("org.freeciv.packetgen"), new Import[]{Import.classIn(List.class)}, "nothing", Collections.<Annotate>emptyList(), "NameOfClass", null, "Packet");

        assertTrue("File should end with line break", toWrite.toString().endsWith("\n"));
}

    // Tests based on real examples

    @Test public void testPublicConstructorNoExceptions() {
        String result = Method.newPublicConstructor(Comment.no(),
                "PACKET_CITY_NAME_SUGGESTION_REQ", "Integer unit_id",
                Block.fromStrings("this.unit_id = new UNIT(unit_id)")).toString();

        assertEquals("Generated source not as expected",
                "\t" + "public PACKET_CITY_NAME_SUGGESTION_REQ(Integer unit_id) {" + "\n" +
                        "\t" + "\t" + "this.unit_id = new UNIT(unit_id);" + "\n" +
                        "\t" + "}" + "\n",
                result);
    }

    @Test public void testPublicConstructor() {
        Block body = new Block(
                asAValue("this.unit_id = new UNIT(from)"),
                IF(asBool("getNumber() != packet"),
                        Block.fromStrings("throw new IOException(\"Tried to create package PACKET_CITY_NAME_SUGGESTION_REQ but packet number was \" + packet)")));
        body.groupBoundary();
        body.addStatement(IF(asBool("getEncodedSize() != headerLen"), new Block(THROW((new TargetClass(IOException.class))
                        .newInstance(sum(
                                literalString("Package size in header and Java packet not the same. Header: "),
                                asAValue("headerLen"),
                                literalString(" Packet: "),
                                asAValue("getEncodedSize()")))))));
        String result = Method.newPublicConstructorWithException(Comment.doc("Construct an object from a DataInput", "",
                Comment.param(Var.local(DataInput.class, "from", null), "data stream that is at the start of the package body"),
                Comment.param(Var.local(int.class, "headerLen", null), "length from header package"),
                Comment.param(Var.local(int.class, "packet", null), "the number of the packet specified in the header"),
                Comment.docThrows(new TargetClass("IOException"), "if the DataInput has a problem")),
                "PACKET_CITY_NAME_SUGGESTION_REQ", "DataInput from, int headerLen, int packet", "IOException", body).toString();

        assertEquals("Generated source not as expected",
                "\t" + "/**" + "\n" +
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
                        "\t" + "\t" + "\t" + "\t" + "\t" + "+ \" Packet: \" + getEncodedSize());\n" +
                        "\t" + "\t" + "}" + "\n" +
                        "\t" + "}" + "\n",
                result);
    }

    @Test public void testPublicReadObjectState() {
        String result = Method.newPublicReadObjectState(Comment.no(),
                new TargetClass("String"), "toString",
                new Block(RETURN(asAString("value.toString()")))).toString();

        assertEquals("Generated source not as expected",
                "\tpublic String toString() {\n" +
                        "\t\treturn value.toString();\n" +
                        "\t}\n",
                result);
    }
}
