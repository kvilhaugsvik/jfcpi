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
    private final String type;
    private final String javatype;

    public Field(String variableName, String type, String javatype) {
        this.variableName = variableName;
        this.type = type;
        this.javatype = javatype;
    }

    public Field(String variableName, FieldTypeBasic.FieldTypeAlias typeAlias) {
        this.variableName = variableName;
        this.type = typeAlias.getName();
        this.javatype = typeAlias.getJavaType();
    }

    public String getVariableName() {
        return variableName;
    }

    public String getType() {
        return type;
    }

    public String getJType() {
        return javatype;
    }
}
