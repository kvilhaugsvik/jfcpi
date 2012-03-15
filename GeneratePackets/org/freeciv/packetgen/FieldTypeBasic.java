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

package org.freeciv.packetgen;

import java.util.*;

public class FieldTypeBasic {
    private final String fieldTypeBasic;
    private final String publicType;
    private final String JavaType;
    private final String[] fromJavaType;
    private final String[] Decode;
    private final String[] encode, EncodedSize;
    private final boolean arrayEater;

    private final Collection<Requirement> requirement;
    private final FieldTypeBasic basicType = this;

    public FieldTypeBasic(String dataIOType, String publicType, String javaType, String[] fromJavaType,
                          String decode, String encode, String encodedSize, boolean needsType, boolean arrayEater) {
        this.fieldTypeBasic = dataIOType + "(" + publicType + ")";
        this.publicType = publicType;
        JavaType = javaType;
        Decode = decode.split("\n");
        this.encode = encode.split("\n");
        EncodedSize = encodedSize.split("\n");
        this.arrayEater = arrayEater;
        this.fromJavaType = fromJavaType;

        requirement = new ArrayList<Requirement>();
        if (needsType)
            requirement.add(new Requirement(publicType.split("\\s")[1], Requirement.Kind.ENUM));
    }

    public String getFieldTypeBasic() {
        return fieldTypeBasic;
    }

    public String getPublicType() {
        return publicType;
    }

    public FieldTypeAlias createFieldType(String name) {
        return new FieldTypeAlias(name);
    }

    public boolean isArrayEater() {
        return arrayEater;
    }

    public class FieldTypeAlias extends ClassWriter implements IDependency {

        private FieldTypeAlias(String name) {
            super(org.freeciv.packet.fieldtype.FieldType.class.getPackage(),
                    new String[]{
                            java.io.DataInput.class.getCanonicalName(),
                            java.io.DataOutput.class.getCanonicalName(),
                            java.io.IOException.class.getCanonicalName(),
                            null,
                            allInPackageOf(org.freeciv.types.FCEnum.class)
                    },
                    "Freeciv's protocol definition",
                    name,
                    "FieldType<" + JavaType + ">");

            addObjectConstant(JavaType, "value");
            if (arrayEater) {
                addConstructorPublic(null, name, JavaType + " value" + ", int arraySize", fromJavaType);
                addConstructorPublicWithExceptions(null, name, "DataInput from" + ", int arraySize", "IOException", Decode);
            } else {
                addConstructorPublic(null, name, JavaType + " value", fromJavaType);
                addConstructorPublicWithExceptions(null, name, "DataInput from", "IOException", Decode);
            }
            addMethodPublicDynamic(null, "void", "encodeTo", "DataOutput to", "IOException", encode);
            addMethodPublicReadObjectState(null, "int", "encodedLength", EncodedSize);
            addMethodPublicReadObjectState(null, JavaType, "getValue", "return value;");
            addMethodPublicReadObjectState(null, "String", "toString", "return value.toString();");
            addMethod(null, Visibility.PUBLIC, Scope.OBJECT, "boolean", "equals", "Object other", null,
                "if (other instanceof " + name + ") {",
                    "return this.value == ((" + name + ")other).getValue();",
                "} else {",
                    "return false;",
                "}");
        }

        public FieldTypeBasic getBasicType() {
            return basicType;
        }

        public String getJavaType() {
            return JavaType;
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
}
