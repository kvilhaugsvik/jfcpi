/*
 * Copyright (c) 2011 - 2013. Sveinung Kvilhaugsvik
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

import org.freeciv.utility.Util;
import com.kvilhaugsvik.javaGenerator.expression.EnumElement;
import com.kvilhaugsvik.javaGenerator.expression.Reference;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import com.kvilhaugsvik.javaGenerator.util.Formatted;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AValue;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.Returnable;
import com.kvilhaugsvik.javaGenerator.formating.TokensToStringStyle;
import com.kvilhaugsvik.javaGenerator.representation.CodeAtoms;
import com.kvilhaugsvik.javaGenerator.representation.HasAtoms;
import com.kvilhaugsvik.javaGenerator.representation.IR;

import java.util.*;

import static com.kvilhaugsvik.javaGenerator.util.BuiltIn.*;

public class ClassWriter extends Formatted implements HasAtoms {
    public static final TargetClass DEFAULT_PARENT = TargetClass.from(Object.class);

    private final TargetClass myAddress;
    private final Imports.ScopeDataForJavaFile scopeData;
    private final Visibility visibility;
    private final Scope scope;
    private final ClassKind kind;
    private final LinkedList<Annotate> classAnnotate;
    private final TargetClass parent;
    private final List<TargetClass> implementsInterface;
    private final boolean isOuter;

    private final LinkedHashMap<String, Var> fields = new LinkedHashMap<String, Var>();
    private final LinkedList<Method> methods = new LinkedList<Method>();
    protected final EnumElements enums = new EnumElements();
    private final LinkedHashMap<String, ClassWriter> innerClasses = new LinkedHashMap<String, ClassWriter>();

    private boolean constructorFromAllFields = false;

    public ClassWriter(ClassKind kind, TargetPackage where, Imports imports,
                       String madeFrom, List<Annotate> classAnnotate,
                       String name,
                       TargetClass parent, List<TargetClass> implementsInterface) {
        if (null == name) throw new IllegalArgumentException("No name for class to be generated");
        if (null == where)
            throw new IllegalArgumentException("null given as package. (Did you mean TargetPackage.TOP_LEVEL?)");

        this.myAddress = new TargetClass(where, new ClassWriter.Atom(name));
        myAddress.setParent(parent);

        this.classAnnotate = new LinkedList<Annotate>(classAnnotate);
        this.parent = parent;
        this.implementsInterface = implementsInterface;
        this.kind = kind;

        visibility = Visibility.PUBLIC;
        this.scope = Scope.CLASS;

        if (null != madeFrom) {
            this.classAnnotate.addFirst(commentAutoGenerated(madeFrom));
        }

        this.scopeData = imports.getScopeData(this.myAddress);
        this.isOuter = true;
    }

    private ClassWriter(TargetClass inside, ClassKind kind, String name, TargetClass parent, List<TargetClass> implementsInterface, Imports.ScopeDataForJavaFile scopeData) {
        LinkedList<IR.CodeAtom> classPart = new LinkedList<IR.CodeAtom>();
        classPart.addAll(inside.getTypedClassName());
        classPart.add(new ClassWriter.Atom(name));
        this.myAddress = new TargetClass(inside.getPackage(), classPart);
        this.myAddress.setParent(parent);

        this.classAnnotate = new LinkedList<Annotate>();
        this.visibility = Visibility.PUBLIC;
        this.scope = Scope.CLASS;
        this.kind = kind;
        this.parent = parent;
        this.implementsInterface = implementsInterface;

        this.scopeData = scopeData;
        this.isOuter = false;
    }

    public void addField(Var field) {
        if (field.getScope().equals(Scope.CODE_BLOCK))
            throw new IllegalArgumentException("Can't add a local variable declaration as a field");

        myAddress.register(new TargetMethod(getAddress(), field.getName(), field.getTType(),
                Scope.CLASS.equals(field.getScope()) ?
                        TargetMethod.Called.STATIC_FIELD :
                        TargetMethod.Called.DYNAMIC_FIELD));

        fields.put(field.getName(), field);
    }

    public void addClassConstant(Visibility visibility, TargetClass type, String name, Typed<? extends AValue> value) {
        addField(Var.field(Collections.<Annotate>emptyList(), visibility, Scope.CLASS, Modifiable.NO, type, name, value));
    }

    public void addClassConstant(Visibility visibility, Class type, String name, Typed<? extends AValue> value) {
        addClassConstant(visibility, TargetClass.from(type), name, value);
    }

    public void addObjectConstant(TargetClass type, String name) {
        addField(Var.field(Collections.<Annotate>emptyList(), Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO, type, name, null));
    }

    public void addObjectConstant(Class type, String name) {
        addObjectConstant(TargetClass.from(type), name);
    }

    public void addPublicObjectConstant(TargetClass type, String name) {
        addField(Var.field(Collections.<Annotate>emptyList(), Visibility.PUBLIC, Scope.OBJECT, Modifiable.NO, type, name, null));
    }

    public void addPublicObjectConstant(Class type, String name) {
        addPublicObjectConstant(TargetClass.from(type), name);
    }

    public void addObjectConstantAndGetter(Var field) {
        addField(field);
        addMethod(Method.newPublicReadObjectState(Comment.no(),
                field.getTType(),
                getterNameJavaish(field),
                new Block(RETURN(field.ref()))));
    }

    protected static String getterNameJavaish(Var field) {
        return "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
    }

    public void addMethod(Method toAdd) {
        methods.add(toAdd);
        myAddress.register(toAdd.getAddressOn(this.getAddress()));
    }

    public void addConstructorFields() {
        constructorFromAllFields = true;
    }

    protected void addEnumerated(EnumElement element) {
        assert kind.equals(ClassKind.ENUM);

        myAddress.register(new TargetMethod(myAddress, element.getEnumValueName(), myAddress,
                TargetMethod.Called.STATIC_FIELD));

        enums.add(element);
    }

    public ClassWriter newInnerClass(ClassKind kind, String name, TargetClass parent, List<TargetClass> implementsInterface) {
        innerClasses.put(name, new ClassWriter(this.getAddress(), kind, name, parent, implementsInterface, scopeData));
        return getInnerClass(name);
    }

    private ClassWriter getInnerClass(String name) {
        return innerClasses.get(name);
    }

    /**
     * Get a line that sets a field's value to the value of the variable of the same name.
     * @param field Name of the field (and variable)
     * @return a line of Java setting the field's value to the value of the variable with the same name
     */
    protected Typed<? extends Returnable> setFieldToVariableSameName(String field) {
        return getField(field).assign(BuiltIn.<AValue>toCode(field));
    }

    private static void formatVariableDeclarations(CodeAtoms to, final Collection<Var> fields) {
        if (!fields.isEmpty()) {
            to.hintStart(TokensToStringStyle.GROUP);
            Scope scopeOfPrevious = fields.iterator().next().getScope();

            for (Var variable : fields) {
                if (!variable.getScope().equals(scopeOfPrevious)) {
                    to.hintEnd(TokensToStringStyle.GROUP);
                    to.hintStart(TokensToStringStyle.GROUP);
                }
                new Statement(variable).writeAtoms(to);
                scopeOfPrevious = variable.getScope();
            }

            to.hintEnd(TokensToStringStyle.GROUP);
        }
    }

    private void constructorFromFields(CodeAtoms to) {
        Block body = new Block();
        LinkedList<Var<? extends AValue>> args = new LinkedList<Var<? extends AValue>>();
        for (Var dec : fields.values()) {
            if (dec.getScope().equals(Scope.CLASS))
                continue;

            body.addStatement(setFieldToVariableSameName(dec.getName()));
            args.add(Var.param(dec.getTType(), dec.getName()));
        }
        Method.newPublicConstructor(Comment.no(), args, body).writeAtoms(to);
    }

    @Override
    public String toString() {
        return super.toString() + "\n";
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        to.rewriteRule(new Util.OneCondition<IR.CodeAtom>() {
            @Override
            public boolean isTrueFor(IR.CodeAtom argument) {
                return HasAtoms.SELF.equals(argument);
            }
        }, myAddress.getTypedSimpleName());

        if (isOuter) {
            to.hintStart(TokensToStringStyle.OUTER_LEVEL);

            scopeData.writeAtoms(to);
        }

        for (Annotate ann : classAnnotate)
            ann.writeAtoms(to);

        visibility.writeAtoms(to);
        if (!isOuter)
            scope.writeAtoms(to);
        kind.writeAtoms(to);
        to.add(myAddress.getTypedSimpleName());
        if (!DEFAULT_PARENT.equals(parent)) {
            to.add(EXTENDS);
            parent.writeAtoms(to);
        }
        if (!implementsInterface.isEmpty()) {
            to.add(IMPLEMENTS);
            to.joinSep(HasAtoms.SEP, implementsInterface);
        }

        to.add(HasAtoms.LSC);

        if (ClassKind.ENUM == kind && !enums.isEmpty())
            enums.writeAtoms(to);

        formatVariableDeclarations(to, fields.values());

        if (constructorFromAllFields)
            constructorFromFields(to);

        LinkedList<Method> constructors = new LinkedList<Method>();
        LinkedList<Method> other = new LinkedList<Method>();
        for (Method toSort : methods)
            if (toSort instanceof Method.Constructor)
                constructors.add(toSort);
            else
                other.add(toSort);

        for (Method method : constructors)
            method.writeAtoms(to);
        for (Method method : other)
            method.writeAtoms(to);

        for (String innerName : innerClasses.keySet())
            innerClasses.get(innerName).writeAtoms(to);

        to.add(HasAtoms.RSC);

        if (isOuter) {
            to.hintEnd(TokensToStringStyle.OUTER_LEVEL);
        }
    }

    public String getName() {
        return myAddress.getSimpleName();
    }

    public TargetClass getAddress() {
        return myAddress;
    }

    public String getPackage() {
        return myAddress.getPackage().getFullAddress();
    }

    public Var getField(String name) {
        return fields.get(name);
    }

    public boolean hasConstant(String name) {
        Var field = getField(name);
        return null != field && field.getModifiable().equals(Modifiable.NO);
    }

    static Annotate commentAutoGenerated(String from) {
        Reference.SetTo value =
                Reference.SetTo.strToVal("value", BuiltIn.literal(ClassWriter.class.getCanonicalName()));
        Reference.SetTo comments =
                Reference.SetTo.strToVal("comments", BuiltIn.literal("Auto generated from " + from));
        return new Annotate(javax.annotation.Generated.class, comments, value);
    }

    public static class Atom extends IR.CodeAtom {
        public Atom(String atom) {
            super(atom);
        }
    }
}
