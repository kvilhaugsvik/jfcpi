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

package org.freeciv.packetgen.enteties;

import org.freeciv.Util;
import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.javaGenerator.ClassKind;
import org.freeciv.packetgen.javaGenerator.ClassWriter;
import org.freeciv.packetgen.javaGenerator.TargetPackage;
import org.freeciv.types.FCEnum;

import java.util.*;

public class Struct extends ClassWriter implements IDependency {
    private final Set<Requirement> iRequire;
    private final Requirement iProvide;

    public Struct(String name, List<Map.Entry<String, String>> fields, Set<Requirement> willNeed) {
        super(ClassKind.CLASS,
                new TargetPackage(FCEnum.class.getPackage()),
                null, "Freeciv C code", name, null, null);

        addConstructorFields();

        for (Map.Entry<String, String> field: fields) {
            addObjectConstant(field.getKey(), field.getValue());
            addMethodPublicReadObjectState(null, field.getKey(), "get" + field.getValue(),
                    "return " + field.getValue() + ";");
        }

        String[] varNames = new String[fields.size()];
        for (int index = 0; index < fields.size(); index++) {
            varNames[index] = "\"" + fields.get(index).getValue() + ": \"" + " + " + fields.get(index).getValue();
        }
        addMethodPublicReadObjectState(null, "String", "toString",
                "return " + Util.joinStringArray(varNames, " + \", \" + ", "\"(\" + ", " + \")\"") + ";");

        iRequire = willNeed;
        iProvide = new Requirement("struct" + " " + name, Requirement.Kind.AS_JAVA_DATATYPE);
    }

    @Override
    public Collection<Requirement> getReqs() {
        return iRequire;
    }

    @Override
    public Requirement getIFulfillReq() {
        return iProvide;
    }
}
