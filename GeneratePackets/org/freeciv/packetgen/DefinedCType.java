/*
 * Copyright (c) 2012. Sveinung Kvilhaugsvik
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

import java.util.*;

public class DefinedCType implements IDependency, FieldTypeBasic.Generator {
    private final Requirement iProvide;
    private final Collection<Requirement> willRequire;
    private final String type;
    private final boolean isNative;

    public DefinedCType(String name, String jType, String outsideRequirement) {
        this.iProvide = new Requirement(name, Requirement.Kind.AS_JAVA_DATATYPE);
        this.type = jType;

        isNative = (null == outsideRequirement);
        if (isNative)
            this.willRequire = Collections.<Requirement>emptySet();
        else
            this.willRequire = Arrays.asList(new Requirement(outsideRequirement, Requirement.Kind.AS_JAVA_DATATYPE));
    }

    @Override
    public FieldTypeBasic getBasicFieldTypeOnInput(NetworkIO io) {
        return new FieldTypeBasic(io.getIFulfillReq().getName(), iProvide.getName(),
                type,
                new String[]{"this.value = value;"},
                "value = " + (isNative? io.getRead() : type + ".valueOf(" + io.getRead() + ")") + ";",
                io.getWrite() + (isNative? "(value);" : "(value.getNumber());"),
                io.getSize(),
                false, willRequire);
    }

    @Override
    public Collection<Requirement> getReqs() {
        return Collections.<Requirement>emptySet();
    }

    @Override
    public Requirement getIFulfillReq() {
        return iProvide;
    }
}