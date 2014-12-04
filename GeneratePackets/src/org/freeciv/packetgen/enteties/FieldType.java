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

import com.kvilhaugsvik.javaGenerator.typeBridge.Value;
import org.freeciv.packet.fieldtype.ElementsLimit;
import org.freeciv.packetgen.Hardcoded;
import com.kvilhaugsvik.dependency.Dependency;
import com.kvilhaugsvik.dependency.ReqKind;
import com.kvilhaugsvik.dependency.Requirement;
import com.kvilhaugsvik.javaGenerator.*;
import com.kvilhaugsvik.javaGenerator.typeBridge.*;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.*;
import org.freeciv.packetgen.enteties.supporting.DataType;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import static com.kvilhaugsvik.javaGenerator.util.BuiltIn.*;

/**
 * Represents a packet field type. It has a value and can serialize and deserialize it.
 */
public class FieldType extends ClassWriter implements Dependency.Item, ReqKind {
    public static final Pattern REMOVE_FROM_CLASS_NAMES = Pattern.compile("[\\(|\\)|\\s|;|\\{|\\}]");

    private final Requirement iAmRequiredAs;
    private final DataType wrappedType;
    private final Block constructorBody;
    private final Block decode;
    private final Block encode;
    private final Block encodedSize;
    private final From1<Typed<AString>, Var> value2String;
    private final boolean arrayEater;
    private final List<? extends Var<? extends AValue>> fieldsToAdd;
    private final List<? extends Method> methodsToAdd;

    protected final Var<AnObject> fValue;
    private final Var<AnObject> pTo;
    private final Var<AnObject> pFromStream;
    private final Var<AValue> old;

    private final Collection<Requirement> requirement;

    /**
     * Create a new field type representing a basic field type.
     * Basic field types are usually not used directly. An alias to it is used in stead.
     * @param dataIOType string identifying the function Freeciv use to read fields of this kind.
     * @param publicType the C type Freeciv converts this field to after reading it.
     * @param wrappedType the Java type to use in stead of the C type.
     * @param constructorBody constructor from a value of the Java type.
     * @param decode constructor from a {@see java.io.DataOutput},
     *               a {@see org.freeciv.packet.fieldtype.ElementsLimit limit}
     *               and
     *               a previous field of the same type.
     *               The data to decode should be read from the DataInput.
     *               The limits on the number of elements to read from the DataInput is in the ElementsLimit.
     *               The previous field of the same type is meant to be used in the delta protocol. It may be null.
     * @param encode code for a serializer function that encodes the field to the given {@see java.io.DataOutput}.
     * @param encodedSize code for a function that returns how large (in bytes) the serialized field would be.
     * @param toString code for a {@see java.lang.Object#toString() toString()} function.
     * @param arrayEater true if this field type will consume an array dimension.
     * @param needs items this field type depends on.
     *              Example: if the Java type of the field's value if generated the field depends on it.
     * @param fieldsToAdd fields to add to the generated Java code for this field type.
     * @param methodsToAdd methods to add to the generated Java code for this field type.
     */
    public FieldType(String dataIOType, String publicType, DataType wrappedType,
                     From1<Block, Var> constructorBody,
                     From3<Block, Var, Var, Var> decode,
                     From2<Block, Var, Var> encode,
                     From1<Typed<AnInt>, Var> encodedSize,
                     From1<Typed<AString>, Var> toString,
                     boolean arrayEater, Collection<Requirement> needs,
                     List<? extends Var<? extends AValue>> fieldsToAdd,
                     List<? extends Method> methodsToAdd) {
        super(ClassKind.CLASS, TargetPackage.from(org.freeciv.packet.fieldtype.FieldType.class.getPackage()),
                Imports.are(),
                "Freeciv's protocol definition", Collections.<Annotate>emptyList(),
                getUnaliasedName((dataIOType + "(" + publicType + ")")),
                DEFAULT_PARENT, Arrays.asList(TargetClass.from(org.freeciv.packet.fieldtype.FieldType.class).addGenericTypeArguments(Arrays.asList(wrappedType.getAddress()))));

        pFromStream = Var.param(TargetClass.from(DataInput.class), "from");
        pTo = Var.param(TargetClass.from(DataOutput.class), "to");
        fValue = Var.field(Collections.<Annotate>emptyList(), Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO,
                wrappedType.getAddress(), "value", null);
        this.old = Var.param(this.getAddress(), "old");

        this.iAmRequiredAs = new Requirement(dataIOType + "(" + publicType + ")", FieldType.class);
        this.wrappedType = wrappedType;
        this.decode = decode.x(fValue, pFromStream, this.old);
        this.encode = encode.x(fValue, pTo);
        this.encodedSize = new Block(RETURN(encodedSize.x(fValue)));
        this.arrayEater = arrayEater;
        this.value2String = toString;
        this.constructorBody = constructorBody.x(fValue);
        this.fieldsToAdd = fieldsToAdd;
        this.methodsToAdd = methodsToAdd;

        requirement = needs;

        assemble();
    }

