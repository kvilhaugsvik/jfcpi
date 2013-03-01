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
import org.freeciv.packetgen.dependency.ReqKind;
import org.freeciv.packetgen.enteties.supporting.IntExpression;
import org.freeciv.packetgen.dependency.Dependency;
import org.freeciv.packetgen.dependency.Requirement;
import com.kvilhaugsvik.javaGenerator.*;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.*;
import com.kvilhaugsvik.javaGenerator.representation.CodeAtoms;

import java.util.*;
import java.util.regex.Pattern;

public class Constant<Kind extends AValue> extends Var<Kind> implements Dependency.Item, ReqKind {
    private final HashSet<Requirement> reqs = new HashSet<Requirement>();

    private static final String constantPrefix = Util.VERSION_DATA_CLASS + ".";
    private static final Pattern FIND_CONSTANTS_CLASS = Pattern.compile(constantPrefix);

    private Constant(TargetClass type, String name, Typed<Kind> expression, Collection<? extends Requirement> needed) {
        super(Collections.<Annotate>emptyList(), Visibility.PUBLIC, Scope.CLASS, Modifiable.NO,
                type, name, expression);
        reqs.addAll(needed);
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
        return new Requirement(super.getName(), Constant.class);
    }

    public static String referToInJavaCode(Requirement req) {
        assert (Constant.class.equals(req.getKind()));

        return constantPrefix + req.getName();
    }

    public static String stripJavaCodeFromReference(String constantName) {
        return FIND_CONSTANTS_CLASS.matcher(constantName).replaceAll("");
    }

    public static Constant<AnInt> isInt(String name, IntExpression expression) {
        return new Constant<AnInt>(TargetClass.fromClass(int.class), name,
                BuiltIn.<AnInt>toCode(expression.toString()), expression.getReqs());
    }

    public static Constant<AString> isString(String name, Typed<AString> expression) {
        return new Constant<AString>(TargetClass.fromClass(String.class), name,
                expression, Collections.<Requirement>emptySet());
    }

    public static Constant<AValue> isClass(String name, Typed<AValue> expression) {
        return new Constant<AValue>(TargetClass.fromClass(Class.class), name,
                expression, Collections.<Requirement>emptySet());
    }

    public static Constant<ABool> isBool(String name, Typed<ABool> expression) {
        return new Constant<ABool>(TargetClass.fromClass(boolean.class), name,
                expression, Collections.<Requirement>emptySet());
    }

    public static Constant<ALong> isLong(String name, Typed<ALong> expression) {
        return new Constant<ALong>(TargetClass.fromClass(long.class), name, expression, Collections.<Requirement>emptySet());
    }
}