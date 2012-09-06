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

import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.supporting.IntExpression;
import org.freeciv.packetgen.enteties.supporting.NetworkIO;
import org.freeciv.packetgen.javaGenerator.*;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.creators.ExprFrom1;
import org.freeciv.packetgen.javaGenerator.expression.creators.ExprFrom2;
import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.Returnable;
import org.freeciv.types.FCEnum;

import java.util.*;

import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.*;

public class Enum extends ClassWriter implements IDependency, FieldTypeBasic.Generator {
    private final boolean bitwise;
    private final Collection<Requirement> iRequire;
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
        super(ClassKind.ENUM, new TargetPackage(FCEnum.class.getPackage()), null, "Freeciv C code", Collections.<Annotate>emptyList(), enumName, null, "FCEnum");

        this.bitwise = bitwise;
        this.iRequire = reqs;

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

        addObjectConstant("int", "number");
        addObjectConstant("boolean", "valid");
        if (nameOverride)
            addField(Var.field(Collections.<Annotate>emptyList(), Visibility.PRIVATE, Scope.OBJECT, Modifiable.YES,
                    new TargetClass("String"), "toStringName", null));
        else
            addObjectConstant("String", "toStringName");

        //TODO: test private constructor generation. perhaps do via Methods.newPrivateConstructor
        addMethod(new Method("", Visibility.PRIVATE, Scope.OBJECT,
                TargetClass.fromName(null), enumName, "int number, String toStringName",
                null, new Block(new MethodCall<Returnable>("this", "number", "toStringName", "true"))));
        addMethod(new Method("", Visibility.PRIVATE, Scope.OBJECT,
                TargetClass.fromName(null), enumName, "int number, String toStringName, boolean valid",
                null,
                new Block(setFieldToVariableSameName("number"),
                        setFieldToVariableSameName("toStringName"),
                        setFieldToVariableSameName("valid"))));

        addMethod(Method.newPublicReadObjectState("", TargetClass.fromName("int"), "getNumber",
                new Block(BuiltIn.RETURN(this.getField("number").ref()))));
        addMethod(Method.newPublicReadObjectState("", TargetClass.fromName("boolean"), "isValid",
                new Block(BuiltIn.RETURN(this.getField("valid").ref()))));
        addMethod(Method.newPublicReadObjectState("", TargetClass.fromName("String"), "toString",
                new Block(BuiltIn.RETURN(this.getField("toStringName").ref()))));
        if (nameOverride)
            addMethod(Method.newPublicDynamicMethod("", new TargetClass(void.class), "setName", "String name", null,
                    new Block(this.getField("toStringName").assign(asAValue("name")))));

        addMethod(Method.newReadClassState("/**" + "\n" +
                                        " * Is the enum bitwise? An enum is bitwise if it's number increase by two's"
                                        + "\n" +
                                        " * exponent." + "\n" +
                                        " * @return true if the enum is bitwise" + "\n" +
                                        " */",
                TargetClass.fromName("boolean"), "isBitWise", new Block(RETURN(asBool(bitwise + "")))));

        Var element = Var.local(this.getName(), "element", null);
        addMethod(new Method("", Visibility.PUBLIC, Scope.CLASS,
                TargetClass.fromName(this.getName()), "valueOf", "int number",
                null, new Block(
                FOR(element, new MethodCall<AValue>("values", new Typed[0]),
                        new Block(IF(asBool("element.getNumber() == number"), new Block(RETURN(element.ref()))))),
                RETURN(asAValue("INVALID")))));
    }

    public void addEnumerated(String comment,
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
            out.add(new Constant(valueName, IntExpression.readFromOther(this,
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
        return new Requirement("enum " + super.getName(), Requirement.Kind.AS_JAVA_DATATYPE);
    }

    @Override
    public FieldTypeBasic getBasicFieldTypeOnInput(final NetworkIO io) {
        final String named = this.getName();
        HashSet<Requirement> req = new HashSet<Requirement>();
        req.add(new Requirement("enum " + named, Requirement.Kind.AS_JAVA_DATATYPE));
        return new FieldTypeBasic(io.getIFulfillReq().getName(), "enum " + named, new TargetClass(named),
                new ExprFrom1<Block, Var>() {
                    @Override
                    public Block x(Var arg1) {
                        return new Block(arg1.assign(asAValue("value")));
                    }
                },
                new ExprFrom2<Block, Var, Var>() {
                    @Override
                    public Block x(Var to, Var from) {
                        return new Block(to.assign(new MethodCall<AValue>(named + ".valueOf", io.getRead())));
                    }
                },
                new ExprFrom2<Block, Var, Var>() {
                    @Override
                    public Block x(Var val, Var to) {
                        return Block.fromStrings(io.getWrite("this.value.getNumber()"));
                    }
                },
                                  io.getSize(),
                BuiltIn.TO_STRING_OBJECT,
                                  false, req);
    }

    @Override
    public Requirement.Kind needsDataInFormat() {
        return Requirement.Kind.FROM_NETWORK_TO_INT;
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

        EnumElementKnowsNumber(String comment, String elementName, int number, String toStringName, boolean valid) {
            super(comment, elementName, number + "", toStringName, valid);

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
            return newEnumValue(null, enumValueName, number, toStringName);
        }

        public static EnumElementKnowsNumber newEnumValue(String comment, String enumValueName, int number,
                                                   String toStringName) {
            return new EnumElementKnowsNumber(comment, enumValueName, number, toStringName, true);
        }

        public static EnumElementKnowsNumber newInvalidEnum(int value) {
            return newInvalidEnum("INVALID", "\"INVALID\"", value);
        }

        public static EnumElementKnowsNumber newInvalidEnum(String nameInCode, String toStringName, int value) {
            return new EnumElementKnowsNumber(null, nameInCode, value, toStringName, false);
        }
    }

    public static class EnumElementFC extends EnumElement {
        private final String valueGen;
        private final String toStringName;
        private final boolean valid;

        protected EnumElementFC(String comment, String elementName, String valueGen, String toStringName, boolean valid) {
            super(Comment.oldCompat(comment), elementName, parList(valueGen, toStringName, valid));

            // Look up numbers in a uniform way
            if (null == toStringName)
                throw new IllegalArgumentException("All elements of enums must have toStringNames");

            this.valueGen = valueGen;
            this.toStringName = toStringName;
            this.valid = valid;
        }

        private static String[] parList(String valueGen, String toStringName, boolean valid) {
            String[] out;
            if (valid) {
                out = new String[2];
            } else {
                out = new String[3];
                out[2] = valid + "";
            }
            out[0] = valueGen;
            out[1] = toStringName;
            return out;
        }

        public String getValueGenerator() {
            return valueGen;
        }

        public String getToStringName() {
            return toStringName;
        }

        public boolean isValid() {
            return valid;
        }

        public static EnumElementFC newEnumValue(String enumValueName, String number) {
            return newEnumValue(null, enumValueName, number);
        }

        public static EnumElementFC newEnumValue(String comment, String enumValueName, String number) {
            return newEnumValue(comment, enumValueName, number, "\"" + enumValueName + "\"");
        }

        public static EnumElementFC newEnumValue(String comment, String enumValueName, String number, String toStringName) {
            return new EnumElementFC(comment, enumValueName, number, toStringName, true);
        }
    }
}
