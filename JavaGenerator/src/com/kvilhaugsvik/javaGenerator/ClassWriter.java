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
import com.kvilhaugsvik.javaGenerator.formating.TokensToStringStyle;
import com.kvilhaugsvik.javaGenerator.representation.CodeAtoms;
import com.kvilhaugsvik.javaGenerator.representation.HasAtoms;
import com.kvilhaugsvik.javaGenerator.representation.IR;

import java.util.*;

import static com.kvilhaugsvik.javaGenerator.util.BuiltIn.*;

/**
 * Generate a Java Class. Will automatically wrap it in the correct source code file.
 */
public class ClassWriter extends Formatted implements HasAtoms, IAnnotatable {
    public static final TargetClass DEFAULT_PARENT = TargetClass.from(Object.class);
    protected final Reference<AValue> internal_ref_this;
    protected final Reference<AValue> internal_ref_super;

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

    /**
     * Create a new Java source code file
     * @param kind The kind of Java source file to create.
     * @param where What package the class live in
     * @param imports What classes should be imported so they can be referred to without using the full address.
     *                Note that this applies to the source file the class is inside.
     * @param madeFrom Where the data used to generate this class came from.
     * @param classAnnotate annotations to put on this class.
     * @param name the name of the class.
     * @param parent the class the generated class should extend. Default is {@see java.lang.Object}.
     * @param implementsInterface interfaces the class implements.
     */
    public ClassWriter(ClassKind kind, TargetPackage where, Imports imports,
                       String madeFrom, List<Annotate> classAnnotate,
                       String name,
                       TargetClass parent, List<TargetClass> implementsInterface) {
        if (null == name) throw new IllegalArgumentException("No name for class to be generated");
        if (null == where)
            throw new IllegalArgumentException("null given as package. (Did you mean TargetPackage.TOP_LEVEL?)");

        this.myAddress = new TargetClass(where, Arrays.asList(new Atom(name)), kind, Collections.<HasAtoms>emptyList(), new HashMap<String, TargetMethod>(), null);
        myAddress.setParent(parent);

        this.internal_ref_this = Var.param(getAddress(), "this").ref();
        this.internal_ref_super = Var.param(parent, "super").ref();

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

        this.myAddress = new TargetClass(inside.getPackage(), classPart, kind, Collections.<HasAtoms>emptyList(), new HashMap<String, TargetMethod>(), null);
        this.myAddress.setParent(parent);

        this.internal_ref_this = Var.param(getAddress(), "this").ref();
        this.internal_ref_super = Var.param(parent, "super").ref();

        this.classAnnotate = new LinkedList<Annotate>();
        this.visibility = Visibility.PUBLIC;
        this.scope = Scope.CLASS;
        this.kind = kind;
        this.parent = parent;
        this.implementsInterface = implementsInterface;

        this.scopeData = scopeData;
        this.isOuter = false;
    }

    /**
     * Add a field to the class
     * @param field the field to add
     */
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

    public void addObjectConstantAndGetter(Var<AValue> field) {
        addField(field);
        addMethod(Method.newPublicReadObjectState(Comment.no(),
                field.getTType(),
                getterNameJavaish(field),
                new Block(RETURN(field.ref()))));
    }

    protected static String getterNameJavaish(Var field) {
        return "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
    }

    /**
     * Add a method to the generated class.
     * @param toAdd the method to add.
     */
    public void addMethod(Method toAdd) {
        methods.add(toAdd);
        myAddress.register(toAdd.getAddressOn(this.getAddress()));
    }

    /**
     * Generate a constructor from all the fields of the class
     */
    public void addConstructorFields() {
        constructorFromAllFields = true;
    }

    protected void addEnumerated(EnumElement element) {
        assert kind.equals(ClassKind.ENUM);

        myAddress.register(new TargetMethod(myAddress, element.getEnumValueName(), myAddress,
                TargetMethod.Called.STATIC_FIELD));

        enums.add(element);
    }

    @Override
    public void annotateMe(Annotate using) {
        classAnnotate.add(using);
    }

    /**
     * Create an inner Java class
     * @param kind The Java class kind.
     * @param name The name of the inner class.
     * @param parent The class the inner class should extend. Default is {@see java.lang.Object}.
     * @param implementsInterface interfaces implemented by the inner class.
     * @return a reference to the new inner class.
     */
    public ClassWriter newInnerClass(ClassKind kind, String name, TargetClass parent, List<TargetClass> implementsInterface) {
        innerClasses.put(name, new ClassWriter(this.getAddress(), kind, name, parent, implementsInterface, scopeData));
        return getInnerClass(name);
    }

    private ClassWriter getInnerClass(String name) {
        return innerClasses.get(name);
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

            final Var<AValue> param = Var.param(dec.getTType(), dec.getName());
            body.addStatement(dec.assign(param.ref()));
            args.add(param);
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

    /**
     * Get the simple name of the class as a {@see java.lang.String}
     * @return the simple name of the class
     */
    public String getName() {
        return myAddress.getSimpleName();
    }

    /**
     * Get the full address of the class as a {@see com.kvilhaugsvik.javaGenerator.TargetClass}
     * @return the full address of the class
     */
    public TargetClass getAddress() {
        return myAddress;
    }

    public Reference getInternalReferenceThis() {
        return internal_ref_this;
    }

    public Reference getInternalReferenceSuper() {
        return internal_ref_super;
    }

    /**
     * Get the package as a {@see java.lang.String}
     * @return the full package address
     */
    public String getPackage() {
        return myAddress.getPackage().getFullAddress();
    }

    /**
     * Get a field with the given name. If no such field exists NULL is returned.
     * @param name the name of the wanted field
     * @return the wanted field if found. Null if it isn't.
     */
    public Var getField(String name) {
        return fields.get(name);
    }

    /**
     * Does a constant with the given name exist in the class?
     * @param name name of the constant to look for.
     * @return true if the field exists and is unmodifiable.
     */
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
