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

import org.freeciv.packet.fieldtype.ElementsLimit;
import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.BitVector;
import org.freeciv.packetgen.enteties.Constant;
import org.freeciv.packetgen.enteties.FieldTypeBasic;
import org.freeciv.packetgen.enteties.SpecialClass;
import org.freeciv.packetgen.enteties.supporting.*;
import org.freeciv.packetgen.javaGenerator.*;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.Statement;
import org.freeciv.packetgen.javaGenerator.typeBridge.From1;
import org.freeciv.packetgen.javaGenerator.typeBridge.From2;
import org.freeciv.packetgen.javaGenerator.typeBridge.Typed;
import org.freeciv.packetgen.javaGenerator.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.typeBridge.willReturn.ABool;
import org.freeciv.packetgen.javaGenerator.typeBridge.willReturn.AValue;
import org.freeciv.packetgen.javaGenerator.typeBridge.willReturn.AnInt;
import org.freeciv.packetgen.javaGenerator.typeBridge.willReturn.Returnable;

import java.util.*;

import static org.freeciv.packetgen.javaGenerator.util.BuiltIn.*;

//TODO: Move data to file
public class Hardcoded {
    // TODO: Make parameters in stead
    public static final Var<TargetClass> pLimits = Var.param(ElementsLimit.class, "limits");
    private static final Var pValue = Var.param(String.class, "value"); // can't know type
    public static final Var fMaxSize = Var.field(Collections.<Annotate>emptyList(),
            Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO,
            TargetClass.fromClass(ElementsLimit.class), "maxArraySize", null);

