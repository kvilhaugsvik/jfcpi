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
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;
import org.freeciv.types.FCEnum;

import java.util.*;

public class Enum extends ClassWriter implements IDependency, FieldTypeBasic.Generator {
    private final boolean bitwise;
    private final Collection<Requirement> iRequire;
    private final EnumElementFC invalidDefault;
    private final EnumElementFC countElement;

    public Enum(String enumName, boolean bitwise, List<EnumElementFC> values) {
        this(enumName, bitwise, null, null, Collections.<Requirement>emptySet(), values);
    }

    public Enum(String enumName, String cntCode, List<EnumElementFC> values) {
        this(enumName, cntCode, null, values);
    }

    public Enum(String enumName, String cntCode, String cntString, List<EnumElementFC> values) {
        this(enumName, false, cntCode, cntString, Collections.<Requirement>emptySet(), values);
    }

    public Enum(String enumName, Collection<Requirement> reqs, List<EnumElementFC> values) {
        this(enumName, false, null, null, reqs, values);
    }

    protected Enum(String enumName, boolean bitwise, String cntCode, String cntString, Collection<Requirement> reqs,
                List<EnumElementFC> values) {
        super(ClassKind.ENUM, new TargetPackage(FCEnum.class.getPackage()), null, "Freeciv C code", enumName, null,
              "FCEnum");

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
        addObjectConstant("String", "toStringName");

        //TODO: test private constructor generation. perhaps do via Methods.newPrivateConstructor
        addMethod(null, Visibility.PRIVATE, Scope.OBJECT, null, enumName, "int number, String toStringName", null,
                  "this(number, toStringName, true);");
        addMethod(null, Visibility.PRIVATE, Scope.OBJECT, null, enumName,
                  "int number, String toStringName, boolean valid",
                  null,
                  new Block(setFieldToVariableSameName("number"),
                            setFieldToVariableSameName("toStringName"),
                            setFieldToVariableSameName("valid")));

        addMethodPublicReadObjectState(null, "int", "getNumber",
                new Block(BuiltIn.RETURN(this.getField("number").ref())));
        addMethodPublicReadObjectState(null, "boolean", "isValid",
                new Block(BuiltIn.RETURN(this.getField("valid").ref())));
        addMethodPublicReadObjectState(null, "String", "toString",
                new Block(BuiltIn.RETURN(this.getField("toStringName").ref())));

        addMethodReadClassState("/**" + "\n" +
                                        " * Is the enum bitwise? An enum is bitwise if it's number increase by two's"
                                        + "\n" +
                                        " * exponent." + "\n" +
                                        " * @return true if the enum is bitwise" + "\n" +
                                        " */",
                                "boolean", "isBitWise", "return " + bitwise + ";");

        addMethod("", Visibility.PUBLIC, Scope.CLASS, this.getName(), "valueOf", "int number", null,
                  "for (" + this.getName() + " element: values()) {",
                  "if (element.getNumber() == number) {",
                  "return element;",
                  "}",
                  "}",
                  "return INVALID;");
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
        return (EnumElementFC)enums.get(named);
    }

    public Collection<IDependency> getEnumConstants() {
        Collection<IDependency> out = new LinkedList<IDependency>();
        for (String valueName : enums.keySet()) {
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
    public FieldTypeBasic getBasicFieldTypeOnInput(NetworkIO io) {
        String named = this.getName();
        HashSet<Requirement> req = new HashSet<Requirement>();
        req.add(new Requirement("enum " + named, Requirement.Kind.AS_JAVA_DATATYPE));
        return new FieldTypeBasic(io.getIFulfillReq().getName(), "enum " + named,
                                  named,
                                  "value = " + named + ".valueOf(" + io.getRead() + ");",
                                  io.getWrite("value.getNumber()"),
                                  io.getSize(),
                                  false, req);
    }

    @Override
    public Requirement.Kind needsDataInFormat() {
        return Requirement.Kind.FROM_NETWORK_TO_INT;
    }

    public static Enum fromArray(String enumName, boolean bitwise, EnumElementFC... values) {
        return new Enum(enumName, bitwise, Arrays.asList(values));
    }

    public static Enum fromArray(String enumName, String cntCode, EnumElementFC... values) {
        return new Enum(enumName, cntCode, Arrays.asList(values));
    }

    public static Enum fromArray(String enumName, String cntCode, String cntString, EnumElementFC... values) {
        return new Enum(enumName, cntCode, cntString, Arrays.asList(values));
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
            super(comment, elementName, parList(valueGen, toStringName, valid));

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
