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

import org.freeciv.packetgen.GeneratorDefaults;
import org.freeciv.packetgen.enteties.supporting.IntExpression;
import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.Requirement;

import java.util.*;
import java.util.regex.Pattern;

public class Constant implements IDependency {
    private final String name;
    private final IntExpression expression;
    private final HashSet<Requirement> reqs = new HashSet<Requirement>();

    private static final String constantPrefix = GeneratorDefaults.CONSTANT_LOCATION + ".";
    private static final Pattern FIND_CONSTANTS_CLASS = Pattern.compile(constantPrefix);

    public Constant(String name, IntExpression expression) {
        this.name = name;
        this.expression = expression;
        reqs.addAll(expression.getReqs());
    }

    public String getName() {
        return name;
    }

    public String getExpression() {
        return expression.toString();
    }

    @Override
    public Collection<Requirement> getReqs() {
        return Collections.unmodifiableCollection(reqs);
    }

    @Override
    public Requirement getIFulfillReq() {
        return new Requirement(name, Requirement.Kind.VALUE);
    }

    public static String referToInJavaCode(Requirement req) {
        assert (Requirement.Kind.VALUE.equals(req.getKind()));

        return constantPrefix + req.getName();
    }

    public static String stripJavaCodeFromReference(String constantName) {
        return FIND_CONSTANTS_CLASS.matcher(constantName).replaceAll("");
    }
}