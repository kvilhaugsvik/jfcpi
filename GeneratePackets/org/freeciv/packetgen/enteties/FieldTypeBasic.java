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

import org.freeciv.packetgen.enteties.supporting.NetworkIO;
import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.javaGenerator.*;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.Import;
import org.freeciv.packetgen.javaGenerator.expression.creators.ExprFrom1;
import org.freeciv.packetgen.javaGenerator.expression.creators.ExprFrom2;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.*;

import java.util.Collection;
import java.util.Collections;

import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.*;

public class FieldTypeBasic implements IDependency {
    private final String fieldTypeBasic;
    private final String publicType;
    private final String javaType;
    private final Block constructorBody;
    private final String[] decode;
    private final String[] encode, encodedSize;
    private final ExprFrom1<AString, AValue> value2String;
    private final boolean arrayEater;

    private final Collection<Requirement> requirement;
    private final FieldTypeBasic basicType = this;

    public FieldTypeBasic(String dataIOType, String publicType, String javaType,
                          String decode, String encode, String encodedSize,
                          boolean arrayEater, Collection<Requirement> needs) {
        Var value =
                Var.field(Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO, javaType, "value", null);
        this.fieldTypeBasic = dataIOType + "(" + publicType + ")";
        this.publicType = publicType;
        this.javaType = javaType;
        this.decode = decode.split("\n");
        this.encode = encode.split("\n");
        this.encodedSize = encodedSize.split("\n");
        this.arrayEater = arrayEater;
        this.value2String = TO_STRING_OBJECT;
        this.constructorBody = new Block(value.assign(asAValue("value")));

        requirement = needs;
    }

    public FieldTypeBasic(String dataIOType, String publicType, String javaType,
                          ExprFrom1<Block, Var>  constructorBody,
                          ExprFrom2<Block, Var, Var> decode, String encode, String encodedSize,
                          ExprFrom1<AString, AValue> toString,
                          boolean arrayEater, Collection<Requirement> needs) {
        Var from = Var.local(java.io.DataInput.class, "from", null);
        Var value =
                Var.field(Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO, javaType, "value", null);

        this.fieldTypeBasic = dataIOType + "(" + publicType + ")";
        this.publicType = publicType;
        this.javaType = javaType;
        this.decode = ClassWriter.newToOld(decode.x(value, from));
        this.encode = encode.split("\n");
        this.encodedSize = encodedSize.split("\n");
        this.arrayEater = arrayEater;
        this.value2String = toString;
        this.constructorBody = constructorBody.x(value);

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
        return new Requirement(fieldTypeBasic, Requirement.Kind.PRIMITIVE_FIELD_TYPE);
    }

    public FieldTypeAlias createFieldType(String name) {
        return new FieldTypeAlias(name);
    }

    public boolean isArrayEater() {
        return arrayEater;
    }

    public class FieldTypeAlias extends ClassWriter implements IDependency {

        private FieldTypeAlias(String name) {
            super(ClassKind.CLASS, new TargetPackage(org.freeciv.packet.fieldtype.FieldType.class.getPackage()),
                              new Import[]{
                                      Import.classIn(java.io.DataInput.class),
                                      Import.classIn(java.io.DataOutput.class),
                                      Import.classIn(java.io.IOException.class),
                                      null,
                                      Import.allIn(new TargetPackage(org.freeciv.types.FCEnum.class.getPackage()))
                              }, "Freeciv's protocol definition", name, null, "FieldType<" + javaType + ">");

            addObjectConstant(javaType, "value");
            if (arrayEater) {
                addConstructorPublic(null, javaType + " value" + ", int arraySize", constructorBody);
                addConstructorPublicWithExceptions(null, "DataInput from" + ", int arraySize", "IOException",
                                                   decode);
            } else {
                addConstructorPublic(null, javaType + " value", constructorBody);
                addConstructorPublicWithExceptions(null, "DataInput from", "IOException", decode);
            }
            addMethodPublicDynamic(null, "void", "encodeTo", "DataOutput to", "IOException", encode);
            addMethodPublicReadObjectState(null, "int", "encodedLength", encodedSize);
            addMethodPublicReadObjectState(null, javaType, "getValue", new Block(RETURN(getField("value").ref())));
            addMethodPublicReadObjectState(null, "String", "toString",
                    new Block(RETURN(value2String.x(getField("value").ref()))));
            addMethod(null, Visibility.PUBLIC, Scope.OBJECT, "boolean", "equals", "Object other", null,
                    new Block(IF(
                            asBool("other instanceof " + name),
                            new Block(RETURN(asBool("this.value == ((" + name + ")other).getValue()"))),
                            new Block(RETURN(FALSE)))));
        }

        public FieldTypeBasic getBasicType() {
            return basicType;
        }

        public String getJavaType() {
            return javaType;
        }

        @Override
        public Collection<Requirement> getReqs() {
            return requirement;
        }

        @Override
        public Requirement getIFulfillReq() {
            return new Requirement(getName(), Requirement.Kind.FIELD_TYPE);
        }
    }

    public static interface Generator {
        public FieldTypeBasic getBasicFieldTypeOnInput(NetworkIO io);
        public Requirement.Kind needsDataInFormat();
    }
}
