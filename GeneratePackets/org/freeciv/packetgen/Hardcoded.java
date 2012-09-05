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
import org.freeciv.packetgen.javaGenerator.*;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.Statement;
import org.freeciv.packetgen.javaGenerator.expression.creators.ExprFrom1;
import org.freeciv.packetgen.javaGenerator.expression.creators.ExprFrom2;
import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.ABool;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AnInt;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.NoValue;

import java.util.*;

import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.*;

//TODO: Move data to file
public class Hardcoded {
    private static final Collection<IDependency> hardCodedElements = Arrays.<IDependency>asList(
            new FieldTypeBasic("uint32", "int", new TargetClass(Long.class),
                    new ExprFrom1<Block, Var>() {
                        @Override
                        public Block x(Var arg1) {
                            return new Block(arg1.assign(asAValue("value")));
                        }
                    },
                    new ExprFrom2<Block, Var, Var>() {
                        @Override
                        public Block x(Var to, Var from) {
                            Var buf = Var.local("int", "bufferValue", from.<AnInt>call("readInt"));
                            return new Block(buf, IF(asBool("0 <= bufferValue"), new Block(
                                    to.assign(asAValue("(long)bufferValue"))), Block.fromStrings(
                                    "final long removedByCast = (-1L * Integer.MIN_VALUE) + Integer.MAX_VALUE + " + "1L",
                                    "this.value = (long)bufferValue + removedByCast")));
                        }
                    },
                    new ExprFrom2<Block, Var, Var>() {
                        @Override
                        public Block x(Var val, Var to) {
                            Block out = new Block();
                            out.addStatement(new Statement(to.call("writeInt", val.<AnInt>call("intValue")),
                                    "int is two's compliment so a uint32 don't lose information"));
                            return out;
                        }
                    },
                    new ExprFrom1<Typed<AnInt>, Var>() {
                        @Override
                        public Typed<AnInt> x(Var arg1) {
                            return asAnInt("4");
                        }
                    },
                    TO_STRING_OBJECT,
                               false, Collections.<Requirement>emptySet()),
            new FieldTypeBasic("requirement", "struct requirement", new TargetClass("requirement"),
                    new ExprFrom1<Block, Var>() {
                        @Override
                        public Block x(Var arg1) {
                            return new Block(arg1.assign(asAValue("value")));
                        }
                    },
                    new ExprFrom2<Block, Var, Var>() {
                        @Override
                        public Block x(Var to, Var from) {
                            return new Block(to.assign((new TargetClass("requirement")).newInstance(
                                    new TargetClass("universal").newInstance(
                                            new MethodCall<AValue>(null, "universals_n.valueOf",
                                                    from.<AValue>call("readUnsignedByte")),
                                            from.<AValue>call("readInt")),
                                    new MethodCall<AValue>(null, "req_range.valueOf",
                                            from.<AValue>call("readUnsignedByte")),
                                    from.<AValue>call("readBoolean"),
                                    from.<AValue>call("readBoolean"))));
                        }
                    },
                    new ExprFrom2<Block, Var, Var>() {
                        @Override
                        public Block x(Var val, Var to) {
                            return Block.fromStrings(
                                    "to.writeByte(this.value.getsource().kind.getNumber())",
                                    "to.writeInt(this.value.getsource().value)",
                                    "to.writeByte(this.value.getrange().getNumber())",
                                    "to.writeBoolean(this.value.getsurvives())",
                                    "to.writeBoolean(this.value.getnegated())");
                        }
                    },
                    new ExprFrom1<Typed<AnInt>, Var>() {
                        @Override
                        public Typed<AnInt> x(Var arg1) {
                            return asAnInt("8");
                        }
                    },
                    TO_STRING_OBJECT,
                    false,
                    Arrays.asList(
                            new Requirement("struct requirement", Requirement.Kind.AS_JAVA_DATATYPE),
                            new Requirement("enum req_range", Requirement.Kind.AS_JAVA_DATATYPE),
                            new Requirement("enum universals_n", Requirement.Kind.AS_JAVA_DATATYPE),
                            new Requirement("struct universal", Requirement.Kind.AS_JAVA_DATATYPE))
            ),
            new FieldTypeBasic("worklist", "struct worklist", new TargetClass("universal[]"),
                    new ExprFrom1<Block, Var>() {
                        @Override
                        public Block x(Var arg1) {
                            return new Block(arg1.assign(asAValue("value")));
                        }
                    },
                    new ExprFrom2<Block, Var, Var>() {
                                   @Override
                                   public Block x(Var to, Var from) {
                                       TargetClass universal = new TargetClass("org.freeciv.types.universal");
                                       Var len = Var.local("int", "length", from.<AValue>call("readUnsignedByte"));
                                       Var counter = Var.local("int", "i",
                                               asAnInt("0"));
                                       return new Block(
                                               len,
                                               to.assign(asAValue("new universal[length]")),
                                               FOR(
                                                       counter,
                                                       isSmallerThan(counter.ref(), len.ref()),
                                                       inc(counter),
                                                       new Block(arraySetElement(
                                                               to,
                                                               counter.ref(),
                                                               universal.newInstance(
                                                                       new MethodCall<AValue>(
                                                                               null,
                                                                               "universals_n.valueOf",
                                                                               from.<AValue>call("readUnsignedByte")),
                                                                       from.<AValue>call("readUnsignedByte")
                                                       )))));
                                   }
                    },
                    new ExprFrom2<Block, Var, Var>() {
                        @Override
                        public Block x(Var val, Var to) {
                            Var elem = Var.local("universal", "element", null);
                            return new Block(
                                    to.call("writeByte", val.read("length")),
                                    FOR(elem, val.ref(),
                                            new Block(to.call("writeByte", asAnInt("element.kind.getNumber()")),
                                                    to.call("writeByte", elem.read("value")))));
                        }
                    },
                    new ExprFrom1<Typed<AnInt>, Var>() {
                        @Override
                        public Typed<AnInt> x(Var value) {
                            return asAnInt("this.value.length");
                        }
                    },
                    TO_STRING_OBJECT,
                    false,
                    Arrays.asList(
                            new Requirement("enum universals_n", Requirement.Kind.AS_JAVA_DATATYPE),
                            new Requirement("struct universal", Requirement.Kind.AS_JAVA_DATATYPE))
            ),
            getFloat("100"),
            getFloat("10000"),
            getFloat("1000000"),
            new FieldTypeBasic("string", "char", new TargetClass(String.class),
                               new ExprFrom1<Block, Var>() {
                                   @Override
                                   public Block x(Var to) {
                                       return new Block(
                                               arrayEaterScopeCheck("arraySize < value.length()"),
                                               to.assign(asAValue("value")));
                                   }
                               },
                               new ExprFrom2<Block, Var, Var>() {
                                   @Override
                                   public Block x(Var to, Var from) {
                                       TargetClass sb = new TargetClass(StringBuffer.class);
                                       Var buf =
                                               Var.local("StringBuffer", "buf",
                                                       sb.newInstance());
                                       Var letter = Var.local("byte", "letter", from.<AValue>call("readByte"));
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
                                                       letter.assign(from.<AValue>call("readByte")))),
                                               IF(asBool("buf.length() == 0"),
                                                       new Block(to.assign(asAString("\"\""))),
                                                       new Block(to.assign(asAString("buf.toString()")))));
                                   }
                               },
                    new ExprFrom2<Block, Var, Var>() {
                        @Override
                        public Block x(Var val, Var to) {
                            return new Block(
                                    to.call("writeBytes", val.ref()),
                                    to.call("writeByte", asAnInt("0")));
                        }
                    },
                    new ExprFrom1<Typed<AnInt>, Var>() {
                        @Override
                        public Typed<AnInt> x(Var value) {
                            return sum(asAnInt("this.value.length()"), asAnInt("1"));
                        }
                    },
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
            new FieldTypeBasic("memory", "unsigned char", new TargetClass("byte[]"),
                               new ExprFrom1<Block, Var>() {
                                   @Override
                                   public Block x(Var to) {
                                       return new Block(
                                               arrayEaterScopeCheck("arraySize != value.length"),
                                               to.assign(asAValue("value")));
                                   }
                               },
                               new ExprFrom2<Block, Var, Var>() {
                                   @Override
                                   public Block x(Var to, Var from) {
                                       Var innBuf =
                                               Var.local("byte[]", "innBuffer",
                                                       asAValue("new byte[arraySize]"));
                                       Block reader = new Block(innBuf);
                                       reader.addStatement(from.call("readFully", innBuf.ref()));
                                       reader.addStatement(to.assign(innBuf.ref()));
                                       return reader;
                                   }
                               },
                    new ExprFrom2<Block, Var, Var>() {
                        @Override
                        public Block x(Var value, Var to) {
                            return new Block(to.call("write", value.ref()));
                        }
                    },
                    new ExprFrom1<Typed<AnInt>, Var>() {
                        @Override
                        public Typed<AnInt> x(Var value) {
                            return asAnInt("this.value.length");
                        }
                    },
                               TO_STRING_OBJECT, true, Collections.<Requirement>emptySet()),
            new FieldTypeBasic("bool8", "bool",  new TargetClass(Boolean.class),
                    new ExprFrom1<Block, Var>() {
                        @Override
                        public Block x(Var arg1) {
                            return new Block(arg1.assign(asAValue("value")));
                        }
                    },
                    new ExprFrom2<Block, Var, Var>() {
                        @Override
                        public Block x(Var to, Var from) {
                            return new Block(to.assign(from.<ABool>call("readBoolean")));
                        }
                    },
                    new ExprFrom2<Block, Var, Var>() {
                        @Override
                        public Block x(Var value, Var to) {
                            return new Block(to.call("writeBoolean", value.ref()));
                        }
                    },
                    new ExprFrom1<Typed<AnInt>, Var>() {
                        @Override
                        public Typed<AnInt> x(Var arg1) {
                            return asAnInt("1");
                        }
                    },
                               TO_STRING_OBJECT,
                               false, Collections.<Requirement>emptySet()),

            /************************************************************************************************
             * Read from and write to the network
             ************************************************************************************************/
            NetworkIO.withBytesAsIntermediate("bitvector"),
            NetworkIO.witIntAsIntermediate("uint8", 1, "from.readUnsignedByte()", "to.writeByte"),
            // to.writeByte wraps around so -128 shares encoding with 128
            NetworkIO.witIntAsIntermediate("sint8", 1, "(int) from.readByte()", "to.writeByte"),
            NetworkIO.witIntAsIntermediate("uint16", 2, "(int) from.readChar()", "to.writeChar"),
            NetworkIO.witIntAsIntermediate("sint16", 2, "(int) from.readShort()", "to.writeShort"),
            NetworkIO.witIntAsIntermediate("sint32", 4, "from.readInt()", "to.writeInt"),

            /************************************************************************************************
             * Built in types
             ************************************************************************************************/
            (IDependency)(new SimpleTypeAlias("int", "Integer", Collections.<Requirement>emptySet()))
    );

    public static Typed<NoValue> arrayEaterScopeCheck(String check) {
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
        handRolledUniversal.addMethod(Method.newPublicReadObjectState(null, TargetClass.fromName("String"), "toString",
                new Block(RETURN(sum(literalString("("),
                        handRolledUniversal.getField("kind").ref(),
                        literalString(":"),
                        handRolledUniversal.getField("value").ref(),
                        literalString(")"))))));
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

    private static FieldTypeBasic getFloat(final String times) {
        return new FieldTypeBasic("float" + times, "float",  new TargetClass(Float.class),
                new ExprFrom1<Block, Var>() {
                    @Override
                    public Block x(Var arg1) {
                        return new Block(arg1.assign(asAValue("value")));
                    }
                },
                new ExprFrom2<Block, Var, Var>() {
                    @Override
                    public Block x(Var out, Var inn) {
                        return new Block(out.assign(divide(inn.<AValue>call("readFloat"), asAValue(times))));
                    }
                },
                new ExprFrom2<Block, Var, Var>() {
                    @Override
                    public Block x(Var value, Var to) {
                        return new Block(to.call("writeFloat", BuiltIn.<AnInt>multiply(value.ref(), asAnInt(times))));
                    }
                },
                new ExprFrom1<Typed<AnInt>, Var>() {
                    @Override
                    public Typed<AnInt> x(Var arg1) {
                        return asAnInt("4");
                    }
                },
                TO_STRING_OBJECT,
                                  false, Collections.<Requirement>emptySet());
    }
}
