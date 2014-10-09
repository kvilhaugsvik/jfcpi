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

package com.kvilhaugsvik.javaGenerator;

import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import org.freeciv.utility.Util;
import org.freeciv.packet.fieldtype.ElementsLimit;
import com.kvilhaugsvik.javaGenerator.expression.MethodCall;
import com.kvilhaugsvik.javaGenerator.expression.Reference;
import com.kvilhaugsvik.javaGenerator.representation.CodeAtoms;
import com.kvilhaugsvik.javaGenerator.representation.HasAtoms;
import com.kvilhaugsvik.javaGenerator.representation.IR;
import com.kvilhaugsvik.javaGenerator.representation.IR.CodeAtom;
import com.kvilhaugsvik.javaGenerator.typeBridge.Value;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AValue;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.Returnable;
import com.kvilhaugsvik.javaGenerator.formating.TokensToStringStyle;
import com.kvilhaugsvik.javaGenerator.formating.ScopeStack.ScopeInfo;
import com.kvilhaugsvik.javaGenerator.formating.CodeStyleBuilder;
import com.kvilhaugsvik.javaGenerator.testData.TheChildReferredToUseOnlyOnce;
import org.junit.Test;

import java.util.*;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class TypedCodeTest {
    @Test public void literalDouble() {
        assertAtomsAre(new String[]{"3.16d"}, BuiltIn.literal(3.16d));
    }

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
        // TODO: Bug: TargetClass.fromClass may load old working TargetArray
        TargetArray array = TargetArray.from(TargetClass.from(char[][].class), 1);
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
        TargetArray array = TargetArray.from(TargetClass.from(int.class), 0);
    }

    @Test public void targetArrayReadEnd() {
        Var arrayVal = Var.param(TargetArray.from(TargetPackage.TOP_LEVEL_AS_STRING, "Under", 2), "arrayVal");
        CodeAtoms asAtoms = new CodeAtoms(arrayVal.ref().<Returnable>call("get", BuiltIn.literal(3), BuiltIn.literal(5)));

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
        CodeAtoms asAtoms = new CodeAtoms(arrayVal.ref().<Returnable>call("get", BuiltIn.literal(3)));

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

    @Test public void annotatedField_codeGen() {
        Annotate annotation = new Annotate(TargetClass.from(TargetPackage.TOP_LEVEL_AS_STRING, "IsAField"));
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

    @Test public void annotatedField_isAnnotated() {
        Annotate annotation = new Annotate(TargetClass.from(TargetPackage.TOP_LEVEL_AS_STRING, "IsAField"));
        Var field = Var.field(Arrays.asList(annotation),
                Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO,
                int.class, "number", null);

        assertTrue("Annotation missing",
                field.isAnnotatedUsing(TargetClass.from(TargetPackage.TOP_LEVEL_AS_STRING, "IsAField")));
        assertFalse("Wrong annotation",
                field.isAnnotatedUsing(TargetClass.from(TargetPackage.TOP_LEVEL_AS_STRING, "NotAnnotatedUsing")));
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
                return TokensToStringStyle.GROUP.equals(argument.seeTopHint());
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
        TargetClass o = TargetClass.from(Object.class);
        o.callV("notify");
    }

    @Test public void callValuedReturns() {
        TargetClass o = TargetClass.from(Pattern.class);
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
        Value<AValue> theCall = TargetClass.from(Pattern.class).callV("LITERAL");

        assertEquals("java.util.regex.Pattern.LITERAL", IR.joinSqueeze(new CodeAtoms(theCall).toArray()));
    }

    @Test public void addressInTopLevelPackage_NoArtifactsAdded() {
        Address inTop = new TargetClass(TargetPackage.TOP_LEVEL, Arrays.asList(new CodeAtom("WhoNeedPackets")), ClassKind.CLASS, Collections.<HasAtoms>emptyList(), new HashMap<String, TargetMethod>(), null);
        CodeAtoms atoms = new CodeAtoms(inTop);
        assertEquals("WhoNeedPackets", atoms.get(0).getAtom().get());
        assertEquals(1, atoms.toArray().length);
    }

    @Test public void address_getFirstComponent_onOther_correctComponent() {
        CodeAtom firstComponent = TargetPackage.from("test.of").getFirstComponent();
        assertEquals("test", firstComponent.get());
    }

    @Test public void address_getFirstComponent_onAbstract_correctComponent() {
        CodeAtom firstComponent = new TargetClass(TargetPackage.TOP_LEVEL, Arrays.asList(new CodeAtom("WhoNeedPackets")), ClassKind.CLASS, Collections.<HasAtoms>emptyList(), new HashMap<String, TargetMethod>(), null).getFirstComponent();
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
        }, TargetClass.from("myPackage", "MyClass"));
        Address bigAddress = Var.field(Collections.<Annotate>emptyList(), Visibility.PUBLIC, Scope.CLASS, Modifiable.NO,
                int.class, "myField", null).ref();
        bigAddress.writeAtoms(atoms);

        assertEquals(Reference.class.getCanonicalName(), atoms.get(0).getHintsBegin().get(0));
        assertEquals(TargetClass.class.getCanonicalName(), atoms.get(0).getHintsBegin().get(1));
        assertEquals(TargetPackage.class.getCanonicalName(), atoms.get(0).getHintsBegin().get(2));
        assertEquals("myPackage", atoms.get(0).getAtom().get());
        assertEquals(TargetPackage.class.getCanonicalName(), atoms.get(0).getHintsEnd().get(0));

        assertEquals(".", atoms.get(1).getAtom().get());

        assertEquals("MyClass", atoms.get(2).getAtom().get());
        assertEquals(TargetClass.class.getCanonicalName(), atoms.get(2).getHintsEnd().get(0));

        assertEquals(".", atoms.get(3).getAtom().get());

        assertEquals("myField", atoms.get(4).getAtom().get());
        assertEquals(Reference.class.getCanonicalName(), atoms.get(4).getHintsEnd().get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void reference_toUndeclaredLocalOfUnknownType_mustBeLocal() {
        Reference.toUndeclaredLocalOfUnknownType("java.util.regex.Pattern.COMMENTS");
    }

    @Test public void reference_toUndeclaredLocalOfUnknownType_possible() {
        Reference.toUndeclaredLocalOfUnknownType("value");
    }

    @Test public void targetPackage_topLevel_fromClass() {
        assertEquals(TargetPackage.TOP_LEVEL, TargetPackage.from((Package)null));
    }

    @Test public void targetPackage_topLevel_fromString() {
        assertEquals(TargetPackage.TOP_LEVEL, TargetPackage.from(TargetPackage.TOP_LEVEL_AS_STRING));
    }

    @Test public void targetPackageCached() {
        TargetPackage p1 = TargetPackage.from(Package.getPackage("com.kvilhaugsvik.javaGenerator"));
        TargetPackage p2 = TargetPackage.from("com.kvilhaugsvik.javaGenerator");
        assertEquals("Did the work twice", p1, p2);
    }

    @Test public void targetClassCacheStringAndClass() {
        TargetClass fromString = TargetClass.from("java.lang", "String");
        TargetClass fromClass = TargetClass.from(String.class);

        assertEquals("The representations are different", fromClass, fromString);
    }

    public static void justExist() {}
    @Test public void targetClassFromClassAddsInfoToCached() {
        TargetClass fromString = TargetClass.from("com.kvilhaugsvik.javaGenerator", "TypedCodeTest");
        try {
            fromString.call("justExist");
            fail("Test makes bad assumption");
        } catch (IllegalArgumentException e) {
        }

        TargetClass fromClass = TargetClass.from(this.getClass());
        assertNotNull(fromClass.call("justExist"));
    }

    @Test public void targetClass_inheritance_fromParent() {
        TargetClass child = TargetClass.from("testPack", "Child");
        TargetClass parent = TargetClass.from("testPack", "Parent");
        parent.register(new TargetMethod(parent, "methodNotOnChild", TargetClass.from(int.class), TargetMethod.Called.STATIC));
        child.setParent(parent);

        assertNotNull(child.callV("methodNotOnChild"));
    }

    @Test public void targetClass_inheritance_fromGrandParent() {
        TargetClass child = TargetClass.from("testPack", "Child");
        TargetClass parent = TargetClass.from("testPack", "Parent");
        child.setParent(parent);
        TargetClass grandParent = TargetClass.from("testPack", "GrandParent");
        parent.setParent(grandParent);

        grandParent.register(new TargetMethod(grandParent, "methodNotOnChildOrParent", TargetClass.from(int.class), TargetMethod.Called.STATIC));

        assertNotNull(child.callV("methodNotOnChildOrParent"));
    }

    @Test(expected = IllegalStateException.class)
    public void targetClass_inheritance_hasParentAlready() {
        TargetClass child = TargetClass.from("testPack", "Child");
        TargetClass parent = TargetClass.from("testPack", "Parent");
        child.setParent(parent);
        TargetClass triesToBeParent = TargetClass.from("testPack", "StepParent");
        child.setParent(triesToBeParent);
    }

    @Test public void targetClass_inheritance_fromClassFindsParent() {
        TargetClass child = TargetClass.from(String.class);
        Var myString = Var.param(child, "myString");

        assertNotNull("Failed to inherit dynamic method wait from Class Object", myString.ref().<Returnable>call("wait"));
    }

    @Test public void targetClass_inheritance_Cache_FromStringWillNotDenyParentInfo() {
        TargetClass child = TargetClass.from("com.kvilhaugsvik.javaGenerator.testData",
                "TheChildReferredToUseOnlyOnce");
        TargetClass parent = TargetClass.from("com.kvilhaugsvik.javaGenerator.testData",
                "TheParentReferredToUseOnlyOnce");
        child.setParent(parent);
        TargetClass childIsClass = TargetClass.from(TheChildReferredToUseOnlyOnce.class);

        assertNotNull(childIsClass.callV("methodNotOnChild"));
    }

    @Test public void targetClass_fromString_varArg() {
        assertEquals("java.lang.Integer...", TargetArray.varArg(TargetClass.from("java.lang", "Integer")).getFullAddress());
    }

    @Test public void targetClass_inner_fromString() {
        assertEquals("Target class representing an inner class gave the wrong address",
                "java.util.AbstractMap.SimpleImmutableEntry",
                TargetClass.from(java.util.AbstractMap.SimpleImmutableEntry.class).getFullAddress());
    }

    @Test public void targetClass_inner_fromClass() {
        assertEquals("Target class representing an inner class gave the wrong address",
                "top.next.HostClass.Inner", TargetClass.from("top.next", "HostClass.Inner").getFullAddress());
    }

    @Test public void targeClass_generics_addsTypeParam() {
        final TargetClass orig = TargetClass.from(HashMap.class);
        final TargetClass typeArgs =
                orig.addGenericTypeArguments(Arrays.asList(TargetClass.from(String.class), TargetClass.from(Integer.class)));

        assertAtomsAre(new String[]{
                "java",
                ".",
                "util",
                ".",
                "HashMap",
                "<",
                "java",
                ".",
                "lang",
                ".",
                "String",
                ",",
                "java",
                ".",
                "lang",
                ".",
                "Integer",
                ">"
        }, typeArgs);
    }

    @Test public void targeClass_generics_originalLeftAlone() {
        final TargetClass orig = TargetClass.from(HashMap.class);
        final TargetClass typeArgs =
                orig.addGenericTypeArguments(Arrays.asList(TargetClass.from(String.class), TargetClass.from(Integer.class)));

        assertAtomsAre(new String[]{
                "java",
                ".",
                "util",
                ".",
                "HashMap"
        }, orig);
    }

    @Test public void targeClass_generics_hasMethods() {
        final TargetClass orig = TargetClass.from(List.class);
        final TargetClass typeArgs =
                orig.addGenericTypeArguments(Arrays.asList(TargetClass.from(String.class)));

        final Value<AValue> result = Var.param(typeArgs, "var").ref().callV("get", BuiltIn.literal(2));
        final String[] expected = {
                "var",
                ".",
                "get",
                "(",
                "2",
                ")"
        };
        assertAtomsAre(expected, result);
    }

    @Test public void scopeData_class_notInScope() {
        Imports.ScopeDataForJavaFile scopeData = Imports.are().getScopeData(TargetClass.from("org.one", "User"));
        CodeAtoms used = new CodeAtoms(TargetClass.from("org.other", "Used"));

        assertFalse("Not imported. In other package. Shouldn't be in scope", scopeData.isInScope(used.toArray()));
    }

    @Test public void scopeData_class_inScope_sinceInSamePackage() {
        Imports.ScopeDataForJavaFile scopeData = Imports.are().getScopeData(TargetClass.from("org.here", "User"));
        CodeAtoms used = new CodeAtoms(TargetClass.from("org.here", "Used"));

        assertTrue("Should be in scope since in same package", scopeData.isInScope(used.toArray()));
    }

    @Test public void scopeData_class_inScope_sincePackageImported() {
        scopeData_class_inScope_sinceImported(Import.allIn(TargetPackage.from("org.there")), "all classes in package");
    }

    private void scopeData_class_inScope_sinceImported(Import<?> theImport, String wasImported) {
        Imports.ScopeDataForJavaFile scopeData = Imports.are(theImport)
                .getScopeData(TargetClass.from("org.here", "User"));
        CodeAtoms used = new CodeAtoms(TargetClass.from("org.there", "Used"));

        assertTrue("Should be in scope since " + wasImported + " was imported", scopeData.isInScope(used.toArray()));
    }

    @Test public void scopeData_class_inScope_sinceClassImported() {
        scopeData_class_inScope_sinceImported(Import.classIn(TargetClass.from("org.there", "Used")), "the class");
    }

    @Test public void scopeData_class_inScope_sinceInJavaLang() {
        Imports.ScopeDataForJavaFile scopeData = Imports.are().getScopeData(TargetClass.from("org.here", "User"));
        CodeAtoms used = new CodeAtoms(TargetClass.from(String.class));

        assertTrue("String is in scope since its in java.lang", scopeData.isInScope(used.toArray()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void scopeData_invalidEnd() {
        Imports.ScopeDataForJavaFile scopeData = Imports.are().getScopeData(TargetClass.from("org.one", "User"));
        CodeAtoms used = new CodeAtoms(TargetClass.from("org.other", "Used"));
        used.add(new CodeAtom("Failer"));

        assertFalse(scopeData.isInScope(used.toArray()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void scopeData_invalidStart() {
        Imports.ScopeDataForJavaFile scopeData = Imports.are().getScopeData(TargetClass.from("org.one", "User"));
        CodeAtoms used = new CodeAtoms(new CodeAtom("Failer"));
        TargetClass.from("org.other", "Used").writeAtoms(used);

        assertFalse(scopeData.isInScope(used.toArray()));
    }

    @Test public void innerClass_enum() {
        final ClassWriter outer = new ClassWriter(
                ClassKind.CLASS, TargetPackage.from("top.sub"),
                Imports.are(),
                "hardcoded test",
                Collections.<Annotate>emptyList(),
                "Outer", ClassWriter.DEFAULT_PARENT, Collections.<TargetClass>emptyList());
        final ClassWriter inner = outer.newInnerClass(ClassKind.ENUM, "Inner",
                ClassWriter.DEFAULT_PARENT, Collections.<TargetClass>emptyList());

        assertAtomsAre(new String[]{
                "top", ".", "sub", ".", "Outer", ".", "Inner"
        }, inner.getAddress());
    }

    @Test public void innerClass_subClass() {
        final ClassWriter outer = new ClassWriter(
                ClassKind.CLASS, TargetPackage.from("top.sub"),
                Imports.are(),
                "hardcoded test",
                Collections.<Annotate>emptyList(),
                "Parent", ClassWriter.DEFAULT_PARENT, Collections.<TargetClass>emptyList());
        final ClassWriter inner = outer.newInnerClass(ClassKind.CLASS, "Child",
                outer.getAddress(), Collections.<TargetClass>emptyList());

        assertAtomsAre(new String[]{
                "top", ".", "sub", ".", "Parent", ".", "Child"
        }, inner.getAddress());
    }

    @Test public void innerClass_callMethodOnInnerClass() {
        final ClassWriter outer = new ClassWriter(
                ClassKind.CLASS, TargetPackage.from("top.sub"),
                Imports.are(),
                "hardcoded test",
                Collections.<Annotate>emptyList(),
                "Parent", ClassWriter.DEFAULT_PARENT, Collections.<TargetClass>emptyList());
        final ClassWriter inner = outer.newInnerClass(ClassKind.CLASS, "Child",
                outer.getAddress(), Collections.<TargetClass>emptyList());
        inner.addMethod(Method.newReadClassState(Comment.no(), TargetClass.from(String.class), "getString", new Block(
                BuiltIn.RETURN(BuiltIn.literal("a string"))
        )));

        Typed call = inner.getAddress().callV("getString");
        assertAtomsAre(new String[]{
                "top", ".", "sub", ".", "Parent", ".", "Child", ".", "getString", "(", ")"
        }, call);
    }

    @Test public void autoConstructor_noFields() {
        ClassWriter on = new ClassWriter(ClassKind.CLASS, TargetPackage.TOP_LEVEL, Imports.are(),
                "test", Collections.<Annotate>emptyList(),
                "NoFields", ClassWriter.DEFAULT_PARENT, Collections.<TargetClass>emptyList());
        on.addConstructorFields();

        assertAtomsAre(
                new String[]{
                        "@javax.annotation.Generated",
                        "(",
                        "comments",
                        "=",
                        "\"Auto generated from test\"",
                        ",",
                        "value",
                        "=",
                        "\"com.kvilhaugsvik.javaGenerator.ClassWriter\"",
                        ")",
                        "public",
                        "class",
                        "NoFields",
                        "{",
                        "public",
                        "NoFields",
                        "(",
                        ")",
                        "{",
                        "}",
                        "}"
                },
                on
        );
    }

    @Test public void autoConstructor_twoObjectFields() {
        ClassWriter on = new ClassWriter(ClassKind.CLASS, TargetPackage.TOP_LEVEL, Imports.are(),
                "test", Collections.<Annotate>emptyList(),
                "SomeFields", ClassWriter.DEFAULT_PARENT, Collections.<TargetClass>emptyList());
        on.addConstructorFields();
        on.addObjectConstant(String.class, "field1");
        on.addObjectConstant(int.class, "field2");

        assertAtomsAre(
                new String[]{
                        "@javax.annotation.Generated",
                        "(",
                        "comments",
                        "=",
                        "\"Auto generated from test\"",
                        ",",
                        "value",
                        "=",
                        "\"com.kvilhaugsvik.javaGenerator.ClassWriter\"",
                        ")",
                        "public",
                        "class",
                        "SomeFields",
                        "{",
                        "private",
                        "final",
                        "java",
                        ".",
                        "lang",
                        ".",
                        "String",
                        "field1",
                        ";","private",
                        "final",
                        "int",
                        "field2",
                        ";",
                        "public",
                        "SomeFields",
                        "(",
                        "java",
                        ".",
                        "lang",
                        ".",
                        "String",
                        "field1",
                        ",",
                        "int",
                        "field2",
                        ")",
                        "{",
                        "this",
                        ".",
                        "field1",
                        "=",
                        "field1",
                        ";",
                        "this",
                        ".",
                        "field2",
                        "=",
                        "field2",
                        ";",
                        "}",
                        "}"
                },
                on
        );
    }

    @Test public void autoConstructor_objectFieldAndClassField() {
        ClassWriter on = new ClassWriter(ClassKind.CLASS, TargetPackage.TOP_LEVEL, Imports.are(),
                "test", Collections.<Annotate>emptyList(),
                "SomeFields", ClassWriter.DEFAULT_PARENT, Collections.<TargetClass>emptyList());
        on.addConstructorFields();
        on.addClassConstant(Visibility.PACKAGE, String.class, "onTheClass", null);
        on.addObjectConstant(int.class, "oneEachObject");

        assertAtomsAre(
                new String[]{
                        "@javax.annotation.Generated",
                        "(",
                        "comments",
                        "=",
                        "\"Auto generated from test\"",
                        ",",
                        "value",
                        "=",
                        "\"com.kvilhaugsvik.javaGenerator.ClassWriter\"",
                        ")",
                        "public",
                        "class",
                        "SomeFields",
                        "{",
                        "static",
                        "final",
                        "java",
                        ".",
                        "lang",
                        ".",
                        "String",
                        "onTheClass",
                        ";","private",
                        "final",
                        "int",
                        "oneEachObject",
                        ";",
                        "public",
                        "SomeFields",
                        "(",
                        "int",
                        "oneEachObject",
                        ")",
                        "{",
                        "this",
                        ".",
                        "oneEachObject",
                        "=",
                        "oneEachObject",
                        ";",
                        "}",
                        "}"
                },
                on
        );
    }

    public static void assertAtomsAre(String[] expected, HasAtoms code) {
        assertAtomsAre(expected, new CodeAtoms(code));
    }

    public static void assertAtomsAre(String[] expected, CodeAtoms atoms) {
        for (int i = 0; i < expected.length; i++)
            assertEquals("Element number " + i + " not as expected", expected[i], atoms.get(i).getAtom().get());

        assertEquals("Same start but different length", expected.length, atoms.toArray().length);
    }
}