    /* An field type alias */
    protected FieldType(String name, String requiredAs, FieldType original, boolean visible) {
        super(ClassKind.CLASS, TargetPackage.from(org.freeciv.packet.fieldtype.FieldType.class.getPackage()),
                Imports.are(),
                "Freeciv's protocol definition", Collections.<Annotate>emptyList(),
                fixClassNameIfBasic(visible ? name : original.getName()),
                DEFAULT_PARENT, Arrays.asList(TargetClass.from(org.freeciv.packet.fieldtype.FieldType.class).addGenericTypeArguments(Arrays.asList(original.getUnderType()))));

        this.pFromStream = original.pFromStream;
        this.pTo = original.pTo;
        this.fValue = original.fValue;
        this.old = Var.param(this.getAddress(), "old");

        this.iAmRequiredAs = new Requirement(requiredAs, FieldType.class);
        this.wrappedType = original.wrappedType;

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
        this.addObjectConstant(getUnderType(), "value");

        List<TargetClass> tIOExcept = Arrays.asList(TargetClass.from(IOException.class));
        Var<AnObject> pValue = Var.param(getUnderType(), "value");

        this.addMethod(Method.newPublicConstructor(Comment.no(),
                new ArrayList<Var<? extends AValue>>(new ArrayList(Arrays.asList(pValue, Hardcoded.pLimits))),
                constructorBody));
        this.addMethod(Method.newPublicConstructorWithException(Comment.no(),
                new ArrayList<Var<? extends AValue>>(new ArrayList(Arrays.asList(pFromStream, Hardcoded.pLimits, this.old))), tIOExcept,
                decode));
        this.addMethod(Method.newPublicDynamicMethod(Comment.no(),
                TargetClass.from(void.class), "encodeTo", Arrays.asList(pTo),
                tIOExcept, encode));
        this.addMethod(Method.newPublicReadObjectState(Comment.no(),
                TargetClass.from(int.class), "encodedLength",
                encodedSize));
        this.addMethod(Method.newPublicReadObjectState(Comment.no(),
                getUnderType(), "getValue",
                new Block(RETURN(this.getField("value").ref()))));
        this.addMethod(Method.newPublicReadObjectState(Comment.no(),
                TargetClass.from(String.class), "toString",
                new Block(RETURN(value2String.x(this.getField("value"))))));
        Var<AnObject> paramOther = Var.param(TargetClass.from(Object.class), "other");
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

        final Var<AValue> pSize = Var.param(int.class, "size");
        this.addMethod(Method.custom(Comment.no(), Visibility.PUBLIC, Scope.CLASS,
                this.getUnderType(), "getValueZero",
                Arrays.<Var<?>>asList(pSize),
                Collections.<TargetClass>emptyList(),
                new Block(
                        IF(isNotSame(literal(0), pSize.ref()),
                                new Block(THROW(UnsupportedOperationException.class,
                                        literal("I only support size 0.")))),
                        RETURN(this.getWrappedDataType().getZeroValue()))));
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

    /**
     * Creates a new field type alias based on this one.
     * @param name the name to give the new field type.
     * @return the new field type.
     */
    public FieldType createFieldType(String name) {
        return createFieldType(name, name);
    }

    /**
     * Creates a new field type alias based on this one.
     * @param name the name to give the new field type.
     * @param reqName the name the {@see com.kvilhaugsvik.dependency.DependencyStore dependency handler} should see.
     * @return the new field type.
     */
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

    /**
     * Does this field type "eat" a dimension of array declarations on their fields?
     * This is true for fields that uses array dimension information passed to it like string and field arrays.
     * @return true iff this field type spends an array declaration.
     */
    public boolean isArrayEater() {
        return arrayEater;
    }

    /**
     * Get the type of the value that this field type wraps.
     * @return the address of the type of the inner value.
     */
    public TargetClass getUnderType() {
        return wrappedType.getAddress();
    }

    /**
     * Get the zero value of this field type. The zero value of the field type is when the wrapped value is zero.
     * @return the zero value of the field type.
     */
    public Value getZeroValue() {
        return this.getAddress().newInstance(
                this.getAddress().callV("getValueZero", literal(0)),
                TargetClass.from(ElementsLimit.class).callV("limit", literal(0)));
    }

    /**
     * Get the type of the value that this field type wraps.
     * @return the DataType of the type of the inner value.
     */
    public DataType getWrappedDataType() {
        return wrappedType;
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
