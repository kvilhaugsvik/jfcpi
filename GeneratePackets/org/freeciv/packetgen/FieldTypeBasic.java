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

public class FieldTypeBasic implements IDependency {
    private final String fieldTypeBasic;
    private final String publicType;
    private final String javaType;
    private final String[] fromJavaType;
    private final String[] decode;
    private final String[] encode, encodedSize;
    private final boolean arrayEater;

    private final Collection<Requirement> requirement;
    private final FieldTypeBasic basicType = this;

    public FieldTypeBasic(String dataIOType, String publicType, String javaType, String[] fromJavaType,
                          String decode, String encode, String encodedSize, boolean arrayEater) {
        this.fieldTypeBasic = dataIOType + "(" + publicType + ")";
        this.publicType = publicType;
        this.javaType = javaType;
        this.decode = decode.split("\n");
        this.encode = encode.split("\n");
        this.encodedSize = encodedSize.split("\n");
        this.arrayEater = arrayEater;
        this.fromJavaType = fromJavaType;

        requirement = new ArrayList<Requirement>();
        String[] publicTypeParts = publicType.split("\\s");
        if ("enum".equals(publicTypeParts[0])) {
            requirement.add(new Requirement(publicTypeParts[1], Requirement.Kind.ENUM));
        }
    }

    public String getFieldTypeBasic() {
        return fieldTypeBasic;
    }

    public String getPublicType() {
        return publicType;
    }

    @Override
    public Collection<Requirement> getReqs() {
        return requirement;
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
                    "FieldType<" + javaType + ">");

            addObjectConstant(javaType, "value");
            if (arrayEater) {
                addConstructorPublic(null, name, javaType + " value" + ", int arraySize", fromJavaType);
                addConstructorPublicWithExceptions(null, name, "DataInput from" + ", int arraySize", "IOException", decode);
            } else {
                addConstructorPublic(null, name, javaType + " value", fromJavaType);
                addConstructorPublicWithExceptions(null, name, "DataInput from", "IOException", decode);
            }
            addMethodPublicDynamic(null, "void", "encodeTo", "DataOutput to", "IOException", encode);
            addMethodPublicReadObjectState(null, "int", "encodedLength", encodedSize);
            addMethodPublicReadObjectState(null, javaType, "getValue", "return value;");
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
}
