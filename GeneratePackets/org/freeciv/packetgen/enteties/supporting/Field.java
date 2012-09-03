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
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;

import java.util.*;

import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.*;

public class Field extends Var {
    private final String onPacket;
    private final FieldTypeBasic.FieldTypeAlias type;
    private final ArrayDeclaration[] declarations;

    @Deprecated public Field(String fieldName, FieldTypeBasic.FieldTypeAlias typeAlias, String onPacket,
                 WeakField.ArrayDeclaration... declarations) {
        this(fieldName, typeAlias, onPacket, Collections.<WeakFlag>emptyList(), declarations);
    }

    public Field(String fieldName, FieldTypeBasic.FieldTypeAlias typeAlias, String onPacket, List<WeakFlag> flags,
                 WeakField.ArrayDeclaration... declarations) {
        super(fieldFlagsToAnnotations(flags), Visibility.PRIVATE, Scope.OBJECT, Modifiable.NO,
              typeAlias.getName() + getArrayDeclaration(typeAlias, declarations.length),
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
        return "new " + this.getFType() + "(" + streamName +
                (type.getBasicType().isArrayEater() ?
                        ", " + declarations[declarations.length - 1].getSize() : "") + ")";
    }

    public String getNewFromJavaType() throws UndefinedException {
        return "new " + this.getFType() + "(" + this.getFieldName() + "[i]" +
                (type.getBasicType().isArrayEater() ?
                        ", " + declarations[declarations.length - 1].getSize() : "") + ")";
    }

    private String getLegalSize(boolean testArrayLength) throws UndefinedException {
        String out = "";
        String arrayLevel = "";

        if (type.getBasicType().isArrayEater())
            out += validateElementsToTransfer(declarations[declarations.length - 1]);

        for (int i = 0; i < getNumberOfDeclarations(); i++) {
            final ArrayDeclaration element = declarations[i];
            out += validateElementsToTransfer(element);
            if (testArrayLength)
                out += "(" + this.getFieldName() + arrayLevel + ".length != " + element
                        .getSize() + ")" + "||";
            arrayLevel += "[0]";
        }
        return out;
    }

    private static String validateElementsToTransfer(ArrayDeclaration element) throws UndefinedException {
        if (null != element.getElementsToTransfer())
            return "(" + element.getMaxSize() + " <= " + element
                .getElementsToTransfer() + ")" + "||";
        else
            return "";
    }

    private String transferTypeCheck() throws UndefinedException {
        String out = "";
        for (ArrayDeclaration dec : declarations) {
            if (dec.hasTransfer()) {
                String javaTypeOfTransfer = dec.getJavaTypeOfTransfer();
                switch (intClassOf(javaTypeOfTransfer)) {
                    case 0:
                        break;
                    case 1:
                        out += dec.getMaxSize() + " < " + "Integer.MAX_VALUE";
                        break;
                    case -1:
                        throw notSupportedIndex(onPacket, getFieldName(), dec);
                }
            }
        }
        return out;
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

    public void appendValidationTo(boolean testArrayLength, Block to) throws UndefinedException {
        String transferTypesAreSafe = transferTypeCheck();
        String sizeChecks = this.getLegalSize(testArrayLength);

        if (!"".equals(transferTypesAreSafe))
            // TODO: make sure it will cause a compile time error or throw an error here
            to.addStatement(ASSERT(asBool(transferTypesAreSafe),
                    literalString("Can't prove that index value will stay in the range Java's signed integers can represent.")));

        if (!"".equals(sizeChecks)) {
            to.addStatement(BuiltIn.IF(asBool(sizeChecks.substring(0, sizeChecks.length() - 2)),
                    Block.fromStrings("throw new IllegalArgumentException(\"Array " + this.getFieldName() +
                            " constructed with value out of scope in packet " + onPacket + "\")")));
        }
    }

    public String[] forElementsInField(String before, String in, String after) {
        assert (null != in && !in.isEmpty());

        LinkedList<String> out = new LinkedList<String>();
        final int level = this.getNumberOfDeclarations();

        if (0 < level && null != before && !before.isEmpty()) {
            out.add(before);
        }

        String[] wrappedInFor = new String[1 + level * 2];
        String replaceWith = "";
        for (int counter = 0; counter < level; counter++) {
            wrappedInFor[counter] = "for (int " + getCounterNumber(counter) + " = 0; " +
                    getCounterNumber(counter) + " < " + "this." + this.getFieldName() + replaceWith + ".length; " +
                    getCounterNumber(counter) + "++) {";
            wrappedInFor[1 + counter + level] = "}";
            replaceWith += "[" + getCounterNumber(counter) + "]";
        }
        wrappedInFor[level] = in.replaceAll("\\[i\\]", replaceWith);

        out.addAll(Arrays.asList(wrappedInFor));

        if (0 < level && null != after && !after.isEmpty()) {
            out.add(after);
        }

        return out.toArray(new String[0]);
    }

    // TODO: in should be a ExprFrom1
    public void forElementsInField(String in, Block out) {
        final int level = this.getNumberOfDeclarations();

        String replaceWith = "";
        Block ref = out;
        for (int counter = 0; counter < level; counter++) {
            Var count = Var.local("int", getCounterNumber(counter) + "", asAnInt("0"));
            Block inner = new Block();

            ref.addStatement(FOR(count,
                    isSmallerThan(count.ref(), asAValue("this." + this.getFieldName() + replaceWith + ".length")),
                    inc(count),
                    inner));
            ref = inner;

            replaceWith += "[" + getCounterNumber(counter) + "]";
        }
        ref.addStatement(asVoid(in.replaceAll("\\[i\\]", replaceWith)));
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
        reqs.add(new Requirement(getFType(), Requirement.Kind.FIELD_TYPE));

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

        public String getMaxSize() {
            return maxSize.toString();
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
                    getMaxSize());
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
