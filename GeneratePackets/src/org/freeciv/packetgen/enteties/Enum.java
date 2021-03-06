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

package org.freeciv.packetgen.enteties;

import com.kvilhaugsvik.dependency.*;
import com.kvilhaugsvik.javaGenerator.typeBridge.*;
import org.freeciv.packetgen.Hardcoded;
import org.freeciv.packetgen.enteties.supporting.DataType;
import org.freeciv.packetgen.enteties.supporting.IntExpression;
import org.freeciv.packetgen.enteties.supporting.NetworkIO;
import com.kvilhaugsvik.javaGenerator.*;
import com.kvilhaugsvik.javaGenerator.expression.EnumElement;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.*;
import org.freeciv.types.FCEnum;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.kvilhaugsvik.javaGenerator.util.BuiltIn.*;

/**
 * Represents an enumeration type. Can generate a field type for it.
 */
public class Enum extends ClassWriter implements Dependency.Item, Dependency.Maker, DataType {
    private final boolean bitwise;
    private final Collection<Requirement> iRequire;
    private final Pattern fieldTypeBasicForMe;
    private final EnumElementFC invalidDefault;
    private final EnumElementFC countElement;

    protected Enum(String enumName, boolean nameOverride, boolean bitwise,
                   String cntCode, Typed<? extends AString> cntString, Collection<Requirement> reqs,
                List<EnumElementFC> values) {
        super(ClassKind.ENUM, TargetPackage.from(FCEnum.class.getPackage()), Imports.are(), "Freeciv C code", Collections.<Annotate>emptyList(), enumName,
                DEFAULT_PARENT, Arrays.asList(TargetClass.from(FCEnum.class)));

        this.bitwise = bitwise;
        this.iRequire = reqs;
        fieldTypeBasicForMe = Pattern.compile("(\\w+)\\((" + getIFulfillReq().getName() + ")\\)");

        // TODO: NumberOfElements can be different if the specenum skips elements
        int numberOfElements = 0;
        EnumElementFC invalidCandidate = null;
        for (EnumElementFC value : values) {
            if (value.isValid()) {
                numberOfElements++;
                this.addEnumerated(value);
            } else if (value.getEnumValueName().equals("INVALID"))
                invalidCandidate = value;

            if (bitwise)
                if (!(value instanceof EnumElementKnowsNumber))
                    throw new IllegalArgumentException("Only spec enums can be declared bitwise");
                else if (0 < ((EnumElementKnowsNumber)value).getNumber())
                    for (int testAgainst = 1; testAgainst < ((EnumElementKnowsNumber)value)
                            .getNumber() * 2; testAgainst = testAgainst * 2)
                        if (((EnumElementKnowsNumber)value).getNumber() < testAgainst)
                            throw new IllegalArgumentException("Claims to be bitwise but is not.");
        }
        if (null != cntCode) {
            if (bitwise) throw new IllegalArgumentException("");
            this.countElement = EnumElementKnowsNumber.newInvalidEnum(cntCode, cntString, numberOfElements);
            this.addEnumerated(this.countElement);
        } else {
            this.countElement = null;
        }
        if (null == invalidCandidate) // TODO: Should C enums have invalid? If not remove this.
            invalidCandidate = EnumElementKnowsNumber.newInvalidEnum(-1);
        this.invalidDefault = invalidCandidate;
        this.addEnumerated(this.invalidDefault);

        addObjectConstant(int.class, "number");
        addObjectConstant(boolean.class, "valid");
        if (nameOverride)
            addField(Var.field(Collections.<Annotate>emptyList(), Visibility.PRIVATE, Scope.OBJECT, Modifiable.YES,
                    TargetClass.from(String.class), "toStringName", null));
        else
            addObjectConstant(String.class, "toStringName");

        Var<AString> fieldToStringName = this.getField("toStringName");
        Var<AnInt> fieldNumber = this.getField("number");
        Var<ABool> fieldValid = this.getField("valid");

        //TODO: test private constructor generation. perhaps do via Methods.newPrivateConstructor
        Var<AnInt> paramNumber = Var.param(int.class, "number");
        Var<AString> paramToStrName = Var.param(String.class, "toStringName");
        Var<ABool> paramValid = Var.param(boolean.class, "valid");
        addMethod(Method.newConstructor(Comment.no(),
                Visibility.PRIVATE, Arrays.asList(paramNumber, paramToStrName, paramValid),
                new Block(fieldNumber.assign(paramNumber.ref()),
                        fieldToStringName.assign(paramToStrName.ref()),
                        fieldValid.assign(paramValid.ref()))));

        addMethod(Method.newPublicReadObjectState(Comment.no(), TargetClass.from(int.class), "getNumber",
                new Block(BuiltIn.RETURN(fieldNumber.ref()))));
        addMethod(Method.newPublicReadObjectState(Comment.no(), TargetClass.from(boolean.class), "isValid",
                new Block(BuiltIn.RETURN(fieldValid.ref()))));
        addMethod(Method.newPublicReadObjectState(Comment.no(), TargetClass.from(String.class), "toString",
                new Block(BuiltIn.RETURN(fieldToStringName.ref()))));
        if (nameOverride) {
            Var<AString> paramName = Var.param(String.class, "name");
            addMethod(Method.newPublicDynamicMethod(Comment.no(),
                    TargetClass.from(void.class), "setName", Arrays.asList(paramName),
                    Collections.<TargetClass>emptyList(),
                    new Block(fieldToStringName.assign(paramName.ref()))));
        }

        addMethod(Method.newReadClassState(Comment.doc("Is the enum bitwise?",
                "An enum is bitwise if it's number increase by two's exponent.",
                Comment.docReturns("true if the enum is bitwise")),
                TargetClass.from(boolean.class), "isBitWise", new Block(RETURN(BuiltIn.literal(bitwise)))));

        Var element = Var.local(this.getAddress(), "element", null);
        addMethod(Method.custom(Comment.no(),
                Visibility.PUBLIC, Scope.CLASS,
                this.getAddress(), "valueOf", Arrays.asList(paramNumber),
                Collections.<TargetClass>emptyList(),
                new Block(
                        FOR(element, this.getAddress().callV("values"),
                                new Block(IF(isSame(element.ref().callV("getNumber"), paramNumber.ref()),
                                        new Block(RETURN(element.ref()))))),
                        RETURN(getAddress().callV("INVALID")))));

        addMethod(Method.custom(
                Comment.doc("Amount of valid elements", "in the enum. Should equal the count element."),
                Visibility.PUBLIC, Scope.CLASS,
                TargetClass.from(int.class), "countValidElements", Collections.<Var<?>>emptyList(),
                Collections.<TargetClass>emptyList(),
                new Block(RETURN(BuiltIn.literal(numberOfElements)))));
    }

