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

import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.BitVector;
import org.freeciv.packetgen.enteties.FieldTypeBasic;
import org.freeciv.packetgen.enteties.SpecialClass;
import org.freeciv.packetgen.enteties.supporting.*;
import org.freeciv.packetgen.javaGenerator.TargetClass;
import org.freeciv.packetgen.javaGenerator.TargetPackage;
import org.freeciv.packetgen.javaGenerator.Var;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.creators.ExprFrom1;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.NoValue;

import java.util.*;

import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.*;

//TODO: Move data to file
public class Hardcoded {
    private static final Collection<IDependency> hardCodedElements = Arrays.<IDependency>asList(
            new FieldTypeBasic("uint32", "int",
                               "Long",
                               "int bufferValue = from.readInt();" + "\n" +
                                       "if (0 <= bufferValue) {" + "\n" +
                                       "value = (long)bufferValue;" + "\n" +
                                       "} else {" + "\n" +
                                       "final long removedByCast = (-1L * Integer.MIN_VALUE) + Integer.MAX_VALUE + " +
                                       "1L;" + "\n" +
                                       "value = (long)bufferValue + removedByCast;" + "\n" +
                                       "}",
                               "to.writeInt(value.intValue()); // int is two's compliment so a uint32 don't lose " +
                                       "information",
                               "return 4;",
                               false, Collections.<Requirement>emptySet()),
            new FieldTypeBasic("requirement", "struct requirement", "requirement",
                    "value = new requirement(\n" +
                            "new universal(universals_n.valueOf(from.readUnsignedByte()),\n" +
                            "from.readInt()),\n" +
                            "req_range.valueOf(from.readUnsignedByte()),\n" +
                            "from.readBoolean(),\n" +
                            "from.readBoolean());\n",
                    "to.writeByte(value.getsource().kind.getNumber());\n" +
                            "to.writeInt(value.getsource().value);\n" +
                            "to.writeByte(value.getrange().getNumber());\n" +
                            "to.writeBoolean(value.getsurvives());\n" +
                            "to.writeBoolean(value.getnegated());",
                    "return 8;",
                    false,
                    Arrays.asList(
                            new Requirement("struct requirement", Requirement.Kind.AS_JAVA_DATATYPE),
                            new Requirement("enum req_range", Requirement.Kind.AS_JAVA_DATATYPE),
                            new Requirement("enum universals_n", Requirement.Kind.AS_JAVA_DATATYPE),
                            new Requirement("struct universal", Requirement.Kind.AS_JAVA_DATATYPE))
            ),
            new FieldTypeBasic("worklist", "struct worklist", "universal[]",
                    "int length = from.readUnsignedByte();" + "\n" +
                            "value = new universal[length];" + "\n" +
                            "for (int i = 0; i < length; i++) {" + "\n" +
                            "value[i] = new universal(" + "\n" +
                            "universals_n.valueOf(from.readUnsignedByte())," + "\n" +
                            "from.readUnsignedByte());" + "\n" +
                            "}",
                    "to.writeByte(value.length);\n" +
                            "for (universal element : value) {" + "\n" +
                            "to.writeByte(element.kind.getNumber());" + "\n" +
                            "to.writeByte(element.value);" + "\n" +
                            "}",
                    "return value.length;",
                    false,
                    Arrays.asList(
                            new Requirement("enum universals_n", Requirement.Kind.AS_JAVA_DATATYPE),
                            new Requirement("struct universal", Requirement.Kind.AS_JAVA_DATATYPE))
            ),
            getFloat("100"),
            getFloat("10000"),
            getFloat("1000000"),
            new FieldTypeBasic("string", "char",
                               "String",
                               new ExprFrom1<Block, Var>() {
                                   @Override
                                   public Block x(Var to) {
                                       return new Block(
                                               arrayEaterScopeCheck("arraySize < value.length()"),
                                               to.assign(asAValue("value")));
                                   }
                               },
                               new ExprFrom1<Block, Var>() {
                                   @Override
                                   public Block x(Var to) {
                                       TargetClass sb = new TargetClass(StringBuffer.class);
                                       Var buf =
                                               Var.local("StringBuffer", "buf",
                                                       sb.newInstance());
                                       Var letter = Var.local("byte", "letter",
                                               asAValue("from.readByte()"));
                                       Var read = Var.local("int", "read",
                                               asAnInt("0"));
                                       return new Block(
                                               buf,
                                               letter,
                                               read,
                                               WHILE(asBool("0 != letter"), new Block(
                                                       asVoid("read++"),
                                                       arrayEaterScopeCheck("arraySize < read"),
                                                       asVoid("buf.append((char)letter)"),
                                                       letter.assign(asAnInt("from.readByte()")))),
                                               IF(asBool("buf.length() == 0"),
                                                       new Block(to.assign(asAString("\"\""))),
                                                       new Block(to.assign(asAString("buf.toString()")))));
                                   }
                               },
                               "to.writeBytes(" + "value" + ");\n" +
                                       "to.writeByte(0);",
                               "return " + "value" + ".length() + 1;",
                               TO_STRING_OBJECT, true, Collections.<Requirement>emptySet()),
            new TerminatedArray("tech_list", "int",
                                new Requirement("MAX_NUM_TECH_LIST", Requirement.Kind.VALUE),
                                new Requirement("A_LAST", Requirement.Kind.VALUE)),
            new TerminatedArray("unit_list", "int",
                                new Requirement("MAX_NUM_UNIT_LIST", Requirement.Kind.VALUE),
                                new Requirement("U_LAST", Requirement.Kind.VALUE)),
            new TerminatedArray("building_list", "int",
                                new Requirement("MAX_NUM_BUILDING_LIST", Requirement.Kind.VALUE),
                                new Requirement("B_LAST", Requirement.Kind.VALUE)),
            new FieldTypeBasic("memory", "unsigned char",
                               "byte[]",
                               new ExprFrom1<Block, Var>() {
                                   @Override
                                   public Block x(Var to) {
                                       return new Block(
                                               arrayEaterScopeCheck("arraySize != value.length"),
                                               to.assign(asAValue("value")));
                                   }
                               },
                               new ExprFrom1<Block, Var>() {
                                   @Override
                                   public Block x(Var to) {
                                       Var innBuf =
                                               Var.local("byte[]", "innBuffer",
                                                       asAValue("new byte[arraySize]"));
                                       Block reader = new Block(innBuf);
                                       reader.addStatement(asVoid("from.readFully(innBuffer)"));
                                       reader.addStatement(to.assign(innBuf.ref()));
                                       return reader;
                                   }
                               },
                               "to.write(" + "value" + ");\n",
                               "return " + "value" + ".length;",
                               TO_STRING_OBJECT, true, Collections.<Requirement>emptySet()),
            new FieldTypeBasic("bool8", "bool",
                               "Boolean",
                               "value = from.readBoolean();",
                               "to.writeBoolean(value);",
                               "return 1;",
                               false, Collections.<Requirement>emptySet()),

            /************************************************************************************************
             * Read from and write to the network
             ************************************************************************************************/
            NetworkIO.withBytesAsIntermediate("bitvector"),
            NetworkIO.witIntAsIntermediate("uint8", "1", "from.readUnsignedByte()", "to.writeByte"),
            // to.writeByte wraps around so -128 shares encoding with 128
            NetworkIO.witIntAsIntermediate("sint8", "1", "(int) from.readByte()", "to.writeByte"),
            NetworkIO.witIntAsIntermediate("uint16", "2", "(int) from.readChar()", "to.writeChar"),
            NetworkIO.witIntAsIntermediate("sint16", "2", "(int) from.readShort()", "to.writeShort"),
            NetworkIO.witIntAsIntermediate("sint32", "4", "from.readInt()", "to.writeInt"),

            /************************************************************************************************
             * Built in types
             ************************************************************************************************/
            (IDependency)(new SimpleTypeAlias("int", "Integer", Collections.<Requirement>emptySet()))
    );

