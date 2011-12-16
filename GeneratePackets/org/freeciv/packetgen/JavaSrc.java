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
        return "package " + "org.freeciv.packet.fieldtype" + ";" + "\n" +
                "\n" +
                "import java.io.DataInput;" + "\n" +
                "import java.io.DataOutput;" + "\n" +
                "import java.io.IOException;" + "\n" +
                "\n" +
                "// This code was auto generated from Freeciv's protocol definition" + "\n" +
                "public class " + name + " implements FieldType<" + JavaType + "> {" + "\n" +
                "\t" + "private " + JavaType + " value;" + "\n" +
                "\n" +
                DataIO.publicConstructorNoExceptions(null, name, JavaType + " value", "this.value = value;") +
                "\n" +
                DataIO.publicConstructor(null, name, "DataInput from", "IOException", Decode) +
                "\n" +
                DataIO.publicDynamicMethod(null, "void", "encodeTo", "DataOutput to", "IOException", encode) +
                "\n" +
                DataIO.publicReadObjectState(null, "int", "encodedLength", EncodedSize) +
                "\n" +
                DataIO.publicReadObjectState(null, JavaType, "getValue", "return value;") +
                "\n" +
                DataIO.publicReadObjectState(null, "String", "toString", "return value.toString();") +
                "}";
    }
}