    public static Enum specEnumBitwise(String enumName, boolean nameOverride, boolean bitwise, List<EnumElementFC> values) {
        return new Enum(enumName, nameOverride, bitwise, null, null, Collections.<Requirement>emptySet(), values);
    }

    public static Enum specEnumCountNotNamed(String enumName, boolean nameOverride, String cntCode, List<EnumElementFC> values) {
        return specEnumCountNamed(enumName, nameOverride, cntCode, BuiltIn.literal(cntCode), values);
    }

    public static Enum specEnumCountNamed(String enumName, boolean nameOverride, String cntCode, Typed<? extends AString> cntString, List<EnumElementFC> values) {
        return new Enum(enumName, nameOverride, false, cntCode, cntString, Collections.<Requirement>emptySet(), values);
    }

    public static Enum cEnum(String enumName, Collection<Requirement> reqs, List<EnumElementFC> values) {
        return new Enum(enumName, false, false, null, null, reqs, values);
    }

    public boolean isBitwise() {
        return bitwise;
    }

    public EnumElementFC getInvalidDefault() {
        return invalidDefault;
    }

    public EnumElementFC getCount() {
        return countElement;
    }

    public EnumElementFC getEnumValue(String named) {
        return (EnumElementFC)enums.getElement(named);
    }

    public Collection<Dependency.Item> getEnumConstants() {
        Collection<Dependency.Item> out = new LinkedList<Dependency.Item>();
        for (String valueName : enums.getElementNames()) {
            out.add(Constant.isInt(valueName,
                    IntExpression.readFromOther(this, getAddress().callV(valueName).<AnInt>call("getNumber"))));
        }
        return out;
    }

    @Override
    public Collection<Requirement> getReqs() {
        return iRequire;
    }

    @Override
    public Requirement getIFulfillReq() {
        return new Requirement("enum " + super.getName(), DataType.class);
    }

    @Override
    public Required getICanProduceReq() {
        return new RequiredMulti(FieldType.class, fieldTypeBasicForMe);
    }

    @Override
    public Dependency.Item produce(Requirement toProduce, Dependency.Item... wasRequired) throws UndefinedException {
        final NetworkIO io = (NetworkIO)wasRequired[0];
        final String named = this.getName();
        HashSet<Requirement> req = new HashSet<Requirement>();
        req.add(new Requirement("enum " + named, DataType.class));
        final Enum parent = this;
        return new FieldType(io.getIFulfillReq().getName(), "enum " + named, parent,
                new From1<Block, Var>() {
                    @Override
                    public Block x(Var arg1) {
                        return new Block(arg1.assign(Hardcoded.pValue.ref()));
                    }
                },
                new From3<Block, Var, Var, Var>() {
                    @Override
                    public Block x(Var to, Var from, Var old) {
                        return new Block(to.assign(parent.getAddress().callV("valueOf", io.getRead(from))));
                    }
                },
                new From2<Block, Var, Var>() {
                    @Override
                    public Block x(Var val, Var to) {
                        return new Block(to.ref().<Returnable>call(io.getWrite(), val.ref().<AValue>call("getNumber")));
                    }
                },
                io.getSize(),
                BuiltIn.TO_STRING_OBJECT,
                false,
                req,
                Collections.<Var<AValue>>emptyList(),
                Collections.<Method>emptyList(),
                FieldType.UNSIZED_ZERO
        );
    }

