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

import com.kvilhaugsvik.javaGenerator.typeBridge.Value;
import org.freeciv.packet.fieldtype.ElementsLimit;
import org.freeciv.packetgen.dependency.Dependency;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.dependency.SimpleDependencyMaker;
import org.freeciv.packetgen.enteties.*;
import org.freeciv.packetgen.enteties.supporting.*;
import com.kvilhaugsvik.javaGenerator.*;
import com.kvilhaugsvik.javaGenerator.Block;
import com.kvilhaugsvik.javaGenerator.Statement;
import com.kvilhaugsvik.javaGenerator.expression.MethodCall;
import com.kvilhaugsvik.javaGenerator.typeBridge.From1;
import com.kvilhaugsvik.javaGenerator.typeBridge.From2;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.ABool;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AValue;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AnInt;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.Returnable;

import java.util.*;

import static com.kvilhaugsvik.javaGenerator.util.BuiltIn.*;

//TODO: Move data to file
public class Hardcoded {
    // TODO: Make parameters in stead
    public static final Var<TargetClass> pLimits = Var.param(ElementsLimit.class, "limits");
    private static final Var pValue = Var.param(String.class, "value"); // can't know type
    public static final Var fMaxSize = Var.field(Collections.<Annotate>emptyList(),
            Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO,
            TargetClass.fromClass(ElementsLimit.class), "maxArraySize", null);
    private static final Value<AValue> noLimit = TargetClass.fromClass(ElementsLimit.class).callV("noLimit");

    public static final BitVector deltaBasic;
    public static final FieldTypeBasic.FieldTypeAlias deltaField;
    static {
        deltaBasic = new BitVector("bv_delta_fields");
        try {
            deltaField = ((FieldTypeBasic) deltaBasic.produce(new Requirement("bit_string(bv_delta_fields)", FieldTypeBasic.class)))
                    .createFieldType("BV_DELTA_FIELDS");
        } catch (UndefinedException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Collection<Dependency.Item> hardCodedElements = Arrays.<Dependency.Item>asList(
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
             * Built in field type aliases
             ************************************************************************************************/
            deltaField,

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
            (Dependency.Item)(new SimpleTypeAlias("int", Integer.class, null)),
            (Dependency.Item)(new SimpleTypeAlias("bool", Boolean.class, null)),
            (Dependency.Item)(new SimpleTypeAlias("float", Float.class, null)),
            (Dependency.Item)(new SimpleTypeAlias("double", Double.class, null)),
            (Dependency.Item)(new SimpleTypeAlias("string", String.class, null)),
            deltaBasic,

            /************************************************************************************************
             * Built in constants
             ************************************************************************************************/
            Constant.isInt("STRING_ENDER", IntExpression.integer("0"))
    );

    private static final Set<Dependency.Maker> hardCodedMakers;
    static {
        final Requirement require_universals_n =
                new Requirement("uint8(enum universals_n)", FieldTypeBasic.FieldTypeAlias.class);
        final Requirement require_universal = new Requirement("struct universal", DataType.class);

        HashSet<Dependency.Maker> makers = new HashSet<Dependency.Maker>();

        makers.add(new SimpleDependencyMaker(
                new Requirement("worklist(struct worklist)", FieldTypeBasic.class),
                require_universals_n, require_universal
        ) {
            @Override
            public Dependency.Item produce(Requirement toProduce, Dependency.Item... wasRequired) throws UndefinedException {
                final TargetClass universals_n = ((ClassWriter) wasRequired[0]).getAddress();
                final TargetClass universal = ((ClassWriter) wasRequired[1]).getAddress();

                TargetArray universalArray = TargetArray.from(universal.scopeKnown(), 1);
                return new TerminatedArray("worklist", "struct worklist", universalArray,
                        null,
                        TerminatedArray.MaxArraySize.NO_LIMIT,
                        TerminatedArray.TransferArraySize.SERIALIZED,
                        universalArray,
                        TerminatedArray.arrayLen,
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
                                        universals_n.newInstance(elem.ref().callV("getkind"), noLimit).call("encodeTo", to.ref()),
                                        to.ref().<Returnable>call("writeByte", elem.ref().callV("getvalue"))
                                );
                            }
                        },
                        new From1<Typed<? extends AValue>, Var>() {
                            @Override
                            public Typed<AValue> x(Var from) {
                                return universal.newInstance(
                                        universals_n.newInstance(from.ref(), noLimit).callV("getValue"),
                                        from.ref().<AValue>call("readUnsignedByte"));
                            }
                        },
                        TO_STRING_ARRAY,
                        Arrays.asList(require_universals_n,
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
            }
        });

        final Requirement requirementReq = new Requirement("struct requirement", DataType.class);
        makers.add(new SimpleDependencyMaker(
                new Requirement("requirement(struct requirement)", FieldTypeBasic.class),
                require_universals_n, requirementReq, require_universal
        ) {
            @Override
            public Dependency.Item produce(Requirement toProduce, Dependency.Item... wasRequired) throws UndefinedException {
                final TargetClass universals_n = ((ClassWriter) wasRequired[0]).getAddress();
                final TargetClass requirementDataType = ((ClassWriter) wasRequired[1]).getAddress();
                final TargetClass universal = ((ClassWriter) wasRequired[2]).getAddress();

                return new FieldTypeBasic("requirement", "struct requirement", requirementDataType,
                        new From1<Block, Var>() {
                            @Override
                            public Block x(Var arg1) {
                                return new Block(arg1.assign(pValue.ref()));
                            }
                        },
                        new From2<Block, Var, Var>() {
                            @Override
                            public Block x(Var to, Var from) {
                                return new Block(to.assign(requirementDataType.newInstance(
                                        universal.newInstance(
                                                universals_n.newInstance(from.ref(), noLimit).callV("getValue"),
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
                                        "to.writeByte(this.value.getsource().getkind().getNumber())",
                                        "to.writeInt(this.value.getsource().getvalue())",
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
                                require_universals_n,
                                new Requirement("struct universal", DataType.class))
                );
            }
        });

        hardCodedMakers = Collections.unmodifiableSet(makers);
    }

    public static void applyManualChanges(PacketsStore toStorage) {
        // TODO: autoconvert the enums
        // TODO: when given the location of the tables convert table items as well
        toStorage.addDependency(new Struct("universal",
                Arrays.asList(
                        new WeakVarDec(new Requirement("enum universals_n", DataType.class), "org.freeciv.types", "universals_n", "kind", 0),
                        new WeakVarDec(TargetPackage.TOP_LEVEL_AS_STRING, "int", "value", 0))));
    }

    public static Collection<Dependency.Item> values() {
        HashSet<Dependency.Item> out = new HashSet<Dependency.Item>(hardCodedElements);
        out.add(new BitVector());
        return out;
    }

    public static Collection<Dependency.Maker> makers() {
        return hardCodedMakers;
    }

    public static FieldTypeBasic getFloat(final String times) {
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
