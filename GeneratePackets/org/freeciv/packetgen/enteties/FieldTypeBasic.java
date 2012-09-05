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
import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.*;

import java.util.Collection;
import java.util.Collections;

import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.*;

public class FieldTypeBasic implements IDependency {
    private final String fieldTypeBasic;
    private final String publicType;
    private final TargetClass javaType;
    private final Block constructorBody;
    private final Block decode;
    private final Block encode;
    private final Block encodedSize;
    private final ExprFrom1<Typed<AString>, Var> value2String;
    private final boolean arrayEater;

    private final Collection<Requirement> requirement;
    private final FieldTypeBasic basicType = this;

    public FieldTypeBasic(String dataIOType, String publicType, TargetClass javaType,
                          ExprFrom1<Block, Var> constructorBody,
                          ExprFrom2<Block, Var, Var> decode,
                          ExprFrom2<Block, Var, Var> encode,
                          ExprFrom1<Typed<AnInt>, Var> encodedSize,
                          ExprFrom1<Typed<AString>, Var> toString,
                          boolean arrayEater, Collection<Requirement> needs) {
        Var from = Var.local(java.io.DataInput.class, "from", null);
        Var to = Var.local(java.io.DataOutput.class, "to", null);
        Var value = Var.field(Collections.<Annotate>emptyList(), Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO,
                        javaType, "value", null);

        this.fieldTypeBasic = dataIOType + "(" + publicType + ")";
        this.publicType = publicType;
        this.javaType = javaType;
        this.decode = decode.x(value, from);
        this.encode = encode.x(value, to);
        this.encodedSize = new Block(RETURN(encodedSize.x(value)));
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
            super(ClassKind.CLASS, new TargetPackage(org.freeciv.packet.fieldtype.FieldType.class.getPackage()), new Import[]{
                                                  Import.classIn(java.io.DataInput.class),
                                                  Import.classIn(java.io.DataOutput.class),
                                                  Import.classIn(java.io.IOException.class),
                                                  null,
                                                  Import.allIn(new TargetPackage(org.freeciv.types.FCEnum.class.getPackage()))
                                          }, "Freeciv's protocol definition", Collections.<Annotate>emptyList(), name, null, "FieldType<" + javaType.getName() + ">");

            addObjectConstant(javaType.getName(), "value");
            if (arrayEater) {
                addMethod(Method.newPublicConstructor(null,
                        getName(), javaType.getName() + " value" + ", int arraySize",
                        constructorBody));
                addMethod(Method.newPublicConstructorWithException(null,
                        getName(), "DataInput from" + ", int arraySize",
                        "IOException", decode));
            } else {
                addMethod(Method.newPublicConstructor(null, getName(), javaType.getName() + " value", constructorBody));
                addMethod(Method.newPublicConstructorWithException(null, getName(), "DataInput from", "IOException", decode));
            }
            addMethod(Method.newPublicDynamicMethod(null, TargetClass.fromName("void"), "encodeTo", "DataOutput to", "IOException", encode));
            addMethod(Method.newPublicReadObjectState(null, TargetClass.fromName("int"), "encodedLength", encodedSize));
            addMethod(Method.newPublicReadObjectState(null, TargetClass.fromName(javaType.getName()), "getValue", new Block(RETURN(getField("value").ref()))));
            addMethod(Method.newPublicReadObjectState(null, TargetClass.fromName("String"), "toString", new Block(RETURN(value2String.x(getField("value"))))));
            addMethod(new Method(null, Visibility.PUBLIC, Scope.OBJECT,
                    TargetClass.fromName("boolean"), "equals", "Object other",
                    null, new Block(IF(
                            asBool("other instanceof " + name),
                            new Block(RETURN(asBool("this.value == ((" + name + ")other).getValue()"))),
                            new Block(RETURN(FALSE))))));
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
            return new Requirement(getName(), Requirement.Kind.FIELD_TYPE);
        }
    }

    public static interface Generator {
        public FieldTypeBasic getBasicFieldTypeOnInput(NetworkIO io);
        public Requirement.Kind needsDataInFormat();
    }
}
