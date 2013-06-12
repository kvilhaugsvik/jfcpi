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

import com.kvilhaugsvik.dependency.*;
import com.kvilhaugsvik.javaGenerator.typeBridge.Value;
import org.freeciv.packet.fieldtype.ElementsLimit;
import org.freeciv.packetgen.enteties.*;
import org.freeciv.packetgen.enteties.supporting.*;
import com.kvilhaugsvik.javaGenerator.*;
import com.kvilhaugsvik.javaGenerator.Block;
import com.kvilhaugsvik.javaGenerator.Statement;
import com.kvilhaugsvik.javaGenerator.typeBridge.From1;
import com.kvilhaugsvik.javaGenerator.typeBridge.From2;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.ABool;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AValue;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AnInt;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.Returnable;

import java.nio.charset.Charset;
import java.util.*;

import static com.kvilhaugsvik.javaGenerator.util.BuiltIn.*;

//TODO: Move data to file
public class Hardcoded {
    // TODO: Make parameters in stead
    public static final Var<TargetClass> pLimits = Var.param(ElementsLimit.class, "limits");
    public static final Var pValue = Var.param(String.class, "value"); // can't know type
    public static final Var fMaxSize = Var.field(Collections.<Annotate>emptyList(),
            Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO,
            TargetClass.from(ElementsLimit.class), "maxArraySize", null);
    public static final Value<AValue> noLimit = TargetClass.from(ElementsLimit.class).callV("noLimit");

    public static final BitVector deltaBasic;
    public static final FieldType deltaField;
    static {
        deltaBasic = new BitVector("bv_delta_fields");
        try {
            deltaField = ((FieldType) deltaBasic.produce(new Requirement("bit_string(bv_delta_fields)", FieldType.class)))
                    .createFieldType("BV_DELTA_FIELDS");
        } catch (UndefinedException e) {
            throw new RuntimeException(e);
        }
    }
    public static final Constant<AnInt> STRING_ENDER =
            Constant.isInt("STRING_ENDER", IntExpression.integer("0"));
    public static final Constant<AnInt> DIFF_ARRAY_ENDER =
            Constant.isInt("DIFF_ARRAY_ENDER", IntExpression.integer("255"));

