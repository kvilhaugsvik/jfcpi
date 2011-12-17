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

import org.freeciv.packet.fieldtype.FieldType;

public class JavaSrc {
    String CSrc;
    String JavaType;
    String[] Decode;
    String[] encode, EncodedSize;

    public JavaSrc(String CSrc, String javaType, String decode, String encode, String encodedSize) {
        this.CSrc = CSrc;
        JavaType = javaType;
        Decode = decode.split("\n");
        this.encode = encode.split("\n");
        EncodedSize = encodedSize.split("\n");
    }

    public String getCSrc() {
        return CSrc;
    }

    public String getJavaType() {
        return JavaType;
    }

    public String toString(String name) {
        ClassWriter out = new ClassWriter(FieldType.class.getPackage(),
                new String[]{"java.io.DataInput", "java.io.DataOutput", "java.io.IOException"},
                "Freeciv's protocol definition",
                name,
                "FieldType<" + JavaType + ">");
        out.addStateVar(JavaType, "value");
        out.addPublicConstructor(null, name, JavaType + " value", "this.value = value;");
        out.addPublicConstructorWithExceptions(null, name, "DataInput from", "IOException", Decode);
        out.addPublicDynamicMethod(null, "void", "encodeTo", "DataOutput to", "IOException", encode);
        out.addPublicReadObjectState(null, "int", "encodedLength", EncodedSize);
        out.addPublicReadObjectState(null, JavaType, "getValue", "return value;");
        out.addPublicReadObjectState(null, "String", "toString", "return value.toString();");

        return out.toString();
    }
}
