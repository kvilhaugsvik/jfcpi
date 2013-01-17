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

import org.freeciv.packetgen.UndefinedException;
import org.freeciv.packetgen.dependency.*;
import org.freeciv.packetgen.enteties.supporting.DataType;
import org.freeciv.packetgen.enteties.supporting.IntExpression;
import org.freeciv.packetgen.enteties.supporting.NetworkIO;
import org.freeciv.packetgen.javaGenerator.*;
import org.freeciv.packetgen.javaGenerator.Block;
import org.freeciv.packetgen.javaGenerator.expression.EnumElement;
import org.freeciv.packetgen.javaGenerator.expression.MethodCall;
import org.freeciv.packetgen.javaGenerator.typeBridge.From1;
import org.freeciv.packetgen.javaGenerator.typeBridge.From2;
import org.freeciv.packetgen.javaGenerator.typeBridge.Typed;
import org.freeciv.packetgen.javaGenerator.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.typeBridge.willReturn.*;
import org.freeciv.types.FCEnum;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.freeciv.packetgen.javaGenerator.util.BuiltIn.*;

public class Enum extends ClassWriter implements IDependency, IDependency.Maker {
    private final boolean bitwise;
    private final Collection<Requirement> iRequire;
    private final Pattern fieldTypeBasicForMe;
    private final EnumElementFC invalidDefault;
    private final EnumElementFC countElement;

    public Enum(String enumName, boolean nameOverride, boolean bitwise, List<EnumElementFC> values) {
        this(enumName, nameOverride, bitwise, null, null, Collections.<Requirement>emptySet(), values);
    }

    public Enum(String enumName, boolean nameOverride, String cntCode, List<EnumElementFC> values) {
        this(enumName, nameOverride, cntCode, null, values);
    }

    public Enum(String enumName, boolean nameOverride, String cntCode, String cntString, List<EnumElementFC> values) {
        this(enumName, nameOverride, false, cntCode, cntString, Collections.<Requirement>emptySet(), values);
    }

    public Enum(String enumName, Collection<Requirement> reqs, List<EnumElementFC> values) {
        this(enumName, false, false, null, null, reqs, values);
    }