    private static final Collection<Dependency.Item> hardCodedElements = Arrays.<Dependency.Item>asList(
            new FieldType("uint32", "int", TargetClass.from(Long.class),
                    new From1<Block, Var>() {
                        @Override
                        public Block x(Var arg1) {
                            return new Block(arg1.assign(pValue.ref()));
                        }
                    },
                    new From2<Block, Var, Var>() {
                        @Override
                        public Block x(Var to, Var from) {
                            final TargetClass integerClass = TargetClass.from(Integer.class);
                            Var removedByCast = Var.local(Modifiable.NO, TargetClass.from(long.class), "removedByCast",
                                    sum(multiply(literal(-1L), integerClass.callV("MIN_VALUE")),
                                            integerClass.callV("MAX_VALUE"), literal(1L)));
                            Var buf = Var.local(int.class, "bufferValue", from.ref().<AnInt>call("readInt"));
                            return new Block(buf, IF(BuiltIn.<ABool>isSmallerThanOrEq(literal(0), buf.ref()),
                                    new Block(to.assign(cast(long.class, buf.ref()))),
                                    new Block(removedByCast,
                                            to.assign(sum(cast(long.class, buf.ref()), removedByCast.ref())))));
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
                    false,
                    Collections.<Requirement>emptySet(),
                    Collections.<Var<AValue>>emptyList(),
                    Collections.<Method>emptyList()
            ),
            getFloat("100"),
            getFloat("10000"),
            getFloat("1000000"),
            TerminatedArray.xBytes("memory", "unsigned char"),

            /************************************************************************************************
             * Built in field type aliases
             ************************************************************************************************/
            deltaField,

            // Workaround: the field data in PACKET_PLAYER_ATTRIBUTE_CHUNK claims to use array diff. Don't believe it.
            TerminatedArray.xBytes("memory", "unsigned char").createFieldType("MEMORY_DIFF"),

            /************************************************************************************************
             * Read from and write to the network
             ************************************************************************************************/
            NetworkIO.simple("uint8", 1, "readUnsignedByte", null, "writeByte"),
            // to.writeByte wraps around so -128 shares encoding with 128
            NetworkIO.simple("sint8", 1, "readByte", int.class, "writeByte"),
            NetworkIO.simple("uint16", 2, "readChar", int.class, "writeChar"),
            NetworkIO.simple("sint16", 2, "readShort", int.class, "writeShort"),
            NetworkIO.simple("sint32", 4, "readInt", null, "writeInt"),

            NetworkIO.simple("bool8", 1, "readBoolean", null, "writeBoolean"),

            /************************************************************************************************
             * Built in types
             ************************************************************************************************/
            (Dependency.Item)(new SimpleTypeAlias("int", Integer.class, 0)),
            (Dependency.Item)(new SimpleTypeAlias("int16", Integer.class, 0)),
            (Dependency.Item)(new SimpleTypeAlias("bool", Boolean.class, 0)),
            (Dependency.Item)(new SimpleTypeAlias("float", Float.class, 0)),
            (Dependency.Item)(new SimpleTypeAlias("double", Double.class, 0)),
            (Dependency.Item)(new SimpleTypeAlias("string", String.class, 1)),

            new BitVector(), // bit string
            deltaBasic,

            (Dependency.Item)(new SimpleTypeAlias("universals_u", Integer.class, 0)), // 4 currently untranslated bytes

            /************************************************************************************************
             * Built in constants
             ************************************************************************************************/
            STRING_ENDER,
            DIFF_ARRAY_ENDER
    );

    public static final SimpleDependencyMaker stringBasicFieldType =
            new SimpleDependencyMaker(new Requirement("string(char)", FieldType.class),
                    new Requirement("STRING_ENDER", Constant.class),
                    new Requirement("FC_DEFAULT_DATA_ENCODING", Constant.class)
            ) {
                @Override
                public Item produce(Requirement toProduce, Item... wasRequired) throws UndefinedException {
                    final Constant encoding = (Constant) wasRequired[1];

                    final TargetClass charsetClass = TargetClass.from(Charset.class);
                    final Var<AValue> charset = Var.field(Collections.<Annotate>emptyList(),
                            Visibility.PRIVATE, Scope.CLASS, Modifiable.NO,
                            charsetClass, "CHARSET", charsetClass.callV("forName", encoding.ref()));

                    return new TerminatedArray("string", "char", TargetClass.from(String.class),
                            (Constant<?>)wasRequired[0],
                            TerminatedArray.MaxArraySize.CONSTRUCTOR_PARAM,
                            TerminatedArray.TransferArraySize.CONSTRUCTOR_PARAM,
                            TerminatedArray.byteArray,
                            new From1<Typed<AnInt>, Var>() {
                                @Override
                                public Typed<AnInt> x(Var value) {
                                    return value.ref().callV("getBytes", charset.ref()).callV("length");
                                }
                            },
                            new From1<Typed<AValue>, Var>() {
                                @Override
                                public Typed<AValue> x(Var everything) {
                                    return everything.ref().<Returnable>call("getBytes", charset.ref());
                                }
                            },
                            new From1<Typed<AValue>, Typed<AValue>>() {
                                @Override
                                public Typed<AValue> x(Typed<AValue> bytes) {
                                    return (TargetClass.from(String.class)).newInstance(bytes, charset.ref());
                                }
                            },
                            TerminatedArray.elemIsByteArray,
                            TerminatedArray.readByte,
                            TO_STRING_OBJECT,
                            Arrays.asList(wasRequired[0].getIFulfillReq(), wasRequired[1].getIFulfillReq()),
                            null,
                            null,
                            new From1<Typed<AnInt>, Var>() {
                                @Override
                                public Typed<AnInt> x(Var value) {
                                    return value.ref().callV("getBytes", charset.ref()).callV("length");
                                }
                            },
                            TerminatedArray.sameNumberOfBufferElementsAndValueElements,
                            Collections.<Method.Helper>emptyList(),
                            false,
                            false,
                            Arrays.<Var<? extends AValue>>asList(charset)
                    );
                }
            };

    private static final Set<Dependency.Maker> hardCodedMakers;
    static {
        HashSet<Dependency.Maker> makers = new HashSet<Dependency.Maker>();

        makers.add(stringBasicFieldType);

        makers.add(new SimpleDependencyMaker(new Requirement("tech_list(int)", FieldType.class),
                new Requirement("A_LAST", Constant.class)){
            @Override
            public Item produce(Requirement toProduce, Item... wasRequired) throws UndefinedException {
                return TerminatedArray.maxSizedTerminated("tech_list", "int", (Constant<?>)wasRequired[0]);
            }
        });

        makers.add(new SimpleDependencyMaker(new Requirement("unit_list(int)", FieldType.class),
                new Requirement("U_LAST", Constant.class)){
            @Override
            public Item produce(Requirement toProduce, Item... wasRequired) throws UndefinedException {
                return TerminatedArray.maxSizedTerminated("unit_list", "int", (Constant<?>)wasRequired[0]);
            }
        });

        makers.add(new SimpleDependencyMaker(new Requirement("building_list(int)", FieldType.class),
                new Requirement("B_LAST", Constant.class)){
            @Override
            public Item produce(Requirement toProduce, Item... wasRequired) throws UndefinedException {
                return TerminatedArray.maxSizedTerminated("building_list", "int", (Constant<?>)wasRequired[0]);
            }
        });

        final Requirement require_universal_field =
                new Requirement("{uint8;uint8}(struct universal)", FieldType.class);
        final Requirement require_universal = new Requirement("struct universal", DataType.class);
        makers.add(new SimpleDependencyMaker(
                new Requirement("worklist(struct worklist)", FieldType.class),
                require_universal_field, require_universal, new Requirement("uint8", NetworkIO.class)
        ) {
            @Override
            public Dependency.Item produce(Requirement toProduce, Dependency.Item... wasRequired) throws UndefinedException {
                final TargetClass universalF = ((ClassWriter) wasRequired[0]).getAddress();
                final TargetClass universal = ((ClassWriter) wasRequired[1]).getAddress();

                TargetArray universalArray = TargetArray.from(universal, 1);
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
                                return new Block(universalF.newInstance(elem.ref(), noLimit).call("encodeTo", to.ref()));
                            }
                        },
                        new From1<Typed<? extends AValue>, Var>() {
                            @Override
                            public Typed<AValue> x(Var from) {
                                return universalF.newInstance(from.ref(), noLimit).callV("getValue");
                            }
                        },
                        TO_STRING_ARRAY,
                        Arrays.asList(require_universal_field,
                                require_universal),
                        null,
                        (NetworkIO)wasRequired[2],
                        new From1<Typed<AnInt>, Var>() {
                            @Override
                            public Typed<AnInt> x(Var val) {
                                return multiply(literal(2), val.ref().callV("length"));
                            }
                        },
                        TerminatedArray.sameNumberOfBufferElementsAndValueElements,
                        Collections.<Method.Helper>emptyList(),
                        false,
                        false,
                        Collections.<Var<? extends AValue>>emptyList()
                );
            }
        });

        makers.add(new Wrapper("requirement(struct requirement)", FieldType.class,
                new Requirement("{{uint8;sint32};uint8;bool8;bool8}(struct requirement)", FieldType.class)));

        hardCodedMakers = Collections.unmodifiableSet(makers);
    }

    public static void applyManualChanges(PacketsStore toStorage) {
        // TODO: autoconvert the enums
        // TODO: when given the location of the tables convert table items as well
        toStorage.addDependency(new StructMaker("universal",
                Arrays.asList(
                        new WeakVarDec(new Requirement("enum universals_n", DataType.class), "kind"),
                        new WeakVarDec(new Requirement("universals_u", DataType.class), "value"))));
    }

    public static Collection<Dependency.Item> values() {
        return Collections.unmodifiableCollection(hardCodedElements);
    }

    public static Collection<Dependency.Maker> makers() {
        return hardCodedMakers;
    }

    public static FieldType getFloat(final String times) {
        return new FieldType("float" + times, "float", TargetClass.from(Float.class),
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
                false,
                Collections.<Requirement>emptySet(),
                Collections.<Var<AValue>>emptyList(),
                Collections.<Method>emptyList()
        );
    }
}
