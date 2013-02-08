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

import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.Requirement;
import com.kvilhaugsvik.javaGenerator.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class SpecialClass extends ClassWriter implements IDependency {
    private final Requirement iProvide;
    private final Set<Requirement> iRequire;

    public SpecialClass(TargetPackage where, String madeFrom, String name,
                        Requirement iProvide, Set<Requirement> iRequire) {
        super(ClassKind.CLASS, where, Imports.are(), madeFrom, Collections.<Annotate>emptyList(), name,
                DEFAULT_PARENT, Collections.<TargetClass>emptyList());

        this.iProvide = iProvide;
        this.iRequire = iRequire;
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
