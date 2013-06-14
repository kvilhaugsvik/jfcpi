/*
 * Copyright (c) 2012, Sveinung Kvilhaugsvik
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

package com.kvilhaugsvik.javaGenerator;

import org.freeciv.utility.Util;
import com.kvilhaugsvik.javaGenerator.util.Formatted;
import com.kvilhaugsvik.javaGenerator.representation.CodeAtoms;
import com.kvilhaugsvik.javaGenerator.representation.HasAtoms;
import com.kvilhaugsvik.javaGenerator.representation.IR.CodeAtom;

import java.util.*;
import java.util.regex.Pattern;

public abstract class Address<On extends Address<?>> extends Formatted implements HasAtoms {
    public static final Address LOCAL_CODE_BLOCK = new Address() {};

    protected static final HashMap<String, Address> cached = new HashMap<String, Address>();

    protected final On where;
    protected final List<? extends CodeAtom> components;

    protected final boolean symbolic;

    protected Address() {
        symbolic = true;

        where = null;
        components = Collections.<CodeAtom>emptyList();
    }

    public Address(On start, List<? extends CodeAtom> parts) {
        this.where = start;
        this.components = parts;

        symbolic = false;

        cached.put(this.getFullAddress(), this);
    }

    private static final Pattern ADDRESS_SPLITTER = Pattern.compile("\\.");
    protected static List<CodeAtom> addressString2Components(String address) {
        String[] parts = ADDRESS_SPLITTER.split(address);
        ArrayList<CodeAtom> build = new ArrayList<CodeAtom>(parts.length);
        for (String part : parts)
            build.add(new CodeAtom(part));

        // TODO: Properly represent ... (and [])
        if (address.endsWith("...")) {
            build.add(new CodeAtom(""));
            build.add(new CodeAtom(""));
            build.add(new CodeAtom(""));
        }

        return build;
    }

    public CodeAtom getFirstComponent() {
        if (includeWhere())
            return where.getFirstComponent();
        else if (0 < components.size())
            return components.get(0);
        else
            throw new NoSuchElementException("No components at all");
    }

    public String getFullAddress() {
        if (includeWhere())
            return where.getFullAddress() + "." + Util.joinStringArray(components, ".", "", "");
        else
            return Util.joinStringArray(components, ".", "", "");
    }

    private boolean includeWhere() {
        return !(symbolic || where.symbolic);
    }

    public CodeAtom getTypedSimpleName() {
        return components.get(components.size() - 1);
    }

    public String getSimpleName() {
        return getTypedSimpleName().get();
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        if (includeWhere()) {
            where.writeAtoms(to);
            to.add(HasAtoms.HAS);
        }
        to.joinSep(HAS, components);
    }

    public static <A extends Address> A getExisting(String name, Class<A> kind) {
        final Address found = cached.get(name);

        if (null == found)
            throw new NoSuchElementException(name + " not found");

        if (!(kind.isInstance(found)))
            throw new ClassCastException(found.getFullAddress() + " is a " +
                    found.getClass().getSimpleName() + " not a " + kind.getSimpleName());

        return kind.cast(found);
    }
}
