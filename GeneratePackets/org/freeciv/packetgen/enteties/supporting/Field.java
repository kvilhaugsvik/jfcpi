/*
 * Copyright (c) 2011. Sveinung Kvilhaugsvik
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

package org.freeciv.packetgen.enteties.supporting;

import org.freeciv.Util;
import org.freeciv.packet.fieldtype.*;
import org.freeciv.packetgen.UndefinedException;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.FieldTypeBasic;
import org.freeciv.packetgen.javaGenerator.*;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.ABool;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AnInt;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.NoValue;

import java.util.*;

import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.*;

public class Field<Kind extends AValue> extends Var<Kind> {
    private final String onPacket;
    private final FieldTypeBasic.FieldTypeAlias type;
    private final ArrayDeclaration[] declarations;

    public Field(String fieldName, FieldTypeBasic.FieldTypeAlias typeAlias, String onPacket, List<WeakFlag> flags,
                 WeakField.ArrayDeclaration... declarations) {
        super(fieldFlagsToAnnotations(flags), Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO,
                new TargetClass(typeAlias.getName() + getArrayDeclaration(typeAlias, declarations.length)),
                fieldName, null);

        if (typeAlias.getBasicType().isArrayEater() && (0 == declarations.length))
            throw new IllegalArgumentException("Array eaters needs array declarations");

        this.type = typeAlias;
        this.onPacket = onPacket;

        this.declarations = decWeakToStrong(declarations, onPacket, fieldName);
    }

    private static List<Annotate> fieldFlagsToAnnotations(List<WeakFlag> flags) {
        LinkedList<Annotate> annotations = new LinkedList<Annotate>();

        for (WeakFlag flag : flags)
            if ("key".equals(flag.getName()))
                annotations.add(new Annotate(Key.class.getSimpleName()));
            else if ("diff".equals(flag.getName()))
                annotations.add(new Annotate(ArrayDiff.class.getSimpleName()));

        return annotations;
    }

    private static ArrayDeclaration[] decWeakToStrong(WeakField.ArrayDeclaration[] declarations,
                                                      String onPacket, String onField) {
        ArrayList<ArrayDeclaration> toDeclarations = new ArrayList<ArrayDeclaration>();
        for (WeakField.ArrayDeclaration weakDec : declarations) {
            toDeclarations.add(new ArrayDeclaration(weakDec.maxSize, weakDec.elementsToTransfer, onPacket, onField));
        }
        return toDeclarations.toArray(new ArrayDeclaration[0]);
    }

    public void introduceNeighbours(Field[] neighbours) {
        HashMap<String, ArrayDeclaration> unsolvedReferences = new HashMap<String, ArrayDeclaration>();
        for (ArrayDeclaration dec : declarations) {
            if (dec.hasTransfer()) {
                unsolvedReferences.put(dec.getFieldThatHoldsSize(), dec);
            }
        }
        if (unsolvedReferences.isEmpty())
            return;

        Field[] others = neighbours;
        for (Field other : others) {
            if (unsolvedReferences.containsKey(other.getFieldName())) { // the value of the field is used
                ArrayDeclaration toIntroduce = unsolvedReferences.get(other.getFieldName());
                toIntroduce.setJavaTypeOfTransfer(other.getJType());
            }
        }
    }

    public String getFieldName() {
        return super.getName();
    }

    public String getFType() {
        return type.getName();
    }

    public String getJType() {
        return type.getJavaType();
    }

    public boolean hasDeclarations() {
        return (0 < getNumberOfDeclarations());
    }

    public int getNumberOfDeclarations() {
        return (type.getBasicType().isArrayEater()) ? declarations.length - 1 : declarations.length;
    }

    public static String getArrayDeclaration(FieldTypeBasic.FieldTypeAlias type, int rawDeclarations) {
        int unhandledArrays = rawDeclarations;
        if (type.getBasicType().isArrayEater())
            unhandledArrays--;
        return Util.repeat("[]", unhandledArrays);
    }

    public String getArrayDeclaration() {
        return getArrayDeclaration(type, declarations.length);
    }

    public String getNewCreation() throws UndefinedException {
        String out = "";
        for (int i = 0; i < getNumberOfDeclarations(); i++) {
            out += "[" + declarations[i].getSize() + "]";
        }
        return out;
    }

    public String getNewFromDataStream(String streamName) throws UndefinedException {
        return "new " + this.getFType() + "(" + streamName + getLimits() + ")";
    }

    private String getLimits() throws UndefinedException {
        return ", " + (type.getBasicType().isArrayEater() ?
                "ElementsLimit.limit(" + declarations[declarations.length - 1].getMaxSize() + ", " +
                        (declarations[declarations.length - 1].hasTransfer() ?
                                declarations[declarations.length - 1].getElementsToTransfer() :
                                declarations[declarations.length - 1].getMaxSize()) :
                "ElementsLimit.noLimit(") + ")";
    }

    public String getNewFromJavaType() throws UndefinedException {
        return "new " + this.getFType() + "(" + this.getFieldName() + "[i]" + getLimits() + ")";
    }

    private static void validateElementsToTransfer(ArrayDeclaration element, Collection<Typed<ABool>> to) throws UndefinedException {
        if (null != element.getElementsToTransfer())
            to.add(GROUP(isSmallerThanOrEq(BuiltIn.<AnInt>toCode(element.getMaxSize().toString()),
                    BuiltIn.<AnInt>toCode(element.getElementsToTransfer()))));
    }

    private static UndefinedException notSupportedIndex(String packetName, String fieldName, ArrayDeclaration dec) throws UndefinedException {
        String javaTypeOfTransfer = dec.getJavaTypeOfTransfer();
        return new UndefinedException(packetName + " uses the field " + dec.getFieldThatHoldsSize() +
                " of the type " + javaTypeOfTransfer + " as an array index for the field " +
                fieldName + " but the type " + javaTypeOfTransfer +
                " isn't supported as an array index.");
    }

    private static int intClassOf(String javaType) {
        if ("Integer".equals(javaType))
            return 0; // safe to use as int. (Byte, Short and Char aren't currently used in fields)
        else if ("Long".equals(javaType))
            return 1; // needs to check that it is small enough to use as int
        else
            return -1; // not supported
    }

    public void appendArrayEaterValidationTo(Block body) throws UndefinedException {
        if (type.getBasicType().isArrayEater() && 1 == declarations.length) {
            // TODO: Remove 1 == declarations.length when field arrays are standard
            this.getTType().register(new TargetMethod("verifyInsideLimits"));
            // TODO: Remove the above hack when the code is cleaner
            // * constructor stops destroying type information to add arrayinfo as text
            // * type isn't stored twice
            body.addStatement(this.call("verifyInsideLimits", getSuperLimit(0)));
        }
    }

    private Typed getSuperLimit(int pos) throws UndefinedException {
        if (pos < declarations.length)
        return new MethodCall("ElementsLimit.superLimit",
                BuiltIn.<AnInt>toCode(declarations[pos].getMaxSize().toString()),
                BuiltIn.<AnInt>toCode(declarations[pos].getSize()),
                getSuperLimit(pos + 1));
        else
            return new MethodCall("ElementsLimit.noLimit");

    }

    public void appendValidationTo(boolean testArrayLength, Block to) throws UndefinedException {
        LinkedList<Typed<ABool>> transferTypeCheck = new LinkedList<Typed<ABool>>();
        for (ArrayDeclaration dec : declarations) {
            if (dec.hasTransfer()) {
                switch (intClassOf(dec.getJavaTypeOfTransfer())) {
                    case 0:
                        break;
                    case 1:
                        transferTypeCheck.add(isSmallerThan(BuiltIn.<AnInt>toCode(dec.getMaxSize().toString()),
                                BuiltIn.<AnInt>toCode("Integer.MAX_VALUE")));
                        break;
                    case -1:
                        throw notSupportedIndex(onPacket, getFieldName(), dec);
                }
            }
        }

        // TODO: make sure it will cause a compile time error or throw an error here
        if (!transferTypeCheck.isEmpty())
            to.addStatement(ASSERT(and(transferTypeCheck.toArray(new Typed[transferTypeCheck.size()])),
                    literal("Can't prove that index value will stay in the range Java's signed integers can represent.")));

        LinkedList<Typed<ABool>> legalSize = new LinkedList<Typed<ABool>>();
        String arrayLevel = "";

        for (int i = 0; i < getNumberOfDeclarations(); i++) {
            final ArrayDeclaration element = declarations[i];
            validateElementsToTransfer(element, legalSize);
            if (testArrayLength)
                legalSize.add(GROUP(BuiltIn.<ABool>toCode(this.getFieldName() + arrayLevel + ".length != " + element.getSize())));
            arrayLevel += "[0]";
        }

        if (!legalSize.isEmpty())
            to.addStatement(BuiltIn.IF(or(legalSize.toArray(new Typed[legalSize.size()])),
                    new Block(THROW(IllegalArgumentException.class, literal("Array " + this.getFieldName() +
                            " constructed with value out of scope in packet " + onPacket)))));
    }

    // TODO: in should be a ExprFrom1
    public void forElementsInField(String in, Block out) {
        final int level = this.getNumberOfDeclarations();

        String replaceWith = "";
        Block ref = out;
        for (int counter = 0; counter < level; counter++) {
            Var count = Var.local("int", getCounterNumber(counter) + "", literal(0));
            Block inner = new Block();

            ref.addStatement(FOR(count,
                    isSmallerThan(count.ref(), BuiltIn.<AValue>toCode("this." + this.getFieldName() + replaceWith + ".length")),
                    inc(count),
                    inner));
            ref = inner;

            replaceWith += "[" + getCounterNumber(counter) + "]";
        }
        ref.addStatement(BuiltIn.<NoValue>toCode(in.replaceAll("\\[i\\]", replaceWith)));
    }

    private char getCounterNumber(int counter) {
        return ((char)('i' + counter));
    }

    private static String toInt(String elementsToTransferType, String packetName, String fieldName, ArrayDeclaration dec) throws UndefinedException {
        switch (intClassOf(elementsToTransferType)) {
            case 0:
                return "";
            case 1:
                return ".intValue()"; // safe since validated
            default:
                throw notSupportedIndex(packetName, fieldName, dec);
        }
    }

    public Collection<Requirement> getReqs() {
        HashSet<Requirement> reqs = new HashSet<Requirement>();
        reqs.add(new Requirement(getFType(), FieldTypeBasic.FieldTypeAlias.class));

        for (ArrayDeclaration declaration : declarations) {
            reqs.addAll(declaration.getReqs());
        }
        return reqs;
    }

    public static class ArrayDeclaration {
        private final IntExpression maxSize;
        private final String elementsToTransfer;
        private final String onPacket, onField;

        private String elementsToTransferType = null;

        public ArrayDeclaration(IntExpression maxSize, String elementsToTransfer, String onPacket, String onField) {
            this.maxSize = maxSize;
            this.elementsToTransfer = elementsToTransfer;
            this.onField = onField;
            this.onPacket = onPacket;
        }

        public IntExpression getMaxSize() {
            return maxSize;
        }

        public String getElementsToTransfer() throws UndefinedException {
            return (hasTransfer() ?
                    "this." + elementsToTransfer + ".getValue()" + toInt(elementsToTransferType,
                                                                         onPacket, onField, this) :
                    elementsToTransfer);
        }

        public String getFieldThatHoldsSize() {
            return elementsToTransfer;
        }

        public Collection<Requirement> getReqs() {
            return maxSize.getReqs();
        }

        private String getSize() throws UndefinedException {
            return (hasTransfer() ?
                    getElementsToTransfer() :
                    getMaxSize().toString());
        }

        public boolean hasTransfer() {
            return null != elementsToTransfer;
        }

        private void assumeInitialized() throws UndefinedException {
            if (hasTransfer() && null == elementsToTransferType)
                throw new UndefinedException("Field " + onField +
                    " in " + onPacket +
                    " refers to a field " + getFieldThatHoldsSize() + " that don't exist");
        }

        public void setJavaTypeOfTransfer(String jType) {
            if ((null == elementsToTransferType))
                elementsToTransferType = jType;
            else
                throw new UnsupportedOperationException("tried to set the type of an array declaration twice");
        }

        public String getJavaTypeOfTransfer() throws UndefinedException {
            assumeInitialized();
            return elementsToTransferType;
        }
    }

}
