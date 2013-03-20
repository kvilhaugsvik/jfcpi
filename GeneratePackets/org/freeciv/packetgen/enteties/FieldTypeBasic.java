/*
 * Copyright (c) 2011, 2012. Sveinung Kvilhaugsvik
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
import org.freeciv.packetgen.dependency.Dependency;
import org.freeciv.packetgen.dependency.ReqKind;
import org.freeciv.packetgen.dependency.Requirement;
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

import static com.kvilhaugsvik.javaGenerator.util.BuiltIn.*;

public class FieldTypeBasic implements Dependency.Item, ReqKind {
    private final Requirement iAmRequiredAs;
    private final TargetClass javaType;
    private final Block constructorBody;
    private final Block decode;
    private final Block encode;
    private final Block encodedSize;
    private final From1<Typed<AString>, Var> value2String;
    private final boolean arrayEater;

    protected final Var<TargetClass> fValue;
    private final Var<TargetClass> pTo;
    private final Var<TargetClass> pFromStream;

    private final Collection<Requirement> requirement;
    private final FieldTypeBasic basicType = this;

    public FieldTypeBasic(String dataIOType, String publicType, TargetClass javaType,
                          From1<Block, Var> constructorBody,
                          From2<Block, Var, Var> decode,
                          From2<Block, Var, Var> encode,
                          From1<Typed<AnInt>, Var> encodedSize,
                          From1<Typed<AString>, Var> toString,
                          boolean arrayEater, Collection<Requirement> needs) {
        pFromStream = Var.param(TargetClass.newKnown(DataInput.class), "from");
        pTo = Var.param(TargetClass.newKnown(DataOutput.class), "to");
        fValue = Var.field(Collections.<Annotate>emptyList(), Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO,
                javaType, "value", null);

        this.iAmRequiredAs = new Requirement(dataIOType + "(" + publicType + ")", FieldTypeBasic.class);
        this.javaType = javaType;
        this.decode = decode.x(fValue, pFromStream);
        this.encode = encode.x(fValue, pTo);
        this.encodedSize = new Block(RETURN(encodedSize.x(fValue)));
        this.arrayEater = arrayEater;
        this.value2String = toString;
        this.constructorBody = constructorBody.x(fValue);

        // TODO: remove when fixed
        this.javaType.register(new TargetMethod(javaType, "toString", TargetClass.fromClass(String.class), TargetMethod.Called.DYNAMIC));
        this.javaType.register(new TargetMethod(javaType, "equals", TargetClass.fromClass(Boolean.class), TargetMethod.Called.DYNAMIC));

        requirement = needs;
    }

    private FieldTypeBasic(String called, FieldTypeBasic original) {
        this.pFromStream = original.pFromStream;
        this.pTo = original.pTo;
        this.fValue = original.fValue;

        this.iAmRequiredAs = new Requirement(called, FieldTypeBasic.class);
        this.javaType = original.javaType;

        this.decode = original.decode;
        this.encode = original.encode;
        this.encodedSize = original.encodedSize;
        this.arrayEater = original.arrayEater;
        this.value2String = original.value2String;
        this.constructorBody = original.constructorBody;

        requirement = original.requirement;
    }

    private void assemble(FieldTypeAlias me) {
        me.addObjectConstant(javaType, "value");

        List<TargetClass> tIOExcept = Arrays.asList(TargetClass.newKnown(IOException.class));
        Var<TargetClass> pValue = Var.param(javaType, "value");

        me.addMethod(Method.newPublicConstructor(Comment.no(),
                new ArrayList<Var<? extends AValue>>(new ArrayList(Arrays.asList(pValue, Hardcoded.pLimits))),
                constructorBody));
        me.addMethod(Method.newPublicConstructorWithException(Comment.no(),
                new ArrayList<Var<? extends AValue>>(new ArrayList(Arrays.asList(pFromStream, Hardcoded.pLimits))), tIOExcept,
                decode));
        me.addMethod(Method.newPublicDynamicMethod(Comment.no(),
                TargetClass.newKnown(void.class), "encodeTo", Arrays.asList(pTo),
                tIOExcept, encode));
        me.addMethod(Method.newPublicReadObjectState(Comment.no(),
                TargetClass.fromClass(int.class), "encodedLength",
                encodedSize));
        me.addMethod(Method.newPublicReadObjectState(Comment.no(),
                javaType, "getValue",
                new Block(RETURN(me.getField("value").ref()))));
        me.addMethod(Method.newPublicReadObjectState(Comment.no(),
                TargetClass.newKnown(String.class), "toString",
                new Block(RETURN(value2String.x(me.getField("value"))))));
        Var<TargetClass> paramOther = Var.param(TargetClass.fromClass(Object.class), "other");
        me.addMethod(Method.custom(Comment.no(),
                Visibility.PUBLIC, Scope.OBJECT,
                TargetClass.fromClass(boolean.class), "equals", Arrays.asList(paramOther),
                Collections.<TargetClass>emptyList(),
                new Block(IF(
                        BuiltIn.isInstanceOf(paramOther.ref(), me.getAddress()),
                        new Block(RETURN(me.getField("value").ref().callV("equals", BuiltIn.cast(me.getAddress().scopeKnown(), paramOther.ref()).callV("getValue")))),
                        new Block(RETURN(FALSE))))));
    }

    public FieldTypeBasic copyUnderNewName(String alias) {
        FieldTypeBasic invisibleAlias = new FieldTypeBasic(alias, this);

        return invisibleAlias;
    }

    public FieldTypeAlias createFieldType(String name) {
        return createFieldType(name, name);
    }

    public FieldTypeAlias createFieldType(String name, String reqName) {
        return new FieldTypeAlias(name, reqName);
    }

    /**
     * Use this to provide another type and replace mentions of the other type with this in the generated code
     * @param alias Name of the field type alias to replace
     * @return A Field type alias that is a copy of this except that it will fulfill the requirement alias
     */
    public FieldTypeAlias aliasUnseenToCode(String alias, FieldTypeAlias to) {
        FieldTypeAlias invisibleAlias = to.invisibleAliasCreation(alias);

        // See if a sub class has messed this up
        assert alias.equals(invisibleAlias.getIFulfillReq().getName()) : "Not a proper alias";
        assert invisibleAlias.toString().equals(to.toString()) : "Different code generated";

        return invisibleAlias;
    }

    public boolean isArrayEater() {
        return arrayEater;
    }

    public TargetClass getUnderType() {
        return javaType;
    }

    @Override
    public Collection<Requirement> getReqs() {
        return Collections.<Requirement>emptySet();
    }

    @Override
    public Requirement getIFulfillReq() {
        return iAmRequiredAs;
    }

    public class FieldTypeAlias extends ClassWriter implements Dependency.Item, ReqKind {
        private final Requirement iAmRequiredAs;

        protected FieldTypeAlias(String name) {
            this(name, name);
        }

        protected FieldTypeAlias(String name, String requiredAs) {
            super(ClassKind.CLASS, TargetPackage.from(org.freeciv.packet.fieldtype.FieldType.class.getPackage()),
                    Imports.are(Import.classIn(DataInput.class),
                            Import.classIn(DataOutput.class),
                            Import.classIn(IOException.class),
                            Import.allIn(FCEnum.class.getPackage())),
                    "Freeciv's protocol definition", Collections.<Annotate>emptyList(), name,
                                          DEFAULT_PARENT, Arrays.asList(TargetClass.newKnown("org.freeciv.packet.fieldtype", "FieldType<" + javaType.getName() + ">")));
            this.iAmRequiredAs = new Requirement(requiredAs, FieldTypeBasic.FieldTypeAlias.class);

            assemble(this);
        }

        public FieldTypeBasic getBasicType() {
            return basicType;
        }

        /**
         * Use this to provide another type and replace mentions of the other type with this in the generated code
         * @param alias Name of the field type alias to replace
         * @return A Field type alias that is a copy of this except that it will fulfill the requirement alias
         */
        public FieldTypeAlias aliasUnseenToCode(String alias) {
            return getBasicType().aliasUnseenToCode(alias, this);
        }

        /**
         * Override this if a subclass changes the generated code
         * @param alias the alias requested
         * @return an instance that is an invisible alias of the class
         */
        protected FieldTypeAlias invisibleAliasCreation(String alias) {
            return new FieldTypeAlias(getName(), alias);
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
}