    public static NoValue arrayEaterScopeCheck(String check) {
        return IF(asBool(check), Block.fromStrings("throw new IllegalArgumentException(\"Value out of scope\")"));
    }

    public static void applyManualChanges(PacketsStore toStorage) {
        // TODO: autoconvert the enums
        // TODO: when given the location of the tables convert table items as well
        SpecialClass handRolledUniversal =
                new SpecialClass(new TargetPackage(org.freeciv.types.FCEnum.class.getPackage()),
                "Freeciv source interpreted by hand", "universal",
                new Requirement("struct universal", Requirement.Kind.AS_JAVA_DATATYPE),
                Collections.<Requirement>emptySet());
        handRolledUniversal.addPublicObjectConstant("universals_n", "kind");
        handRolledUniversal.addPublicObjectConstant("int", "value");
        handRolledUniversal.addConstructorFields();
        handRolledUniversal.addMethodPublicReadObjectState(null, "String", "toString",
                new Block(RETURN(sum(literalString("("),
                        handRolledUniversal.getField("kind").ref(),
                        literalString(":"),
                        handRolledUniversal.getField("value").ref(),
                        literalString(")")))));
        toStorage.addDependency(handRolledUniversal);
    }

    public static Collection<IDependency> values() {
        HashSet<IDependency> out = new HashSet<IDependency>(hardCodedElements);
        BitVector bitString = new BitVector();
        out.add(bitString);
        out.add(bitString.getBasicFieldTypeOnInput(
                NetworkIO.withBytesAsIntermediate("bit_string")));
        return out;
    }

    private static FieldTypeBasic getFloat(String times) {
        return new FieldTypeBasic("float" + times, "float",
                                  "Float",
                                  "value = from.readFloat() / " + times + ";",
                                  "to.writeFloat(value * " + times + ");",
                                  "return 4;",
                                  false, Collections.<Requirement>emptySet());
    }
}
