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

import com.kvilhaugsvik.javaGenerator.expression.Reference;
import org.freeciv.packet.fieldtype.*;
import org.freeciv.packetgen.Hardcoded;
import com.kvilhaugsvik.dependency.UndefinedException;
import com.kvilhaugsvik.dependency.Requirement;
import org.freeciv.packetgen.enteties.FieldType;
import com.kvilhaugsvik.javaGenerator.*;
import com.kvilhaugsvik.javaGenerator.typeBridge.Value;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AValue;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AnInt;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.Returnable;

import java.util.*;

public class Field<Kind extends AValue> extends Var<Kind> {
    private final static int DELTA_NUMBER_NOT_SET = -1;

    private final String onPacket;
    private final FieldType type;
    private final ArrayDeclaration[] declarations;

    private int deltaFieldNumber = DELTA_NUMBER_NOT_SET;

    public Field(String fieldName, FieldType typeAlias, String onPacket, List<WeakFlag> flags,
                 WeakField.ArrayDeclaration... declarations) {
        super(fieldFlagsToAnnotations(flags), Visibility.PROTECTED, Scope.OBJECT, Modifiable.NO, typeAlias.getAddress(), fieldName, null, TargetClass.SELF_TYPED);

        if (typeAlias.isArrayEater() && (0 == declarations.length))
            throw new IllegalArgumentException("Array eaters needs array declarations");

        this.type = typeAlias;
        this.onPacket = onPacket;

        this.declarations = decWeakToStrong(declarations, onPacket, fieldName);
    }

    private static List<Annotate> fieldFlagsToAnnotations(List<WeakFlag> flags) {
        LinkedList<Annotate> annotations = new LinkedList<Annotate>();

        for (WeakFlag flag : flags)
            if ("key".equals(flag.getName()))
                annotations.add(new Annotate(Key.class));
            else if ("diff".equals(flag.getName()))
                annotations.add(new Annotate(ArrayDiff.class));
            else if ("add-cap".equals(flag.getName()))
                annotations.add(capAnnotate(flag, CapAdd.class));
            else if ("remove-cap".equals(flag.getName()))
                annotations.add(capAnnotate(flag, CapRemove.class));

        return annotations;
    }

    private static Annotate capAnnotate(WeakFlag flag, Class<?> annotation) {
        assert 1 == flag.getArguments().length :
                "Unexpected number of capability arguments (" + flag.getArguments().length + ")";
        return new Annotate(annotation,
                Reference.SetTo.strToVal("value", BuiltIn.literal(flag.getArguments()[0])));
    }

    private static ArrayDeclaration[] decWeakToStrong(WeakField.ArrayDeclaration[] declarations,
                                                      String onPacket, String onField) {
        ArrayList<ArrayDeclaration> toDeclarations = new ArrayList<ArrayDeclaration>();
        for (WeakField.ArrayDeclaration weakDec : declarations) {
            toDeclarations.add(new ArrayDeclaration(weakDec.maxSize, weakDec.elementsToTransfer, onPacket, onField));
        }
        return toDeclarations.toArray(new ArrayDeclaration[0]);
    }

    public void introduceNeighbours(List<Field> neighbours) {
        HashMap<String, ArrayDeclaration> unsolvedReferences = new HashMap<String, ArrayDeclaration>();
        for (ArrayDeclaration dec : declarations) {
            if (dec.hasTransfer()) {
                unsolvedReferences.put(dec.getFieldThatHoldsSize(), dec);
            }
        }
        if (unsolvedReferences.isEmpty())
            return;

        for (Field other : neighbours) {
            if (unsolvedReferences.containsKey(other.getName())) { // the value of the field is used
                ArrayDeclaration toIntroduce = unsolvedReferences.get(other.getName());
                toIntroduce.setJavaTypeOfTransfer(other);
            }
        }
    }

    public TargetClass getUnderType() {
        return type.getUnderType();
    }

    /**
     * Get the zero value of the field.
     * @return the zero value of the field.
     */
    public Value getZeroValue() {
        if (this.isAnnotatedUsing(ArrayDiff.class)) {
            /* A diff array zero has elements (that are zero them self). */

            assert 0 < declarations.length : "A diff array should have a max size.";
            return type.getZeroValue(declarations[0].getMaxSize());
        } else {
            return type.getZeroValue(BuiltIn.literal(0));
        }
    }

