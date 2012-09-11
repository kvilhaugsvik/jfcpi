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
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.formating.CodeStyle;
import org.freeciv.packetgen.javaGenerator.formating.CodeStyle.ScopeStack.ScopeInfo;
import org.freeciv.packetgen.javaGenerator.formating.CodeStyleBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class TypedCodeTest {
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
                new CodeStyleBuilder<ScopeInfo>(CodeStyle.Action.INSERT_SPACE,
                        ScopeInfo.class);

        builder.whenBetween(HasAtoms.EOL, HasAtoms.CCommentStart, CodeStyle.Action.BREAK_LINE_BLOCK);
        builder.whenAfter(HasAtoms.EOL, CodeStyle.Action.BREAK_LINE);
        builder.whenBefore(HasAtoms.EOL, CodeStyle.Action.DO_NOTHING);
        builder.atTheBeginning(CodeStyle.Action.DO_NOTHING);

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
                new CodeStyleBuilder<ScopeInfo>(CodeStyle.Action.INSERT_SPACE,
                        ScopeInfo.class);

        builder.whenBetween(HasAtoms.EOL, HasAtoms.RSC, CodeStyle.Action.BREAK_LINE);
        builder.whenAfter(HasAtoms.EOL, CodeStyle.Action.BREAK_LINE, new Util.OneCondition<ScopeInfo>() {
            @Override
            public boolean isTrueFor(ScopeInfo argument) {
                return CodeStyle.GROUP.equals(argument.seeTopHint());
            }
        });
        builder.whenAfter(HasAtoms.EOL, CodeStyle.Action.BREAK_LINE_BLOCK);
        builder.whenAfter(HasAtoms.LSC, CodeStyle.Action.BREAK_LINE);
        builder.whenAfter(HasAtoms.RSC, CodeStyle.Action.BREAK_LINE);
        builder.whenBefore(HasAtoms.RSC, CodeStyle.Action.BREAK_LINE);
        builder.whenBefore(HasAtoms.EOL, CodeStyle.Action.DO_NOTHING);
        builder.atTheBeginning(CodeStyle.Action.DO_NOTHING);

        Block haveStatementGroup = new Block();
        Var i = Var.local("int", "i", null);
        Var j = Var.local("int", "j", null);

        haveStatementGroup.groupBoundary(); // redundant. Causes exception unless handled

        haveStatementGroup.addStatement(i);
        haveStatementGroup.addStatement(j);

        haveStatementGroup.groupBoundary();
        haveStatementGroup.groupBoundary(); // twice in a row. Disables the next boundary unless handled

        haveStatementGroup.addStatement(i.assign(BuiltIn.asAValue("0")));
        haveStatementGroup.addStatement(BuiltIn.inc(i));

        haveStatementGroup.groupBoundary();

        haveStatementGroup.addStatement(j.assign(BuiltIn.sum(i.ref(), i.ref())));

        haveStatementGroup.groupBoundary(); // redundant. Causes exception unless handled

        CodeAtoms toRunOn = new CodeAtoms(haveStatementGroup);

        assertEquals("{\nint i;\nint j;\n\ni = 0;\ni ++;\n\nj = i + i;\n}",
                Util.joinStringArray(builder.getStyle().asFormattedLines(toRunOn).toArray(), "\n", "", ""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLocalAsFieldWhileAskingForGetter() {
        Var notAField = Var.local(Integer.class, "i", null);
        ClassWriter testcase = new ClassWriter(ClassKind.CLASS, new TargetPackage("top"), null, null,
                Collections.<Annotate>emptyList(), "Testcase", null, null);

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
}