    private static final Collection<IDependency> hardCodedElements = Arrays.<IDependency>asList(
            new FieldTypeBasic("uint32", "int", TargetClass.fromClass(Long.class),
                    new From1<Block, Var>() {
                        @Override
                        public Block x(Var arg1) {
                            return new Block(arg1.assign(pValue.ref()));
                        }
                    },
                    new From2<Block, Var, Var>() {
                        @Override
                        public Block x(Var to, Var from) {
                            Var buf = Var.local(int.class, "bufferValue", from.ref().<AnInt>call("readInt"));
                            return new Block(buf, IF(BuiltIn.<ABool>toCode("0 <= bufferValue"), new Block(
                                    to.assign(BuiltIn.<AValue>toCode("(long)bufferValue"))), Block.fromStrings(
                                    "final long removedByCast = (-1L * Integer.MIN_VALUE) + Integer.MAX_VALUE + " + "1L",
                                    "this.value = (long)bufferValue + removedByCast")));
                        }
                    },
                    new From2<Block, Var, Var>() {
                        @Override
                        public Block x(Var val, Var to) {
                            Block out = new Block();
                            out.addStatement(new Statement(to.ref().<Returnable>call("writeInt", val.ref().<AnInt>call("intValue")),
                                    Comment.c("int is two's compliment so a uint32 don't lose information")));
                            return out;
                        }
                    },
                    new From1<Typed<AnInt>, Var>() {
                        @Override
                        public Typed<AnInt> x(Var arg1) {
                            return literal(4);
                        }
                    },
                    TO_STRING_OBJECT,
                               false, Collections.<Requirement>emptySet()),
            new FieldTypeBasic("requirement", "struct requirement", TargetClass.newKnown("org.freeciv.types", "requirement"),
                    new From1<Block, Var>() {
                        @Override
                        public Block x(Var arg1) {
                            return new Block(arg1.assign(pValue.ref()));
                        }
                    },
                    new From2<Block, Var, Var>() {
                        @Override
                        public Block x(Var to, Var from) {
                            return new Block(to.assign((TargetClass.newKnown("org.freeciv.types", "requirement")).newInstance(
                                    TargetClass.newKnown("org.freeciv.types", "universal").newInstance(
                                            new MethodCall<AValue>("universals_n.valueOf",
                                                    from.ref().<AValue>call("readUnsignedByte")),
                                            from.ref().<AValue>call("readInt")),
                                    new MethodCall<AValue>("req_range.valueOf",
                                            from.ref().<AValue>call("readUnsignedByte")),
                                    from.ref().<AValue>call("readBoolean"),
                                    from.ref().<AValue>call("readBoolean"))));
                        }
                    },
                    new From2<Block, Var, Var>() {
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
                    new From1<Typed<AnInt>, Var>() {
                        @Override
                        public Typed<AnInt> x(Var arg1) {
                            return literal(8);
                        }
                    },
                    TO_STRING_OBJECT,
                    false,
                    Arrays.asList(
                            new Requirement("struct requirement", DataType.class),
                            new Requirement("enum req_range", DataType.class),
                            new Requirement("enum universals_n", DataType.class),
                            new Requirement("struct universal", DataType.class))
            ),
            getFloat("100"),
            getFloat("10000"),
            getFloat("1000000"),
            new TerminatedArray("string", "char", TargetClass.fromClass(String.class),
                    new Requirement("STRING_ENDER", Constant.class),
                    TerminatedArray.MaxArraySize.CONSTRUCTOR_PARAM,
                    TerminatedArray.TransferArraySize.CONSTRUCTOR_PARAM,
                    TerminatedArray.byteArray,
                    new From1<Typed<AnInt>, Var>() {
                        @Override
                        public Typed<AnInt> x(Var value) {
                            return value.ref().callV("getBytes").callV("length");
                        }
                    },
                    TerminatedArray.addAfterIfSmallerThanMaxSize,
                    new From1<Typed<AValue>, Var>() {
                        @Override
                        public Typed<AValue> x(Var everything) {
                            return everything.ref().<Returnable>call("getBytes");
                        }
                    },
                    new From1<Typed<AValue>, Typed<AValue>>() {
                        @Override
                        public Typed<AValue> x(Typed<AValue> bytes) {
                            return (TargetClass.fromClass(String.class)).newInstance(bytes);
                        }
                    },
                    TerminatedArray.elemIsByteArray,
                    TerminatedArray.readByte,
                    TO_STRING_OBJECT,
                    Arrays.asList(new Requirement("STRING_ENDER", Constant.class)),
                    null,
                    null,
                    new From1<Typed<AnInt>, Var>() {
                        @Override
                        public Typed<AnInt> x(Var value) {
                            return value.ref().callV("getBytes").callV("length");
                        }
                    },
                    TerminatedArray.sameNumberOfBufferElementsAndValueElements,
                    Collections.<Method.Helper>emptyList(),
                    false
            ),
            TerminatedArray.maxSizedTerminated("tech_list", "int",
                    new Requirement("A_LAST", Constant.class)),
            TerminatedArray.maxSizedTerminated("unit_list", "int",
                    new Requirement("U_LAST", Constant.class)),
            TerminatedArray.maxSizedTerminated("building_list", "int",
                    new Requirement("B_LAST", Constant.class)),
            TerminatedArray.xBytes("memory", "unsigned char"),
            new FieldTypeBasic("bool8", "bool", TargetClass.fromClass(Boolean.class),
                    new From1<Block, Var>() {
                        @Override
                        public Block x(Var arg1) {
                            return new Block(arg1.assign(pValue.ref()));
                        }
                    },
                    new From2<Block, Var, Var>() {
                        @Override
                        public Block x(Var to, Var from) {
                            return new Block(to.assign(from.ref().<ABool>call("readBoolean")));
                        }
                    },
                    new From2<Block, Var, Var>() {
                        @Override
                        public Block x(Var value, Var to) {
                            return new Block(to.ref().<Returnable>call("writeBoolean", value.ref()));
                        }
                    },
                    new From1<Typed<AnInt>, Var>() {
                        @Override
                        public Typed<AnInt> x(Var arg1) {
                            return literal(1);
                        }
                    },
                               TO_STRING_OBJECT,
                               false, Collections.<Requirement>emptySet()),

            /************************************************************************************************
             * Read from and write to the network
             ************************************************************************************************/
            NetworkIO.witIntAsIntermediate("uint8", 1, "readUnsignedByte", true, "writeByte"),
            // to.writeByte wraps around so -128 shares encoding with 128
            NetworkIO.witIntAsIntermediate("sint8", 1, "readByte", false, "writeByte"),
            NetworkIO.witIntAsIntermediate("uint16", 2, "readChar", false, "writeChar"),
            NetworkIO.witIntAsIntermediate("sint16", 2, "readShort", false, "writeShort"),
            NetworkIO.witIntAsIntermediate("sint32", 4, "readInt", true, "writeInt"),

            /************************************************************************************************
             * Built in types
             ************************************************************************************************/
            (IDependency)(new SimpleTypeAlias("int", "java.lang", "Integer", Collections.<Requirement>emptySet())),

            /************************************************************************************************
             * Built in constants
             ************************************************************************************************/
            Constant.isInt("STRING_ENDER", IntExpression.integer("0"))
    );

    public static void applyManualChanges(PacketsStore toStorage) {
        // TODO: autoconvert the enums
        // TODO: when given the location of the tables convert table items as well
        SpecialClass handRolledUniversal =
                new SpecialClass(TargetPackage.from(org.freeciv.types.FCEnum.class.getPackage()),
                "Freeciv source interpreted by hand", "universal",
                new Requirement("struct universal", DataType.class),
                Collections.<Requirement>emptySet());
        handRolledUniversal.addPublicObjectConstant(TargetClass.newKnown("org.freeciv.types", "universals_n"), "kind");
        handRolledUniversal.addPublicObjectConstant(int.class, "value");
        handRolledUniversal.addConstructorFields();
        handRolledUniversal.addMethod(Method.newPublicReadObjectState(Comment.no(),
                TargetClass.newKnown(String.class), "toString",
                new Block(RETURN(sum(literal("("),
                        handRolledUniversal.getField("kind").ref(),
                        literal(":"),
                        handRolledUniversal.getField("value").ref(),
                        literal(")"))))));
        toStorage.addDependency(handRolledUniversal);

        TargetArray universalArray = TargetArray.from(handRolledUniversal.getAddress().scopeKnown(), 1);
        FieldTypeBasic workListNeedsUniversal = new TerminatedArray("worklist", "struct worklist", universalArray,
                null,
                TerminatedArray.MaxArraySize.NO_LIMIT,
                TerminatedArray.TransferArraySize.SERIALIZED,
                universalArray,
                TerminatedArray.arrayLen,
                TerminatedArray.neverAnythingAfter,
                null,
                new From1<Typed<AValue>, Typed<AValue>>() {
                    @Override
                    public Typed<AValue> x(Typed<AValue> bytes) {
                        return bytes; // TODO: Fix
                    }
                },
                new From2<Block, Var, Var>() {
                    @Override
                    public Block x(Var to, Var elem) {
                        return new Block(
                                to.ref().<Returnable>call("writeByte", elem.ref().callV("kind").callV("getNumber")),
                                to.ref().<Returnable>call("writeByte", elem.read("value"))
                        );
                    }
                },
                new From1<Typed<? extends AValue>, Var>() {
                    @Override
                    public Typed<AValue> x(Var from) {
                        TargetClass universal = TargetClass.newKnown("org.freeciv.types", "universal");
                        return universal.newInstance(
                                new MethodCall<AValue>(
                                        "universals_n.valueOf",
                                        from.ref().<AValue>call("readUnsignedByte")),
                                from.ref().<AValue>call("readUnsignedByte"));
                    }
                },
                TO_STRING_ARRAY,
                Arrays.asList(new Requirement("enum universals_n", DataType.class),
                        new Requirement("struct universal", DataType.class)),
                null,
                NetworkIO.witIntAsIntermediate("uint8", 1, "readUnsignedByte", true, "writeByte"),
                new From1<Typed<AnInt>, Var>() {
                    @Override
                    public Typed<AnInt> x(Var val) {
                        return multiply(literal(2), val.read("length"));
                    }
                },
                TerminatedArray.sameNumberOfBufferElementsAndValueElements,
                Collections.<Method.Helper>emptyList(),
                false
        );
        toStorage.addDependency(workListNeedsUniversal);
    }

    public static Collection<IDependency> values() {
        HashSet<IDependency> out = new HashSet<IDependency>(hardCodedElements);
        out.add(new BitVector());
        return out;
    }

    private static FieldTypeBasic getFloat(final String times) {
        return new FieldTypeBasic("float" + times, "float", TargetClass.fromClass(Float.class),
                new From1<Block, Var>() {
                    @Override
                    public Block x(Var arg1) {
                        return new Block(arg1.assign(pValue.ref()));
                    }
                },
                new From2<Block, Var, Var>() {
                    @Override
                    public Block x(Var out, Var inn) {
                        return new Block(out.assign(divide(inn.ref().<AValue>call("readFloat"), BuiltIn.<AValue>toCode(times))));
                    }
                },
                new From2<Block, Var, Var>() {
                    @Override
                    public Block x(Var value, Var to) {
                        return new Block(to.ref().<Returnable>call("writeFloat", BuiltIn.<AnInt>multiply(value.ref(), BuiltIn.<AnInt>toCode(times))));
                    }
                },
                new From1<Typed<AnInt>, Var>() {
                    @Override
                    public Typed<AnInt> x(Var arg1) {
                        return BuiltIn.<AnInt>toCode("4");
                    }
                },
                TO_STRING_OBJECT,
                                  false, Collections.<Requirement>emptySet());
    }
}
