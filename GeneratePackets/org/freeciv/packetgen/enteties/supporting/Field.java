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

import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.FieldTypeBasic;

import java.util.*;

public class Field {
    private final String variableName;
    private final FieldTypeBasic.FieldTypeAlias type;
    private final ArrayDeclaration[] declarations;

    public Field(String variableName, FieldTypeBasic.FieldTypeAlias typeAlias, ArrayDeclaration... declarations) {
        if (typeAlias.getBasicType().isArrayEater() && (0 == declarations.length))
            throw new IllegalArgumentException("Array eaters needs array declarations");

        this.variableName = variableName;
        this.type = typeAlias;
        this.declarations = declarations;
    }

    public String getVariableName() {
        return variableName;
    }

    public String getType() {
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

    public String getArrayDeclaration() {
        String out = "";
        for (int i = 0; i < getNumberOfDeclarations(); i++) {
            out += "[]";
        }
        return out;
    }

    public String getNewCreation() {
        String out = "";
        for (int i = 0; i < getNumberOfDeclarations(); i++) {
            out += "[" + declarations[i].getSize() + "]";
        }
        return out;
    }

    public String getNewFromDataStream(String streamName) {
        return "new " + this.getType() + "(" + streamName +
                (type.getBasicType().isArrayEater() ?
                        ", " + declarations[declarations.length - 1].getSize() : "") + ");";
    }

    public String getNewFromJavaType() {
        return "new " + this.getType() + "(" + this.getVariableName() + "[i]" +
                (type.getBasicType().isArrayEater() ?
                        ", " + declarations[declarations.length - 1].getSize() : "") + ");";
    }

    private String getLegalSize(boolean testArrayLength) {
        String out = "";
        String arrayLevel = "";

        if (type.getBasicType().isArrayEater())
            out += validateElementsToTransfer(declarations[declarations.length - 1]);

        for (int i = 0; i < getNumberOfDeclarations(); i++) {
            final ArrayDeclaration element = declarations[i];
            out += validateElementsToTransfer(element);
            if (testArrayLength)
                out += "(" + this.getVariableName() + arrayLevel + ".length != " + element
                        .getSize() + ")" + "||";
            arrayLevel += "[0]";
        }
        return out;
    }

    private static String validateElementsToTransfer(ArrayDeclaration element) {
        if (null != element.getElementsToTransfer())
            return "(" + element.getMaxSize() + " <= " + element
                .getElementsToTransfer() + ")" + "||";
        else
            return "";
    }

    public String[] validate(String name, boolean testArrayLength) {
        String sizeChecks = this.getLegalSize(testArrayLength);
        if ("".equals(sizeChecks))
            return new String[]{};
        else
            return new String[]{
                "if " + "(" + sizeChecks.substring(0, sizeChecks.length() - 2) + ")",
                "\t" + "throw new IllegalArgumentException(\"Array " + this.getVariableName() +
                        " constructed with value out of scope in packet " + name + "\");"};
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
            wrappedInFor[counter] = "for(int " + getCounterNumber(counter) + " = 0; " +
                    getCounterNumber(counter) + " < " + "this." + this.getVariableName() + replaceWith + ".length; " +
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

    private char getCounterNumber(int counter) {
        return ((char)('i' + counter));
    }

    public Collection<Requirement> getReqs() {
        HashSet<Requirement> reqs = new HashSet<Requirement>();
        reqs.add(new Requirement(getType(), Requirement.Kind.FIELD_TYPE));

        for (ArrayDeclaration declaration : declarations) {
            reqs.addAll(declaration.getReqs());
        }
        return reqs;
    }

    public static class ArrayDeclaration {
        private final IntExpression maxSize;
        private final String elementsToTransfer;

        public ArrayDeclaration(IntExpression maxSize, String elementsToTransfer) {
            this.maxSize = maxSize;
            this.elementsToTransfer = elementsToTransfer;
        }

        public String getMaxSize() {
            return maxSize.toString();
        }

        public String getElementsToTransfer() {
            return (null == elementsToTransfer ?
                    elementsToTransfer :
                    "this." + elementsToTransfer + ".getValue()");
        }

        public Collection<Requirement> getReqs() {
            return maxSize.getReqs();
        }

        private String getSize() {
            return (null == elementsToTransfer ?
                    getMaxSize() :
                    getElementsToTransfer());
        }
    }

    public static class WeakField {
        private final String name, type;
        private final ArrayDeclaration[] declarations;

        public WeakField(String name, String kind, ArrayDeclaration... declarations) {
            this.name = name;
            this.type = kind;
            this.declarations = declarations;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public ArrayDeclaration[] getDeclarations() {
            return declarations;
        }
    }
}
