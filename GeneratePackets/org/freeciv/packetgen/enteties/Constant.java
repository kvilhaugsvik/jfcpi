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
import org.freeciv.packetgen.enteties.supporting.IntExpression;
import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.javaGenerator.*;
import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.*;

import java.util.*;
import java.util.regex.Pattern;

public class Constant extends Var implements IDependency {
    private final HashSet<Requirement> reqs = new HashSet<Requirement>();

    private static final String constantPrefix = Util.VERSION_DATA_CLASS + ".";
    private static final Pattern FIND_CONSTANTS_CLASS = Pattern.compile(constantPrefix);

    private Constant(String name, Typed<? extends AValue> expression, String typeName) {
        super(Collections.<Annotate>emptyList(), Visibility.PUBLIC, Scope.CLASS, Modifiable.NO,
                typeName, name, expression);
    }

    public Constant(String name, IntExpression expression) {
        super(Collections.<Annotate>emptyList(), Visibility.PUBLIC, Scope.CLASS, Modifiable.NO,
                "int", name, BuiltIn.asAnInt(expression.toString()));
        reqs.addAll(expression.getReqs());
    }

    public String getType() {
        return super.getType();
    }

    public String getName() {
        return super.getName();
    }

    public String getExpression() {
        return Util.joinStringArray(
                ClassWriter.DEFAULT_STYLE_INDENT.asFormattedLines(new CodeAtoms(super.getValue())).toArray(),
                "\n", "", "");
    }

    @Override
    public Collection<Requirement> getReqs() {
        return Collections.unmodifiableCollection(reqs);
    }

    @Override
    public Requirement getIFulfillReq() {
        return new Requirement(super.getName(), Requirement.Kind.VALUE);
    }

    public static String referToInJavaCode(Requirement req) {
        assert (Requirement.Kind.VALUE.equals(req.getKind()));

        return constantPrefix + req.getName();
    }

    public static String stripJavaCodeFromReference(String constantName) {
        return FIND_CONSTANTS_CLASS.matcher(constantName).replaceAll("");
    }

    public static Constant isString(String name, Typed<AString> expression) {
        return new Constant(name, expression, "String");
    }

    public static Constant isLong(String name, Typed<ALong> expression) {
        return new Constant(name, expression, "long");
    }
}