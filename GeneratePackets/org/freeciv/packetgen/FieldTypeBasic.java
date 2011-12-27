/*
 * Copyright (c) 2011. Sveinung Kvilhaugsvik
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

public class FieldTypeBasic {
    private final String fieldTypeBasic;
    private final String publicType;
    private final String JavaType;
    private final String[] Decode;
    private final String[] encode, EncodedSize;
    private final boolean arrayEater;

    private boolean hasReq;

    public FieldTypeBasic(String dataIOType, String publicType, String javaType,
                          String decode, String encode, String encodedSize, boolean needsType, boolean arrayEater) {
        this.fieldTypeBasic = dataIOType + "(" + publicType + ")";
        this.publicType = publicType;
        JavaType = javaType;
        Decode = decode.split("\n");
        this.encode = encode.split("\n");
        EncodedSize = encodedSize.split("\n");
        this.arrayEater = arrayEater;

        if (needsType)
            hasReq = false;
        else
            hasReq = true;
    }

    public String getFieldTypeBasic() {
        return fieldTypeBasic;
    }

    public boolean hasRequired() {
        return hasReq;
    }

    public void requirementsFound() {
        this.hasReq = true;
    }

    public String getPublicType() {
        return publicType;
    }

    public FieldTypeAlias createFieldType(String name) {
        return new FieldTypeAlias(name, this);
    }

    public boolean isArrayEater() {
        return arrayEater;
    }

    public class FieldTypeAlias extends ClassWriter {
        private FieldTypeBasic basicType;

        private FieldTypeAlias(String name, FieldTypeBasic basicType) {
            super(org.freeciv.packet.fieldtype.FieldType.class.getPackage(),
                    new String[]{
                            "java.io.DataInput", "java.io.DataOutput", "java.io.IOException",
                            null,
                            org.freeciv.types.FCEnum.class.getPackage().getName() + ".*"
                    },
                    "Freeciv's protocol definition",
                    name,
                    "FieldType<" + JavaType + ">");

            this.basicType = basicType;

            addObjectConstant(JavaType, "value");
            addPublicConstructor(null, name, JavaType + " value", "this.value = value;");
            addPublicConstructorWithExceptions(null, name, "DataInput from", "IOException", Decode);
            addPublicDynamicMethod(null, "void", "encodeTo", "DataOutput to", "IOException", encode);
            addPublicReadObjectState(null, "int", "encodedLength", EncodedSize);
            addPublicReadObjectState(null, JavaType, "getValue", "return value;");
            addPublicReadObjectState(null, "String", "toString", "return value.toString();");
        }

        public FieldTypeBasic getBasicType() {
            return basicType;
        }

        public String getJavaType() {
            return JavaType;
        }
    }
}
