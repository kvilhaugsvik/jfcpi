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

package org.freeciv.packetgen;

import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;

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

    boolean hasDeclarations() {
        return (0 < getNumberOfDeclarations());
    }

    int getNumberOfDeclarations() {
        return (type.getBasicType().isArrayEater())? declarations.length - 1: declarations.length;
    }

    String getArrayDeclaration() {
        String out = "";
        for (int i = 0; i < getNumberOfDeclarations(); i++) {
            out += "[]";
        }
        return out;
    }

    String getNewCreation(String callOnElementsToTransfer) {
        String out = "";
        for (int i = 0; i < getNumberOfDeclarations(); i++) {
            out += "[" + declarations[i].getSize(callOnElementsToTransfer) + "]";
        }
        return out;
    }

    String getNewFromDataStream(String streamName) {
        return "new " + this.getType() + "(" + streamName +
                (type.getBasicType().isArrayEater()?
                        ", " + declarations[declarations.length -1].getSize(".getValue()"): "") + ");";
    }

    String getNewFromJavaType() {
        return "new " + this.getType() + "(" + this.getVariableName() + "[i]" +
                (type.getBasicType().isArrayEater()?
                        ", " + declarations[declarations.length -1].getSize(".getValue()"): "") + ");";
    }

    private String getLegalSize(String callOnElementsToTransfer) {
        String out = "";
        String arrayLevel = "";
        for (int i = 0; i < getNumberOfDeclarations(); i++) {
            final ArrayDeclaration element = declarations[i];
            if (null != element.elementsToTransfer)
                out += "(" + element.getMaxSize() + " <= " + element.elementsToTransfer + callOnElementsToTransfer + ")" + "||";
            out += "(" + this.getVariableName() + arrayLevel + ".length != " + element.getSize(callOnElementsToTransfer) + ")";
            out += "||";
            arrayLevel += "[0]";
        }
        return out.substring(0, out.length() - 2);
    }

    String[] validate(String callOnElementsToTransfer, String name) {
        return new String[]{
                "if " + "(" + this.getLegalSize(callOnElementsToTransfer) + ")",
                "\t" + "throw new IllegalArgumentException(\"Array " + this.getVariableName() +
                        " constructed with value out of scope in packet " + name + "\");"};
    }

    private static final Pattern aConstant = Pattern.compile("\\D\\w*");
    public Collection<Requirement> getReqs() {
        HashSet<Requirement> reqs = new HashSet<Requirement>();
        reqs.add(new Requirement(getType(), Requirement.Kind.FIELD_TYPE));

        for (ArrayDeclaration declaration : declarations) {
            if (aConstant.matcher(declaration.maxSize).matches())
                reqs.add(new Requirement(declaration.maxSize, Requirement.Kind.VALUE));
        }
        return reqs;
    }

    public static class ArrayDeclaration {
        private final String maxSize, elementsToTransfer;

        public ArrayDeclaration(String maxSize, String elementsToTransfer) {
            this.maxSize = maxSize;
            this.elementsToTransfer = elementsToTransfer;
        }

        public String getMaxSize() {
            if (aConstant.matcher(maxSize).matches())
                return "Constants." + maxSize;
            else
                return maxSize;
        }

        private String getSize(String callOnElementsToTransfer) {
            return (null == elementsToTransfer? getMaxSize(): elementsToTransfer + callOnElementsToTransfer);
        }
    }
}