    protected Enum(String enumName, boolean nameOverride, boolean bitwise,
                   String cntCode, String cntString, Collection<Requirement> reqs,
                List<EnumElementFC> values) {
        super(ClassKind.ENUM, TargetPackage.from(FCEnum.class.getPackage()), Imports.are(), "Freeciv C code", Collections.<Annotate>emptyList(), enumName,
                DEFAULT_PARENT, Arrays.asList(TargetClass.newKnown(FCEnum.class)));

        this.bitwise = bitwise;
        this.iRequire = reqs;
        fieldTypeBasicForMe = Pattern.compile("(\\w+)\\((" + getIFulfillReq().getName() + ")\\)");

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
            this.countElement = EnumElementKnowsNumber.newInvalidEnum(cntCode,
                                                                      (null == cntString ? '"' + cntCode + '"' :
                                                                              cntString),
                                                                      numberOfElements);
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
                    TargetClass.fromClass(String.class), "toStringName", null));
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
                Visibility.PRIVATE, Arrays.asList(paramNumber, paramToStrName),
                new Block(new MethodCall<Returnable>("this", paramNumber.ref(), paramToStrName.ref(), TRUE))));
        addMethod(Method.newConstructor(Comment.no(),
                Visibility.PRIVATE, Arrays.asList(paramNumber, paramToStrName, paramValid),
                new Block(fieldNumber.assign(paramNumber.ref()),
                        fieldToStringName.assign(paramToStrName.ref()),
                        fieldValid.assign(paramValid.ref()))));

        addMethod(Method.newPublicReadObjectState(Comment.no(), TargetClass.fromClass(int.class), "getNumber",
                new Block(BuiltIn.RETURN(fieldNumber.ref()))));
        addMethod(Method.newPublicReadObjectState(Comment.no(), TargetClass.fromClass(boolean.class), "isValid",
                new Block(BuiltIn.RETURN(fieldValid.ref()))));
        addMethod(Method.newPublicReadObjectState(Comment.no(), TargetClass.newKnown(String.class), "toString",
                new Block(BuiltIn.RETURN(fieldToStringName.ref()))));
        if (nameOverride) {
            Var<AString> paramName = Var.param(String.class, "name");
            addMethod(Method.newPublicDynamicMethod(Comment.no(),
                    TargetClass.fromClass(void.class), "setName", Arrays.asList(paramName),
                    Collections.<TargetClass>emptyList(),
                    new Block(fieldToStringName.assign(paramName.ref()))));
        }

        addMethod(Method.newReadClassState(Comment.doc("Is the enum bitwise?",
                "An enum is bitwise if it's number increase by two's exponent.",
                Comment.docReturns("true if the enum is bitwise")),
                TargetClass.fromClass(boolean.class), "isBitWise", new Block(RETURN(BuiltIn.<ABool>toCode(bitwise + "")))));

        Var element = Var.local(this.getAddress().scopeKnown(), "element", null);
        addMethod(Method.custom(Comment.no(),
                Visibility.PUBLIC, Scope.CLASS,
                this.getAddress().scopeKnown(), "valueOf", Arrays.asList(paramNumber),
                Collections.<TargetClass>emptyList(),
                new Block(
                        FOR(element, new MethodCall<AValue>("values", new Typed[0]),
                                new Block(IF(BuiltIn.<ABool>toCode("element.getNumber() == number"), new Block(RETURN(element.ref()))))),
                        RETURN(BuiltIn.<AValue>toCode("INVALID")))));
    }

    public void addEnumerated(Comment comment,
                              String enumName,
                              int number,
                              String toStringName) {
        addEnumerated(EnumElementKnowsNumber.newEnumValue(comment, enumName, number, toStringName));
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

    public Collection<IDependency> getEnumConstants() {
        Collection<IDependency> out = new LinkedList<IDependency>();
        for (String valueName : enums.getElementNames()) {
            out.add(Constant.isInt(valueName, IntExpression.readFromOther(this,
                                                                        this.getPackage() + "." + this
                                                                                .getName() + "." + valueName + "" +
                                                                                ".getNumber()")));
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
        return new RequiredMulti(FieldTypeBasic.class, fieldTypeBasicForMe);
    }

    @Override
    public IDependency produce(Requirement toProduce, IDependency... wasRequired) throws UndefinedException {
        final NetworkIO io = (NetworkIO)wasRequired[0];
        final String named = this.getName();
        HashSet<Requirement> req = new HashSet<Requirement>();
        req.add(new Requirement("enum " + named, DataType.class));
        final TargetClass parent = getAddress().scopeKnown();
        return new FieldTypeBasic(io.getIFulfillReq().getName(), "enum " + named, parent,
                new From1<Block, Var>() {
                    @Override
                    public Block x(Var arg1) {
                        return new Block(arg1.assign(BuiltIn.<AValue>toCode("value")));
                    }
                },
                new From2<Block, Var, Var>() {
                    @Override
                    public Block x(Var to, Var from) {
                        return new Block(to.assign(new MethodCall<AValue>(named + ".valueOf", io.getRead().x(from))));
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
                                  false, req);
    }

    @Override
    public List<Requirement> neededInput(Requirement toProduce) {
        Matcher search = fieldTypeBasicForMe.matcher(toProduce.getName());
        if (search.matches() && toProduce.getKind().equals(FieldTypeBasic.class))
            return Arrays.asList(new Requirement(search.group(1), NetworkIO.class));
        else
            throw new IllegalArgumentException("The requirement " + toProduce +
                    " isn't a basic field type using the enum " + getName());
    }

    @Deprecated
    public static Enum fromArray(String enumName, boolean bitwise, EnumElementFC... values) {
        return new Enum(enumName, false, bitwise, Arrays.asList(values));
    }

    @Deprecated
    public static Enum fromArray(String enumName, String cntCode, EnumElementFC... values) {
        return new Enum(enumName, false, cntCode, Arrays.asList(values));
    }

    @Deprecated
    public static Enum fromArray(String enumName, String cntCode, String cntString, EnumElementFC... values) {
        return new Enum(enumName, false, cntCode, cntString, Arrays.asList(values));
    }

    public static Enum fromArray(String enumName, Collection<Requirement> reqs, EnumElementFC... values) {
        return new Enum(enumName, reqs, Arrays.asList(values));
    }

    public static class EnumElementKnowsNumber extends EnumElementFC {
        private final int number;

        EnumElementKnowsNumber(Comment comment, String elementName, int number, String toStringName, boolean valid) {
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
            return newEnumValue(enumValueName, number, '"' + enumValueName + '"');
        }

        public static EnumElementKnowsNumber newEnumValue(String enumValueName, int number, String toStringName) {
            return newEnumValue(Comment.no(), enumValueName, number, toStringName);
        }

        public static EnumElementKnowsNumber newEnumValue(Comment comment, String enumValueName, int number,
                                                   String toStringName) {
            return new EnumElementKnowsNumber(comment, enumValueName, number, toStringName, true);
        }

        public static EnumElementKnowsNumber newInvalidEnum(int value) {
            return newInvalidEnum("INVALID", "\"INVALID\"", value);
        }

        public static EnumElementKnowsNumber newInvalidEnum(String nameInCode, String toStringName, int value) {
            return new EnumElementKnowsNumber(Comment.no(), nameInCode, value, toStringName, false);
        }
    }

    public static class EnumElementFC extends EnumElement {
        private final IntExpression valueGen;
        private final String toStringName;
        private final boolean valid;

        protected EnumElementFC(Comment comment, String elementName, IntExpression valueGen, String toStringName, boolean valid) {
            super(comment, elementName, parList(valueGen, toStringName, valid));

            // Look up numbers in a uniform way
            if (null == toStringName)
                throw new IllegalArgumentException("All elements of enums must have toStringNames");

            this.valueGen = valueGen;
            this.toStringName = toStringName;
            this.valid = valid;
        }

        private static Typed<? extends AValue>[] parList(IntExpression valueGen, String toStringName, boolean valid) {
            Typed<? extends AValue>[] out;
            if (valid) {
                out = new Typed[2];
            } else {
                out = new Typed[3];
                out[2] = valid ? BuiltIn.TRUE : BuiltIn.FALSE;
            }
            out[0] = BuiltIn.<AnInt>toCode(valueGen.toString());
            out[1] = BuiltIn.<AString>toCode(toStringName);
            return out;
        }

        public String getValueGenerator() {
            return valueGen.toString();
        }

        public String getToStringName() {
            return toStringName;
        }

        public boolean isValid() {
            return valid;
        }

        public static EnumElementFC newEnumValue(String enumValueName, IntExpression number) {
            return newEnumValue(Comment.no(), enumValueName, number);
        }

        public static EnumElementFC newEnumValue(Comment comment, String enumValueName, IntExpression number) {
            return newEnumValue(comment, enumValueName, number, "\"" + enumValueName + "\"");
        }

        public static EnumElementFC newEnumValue(Comment comment, String enumValueName, IntExpression number, String toStringName) {
            return new EnumElementFC(comment, enumValueName, number, toStringName, true);
        }
    }
}
