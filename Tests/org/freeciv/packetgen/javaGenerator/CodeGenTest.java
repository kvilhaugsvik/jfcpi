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
import org.freeciv.connection.Interpretated;
import org.freeciv.packet.fieldtype.FieldType;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.Import;
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.ABool;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AString;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AnInt;
import org.freeciv.packetgen.javaGenerator.formating.CodeStyle;
import org.junit.Test;

import java.io.DataInput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.*;

public class CodeGenTest {
    private static final String generatorname = ",\n\tvalue = \"org.freeciv.packetgen.javaGenerator.ClassWriter\")";

    @Test public void testMethodEverything() {
        String result = toStringAsIfInAClass(Method.custom(Comment.c("comment"), Visibility.PUBLIC, Scope.CLASS,
                TargetClass.fromClass(int.class), "testMethod", Arrays.asList(Var.param(String.class, "a")),
                Arrays.asList(TargetClass.fromClass(Throwable.class)),
                new Block(RETURN(literal(5)))));

        assertEquals("Generated source not as expected",
                "\t" + "/* comment */" + "\n" +
                        "\t" + "public static int testMethod(String a) throws Throwable {" + "\n" +
                        "\t" + "\t" + "return 5;\n" +
                        "\t" + "}" + "\n",
                result);
    }

    private String toStringAsIfInAClass(Method toSurround) {
        CodeAtoms result = new CodeAtoms();
        result.hintStart(CodeStyle.OUTER_LEVEL);
        toSurround.writeAtoms(result);
        result.hintEnd(CodeStyle.OUTER_LEVEL);

        String start = "\t";

        List<String> lines = ClassWriter.DEFAULT_STYLE_INDENT.asFormattedLines(result);
        if (0 == lines.size())
            return "";
        StringBuilder out = new StringBuilder(start);
        out.append(lines.get(0));
        for (int i = 1; i < lines.size(); i++) {
            out.append("\n");
            String line = lines.get(i);
            if (!"".equals(line)) {
                out.append(start);
                out.append(line);
            }
        }
        return out.toString() + "\n";
    }

    @Test public void testMethodNoComment() {
        String result = toStringAsIfInAClass(Method.custom(Comment.no(), Visibility.PUBLIC, Scope.CLASS,
                TargetClass.fromClass(int.class), "testMethod", Arrays.asList(Var.param(String.class, "a")),
                Arrays.asList(TargetClass.fromClass(Throwable.class)),
                new Block(RETURN(literal(5)))));

        assertEquals("Generated source not as expected",
                        "\t" + "public static int testMethod(String a) throws Throwable {" + "\n" +
                        "\t" + "\t" + "return 5;\n" +
                        "\t" + "}" + "\n",
                result);
    }

    @Test public void testMethodNoParams() {
        String result = toStringAsIfInAClass(Method.custom(Comment.c("comment"), Visibility.PUBLIC, Scope.CLASS,
                TargetClass.fromClass(int.class), "testMethod", Collections.<Var<AValue>>emptyList(),
                Arrays.<TargetClass>asList(TargetClass.newKnown(Throwable.class)), new Block(RETURN(literal(5)))));

        assertEquals("Generated source not as expected",
                "\t" + "/* comment */" + "\n" +
                        "\t" + "public static int testMethod() throws Throwable {" + "\n" +
                        "\t" + "\t" + "return 5;\n" +
                        "\t" + "}" + "\n",
                result);
    }

