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

public class Field {
    private final String variableName;
    private final FieldTypeBasic.FieldTypeAlias type;
    private final ArrayDeclaration[] declarations;

    public Field(String variableName, FieldTypeBasic.FieldTypeAlias typeAlias, ArrayDeclaration... declarations) {
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
        return (0 < declarations.length);
    }

    int getNumberOfDeclarations() {
        return declarations.length;
    }

    String getArrayDeclaration() {
        String out = "";
        for (ArrayDeclaration element: declarations) {
            out += "[]";
        }
        return out;
    }

    String getNewCreation(String callOnElementsToTransfer) {
        String out = "";
        for (ArrayDeclaration element: declarations) {
            out += element.getNewCreation(callOnElementsToTransfer);
        }
        return out;
    }

    public static class ArrayDeclaration {
        private final String maxSize, elementsToTransfer;

        public ArrayDeclaration(String maxSize, String elementsToTransfer) {
            this.maxSize = maxSize;
            this.elementsToTransfer = elementsToTransfer;
        }

        private String getNewCreation(String callOnElementsToTransfer) {
            return "[" + (null == elementsToTransfer? maxSize: elementsToTransfer + callOnElementsToTransfer) + "]";
        }
    }
}
