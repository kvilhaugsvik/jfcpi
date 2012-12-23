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
import org.freeciv.packetgen.dependency.ReqKind;
import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.javaGenerator.*;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.Import;
import org.freeciv.packetgen.javaGenerator.expression.creators.ExprFrom1;
import org.freeciv.packetgen.javaGenerator.expression.creators.ExprFrom2;
import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.*;

import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.*;

public class FieldTypeBasic implements IDependency, ReqKind {
    private final String fieldTypeBasic;
    private final String publicType;
    private final TargetClass javaType;
    private final Block constructorBody;
    private final Block decode;
    private final Block encode;
    private final Block encodedSize;
    private final ExprFrom1<Typed<AString>, Var> value2String;
    private final boolean arrayEater;

    protected final Var<TargetClass> fValue;
    private final Var<TargetClass> pTo;
    private final Var<TargetClass> pFromStream;

    private final Collection<Requirement> requirement;
    private final FieldTypeBasic basicType = this;

    public FieldTypeBasic(String dataIOType, String publicType, TargetClass javaType,
                          ExprFrom1<Block, Var> constructorBody,
                          ExprFrom2<Block, Var, Var> decode,
                          ExprFrom2<Block, Var, Var> encode,
                          ExprFrom1<Typed<AnInt>, Var> encodedSize,
                          ExprFrom1<Typed<AString>, Var> toString,
                          boolean arrayEater, Collection<Requirement> needs) {
        pFromStream = Var.param(new TargetClass(DataInput.class, true), "from");
        pTo = Var.param(new TargetClass(DataOutput.class, true), "to");
        fValue = Var.field(Collections.<Annotate>emptyList(), Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO,
                javaType, "value", null);

        this.fieldTypeBasic = dataIOType + "(" + publicType + ")";
        this.publicType = publicType;
        this.javaType = javaType;
        this.decode = decode.x(fValue, pFromStream);
        this.encode = encode.x(fValue, pTo);
        this.encodedSize = new Block(RETURN(encodedSize.x(fValue)));
        this.arrayEater = arrayEater;
        this.value2String = toString;
        this.constructorBody = constructorBody.x(fValue);

        requirement = needs;
    }

    public String getFieldTypeBasic() {
        return fieldTypeBasic;
    }

    public String getPublicType() {
        return publicType;
    }

    @Override
    public Collection<Requirement> getReqs() {
        return Collections.<Requirement>emptySet();
    }

    @Override
    public Requirement getIFulfillReq() {
        return new Requirement(fieldTypeBasic, FieldTypeBasic.class);
    }

    public FieldTypeAlias createFieldType(String name) {
        return new FieldTypeAlias(name);
    }

    public boolean isArrayEater() {
        return arrayEater;
    }

    public class FieldTypeAlias extends ClassWriter implements IDependency, ReqKind {
        private final String requiredAs;

        protected FieldTypeAlias(String name) {
            this(name, name);
        }

        protected FieldTypeAlias(String name, String requiredAs) {
            super(ClassKind.CLASS, new TargetPackage(org.freeciv.packet.fieldtype.FieldType.class.getPackage()), new Import[]{
                                                  Import.classIn(java.io.DataInput.class),
                                                  Import.classIn(java.io.DataOutput.class),
                                                  Import.classIn(java.io.IOException.class),
                                                  null,
                                                  Import.allIn(new TargetPackage(org.freeciv.types.FCEnum.class.getPackage()))
                                          }, "Freeciv's protocol definition", Collections.<Annotate>emptyList(), name,
                                          TargetClass.fromName(null), Arrays.asList(new TargetClass("FieldType<" + javaType.getName() + ">")));
            this.requiredAs = requiredAs;

            addObjectConstant(javaType.getName(), "value");

            List<TargetClass> tIOExcept = Arrays.asList(new TargetClass("IOException"));
            Var<TargetClass> pValue = Var.param(javaType, "value");

            addMethod(Method.newPublicConstructor(Comment.no(),
                    getName(), new ArrayList<Var<? extends AValue>>(new ArrayList(Arrays.asList(pValue, Hardcoded.pLimits))),
                    constructorBody));
            addMethod(Method.newPublicConstructorWithException(Comment.no(),
                    getName(), new ArrayList<Var<? extends AValue>>(new ArrayList(Arrays.asList(pFromStream, Hardcoded.pLimits))), tIOExcept,
                    decode));
            addMethod(Method.newPublicDynamicMethod(Comment.no(),
                    new TargetClass(void.class, true), "encodeTo", Arrays.asList(pTo),
                    tIOExcept, encode));
            addMethod(Method.newPublicReadObjectState(Comment.no(),
                    TargetClass.fromName("int"), "encodedLength",
                    encodedSize));
            addMethod(Method.newPublicReadObjectState(Comment.no(),
                    javaType, "getValue",
                    new Block(RETURN(getField("value").ref()))));
            addMethod(Method.newPublicReadObjectState(Comment.no(),
                    TargetClass.fromName("String"), "toString",
                    new Block(RETURN(value2String.x(getField("value"))))));
            Var<TargetClass> paramOther = Var.param(new TargetClass(Object.class), "other");
            addMethod(Method.custom(Comment.no(),
                    Visibility.PUBLIC, Scope.OBJECT,
                    TargetClass.fromName("boolean"), "equals", Arrays.asList(paramOther),
                    Collections.<TargetClass>emptyList(),
                    new Block(IF(
                            BuiltIn.<ABool>toCode("other instanceof " + name),
                            new Block(RETURN(BuiltIn.<ABool>toCode("this.value == ((" + name + ")other).getValue()"))),
                            new Block(RETURN(FALSE))))));
        }

        public FieldTypeBasic getBasicType() {
            return basicType;
        }

        public String getJavaType() {
            return javaType.getName();
        }

        public TargetClass getUnderType() {
            return javaType;
        }

        /**
         * Use this to provide another type and replace mentions of the other type with this in the generated code
         * @param alias Name of the field type alias to replace
         * @return A Field type alias that is a copy of this except that it will fulfill the requirement alias
         */
        public FieldTypeAlias aliasUnseenToCode(String alias) {
            FieldTypeAlias invisibleAlias = invisibleAliasCreation(alias);

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
        protected FieldTypeAlias invisibleAliasCreation(String alias) {
            return new FieldTypeAlias(getName(), alias);
        }

        @Override
        public Collection<Requirement> getReqs() {
            return requirement;
        }

        @Override
        public Requirement getIFulfillReq() {
            return new Requirement(requiredAs, FieldTypeBasic.FieldTypeAlias.class);
        }
    }
}
