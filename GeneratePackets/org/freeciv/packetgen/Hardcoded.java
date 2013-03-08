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
import org.freeciv.packetgen.dependency.Wrapper;
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

import java.util.*;

import static com.kvilhaugsvik.javaGenerator.util.BuiltIn.*;

//TODO: Move data to file
public class Hardcoded {
    // TODO: Make parameters in stead
    public static final Var<TargetClass> pLimits = Var.param(ElementsLimit.class, "limits");
    public static final Var pValue = Var.param(String.class, "value"); // can't know type
    public static final Var fMaxSize = Var.field(Collections.<Annotate>emptyList(),
            Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO,
            TargetClass.fromClass(ElementsLimit.class), "maxArraySize", null);
    public static final Value<AValue> noLimit = TargetClass.fromClass(ElementsLimit.class).callV("noLimit");

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

            /************************************************************************************************
             * Built in field type aliases
             ************************************************************************************************/
            deltaField,

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
            Constant.isInt("STRING_ENDER", IntExpression.integer("0"))
    );

    private static final Set<Dependency.Maker> hardCodedMakers;
    static {
        final Requirement require_universal_field =
                new Requirement("{uint8;uint8}(struct universal)", FieldTypeBasic.FieldTypeAlias.class);
        final Requirement require_universal = new Requirement("struct universal", DataType.class);

        HashSet<Dependency.Maker> makers = new HashSet<Dependency.Maker>();

        makers.add(new SimpleDependencyMaker(
                new Requirement("worklist(struct worklist)", FieldTypeBasic.class),
                require_universal_field, require_universal, new Requirement("uint8", NetworkIO.class)
        ) {
            @Override
            public Dependency.Item produce(Requirement toProduce, Dependency.Item... wasRequired) throws UndefinedException {
                final TargetClass universalF = ((ClassWriter) wasRequired[0]).getAddress();
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
                        false
                );
            }
        });

        makers.add(new Wrapper("requirement(struct requirement)", FieldTypeBasic.class,
                new Requirement("{{uint8;sint32};uint8;bool8;bool8}(struct requirement)", FieldTypeBasic.class)));

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