    public String getFType() {
        return type.getName();
    }

    public String getJType() {
        return type.getUnderType().getSimpleName();
    }

    public Var<Kind> getTmpLocalVar(Typed<Kind> value) {
        return Var.local(getTType(), getName() + "_tmp", value);
    }

    public void setDelta(int deltaNumber) {
        if (deltaNumber < 0)
            throw new IllegalArgumentException("Delta number can't be negative");
        if (this.deltaFieldNumber != DELTA_NUMBER_NOT_SET)
            throw new IllegalStateException("Delta number already set");

        this.deltaFieldNumber = deltaNumber;
    }

    public void resetDelta() {
        this.deltaFieldNumber = DELTA_NUMBER_NOT_SET;
    }

    public boolean isDelta() {
        return DELTA_NUMBER_NOT_SET != this.deltaFieldNumber;
    }

    public int getDeltaFieldNumber() {
        if (DELTA_NUMBER_NOT_SET == this.deltaFieldNumber)
            throw new IllegalStateException("Not delta (yet)");

        return deltaFieldNumber;
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
        if (type.isArrayEater()) {
            body.addStatement(ref().<Returnable>call("verifyInsideLimits", getSuperLimit(0, false)));
        }
    }

    public Typed getSuperLimit(int pos, boolean inFactory) throws UndefinedException {
        if (pos < declarations.length) {
            LinkedList<Typed<AnInt>> args = new LinkedList<Typed<AnInt>>();
            args.add(declarations[pos].getMaxSize());
            if (declarations[pos].hasTransfer())
                args.add(declarations[pos].getTransferValue(inFactory));
            if (pos + 1 < declarations.length)
                args.add(getSuperLimit(pos + 1, inFactory));
            return TargetClass.from(ElementsLimit.class).callV("limit", args.toArray(new Typed[0]));
        } else {
            return Hardcoded.noLimit;
        }
    }

    public Collection<Requirement> getReqs() {
        HashSet<Requirement> reqs = new HashSet<Requirement>();
        reqs.add(new Requirement(getFType(), org.freeciv.packetgen.enteties.FieldType.class));

        for (ArrayDeclaration declaration : declarations) {
            reqs.addAll(declaration.getReqs());
        }
        return reqs;
    }

    public static class ArrayDeclaration {
        private final IntExpression maxSize;
        private final String elementsToTransfer;
        private final String onPacket, onField;

        private Field elementsToTransferTyped = null;

        public ArrayDeclaration(IntExpression maxSize, String elementsToTransfer, String onPacket, String onField) {
            this.maxSize = maxSize;
            this.elementsToTransfer = elementsToTransfer;
            this.onField = onField;
            this.onPacket = onPacket;
        }

        public IntExpression getMaxSize() {
            return maxSize;
        }

        public String getFieldThatHoldsSize() {
            return elementsToTransfer;
        }

        public Collection<Requirement> getReqs() {
            return maxSize.getReqs();
        }

        private Typed<AnInt> getTransferValue(boolean inFactory) throws UndefinedException {
            final Value<AnInt> fieldValue;

            if (inFactory) {
                // assumed to be a local temporary variable
                fieldValue = elementsToTransferTyped.getTmpLocalVar(null).ref().callV("getValue");
            } else {
                fieldValue = elementsToTransferTyped.ref().callV("getValue");
            }

            switch (intClassOf(getJavaTypeOfTransfer())) {
                case 0:
                    return fieldValue;
                case 1:
                    return fieldValue.callV("intValue");
                default:
                    throw notSupportedIndex(onPacket, onField, this);
            }
        }

        public boolean hasTransfer() {
            return null != elementsToTransfer;
        }

        private void assumeInitialized() throws UndefinedException {
            if (hasTransfer() && null == elementsToTransferTyped)
                throw new UndefinedException("Field " + onField +
                    " in " + onPacket +
                    " refers to a field " + getFieldThatHoldsSize() + " that don't exist");
        }

        public void setJavaTypeOfTransfer(Field jType) {
            if ((null == elementsToTransferTyped))
                elementsToTransferTyped = jType;
            else
                throw new UnsupportedOperationException("tried to set the type of an array declaration twice");
        }

        public String getJavaTypeOfTransfer() throws UndefinedException {
            assumeInitialized();
            return elementsToTransferTyped.getJType();
        }
    }

}
