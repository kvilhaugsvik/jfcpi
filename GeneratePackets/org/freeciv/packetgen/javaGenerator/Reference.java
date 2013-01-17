/*
 * Copyright (c) 2013. Sveinung Kvilhaugsvik
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

package org.freeciv.packetgen.javaGenerator;

import org.freeciv.packetgen.javaGenerator.representation.CodeAtoms;
import org.freeciv.packetgen.javaGenerator.representation.IR;
import org.freeciv.packetgen.javaGenerator.typeBridge.Typed;
import org.freeciv.packetgen.javaGenerator.typeBridge.Value;
import org.freeciv.packetgen.javaGenerator.typeBridge.willReturn.AValue;
import org.freeciv.packetgen.javaGenerator.typeBridge.willReturn.Returnable;
import org.freeciv.packetgen.javaGenerator.util.ValueHelper;

public class Reference<Contains extends AValue> extends Address implements Value<Contains> {
    public static final Reference<AValue> THIS = Var.<AValue>param(TargetClass.SELF_TYPED, "this").ref();

    private final ValueHelper valueHelper;

    Reference(TargetClass type, Address where, IR.CodeAtom name) {
        super(where, name);
        this.valueHelper = new ValueHelper(type, this);
    }

    public static Reference refOn(Var of) {
        IR.CodeAtom name = new IR.CodeAtom(of.getName());
        switch (of.getScope()) {
            case CLASS:
                return new Reference(of.getTType(), TargetClass.SELF_TYPED, name);
            case OBJECT:
                return new Reference(of.getTType(), THIS, name);
            case CODE_BLOCK:
            default:
                return new Reference(of.getTType(), Address.LOCAL_CODE_BLOCK, name);
        }
    }

    public static Reference toUndeclaredLocalOfUnknownType(String variable) {
        if (0 == variable.indexOf('.'))
            return new Reference(TargetClass.TYPE_NOT_KNOWN, Address.LOCAL_CODE_BLOCK, new IR.CodeAtom(variable));
        else
            throw new IllegalArgumentException("Not local");
    }

    public <Ret extends Returnable> Typed<Ret> call(String method, Typed<? extends AValue>... params) {
        return valueHelper.<Ret>call(method, params);
    }

    @Override
    public <Ret extends AValue> Value<Ret> callV(String method, Typed<? extends AValue>... params) {
        return valueHelper.<Ret>callV(method, params);
    }

    public <Ret extends AValue> SetTo<Ret> assign(final Typed<Ret> value) {
        return new SetTo<Ret>(this, value);
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        to.hintStart(Reference.class.getCanonicalName());
        super.writeAtoms(to);
        to.hintEnd(Reference.class.getCanonicalName());
    }

    public static class SetTo<Ret extends AValue> extends Type<Ret> {
        private final Address referName;
        private final Typed<Ret> value;

        private SetTo(Address referName, Typed<Ret> value) {
            this.referName = referName;
            this.value = value;
        }

        @Override
        public void writeAtoms(CodeAtoms to) {
            referName.writeAtoms(to);
            to.add(ASSIGN);
            value.writeAtoms(to);
        }

        public static <Ret extends AValue> SetTo<Ret> strToVal(String variable, Typed<Ret> value) {
            return new SetTo<Ret>(new Address(LOCAL_CODE_BLOCK, addressString2Components(variable)), value);
        }
    }
}
