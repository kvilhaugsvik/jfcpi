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
import org.freeciv.packet.fieldtype.ElementsLimit;
import org.freeciv.packetgen.javaGenerator.IR.CodeAtom;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.Import;
import org.freeciv.packetgen.javaGenerator.expression.Value;
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.Returnable;
import org.freeciv.packetgen.javaGenerator.formating.CodeStyle;
import org.freeciv.packetgen.javaGenerator.formating.ScopeStack.ScopeInfo;
import org.freeciv.packetgen.javaGenerator.formating.CodeStyleBuilder;
import org.freeciv.packetgen.javaGenerator.testData.TheChildReferredToUseOnlyOnce;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class TypedCodeTest {
    @Test public void targetArrayNewInstance() {
        TargetArray array = TargetArray.from(TargetPackage.TOP_LEVEL_AS_STRING, "Under", 2);
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
        TargetArray array = TargetArray.from(TargetPackage.TOP_LEVEL_AS_STRING, "Under", 2);
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
        TargetArray array = TargetArray.from(TargetPackage.TOP_LEVEL_AS_STRING, "Under", 2);
        CodeAtoms asAtoms = new CodeAtoms(array.newInstance(BuiltIn.literal(3), BuiltIn.literal(5), BuiltIn.literal(2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void targetArrayNewInstanceDimensionsInText() {
        TargetArray array = TargetArray.from(TargetPackage.TOP_LEVEL_AS_STRING, "Under[]", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void targetArrayNewInstanceToManyDimensionsInClass() {
        TargetArray array = TargetArray.from(int[][].class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void targetArrayNewInstanceDimensionsInTargetClass() {
        TargetArray array = TargetArray.from(TargetClass.fromClass(int[][].class), 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void targetArrayNewInstanceNoArrayFromClass() {
        TargetArray array = TargetArray.from(int.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void targetArrayNewInstanceNoArrayFromString() {
        TargetArray array = TargetArray.from(TargetPackage.TOP_LEVEL_AS_STRING, "int", 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void targetArrayNewInstanceNoArrayFromTargetClass() {
        TargetArray array = TargetArray.from(TargetClass.fromClass(int.class), 0);
    }

    @Test public void targetArrayReadEnd() {
        Var arrayVal = Var.param(TargetArray.from(TargetPackage.TOP_LEVEL_AS_STRING, "Under", 2), "arrayVal");
        CodeAtoms asAtoms = new CodeAtoms(arrayVal.ref().<Returnable>call("[]", BuiltIn.literal(3), BuiltIn.literal(5)));

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
        Var arrayVal = Var.param(TargetArray.from(TargetPackage.TOP_LEVEL_AS_STRING, "Under", 2), "arrayVal");
        CodeAtoms asAtoms = new CodeAtoms(arrayVal.ref().<Returnable>call("[]", BuiltIn.literal(3)));

        assertEquals("arrayVal", asAtoms.get(0).getAtom().get());
        assertEquals("[", asAtoms.get(1).getAtom().get());
        assertEquals("3", asAtoms.get(2).getAtom().get());
        assertEquals("]", asAtoms.get(3).getAtom().get());
        assertEquals(4, asAtoms.toArray().length);
    }

    @Test public void targetArray_read_length() {
        Value<AValue> arrayVal = Var.param(TargetArray.from(TargetPackage.TOP_LEVEL_AS_STRING, "Under", 1), "arrayVal")
                .ref().callV("length");
        CodeAtoms asAtoms = new CodeAtoms(arrayVal);

        assertEquals("arrayVal.length", IR.joinSqueeze(asAtoms.toArray()));
    }

    @Test public void annotatedField() {
        Annotate annotation = new Annotate("IsAField");
        Var field = Var.field(Arrays.asList(annotation),
                Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO,
                int.class, "number", null);
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
        Var i = Var.local(int.class, "i", null);
        Var j = Var.local(int.class, "j", null);

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
        Var i = Var.local(int.class, "i", null);
        Var j = Var.local(int.class, "j", null);
        Block block = new Block(i, j);

        assertEquals(2, block.numberOfStatements());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLocalAsFieldWhileAskingForGetter() {
        Var notAField = Var.local(Integer.class, "i", null);
        ClassWriter testcase = new ClassWriter(ClassKind.CLASS, TargetPackage.TOP_LEVEL, Imports.are(), null,
                Collections.<Annotate>emptyList(), "Testcase",
                ClassWriter.DEFAULT_PARENT, Collections.<TargetClass>emptyList());

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
        aVar.ref().<Returnable>call("thisMethodIsNotOnObject");
    }

    @Test(expected = IllegalArgumentException.class)
    public void callValuedReturnsVoid() {
        TargetClass o = TargetClass.fromClass(Object.class);
        o.callV("notify");
    }

    @Test public void callValuedReturns() {
        TargetClass o = TargetClass.fromClass(Pattern.class);
        Value<AValue> theCall = o.callV("compile", BuiltIn.literal("a*"));

        assertNotNull(theCall);
    }

    @Test public void targetMethod_readField_static() throws NoSuchFieldException {
        TargetMethod method = new TargetMethod(Pattern.class.getField("LITERAL"));
        Value<AValue> theCall = method.callV();

        assertEquals("java.util.regex.Pattern.LITERAL", IR.joinSqueeze(new CodeAtoms(theCall).toArray()));
    }

    @Test public void targetMethod_readField_dynamic() throws NoSuchFieldException {
        TargetMethod method = new TargetMethod(ElementsLimit.class.getField("full_array_size"));
        Value<AValue> theCall = method.callV(BuiltIn.<AValue>toCode("a"));

        assertEquals("a.full_array_size", IR.joinSqueeze(new CodeAtoms(theCall).toArray()));
    }

    @Test public void targetMethod_readField_madeInTargetClass() throws NoSuchFieldException {
        Value<AValue> theCall = TargetClass.fromClass(Pattern.class).callV("LITERAL");

        assertEquals("java.util.regex.Pattern.LITERAL", IR.joinSqueeze(new CodeAtoms(theCall).toArray()));
    }

    @Test public void addressInTopLevelPackage_NoArtifactsAdded() {
        Address inTop = new Address(TargetPackage.TOP_LEVEL, new CodeAtom("WhoNeedPackets"));
        CodeAtoms atoms = new CodeAtoms(inTop);
        assertEquals("WhoNeedPackets", atoms.get(0).getAtom().get());
        assertEquals(1, atoms.toArray().length);
    }

    @Test public void address_getFirstComponent_onOther_correctComponent() {
        CodeAtom firstComponent = TargetPackage.from("test.of").getFirstComponent();
        assertEquals("test", firstComponent.get());
    }

    @Test public void address_getFirstComponent_onAbstract_correctComponent() {
        CodeAtom firstComponent = new Address(TargetPackage.TOP_LEVEL, new CodeAtom("WhoNeedPackets")).getFirstComponent();
        assertEquals("WhoNeedPackets", firstComponent.get());
    }

    @Test(expected = NoSuchElementException.class)
    public void address_special_hasNoFirstComponent() {
        TargetPackage.TOP_LEVEL.getFirstComponent();
    }

    @Test public void address_HintsIdentifyTheRightParts() {
        CodeAtoms atoms = new CodeAtoms();
        atoms.rewriteRule(new Util.OneCondition<CodeAtom>() {
            @Override
            public boolean isTrueFor(CodeAtom atom) {
                return HasAtoms.SELF.equals(atom);
            }
        }, TargetClass.fromName("myPackage", "MyClass").scopeUnknown());
        Address bigAddress = Var.field(Collections.<Annotate>emptyList(), Visibility.PUBLIC, Scope.CLASS, Modifiable.NO,
                int.class, "myField", null).ref();
        bigAddress.writeAtoms(atoms);

        assertEquals(Var.Reference.class.getCanonicalName(), atoms.get(0).getHintsBegin().get(0));
        assertEquals(TargetClass.class.getCanonicalName(), atoms.get(0).getHintsBegin().get(1));
        assertEquals(TargetPackage.class.getCanonicalName(), atoms.get(0).getHintsBegin().get(2));
        assertEquals("myPackage", atoms.get(0).getAtom().get());
        assertEquals(TargetPackage.class.getCanonicalName(), atoms.get(0).getHintsEnd().get(0));

        assertEquals(".", atoms.get(1).getAtom().get());

        assertEquals("MyClass", atoms.get(2).getAtom().get());
        assertEquals(TargetClass.class.getCanonicalName(), atoms.get(2).getHintsEnd().get(0));

        assertEquals(".", atoms.get(3).getAtom().get());

        assertEquals("myField", atoms.get(4).getAtom().get());
        assertEquals(Var.Reference.class.getCanonicalName(), atoms.get(4).getHintsEnd().get(0));
    }

    @Test public void targetPackage_topLevel_fromClass() {
        assertEquals(TargetPackage.TOP_LEVEL, TargetPackage.from((Package)null));
    }

    @Test public void targetPackage_topLevel_fromString() {
        assertEquals(TargetPackage.TOP_LEVEL, TargetPackage.from(TargetPackage.TOP_LEVEL_AS_STRING));
    }

    @Test public void targetPackageCached() {
        TargetPackage p1 = TargetPackage.from(Package.getPackage("org.freeciv.packetgen.javaGenerator"));
        TargetPackage p2 = TargetPackage.from("org.freeciv.packetgen.javaGenerator");
        assertEquals("Did the work twice", p1, p2);
    }

    @Test public void targetClassCacheStringAndClass() {
        TargetClass fromString = TargetClass.fromName("java.lang", "String");
        TargetClass fromClass = TargetClass.fromClass(String.class);

        assertEquals("The representations are different", fromClass, fromString);
    }

    public static void justExist() {}
    @Test public void targetClassFromClassAddsInfoToCached() {
        TargetClass fromString = TargetClass.fromName("org.freeciv.packetgen.javaGenerator", "TypedCodeTest");
        try {
            fromString.call("justExist");
            fail("Test makes bad assumption");
        } catch (IllegalArgumentException e) {
        }

        TargetClass fromClass = TargetClass.fromClass(this.getClass());
        assertNotNull(fromClass.call("justExist"));
    }

    @Test public void targetClassMethodRegisteredOnOneScopeIsThereInTheOther() {
        TargetClass aClass = new TargetClass(TargetPackage.TOP_LEVEL_AS_STRING, "RandomName5792452", true);
        try {
            aClass.call("notThereYet");
            fail("Test makes bad assumption");
        } catch (IllegalArgumentException e) {}
        aClass.scopeUnknown().register(new TargetMethod(aClass, "notThereYet",
                TargetClass.fromClass(int.class), TargetMethod.Called.STATIC));

        assertNotNull("Method registered in unknown scope not there in known scope.", aClass.call("notThereYet"));
    }

    @Test public void targetClassScopeChangeOnlyMakesTwoCopiesStartInScope() {
        TargetClass aClass = new TargetClass(TargetPackage.TOP_LEVEL_AS_STRING, "Thing", true);
        assertEquals("The original wasn't kept. A new one was created.", aClass, aClass.scopeUnknown().scopeKnown());
    }

    @Test public void targetClassScopeChangeOnlyMakesTwoCopiesStartNotInScope() {
        TargetClass aClass = new TargetClass(TargetPackage.TOP_LEVEL_AS_STRING, "Thing", false);
        assertEquals("The original isn't kept. A new one was created.", aClass, aClass.scopeKnown().scopeUnknown());
    }

    @Test public void targetClass_inheritance_fromParent() {
        TargetClass child = TargetClass.fromName("testPack", "Child");
        TargetClass parent = TargetClass.fromName("testPack", "Parent");
        parent.register(new TargetMethod(parent, "methodNotOnChild", TargetClass.fromClass(int.class), TargetMethod.Called.STATIC));
        child.setParent(parent);

        assertNotNull(child.callV("methodNotOnChild"));
    }

    @Test public void targetClass_inheritance_fromGrandParent() {
        TargetClass child = TargetClass.fromName("testPack", "Child");
        TargetClass parent = TargetClass.fromName("testPack", "Parent");
        child.setParent(parent);
        TargetClass grandParent = TargetClass.fromName("testPack", "GrandParent");
        parent.setParent(grandParent);

        grandParent.register(new TargetMethod(grandParent, "methodNotOnChildOrParent", TargetClass.fromClass(int.class), TargetMethod.Called.STATIC));

        assertNotNull(child.callV("methodNotOnChildOrParent"));
    }

    @Test(expected = IllegalStateException.class)
    public void targetClass_inheritance_hasParentAlready() {
        TargetClass child = TargetClass.fromName("testPack", "Child");
        TargetClass parent = TargetClass.fromName("testPack", "Parent");
        child.setParent(parent);
        TargetClass triesToBeParent = TargetClass.fromName("testPack", "StepParent");
        child.setParent(triesToBeParent);
    }

    @Test public void targetClass_inheritance_fromClassFindsParent() {
        TargetClass child = TargetClass.fromClass(String.class);
        Var myString = Var.param(child, "myString");

        assertNotNull("Failed to inherit dynamic method wait from Class Object", myString.ref().<Returnable>call("wait"));
    }

    @Test public void targetClass_inheritance_Cache_FromStringWillNotDenyParentInfo() {
        TargetClass child = TargetClass.fromName("org.freeciv.packetgen.javaGenerator.testData",
                "TheChildReferredToUseOnlyOnce");
        TargetClass parent = TargetClass.fromName("org.freeciv.packetgen.javaGenerator.testData",
                "TheParentReferredToUseOnlyOnce");
        child.setParent(parent);
        TargetClass childIsClass = TargetClass.fromClass(TheChildReferredToUseOnlyOnce.class);

        assertNotNull(childIsClass.callV("methodNotOnChild"));
    }

    @Test public void targetClass_fromString_varArg() {
        assertEquals("java.lang.Integer...", TargetClass.fromName("java.lang", "Integer...").getFullAddress());
    }

    @Test public void scopeData_class_notInScope() {
        Imports.ScopeDataForJavaFile scopeData = Imports.are().getScopeData(TargetClass.fromName("org.one", "User"));
        CodeAtoms used = new CodeAtoms(TargetClass.fromName("org.other", "Used"));

        assertFalse("Not imported. In other package. Shouldn't be in scope", scopeData.isInScope(used.toArray()));
    }

    @Test public void scopeData_class_inScope_sinceInSamePackage() {
        Imports.ScopeDataForJavaFile scopeData = Imports.are().getScopeData(TargetClass.fromName("org.here", "User"));
        CodeAtoms used = new CodeAtoms(TargetClass.fromName("org.here", "Used"));

        assertTrue("Should be in scope since in same package", scopeData.isInScope(used.toArray()));
    }

    @Test public void scopeData_class_inScope_sincePackageImported() {
        scopeData_class_inScope_sinceImported(Import.allIn(TargetPackage.from("org.there")), "all classes in package");
    }

    private void scopeData_class_inScope_sinceImported(Import<?> theImport, String wasImported) {
        Imports.ScopeDataForJavaFile scopeData = Imports.are(theImport)
                .getScopeData(TargetClass.fromName("org.here", "User"));
        CodeAtoms used = new CodeAtoms(TargetClass.fromName("org.there", "Used"));

        assertTrue("Should be in scope since " + wasImported + " was imported", scopeData.isInScope(used.toArray()));
    }

    @Test public void scopeData_class_inScope_sinceClassImported() {
        scopeData_class_inScope_sinceImported(Import.classIn(TargetClass.fromName("org.there", "Used")), "the class");
    }

    @Test public void scopeData_class_inScope_sinceInJavaLang() {
        Imports.ScopeDataForJavaFile scopeData = Imports.are().getScopeData(TargetClass.fromName("org.here", "User"));
        CodeAtoms used = new CodeAtoms(TargetClass.fromClass(String.class).scopeUnknown());

        assertTrue("String is in scope since its in java.lang", scopeData.isInScope(used.toArray()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void scopeData_invalidEnd() {
        Imports.ScopeDataForJavaFile scopeData = Imports.are().getScopeData(TargetClass.fromName("org.one", "User"));
        CodeAtoms used = new CodeAtoms(TargetClass.fromName("org.other", "Used"));
        used.add(new CodeAtom("Failer"));

        assertFalse(scopeData.isInScope(used.toArray()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void scopeData_invalidStart() {
        Imports.ScopeDataForJavaFile scopeData = Imports.are().getScopeData(TargetClass.fromName("org.one", "User"));
        CodeAtoms used = new CodeAtoms(new CodeAtom("Failer"));
        TargetClass.fromName("org.other", "Used").writeAtoms(used);

        assertFalse(scopeData.isInScope(used.toArray()));
    }
}