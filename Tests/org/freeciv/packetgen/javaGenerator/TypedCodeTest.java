/*
 * Copyright (c) 2012, Sveinung Kvilhaugsvik
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
import org.freeciv.packetgen.javaGenerator.IR.CodeAtom;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.Value;
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;
import org.freeciv.packetgen.javaGenerator.formating.CodeStyle;
import org.freeciv.packetgen.javaGenerator.formating.CodeStyle.ScopeStack.ScopeInfo;
import org.freeciv.packetgen.javaGenerator.formating.CodeStyleBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class TypedCodeTest {
    @Test public void targetArrayNewInstance() {
        TargetArray array = new TargetArray("Under", 2, true);
        CodeAtoms asAtoms = new CodeAtoms(array.newInstance(BuiltIn.literal(3), BuiltIn.literal(5)));

        assertEquals("new", asAtoms.get(0).getAtom().get());
        assertEquals("Under", asAtoms.get(1).getAtom().get());
        assertEquals("[", asAtoms.get(2).getAtom().get());
        assertEquals("3", asAtoms.get(3).getAtom().get());
        assertEquals("]", asAtoms.get(4).getAtom().get());
        assertEquals("[", asAtoms.get(5).getAtom().get());
        assertEquals("5", asAtoms.get(6).getAtom().get());
        assertEquals("]", asAtoms.get(7).getAtom().get());
    }

    @Test public void targetArrayNewInstanceFewArgumentsPlacedRight() {
        TargetArray array = new TargetArray("Under", 2, true);
        CodeAtoms asAtoms = new CodeAtoms(array.newInstance(BuiltIn.literal(3)));

        assertEquals("new", asAtoms.get(0).getAtom().get());
        assertEquals("Under", asAtoms.get(1).getAtom().get());
        assertEquals("[", asAtoms.get(2).getAtom().get());
        assertEquals("3", asAtoms.get(3).getAtom().get());
        assertEquals("]", asAtoms.get(4).getAtom().get());
        assertEquals("[", asAtoms.get(5).getAtom().get());
        assertEquals("]", asAtoms.get(6).getAtom().get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void targetArrayNewInstanceToManyArguments() {
        TargetArray array = new TargetArray("Under", 2, true);
        CodeAtoms asAtoms = new CodeAtoms(array.newInstance(BuiltIn.literal(3), BuiltIn.literal(5), BuiltIn.literal(2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void targetArrayNewInstanceDimensionsInText() {
        TargetArray array = new TargetArray("Under[]", 1, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void targetArrayNewInstanceToManyDimensionsInClass() {
        TargetArray array = new TargetArray(int[][].class, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void targetArrayNewInstanceDimensionsInTargetClass() {
        TargetArray array = new TargetArray(new TargetClass("int[][]"), 1, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void targetArrayNewInstanceNoArrayFromClass() {
        TargetArray array = new TargetArray(int.class, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void targetArrayNewInstanceNoArrayFromString() {
        TargetArray array = new TargetArray("int", 0, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void targetArrayNewInstanceNoArrayFromTargetClass() {
        TargetArray array = new TargetArray(new TargetClass(int.class), 0, true);
    }

    @Test public void targetArrayReadEnd() {
        Var arrayVal = Var.param(new TargetArray("Under", 2, true), "arrayVal");
        CodeAtoms asAtoms = new CodeAtoms(arrayVal.call("[]", BuiltIn.literal(3), BuiltIn.literal(5)));

        assertEquals("arrayVal", asAtoms.get(0).getAtom().get());
        assertEquals("[", asAtoms.get(1).getAtom().get());
        assertEquals("3", asAtoms.get(2).getAtom().get());
        assertEquals("]", asAtoms.get(3).getAtom().get());
        assertEquals("[", asAtoms.get(4).getAtom().get());
        assertEquals("5", asAtoms.get(5).getAtom().get());
        assertEquals("]", asAtoms.get(6).getAtom().get());
        assertEquals(7, asAtoms.toArray().length);
    }

    @Test public void targetArrayReadSubArray() {
        Var arrayVal = Var.param(new TargetArray("Under", 2, true), "arrayVal");
        CodeAtoms asAtoms = new CodeAtoms(arrayVal.call("[]", BuiltIn.literal(3)));

        assertEquals("arrayVal", asAtoms.get(0).getAtom().get());
        assertEquals("[", asAtoms.get(1).getAtom().get());
        assertEquals("3", asAtoms.get(2).getAtom().get());
        assertEquals("]", asAtoms.get(3).getAtom().get());
        assertEquals(4, asAtoms.toArray().length);
    }

    @Test public void annotatedField() {
        Annotate annotation = new Annotate("IsAField");
        Var field = Var.field(Arrays.asList(annotation),
                              Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO,
                              "int", "number", null);
        CodeAtoms asAtoms = new CodeAtoms(field);

        assertEquals("@IsAField", asAtoms.get(0).getAtom().get());
        assertEquals("private", asAtoms.get(1).getAtom().get());
        assertEquals("final", asAtoms.get(2).getAtom().get());
        assertEquals("int", asAtoms.get(3).getAtom().get());
        assertEquals("number", asAtoms.get(4).getAtom().get());
    }

    @Test public void breakLineBlock() {
        CodeStyleBuilder<ScopeInfo> builder =
                new CodeStyleBuilder<ScopeInfo>(CodeStyleBuilder.<ScopeInfo>INSERT_SPACE(),
                        ScopeInfo.class);

        builder.whenFirst(builder.condLeftIs(HasAtoms.EOL), builder.condRightIs(HasAtoms.CCommentStart), CodeStyleBuilder.DependsOn.token_both, builder.BREAK_LINE_BLOCK);
        builder.whenFirst(builder.condLeftIs(HasAtoms.EOL), CodeStyleBuilder.DependsOn.token_left, builder.BREAK_LINE);
        builder.whenFirst(builder.condRightIs(HasAtoms.EOL), CodeStyleBuilder.DependsOn.token_right, builder.DO_NOTHING);
        builder.whenFirst(builder.condAtTheBeginning(), EnumSet.<CodeStyleBuilder.DependsOn>noneOf(CodeStyleBuilder.DependsOn.class), builder.DO_NOTHING);

        CodeAtoms toRunOn = new CodeAtoms();
        toRunOn.add(new CodeAtom("A"));
        toRunOn.add(new CodeAtom("b"));
        toRunOn.add(HasAtoms.EOL);
        toRunOn.add(new CodeAtom("C"));
        toRunOn.add(new CodeAtom("d"));
        toRunOn.add(HasAtoms.EOL);
        toRunOn.add(HasAtoms.CCommentStart);
        toRunOn.add(new CodeAtom("comment"));
        toRunOn.add(HasAtoms.CCommentEnd);
        toRunOn.add(new CodeAtom("E"));
        toRunOn.add(new CodeAtom("f"));
        toRunOn.add(HasAtoms.EOL);

        assertEquals("A b;\nC d;\n\n/* comment */ E f;",
                Util.joinStringArray(builder.getStyle().asFormattedLines(toRunOn).toArray(), "\n", "", ""));
    }

    @Test public void breakLineGroupedBlock() {
        CodeStyleBuilder<ScopeInfo> builder =
                new CodeStyleBuilder<ScopeInfo>(CodeStyleBuilder.<ScopeInfo>INSERT_SPACE(),
                        ScopeInfo.class);

        builder.whenFirst(builder.condLeftIs(HasAtoms.EOL), builder.condRightIs(HasAtoms.RSC), CodeStyleBuilder.DependsOn.token_both, builder.BREAK_LINE);
        builder.whenFirst(new Util.OneCondition<ScopeInfo>() {
            @Override
            public boolean isTrueFor(ScopeInfo argument) {
                return CodeStyle.GROUP.equals(argument.seeTopHint());
            }
        }, builder.condLeftIs(HasAtoms.EOL), CodeStyleBuilder.DependsOn.token_left, builder.BREAK_LINE);
        builder.whenFirst(builder.condLeftIs(HasAtoms.EOL), CodeStyleBuilder.DependsOn.token_left, builder.BREAK_LINE_BLOCK);
        builder.whenFirst(builder.condLeftIs(HasAtoms.LSC), CodeStyleBuilder.DependsOn.token_left, builder.BREAK_LINE);
        builder.whenFirst(builder.condLeftIs(HasAtoms.RSC), CodeStyleBuilder.DependsOn.token_left, builder.BREAK_LINE);
        builder.whenFirst(builder.condRightIs(HasAtoms.RSC), CodeStyleBuilder.DependsOn.token_right, builder.BREAK_LINE);
        builder.whenFirst(builder.condRightIs(HasAtoms.EOL), CodeStyleBuilder.DependsOn.token_right, builder.DO_NOTHING);
        builder.whenFirst(builder.condAtTheBeginning(), EnumSet.<CodeStyleBuilder.DependsOn>noneOf(CodeStyleBuilder.DependsOn.class), builder.DO_NOTHING);

        Block haveStatementGroup = new Block();
        Var i = Var.local("int", "i", null);
        Var j = Var.local("int", "j", null);

        haveStatementGroup.groupBoundary(); // redundant. Causes exception unless handled

        haveStatementGroup.addStatement(i);
        haveStatementGroup.addStatement(j);

        haveStatementGroup.groupBoundary();
        haveStatementGroup.groupBoundary(); // twice in a row. Disables the next boundary unless handled

        haveStatementGroup.addStatement(i.assign(BuiltIn.literal(0)));
        haveStatementGroup.addStatement(BuiltIn.inc(i));

        haveStatementGroup.groupBoundary();

        haveStatementGroup.addStatement(j.assign(BuiltIn.sum(i.ref(), i.ref())));

        haveStatementGroup.groupBoundary(); // redundant. Causes exception unless handled

        CodeAtoms toRunOn = new CodeAtoms(haveStatementGroup);

        assertEquals("{\nint i;\nint j;\n\ni = 0;\ni ++;\n\nj = i + i;\n}",
                Util.joinStringArray(builder.getStyle().asFormattedLines(toRunOn).toArray(), "\n", "", ""));
    }

    @Test public void blockEmpty() {
        Block empty = new Block();

        assertEquals(0, empty.numberOfStatements());

        CodeAtoms result = new CodeAtoms();
        empty.writeAtoms(result);

        assertEquals(HasAtoms.LSC, result.get(0).getAtom());
        assertEquals(HasAtoms.RSC, result.get(1).getAtom());
    }

    @Test
    public void block2Statements() {
        Var i = Var.local("int", "i", null);
        Var j = Var.local("int", "j", null);
        Block block = new Block(i, j);

        assertEquals(2, block.numberOfStatements());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLocalAsFieldWhileAskingForGetter() {
        Var notAField = Var.local(Integer.class, "i", null);
        ClassWriter testcase = new ClassWriter(ClassKind.CLASS, new TargetPackage("top"), null, null,
                Collections.<Annotate>emptyList(), "Testcase",
                TargetClass.fromName(null), Collections.<TargetClass>emptyList());

        testcase.addObjectConstantAndGetter(notAField);
    }

    @Test public void longComment() {
        String[] asText = new String[25];
        Arrays.fill(asText, "deliver");
        assertEquals("/*\n" +
                " * deliver deliver deliver deliver deliver deliver deliver deliver deliver deliver deliver deliver\n" +
                " * deliver deliver deliver deliver deliver deliver deliver deliver deliver deliver deliver deliver\n" +
                " * deliver\n" +
                " */",
                Comment.c(asText).toString());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void methodCallManuallyCrashIfNotHandled() {
        MethodCall<AValue> call = new MethodCall<AValue>(TargetMethod.Called.MANUALLY, "call");
        call.writeAtoms(new CodeAtoms());
    }

    @Test(expected = IllegalArgumentException.class)
    public void callNonExistingMethodOnVar() {
        Var aVar = Var.param(Object.class, "aVar");
        aVar.call("thisMethodIsNotOnObject");
    }

    @Test(expected = IllegalArgumentException.class)
    public void callValuedReturnsVoid() {
        TargetClass o = new TargetClass(Object.class);
        o.callV("notify");
    }

    @Test public void callValuedReturns() {
        TargetClass o = new TargetClass(java.util.regex.Pattern.class);
        Value<AValue> theCall = o.callV("compile", BuiltIn.literal("a*"));

        assertNotNull(theCall);
    }
}
