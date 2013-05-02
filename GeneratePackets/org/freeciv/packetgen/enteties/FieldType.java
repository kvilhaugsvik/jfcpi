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

package org.freeciv.packetgen.enteties;

import org.freeciv.packetgen.Hardcoded;
import com.kvilhaugsvik.dependency.Dependency;
import com.kvilhaugsvik.dependency.ReqKind;
import com.kvilhaugsvik.dependency.Requirement;
import com.kvilhaugsvik.javaGenerator.*;
import com.kvilhaugsvik.javaGenerator.Block;
import com.kvilhaugsvik.javaGenerator.Import;
import com.kvilhaugsvik.javaGenerator.typeBridge.From1;
import com.kvilhaugsvik.javaGenerator.typeBridge.From2;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.*;
import org.freeciv.types.FCEnum;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import static com.kvilhaugsvik.javaGenerator.util.BuiltIn.*;

public class FieldType extends ClassWriter implements Dependency.Item, ReqKind {
    public static final Pattern REMOVE_FROM_CLASS_NAMES = Pattern.compile("[\\(|\\)|\\s|;|\\{|\\}]");

    private final Requirement iAmRequiredAs;
    private final TargetClass javaType;
    private final Block constructorBody;
    private final Block decode;
    private final Block encode;
    private final Block encodedSize;
    private final From1<Typed<AString>, Var> value2String;
    private final boolean arrayEater;
    private final List<? extends Var<? extends AValue>> fieldsToAdd;
    private final List<? extends Method> methodsToAdd;

    protected final Var<TargetClass> fValue;
    private final Var<TargetClass> pTo;
    private final Var<TargetClass> pFromStream;

    private final Collection<Requirement> requirement;

    /* A basic field type */
    public FieldType(String dataIOType, String publicType, TargetClass javaType,
                     From1<Block, Var> constructorBody,
                     From2<Block, Var, Var> decode,
                     From2<Block, Var, Var> encode,
                     From1<Typed<AnInt>, Var> encodedSize,
                     From1<Typed<AString>, Var> toString,
                     boolean arrayEater, Collection<Requirement> needs,
                     List<? extends Var<? extends AValue>> fieldsToAdd,
                     List<? extends Method> methodsToAdd) {
        super(ClassKind.CLASS, TargetPackage.from(org.freeciv.packet.fieldtype.FieldType.class.getPackage()),
                Imports.are(Import.allIn(FCEnum.class.getPackage())),
                "Freeciv's protocol definition", Collections.<Annotate>emptyList(),
                getUnaliasedName((dataIOType + "(" + publicType + ")")),
                DEFAULT_PARENT, Arrays.asList(TargetClass.from("org.freeciv.packet.fieldtype", "FieldType<" + javaType.getFullAddress() + ">")));

        pFromStream = Var.param(TargetClass.from(DataInput.class), "from");
        pTo = Var.param(TargetClass.from(DataOutput.class), "to");
        fValue = Var.field(Collections.<Annotate>emptyList(), Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO,
                javaType, "value", null);

        this.iAmRequiredAs = new Requirement(dataIOType + "(" + publicType + ")", FieldType.class);
        this.javaType = javaType;
        this.decode = decode.x(fValue, pFromStream);
        this.encode = encode.x(fValue, pTo);
        this.encodedSize = new Block(RETURN(encodedSize.x(fValue)));
        this.arrayEater = arrayEater;
        this.value2String = toString;
        this.constructorBody = constructorBody.x(fValue);
        this.fieldsToAdd = fieldsToAdd;
        this.methodsToAdd = methodsToAdd;

        // TODO: remove when fixed
        this.javaType.register(new TargetMethod(javaType, "toString", TargetClass.from(String.class), TargetMethod.Called.DYNAMIC));
        this.javaType.register(new TargetMethod(javaType, "equals", TargetClass.from(Boolean.class), TargetMethod.Called.DYNAMIC));

        requirement = needs;

        assemble();
    }

    /* An field type alias */
    protected FieldType(String name, String requiredAs, FieldType original, boolean visible) {
        super(ClassKind.CLASS, TargetPackage.from(org.freeciv.packet.fieldtype.FieldType.class.getPackage()),
                Imports.are(Import.allIn(FCEnum.class.getPackage())),
                "Freeciv's protocol definition", Collections.<Annotate>emptyList(),
                fixClassNameIfBasic(visible ? name : original.getName()),
                DEFAULT_PARENT, Arrays.asList(TargetClass.from("org.freeciv.packet.fieldtype", "FieldType<" + original.javaType.getFullAddress() + ">")));

        this.pFromStream = original.pFromStream;
        this.pTo = original.pTo;
        this.fValue = original.fValue;

        this.iAmRequiredAs = new Requirement(requiredAs, FieldType.class);
        this.javaType = original.javaType;

        this.decode = original.decode;
        this.encode = original.encode;
        this.encodedSize = original.encodedSize;
        this.arrayEater = original.arrayEater;
        this.value2String = original.value2String;
        this.constructorBody = original.constructorBody;
        this.fieldsToAdd = original.fieldsToAdd;
        this.methodsToAdd = original.methodsToAdd;

        requirement = original.requirement;

        assemble();
    }