    @Override
    public List<Requirement> neededInput(Requirement toProduce) {
        Matcher search = fieldTypeBasicForMe.matcher(toProduce.getName());
        if (search.matches() && toProduce.getKind().equals(FieldType.class))
            return Arrays.asList(new Requirement(search.group(1), NetworkIO.class));
        else
            throw new IllegalArgumentException("The requirement " + toProduce +
                    " isn't a basic field type using the enum " + getName());
    }

    @Override
    public Value getZeroValue() {
        return getAddress().callV("valueOf", BuiltIn.literal(0));
    }

    /**
     * Represents an enumerated value that knows its own number.
     */
    public static class EnumElementKnowsNumber extends EnumElementFC {
        private final int number;

        EnumElementKnowsNumber(Comment comment, String elementName, int number, Typed<? extends AString> toStringName, boolean valid) {
            super(comment, elementName, IntExpression.integer(number + ""), toStringName, valid);

            if (null == elementName)
                throw new IllegalArgumentException("All elements of enums must have names");

            // Look up numbers in a uniform way
            if (null == toStringName)
                throw new IllegalArgumentException("All elements of enums must have toStringNames");

            this.number = number;
        }

        public int getNumber() {
            return number;
        }

        public static EnumElementKnowsNumber newEnumValue(String enumValueName, int number) {
            return newEnumValue(enumValueName, number, BuiltIn.literal(enumValueName));
        }

        public static EnumElementKnowsNumber newEnumValue(String enumValueName, int number, Typed<? extends AString> toStringName) {
            return newEnumValue(Comment.no(), enumValueName, number, toStringName);
        }

        public static EnumElementKnowsNumber newEnumValue(Comment comment, String enumValueName, int number,
                                                          Typed<? extends AString> toStringName) {
            return new EnumElementKnowsNumber(comment, enumValueName, number, toStringName, true);
        }

        public static EnumElementKnowsNumber newInvalidEnum(int value) {
            return newInvalidEnum("INVALID", BuiltIn.literal("INVALID"), value);
        }

        public static EnumElementKnowsNumber newInvalidEnum(String nameInCode, Typed<? extends AString> toStringName, int value) {
            return new EnumElementKnowsNumber(Comment.no(), nameInCode, value, toStringName, false);
        }
    }

    /**
     * Represents an enumerated value.
     */
    public static class EnumElementFC extends EnumElement {
        private final IntExpression valueGen;
        private final Typed<? extends AString> toStringName;
        private final boolean valid;

        protected EnumElementFC(Comment comment, String elementName, IntExpression valueGen, Typed<? extends AString> toStringName, boolean valid) {
            super(comment, elementName, parList(valueGen, toStringName, valid));

            // Look up numbers in a uniform way
            if (null == toStringName)
                throw new IllegalArgumentException("All elements of enums must have toStringNames");

            this.valueGen = valueGen;
            this.toStringName = toStringName;
            this.valid = valid;
        }

        private static Typed<? extends AValue>[] parList(IntExpression valueGen, Typed<? extends AString> toStringName, boolean valid) {
            Typed<? extends AValue>[] out;
            out = new Typed[3];
            out[0] = valueGen;
            out[1] = toStringName;
            out[2] = valid ? BuiltIn.TRUE : BuiltIn.FALSE;
            return out;
        }

        public String getValueGenerator() {
            return valueGen.toString();
        }

        /* Only for UI/test use */
        public String getToStringName() {
            return toStringName.toString();
        }

        public boolean isValid() {
            return valid;
        }

        public static EnumElementFC newEnumValue(String enumValueName, IntExpression number) {
            return newEnumValue(Comment.no(), enumValueName, number);
        }

        public static EnumElementFC newEnumValue(Comment comment, String enumValueName, IntExpression number) {
            return newEnumValue(comment, enumValueName, number, BuiltIn.literal(enumValueName));
        }

        public static EnumElementFC newEnumValue(Comment comment, String enumValueName, IntExpression number, Typed<? extends AString> toStringName) {
            return new EnumElementFC(comment, enumValueName, number, toStringName, true);
        }

        /**
         * Create the enum element INVALID and give it the value of the expression.
         * @param intExpr the int extression that represents the invalid value.
         * @return an enum element called INVALID with the specified value.
         */
        public static EnumElementFC newInvalidEnumValue(IntExpression intExpr) {
            return new EnumElementFC(Comment.no(), "INVALID", intExpr, BuiltIn.literal("INVALID"), false);
        }
    }
}