    @Test public void testMethodManyLevelsOfIndention() {
        String result = toStringAsIfInAClass(Method.custom(Comment.c("comment"), Visibility.PUBLIC, Scope.CLASS,
                TargetClass.fromClass(int.class), "testMethod", Collections.<Var<AValue>>emptyList(),
                Collections.<TargetClass>emptyList(),
                new Block(WHILE(TRUE,
                        new Block(WHILE(TRUE,
                                new Block(WHILE(TRUE,
                                        new Block(WHILE(TRUE,
                                                new Block(RETURN(literal(5)))))))))))));

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
        Block closesScopeNotOpened = new Block(WHILE(TRUE, new Block(RETURN(literal(5))))) {
            @Override
            public void writeAtoms(CodeAtoms to) {
                super.writeAtoms(to);
                to.add(HasAtoms.RSC);
            }
        };
        String result = (Method.custom(Comment.c("comment"), Visibility.PUBLIC, Scope.CLASS,
                TargetClass.fromClass(int.class), "testMethod", Collections.<Var<AValue>>emptyList(),
                Collections.<TargetClass>emptyList(), closesScopeNotOpened)).toString();
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
                TargetClass.fromClass(int.class), "testMethod", Collections.<Var<AValue>>emptyList(),
                Collections.<TargetClass>emptyList(), forgetsToCloseScope)).toString();
    }

    @Test public void testMethodEverythingTwoLineComment() {
        String result = (Method.custom(Comment.c("comment comment comment comment comment comment " +
                "comment comment comment comment comment comment " +
                "more comment"), Visibility.PUBLIC, Scope.CLASS,
                TargetClass.fromClass(int.class), "testMethod",  Arrays.asList(Var.param(String.class, "a")),
                Arrays.asList(TargetClass.fromClass(Throwable.class)),
                new Block(RETURN(literal(5))))).toString();

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
        isSeparated.addStatement(BuiltIn.<AValue>toCode("int a = 5"));
        isSeparated.groupBoundary();
        isSeparated.addStatement(BuiltIn.<AValue>toCode("return a"));
        String result = toStringAsIfInAClass(Method.custom(Comment.c("comment"), Visibility.PUBLIC, Scope.CLASS,
                TargetClass.fromClass(int.class), "testMethod", Arrays.asList(Var.param(String.class, "a")),
                Arrays.asList(TargetClass.fromClass(Throwable.class)), isSeparated));

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
                TargetClass.fromClass(boolean.class), "isTrue",
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
                (Var.field(Collections.<Annotate>emptyList(), Visibility.PRIVATE, Scope.CLASS, Modifiable.NO, int.class, "integer", literal(25))).toString());
    }

    @Test public void testObjectConstantDeclaration() {
        assertEquals("Generated source not as expected",
                "private final int integer;",
                (Var.field(Collections.<Annotate>emptyList(), Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO, int.class, "integer", null)).toString());
    }

    @Test public void testStateVarDeclaration() {
        assertEquals("Generated source not as expected",
                "private int integer;",
                (Var.field(Collections.<Annotate>emptyList(), Visibility.PRIVATE, Scope.OBJECT, Modifiable.YES, int.class, "integer", null)).toString());
    }

    @Test public void testPackageImport() {
        assertEquals("Generated source not as expected",
                "import org.freeciv.connection.Interpretated;",
                Import.classIn(Interpretated.class).toString());
    }

    @Test public void testImportAllInPackageOf() {
        assertEquals("Generated source not as expected",
                "import org.freeciv.packet.fieldtype.*;",
                Import.allIn(FieldType.class.getPackage()).toString());
    }

    @Test public void testCommentAutoGenerated() {
        assertEquals("Generated source not as expected",
                "@Generated(comments = \"Auto generated from nothing\"" + generatorname,
                ClassWriter.commentAutoGenerated("nothing").getJavaCodeIndented(""));
    }

    @Test public void testClassWriterEmptyClass() {
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, TargetPackage.from("org.freeciv.packetgen"),
                new Import[]{Import.classIn(org.freeciv.packet.Packet.class)}, "nothing",
                Collections.<Annotate>emptyList(), "NameOfClass",
                ClassWriter.DEFAULT_PARENT, Arrays.asList(TargetClass.newKnown(org.freeciv.packet.Packet.class)));
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
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, TargetPackage.TOP_LEVEL, new Import[]{Import.classIn(org.freeciv.packet.Packet.class)}, "nothing", Collections.<Annotate>emptyList(), "NameOfClass",
                ClassWriter.DEFAULT_PARENT, Arrays.asList(TargetClass.newKnown(org.freeciv.packet.Packet.class)));
        assertEquals("Generated source not as expected",
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "import javax.annotation.Generated;" + "\n" +
                        "\n" +
                        "@Generated(comments = \"Auto generated from nothing\"" + generatorname + "\n" +
                        "public class NameOfClass implements Packet {" + "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterEmptyNoImports() {
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, TargetPackage.from("org.freeciv.packetgen"), null, "nothing", Collections.<Annotate>emptyList(), "NameOfClass",
                ClassWriter.DEFAULT_PARENT, Arrays.asList(TargetClass.newKnown(org.freeciv.packet.Packet.class)));
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
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, TargetPackage.from("org.freeciv.packetgen"), new Import[]{Import.classIn(org.freeciv.packet.Packet.class)}, null, Collections.<Annotate>emptyList(), "NameOfClass",
                ClassWriter.DEFAULT_PARENT, Arrays.asList(TargetClass.newKnown(org.freeciv.packet.Packet.class)));
        assertEquals("Generated source not as expected",
                "package org.freeciv.packetgen;" + "\n" +
                        "\n" +
                        "import org.freeciv.packet.Packet;" + "\n" +
                        "\n" +
                        "public class NameOfClass implements Packet {" + "}" + "\n",
                toWrite.toString());
    }

    @Test public void testClassWriterEmptyNoInterfaceGiven() {
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, TargetPackage.from("org.freeciv.packetgen"), new Import[]{Import.classIn(org.freeciv.packet.Packet.class)}, "nothing", Collections.<Annotate>emptyList(), "NameOfClass",
                ClassWriter.DEFAULT_PARENT, Collections.<TargetClass>emptyList());
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
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, TargetPackage.from("org.freeciv.packetgen"), new Import[]{Import.classIn(org.freeciv.packet.Packet.class)}, "nothing", Collections.<Annotate>emptyList(), null,
                ClassWriter.DEFAULT_PARENT, Arrays.asList(TargetClass.newKnown(org.freeciv.packet.Packet.class)));
    }

    @Test public void testClassWriterEmptyEnum() {
        ClassWriter toWrite = new ClassWriter(ClassKind.ENUM, TargetPackage.from("org.freeciv.packetgen"), new Import[]{Import.classIn(org.freeciv.packet.Packet.class)}, "nothing", Collections.<Annotate>emptyList(), "NameOfClass",
                ClassWriter.DEFAULT_PARENT, Arrays.asList(TargetClass.newKnown(org.freeciv.packet.Packet.class)));
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
        ClassWriter toWrite = new ClassWriter(ClassKind.ENUM, TargetPackage.from("org.freeciv.packetgen"), new Import[]{Import.classIn(org.freeciv.packet.Packet.class)}, "nothing", Collections.<Annotate>emptyList(), "NameOfClass",
                ClassWriter.DEFAULT_PARENT, Arrays.asList(TargetClass.newKnown(org.freeciv.packet.Packet.class)));
        toWrite.addEnumerated(EnumElement.newEnumValue("ONE", literal(1), literal("one")));
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
        ClassWriter toWrite = new ClassWriter(ClassKind.ENUM, TargetPackage.from("org.freeciv.packetgen"), new Import[]{Import.classIn(org.freeciv.packet.Packet.class)}, "nothing", Collections.<Annotate>emptyList(), "NameOfClass",
                ClassWriter.DEFAULT_PARENT, Arrays.asList(TargetClass.newKnown(org.freeciv.packet.Packet.class)));
        toWrite.addEnumerated(EnumElement.newEnumValue("ONE", literal(1)));
        toWrite.addEnumerated(EnumElement.newEnumValue("TWO", literal(2)));
        toWrite.addEnumerated(EnumElement.newEnumValue("THREE", literal(3)));
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
        ClassWriter toWrite = new ClassWriter(ClassKind.ENUM, TargetPackage.from("org.freeciv.packetgen"), new Import[]{Import.classIn(org.freeciv.packet.Packet.class)}, "nothing", Collections.<Annotate>emptyList(), "NameOfClass",
                ClassWriter.DEFAULT_PARENT, Arrays.asList(TargetClass.newKnown(org.freeciv.packet.Packet.class)));
        toWrite.addEnumerated(EnumElement.newEnumValue("ONE", literal(1)));
        toWrite.addEnumerated(EnumElement.newEnumValue("TWO", literal(2)));
        toWrite.addEnumerated(EnumElement.newEnumValue("THREE", literal(3)));
        toWrite.addEnumerated(EnumElement.newEnumValue("SMALLEST", literal(-2)));
        toWrite.addEnumerated(EnumElement.newEnumValue("INVALID", literal(-1)));
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
        ClassWriter toWrite = new ClassWriter(ClassKind.ENUM, TargetPackage.from("org.freeciv.packetgen"), new Import[]{Import.classIn(org.freeciv.packet.Packet.class)}, "nothing", Collections.<Annotate>emptyList(), "NameOfClass",
                ClassWriter.DEFAULT_PARENT, Arrays.asList(TargetClass.newKnown(org.freeciv.packet.Packet.class)));
        toWrite.addEnumerated(EnumElement.newEnumValue("ONE", literal(1)));
        toWrite.addEnumerated(EnumElement.newEnumValue(Comment.c("Not a prime number"), "TWO", literal(2)));
        toWrite.addEnumerated(EnumElement.newEnumValue("THREE", literal(3)));
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
        ClassWriter toWrite = new ClassWriter(ClassKind.ENUM, TargetPackage.from("org.freeciv.packetgen"), new Import[]{Import.classIn(org.freeciv.packet.Packet.class)}, "nothing", Collections.<Annotate>emptyList(), "NameOfClass",
                ClassWriter.DEFAULT_PARENT, Arrays.asList(TargetClass.newKnown(org.freeciv.packet.Packet.class)));
        toWrite.addEnumerated(EnumElement.newEnumValue("ONE", literal(1), literal("one")));
        toWrite.addEnumerated(EnumElement.newEnumValue("2nd", literal(2), literal("2nd")));
        toWrite.addEnumerated(EnumElement.newEnumValue("TWO", literal(2), literal("two")));
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
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, TargetPackage.from("org.freeciv.packetgen"), new Import[]{Import.classIn(org.freeciv.packet.Packet.class)}, "nothing", Collections.<Annotate>emptyList(), "NameOfClass",
                ClassWriter.DEFAULT_PARENT, Arrays.asList(TargetClass.newKnown(org.freeciv.packet.Packet.class)));
        toWrite.addEnumerated(EnumElement.newEnumValue("One", literal(1)));
    }

    @Test public void testClassWriterEmptyTwoBlocksOfImports() {
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, TargetPackage.from("org.freeciv.packetgen"), new Import[]{
                                        Import.classIn(org.freeciv.packet.Packet.class),
                                        null,
                                        Import.classIn(List.class)
                                }, "nothing", Collections.<Annotate>emptyList(), "NameOfClass",
                                ClassWriter.DEFAULT_PARENT, Arrays.asList(TargetClass.newKnown(org.freeciv.packet.Packet.class)));

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
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, TargetPackage.from("org.freeciv.packetgen"), new Import[]{
                                        Import.classIn(org.freeciv.packet.Packet.class),
                                        null,
                                        Import.classIn(List.class)
                                }, "nothing", Collections.<Annotate>emptyList(), "NameOfClass",
                                ClassWriter.DEFAULT_PARENT, Arrays.asList(TargetClass.newKnown(org.freeciv.packet.Packet.class)));

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
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, TargetPackage.from("org.freeciv.packetgen"), new Import[]{Import.classIn(List.class)}, "nothing", Collections.<Annotate>emptyList(), "NameOfClass",
                ClassWriter.DEFAULT_PARENT, Arrays.asList(TargetClass.newKnown(org.freeciv.packet.Packet.class)));
        toWrite.addClassConstant(Visibility.PRIVATE, int.class, "five", literal(5));

        String[] lines = toWrite.toString().split("\n");

        assertTrue("Generated source not as expected " + lines[lines.length - 1].trim(),
                lines[lines.length - 1].trim().endsWith("}"));
        assertTrue("Class has blank line before end " + lines[lines.length - 2].trim(),
                !lines[lines.length - 2].trim().isEmpty());
    }

    @Test public void testClassWriterStyleAlwaysEndWithNewLine() {
        ClassWriter toWrite = new ClassWriter(ClassKind.CLASS, TargetPackage.from("org.freeciv.packetgen"), new Import[]{Import.classIn(List.class)}, "nothing", Collections.<Annotate>emptyList(), "NameOfClass",
                ClassWriter.DEFAULT_PARENT, Arrays.asList(TargetClass.newKnown(org.freeciv.packet.Packet.class)));

        assertTrue("File should end with line break", toWrite.toString().endsWith("\n"));
}

    // Tests based on real examples

    @Test public void testPublicConstructorNoExceptions() {
        String result = Method.newPublicConstructor(Comment.no(),
                Arrays.asList(Var.param(Integer.class, "unit_id")),
                Block.fromStrings("this.unit_id = new UNIT(unit_id)")).toString();

        assertEquals("Generated source not as expected",
                "\t" + "public " + HasAtoms.SELF.get() + "(Integer unit_id) {" + "\n" +
                        "\t" + "\t" + "this.unit_id = new UNIT(unit_id);" + "\n" +
                        "\t" + "}" + "\n",
                result);
    }

    @Test public void testPublicConstructor() {
        TargetClass ioe = TargetClass.newKnown(IOException.class);
        Var<TargetClass> pFrom = Var.param(TargetClass.newKnown(DataInput.class), "from");
        Var<AnInt> pHeaderLen = Var.param(int.class, "headerLen");
        Var<AnInt> pPacket = Var.param(int.class, "packet");
        Block body = new Block(
                BuiltIn.<AValue>toCode("this.unit_id = new UNIT(from)"),
                IF(BuiltIn.<ABool>toCode("getNumber() != packet"),
                        Block.fromStrings("throw new IOException(\"Tried to create package PACKET_CITY_NAME_SUGGESTION_REQ but packet number was \" + packet)")));
        body.groupBoundary();
        body.addStatement(IF(BuiltIn.<ABool>toCode("getEncodedSize() != headerLen"),
                new Block(THROW((TargetClass.newKnown(IOException.class))
                        .newInstance(sum(
                                literal("Package size in header and Java packet not the same. Header: "),
                                BuiltIn.<AValue>toCode("headerLen"),
                                literal(" Packet: "),
                                BuiltIn.<AValue>toCode("getEncodedSize()")))))));
        String result = toStringAsIfInAClass(
                Method.newPublicConstructorWithException(Comment.doc("Construct an object from a DataInput", "",
                        Comment.param(pFrom, "data stream that is at the start of the package body"),
                        Comment.param(pHeaderLen, "length from header package"),
                        Comment.param(pPacket, "the number of the packet specified in the header"),
                        Comment.docThrows(TargetClass.newKnown(IOException.class), "if the DataInput has a problem")),
                        Arrays.asList(pFrom, pHeaderLen, pPacket),
                        Arrays.asList(ioe),
                        body));

        assertEquals("Generated source not as expected",
                "\t" + "/**" + "\n" +
                        "\t" + " * Construct an object from a DataInput" + "\n" +
                        "\t" + " * @param from data stream that is at the start of the package body" + "\n" +
                        "\t" + " * @param headerLen length from header package" + "\n" +
                        "\t" + " * @param packet the number of the packet specified in the header" + "\n" +
                        "\t" + " * @throws IOException if the DataInput has a problem" + "\n" +
                        "\t" + " */" + "\n" +
                        "\t" + "public " + HasAtoms.SELF.get() + "(DataInput from, int headerLen, int packet) throws IOException {\n" +
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
                TargetClass.fromClass(String.class), "toString",
                new Block(RETURN(BuiltIn.<AString>toCode("value.toString()")))).toString();

        assertEquals("Generated source not as expected",
                "\tpublic String toString() {\n" +
                        "\t\treturn value.toString();\n" +
                        "\t}\n",
                result);
    }

    private CodeAtoms codeAtomsAddRefuseNextIfAdd() {
        CodeAtoms atoms = new CodeAtoms();
        atoms.add(HasAtoms.ADD);
        atoms.refuseNextIf(new Util.OneCondition<IR.CodeAtom>() {
            @Override
            public boolean isTrueFor(IR.CodeAtom argument) {
                return HasAtoms.ADD.equals(argument);
            }
        });
        return atoms;
    }

    @Test public void codeAtoms_RefuseNextIf_RefusesNextWhenConditionTriggered() {
        CodeAtoms atoms = codeAtomsAddRefuseNextIfAdd();
        atoms.add(HasAtoms.ADD);

        assertEquals(HasAtoms.ADD, atoms.get(0).getAtom());
        assertEquals(1, atoms.toArray().length);
    }

    @Test public void codeAtoms_RefuseNextIf_AcceptsNextWhenConditionNotTriggered() {
        CodeAtoms atoms = codeAtomsAddRefuseNextIfAdd();
        atoms.add(HasAtoms.SUB);

        assertEquals(HasAtoms.ADD, atoms.get(0).getAtom());
        assertEquals(HasAtoms.SUB, atoms.get(1).getAtom());
    }

    @Test public void codeAtoms_RefuseNextIf_ConditionOnlyTriggeredOnNext_WhenWasTriggered() {
        CodeAtoms atoms = codeAtomsAddRefuseNextIfAdd();
        atoms.add(HasAtoms.ADD);
        atoms.add(HasAtoms.ADD);

        assertEquals(HasAtoms.ADD, atoms.get(0).getAtom());
        assertEquals(HasAtoms.ADD, atoms.get(1).getAtom());
        assertEquals(2, atoms.toArray().length);
    }

    @Test public void codeAtoms_RefuseNextIf_ConditionOnlyTriggeredOnNext_WhenWasNotTriggered() {
        CodeAtoms atoms = codeAtomsAddRefuseNextIfAdd();
        atoms.add(HasAtoms.SUB);
        atoms.add(HasAtoms.ADD);

        assertEquals(HasAtoms.ADD, atoms.get(0).getAtom());
        assertEquals(HasAtoms.SUB, atoms.get(1).getAtom());
        assertEquals(HasAtoms.ADD, atoms.get(2).getAtom());
    }

    @Test public void codeAtoms_rewriteRule_rewrites_usingAtom() {
        CodeAtoms atoms = new CodeAtoms();
        atoms.add(HasAtoms.ADD);
        atoms.rewriteRule(new Util.OneCondition<IR.CodeAtom>() {
            @Override
            public boolean isTrueFor(IR.CodeAtom argument) {
                return true;
            }
        }, HasAtoms.SUB);
        atoms.add(HasAtoms.MUL);

        assertEquals("Test assumption changed", HasAtoms.ADD, atoms.get(0).getAtom());
        assertEquals("Didn't rewrite", HasAtoms.SUB, atoms.get(1).getAtom());
    }

    @Test public void codeAtoms_rewriteRule_rewrites_usingHasAtoms() {
        CodeAtoms atoms = new CodeAtoms();
        atoms.add(HasAtoms.ADD);
        atoms.rewriteRule(
                new Util.OneCondition<IR.CodeAtom>() {
                    @Override
                    public boolean isTrueFor(IR.CodeAtom argument) {
                        return HasAtoms.MUL.equals(argument);
                    }
                },
                new HasAtoms() {
                    @Override
                    public void writeAtoms(CodeAtoms to) {
                        to.add(SUB);
                        to.add(SUB);
                    }
                });
        atoms.add(HasAtoms.MUL);

        assertEquals("Test assumption changed", HasAtoms.ADD, atoms.get(0).getAtom());
        assertEquals("Didn't rewrite", HasAtoms.SUB, atoms.get(1).getAtom());
        assertEquals("Didn't rewrite", HasAtoms.SUB, atoms.get(2).getAtom());
    }

    @Test public void codeAtoms_rewriteRule_prioritizesTheNewestRule() {
        CodeAtoms atoms = new CodeAtoms();
        atoms.add(HasAtoms.ADD);
        atoms.rewriteRule(new Util.OneCondition<IR.CodeAtom>() {
            @Override
            public boolean isTrueFor(IR.CodeAtom argument) {
                return true;
            }
        }, HasAtoms.SUB);
        atoms.rewriteRule(new Util.OneCondition<IR.CodeAtom>() {
            @Override
            public boolean isTrueFor(IR.CodeAtom argument) {
                return true;
            }
        }, HasAtoms.ADD);
        atoms.add(HasAtoms.MUL);

        assertEquals("Test assumption changed", HasAtoms.ADD, atoms.get(0).getAtom());
        assertNotSame("Picked the oldest rewrite rule", HasAtoms.SUB, atoms.get(1).getAtom());
        assertEquals("Didn't rewrite", HasAtoms.ADD, atoms.get(1).getAtom());
    }

    @Test public void codeAtoms_rewriteRule_hintsAtTheRightPlaces() {
        CodeAtoms atoms = new CodeAtoms();
        atoms.add(HasAtoms.ADD);
        atoms.rewriteRule(
                new Util.OneCondition<IR.CodeAtom>() {
                              @Override
                              public boolean isTrueFor(IR.CodeAtom argument) {
                                  return HasAtoms.MUL.equals(argument);
                              }
                          },
                new HasAtoms() {
                    @Override
                    public void writeAtoms(CodeAtoms to) {
                        to.add(new IR.CodeAtom("Here is the start"));
                        to.add(new IR.CodeAtom("Here is the middle"));
                        to.add(new IR.CodeAtom("Here is the end"));
                    }
                });
        atoms.hintStart("The hint");
        atoms.add(HasAtoms.MUL);
        atoms.hintEnd("The hint");

        assertEquals("Test assumption changed", HasAtoms.ADD, atoms.get(0).getAtom());

        assertTrue("Wrong place for hint", atoms.get(0).getHintsBefore().isEmpty());
        assertTrue("Wrong place for hint", atoms.get(0).getHintsAfter().isEmpty());

        assertEquals("Hint not began", "The hint", atoms.get(1).getHintsBefore().get(0).get());
        assertTrue("Wrong place for hint", atoms.get(1).getHintsAfter().isEmpty());

        assertTrue("Wrong place for hint", atoms.get(2).getHintsBefore().isEmpty());
        assertTrue("Wrong place for hint", atoms.get(2).getHintsAfter().isEmpty());

        assertEquals("Hint not ended", "The hint", atoms.get(3).getHintsAfter().get(0).get());
        assertTrue("Wrong place for hint", atoms.get(3).getHintsBefore().isEmpty());
    }
}
