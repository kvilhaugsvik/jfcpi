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

import org.freeciv.packetgen.dependency.ReqKind;
import org.freeciv.packetgen.enteties.supporting.NetworkIO;
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

    private final Var<TargetClass> fValue;
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

        protected FieldTypeAlias(String name) {
            super(ClassKind.CLASS, new TargetPackage(org.freeciv.packet.fieldtype.FieldType.class.getPackage()), new Import[]{
                                                  Import.classIn(java.io.DataInput.class),
                                                  Import.classIn(java.io.DataOutput.class),
                                                  Import.classIn(java.io.IOException.class),
                                                  null,
                                                  Import.allIn(new TargetPackage(org.freeciv.types.FCEnum.class.getPackage()))
                                          }, "Freeciv's protocol definition", Collections.<Annotate>emptyList(), name,
                                          TargetClass.fromName(null), Arrays.asList(new TargetClass("FieldType<" + javaType.getName() + ">")));

            addObjectConstant(javaType.getName(), "value");

            List<TargetClass> tIOExcept = Arrays.asList(new TargetClass("IOException"));
            Var<TargetClass> pValue = Var.param(javaType, "value");
            Var<AnInt> pArraySize = Var.param(int.class, "arraySize");
            Var<AnInt> pMaxArraySizeNow = Var.param(int.class, "maxArraySizeThisTime");

            List<Var<? extends AValue>> paramsConstructArgs = new ArrayList(Arrays.asList(pValue));
            List<Var<? extends AValue>> paramsConstructIO = new ArrayList(Arrays.asList(pFromStream));

            if (arrayEater) {
                paramsConstructArgs.add(pArraySize);
                paramsConstructIO.add(pArraySize);
                generateConstructors(tIOExcept, paramsConstructArgs, paramsConstructIO,
                        new Block(new MethodCall("this", pValue.ref(), pArraySize.ref(), pArraySize.ref())),
                        new Block(new MethodCall("this", pFromStream.ref(), pArraySize.ref(), pArraySize.ref())));
                paramsConstructArgs.add(pMaxArraySizeNow);
                paramsConstructIO.add(pMaxArraySizeNow);
            }

            generateConstructors(tIOExcept, paramsConstructArgs, paramsConstructIO, constructorBody, decode);
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

        private void generateConstructors(List<TargetClass> tIOExcept,
                                          List<Var<? extends AValue>> paramsConstructArgs,
                                          List<Var<? extends AValue>> paramsConstructIO,
                                          Block javaVarsConstructor,
                                          Block serializedConstructor) {
            addMethod(Method.newPublicConstructor(Comment.no(),
                    getName(), new ArrayList<Var<? extends AValue>>(paramsConstructArgs),
                    javaVarsConstructor));
            addMethod(Method.newPublicConstructorWithException(Comment.no(),
                    getName(), new ArrayList<Var<? extends AValue>>(paramsConstructIO), tIOExcept,
                    serializedConstructor));
        }

        public FieldTypeBasic getBasicType() {
            return basicType;
        }

        public String getJavaType() {
            return javaType.getName();
        }

        @Override
        public Collection<Requirement> getReqs() {
            return requirement;
        }

        @Override
        public Requirement getIFulfillReq() {
            return new Requirement(getName(), FieldTypeBasic.FieldTypeAlias.class);
        }
    }

    public static interface Generator {
        public FieldTypeBasic getBasicFieldTypeOnInput(NetworkIO io);
        public Class<? extends ReqKind> needsDataInFormat();
    }
}