    private void assemble() {
        this.addObjectConstant(javaType, "value");

        List<TargetClass> tIOExcept = Arrays.asList(TargetClass.from(IOException.class));
        Var<TargetClass> pValue = Var.param(javaType, "value");

        this.addMethod(Method.newPublicConstructor(Comment.no(),
                new ArrayList<Var<? extends AValue>>(new ArrayList(Arrays.asList(pValue, Hardcoded.pLimits))),
                constructorBody));
        this.addMethod(Method.newPublicConstructorWithException(Comment.no(),
                new ArrayList<Var<? extends AValue>>(new ArrayList(Arrays.asList(pFromStream, Hardcoded.pLimits))), tIOExcept,
                decode));
        this.addMethod(Method.newPublicDynamicMethod(Comment.no(),
                TargetClass.from(void.class), "encodeTo", Arrays.asList(pTo),
                tIOExcept, encode));
        this.addMethod(Method.newPublicReadObjectState(Comment.no(),
                TargetClass.from(int.class), "encodedLength",
                encodedSize));
        this.addMethod(Method.newPublicReadObjectState(Comment.no(),
                javaType, "getValue",
                new Block(RETURN(this.getField("value").ref()))));
        this.addMethod(Method.newPublicReadObjectState(Comment.no(),
                TargetClass.from(String.class), "toString",
                new Block(RETURN(value2String.x(this.getField("value"))))));
        Var<TargetClass> paramOther = Var.param(TargetClass.from(Object.class), "other");
        this.addMethod(Method.custom(Comment.no(),
                Visibility.PUBLIC, Scope.OBJECT,
                TargetClass.from(boolean.class), "equals", Arrays.asList(paramOther),
                Collections.<TargetClass>emptyList(),
                new Block(IF(
                        BuiltIn.isInstanceOf(paramOther.ref(), this.getAddress()),
                        new Block(RETURN(this.getField("value").ref().callV("equals", BuiltIn.cast(this.getAddress(), paramOther.ref()).callV("getValue")))),
                        new Block(RETURN(FALSE))))));

        for (Var<? extends AValue> toAdd : fieldsToAdd)
            this.addField(toAdd);

        for (Method toAdd : methodsToAdd)
            this.addMethod(toAdd);
    }

    private static String fixClassNameIfBasic(String name) {
        if (REMOVE_FROM_CLASS_NAMES.matcher(name).find())
            return getUnaliasedName(name);
        else
            return name;
    }

    private static String getUnaliasedName(String name) {
        return "UNALIASED_" + REMOVE_FROM_CLASS_NAMES.matcher(name).replaceAll("_");
    }

    public FieldType createFieldType(String name) {
        return createFieldType(name, name);
    }

    public FieldType createFieldType(String name, String reqName) {
        return new FieldType(name, reqName, this, true);
    }

    /**
     * Use this to provide another type and replace mentions of the other type with this in the generated code
     * @param alias Name of the field type alias to replace
     * @return A Field type alias that is a copy of this except that it will fulfill the requirement alias
     */
    public FieldType aliasUnseenToCode(String alias) {
        FieldType invisibleAlias = this.invisibleAliasCreation(alias);

        // See if a sub class has messed this up
        assert alias.equals(invisibleAlias.getIFulfillReq().getName()) : "Not a proper alias";
        assert invisibleAlias.toString().equals(this.toString()) : "Different code generated";

        return invisibleAlias;
    }

    /**
     * Override this if a subclass changes the generated code
     * @param alias the alias requested
     * @return an instance that is an invisible alias of the class
     */
    protected FieldType invisibleAliasCreation(String alias) {
        return new FieldType(getName(), alias, this, false);
    }

    public boolean isArrayEater() {
        return arrayEater;
    }

    public TargetClass getUnderType() {
        return javaType;
    }

    @Override
    public Collection<Requirement> getReqs() {
        return requirement;
    }

    @Override
    public Requirement getIFulfillReq() {
        return iAmRequiredAs;
    }
}
