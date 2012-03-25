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

import java.util.*;

import org.freeciv.packetgen.FieldTypeBasic.FieldTypeAlias;

public class PacketsStore {
    private final boolean hasTwoBytePacketNumber;

    private final HashSet<Requirement> notFoundWhenNeeded = new HashSet<Requirement>();
    private final DependencyStore requirements = new DependencyStore();

    // To avoid duplication of structures have packets store the packets and packetsByNumber translate the keys
    // Idea from http://stackoverflow.com/q/822701
    private final HashMap<String, Packet> packets = new HashMap<String, Packet>();
    private final TreeMap<Integer, String> packetsByNumber = new TreeMap<Integer, String>();

    public PacketsStore(boolean hasTwoBytePacketNumber) {
        this.hasTwoBytePacketNumber = hasTwoBytePacketNumber;
    }

    public void registerTypeAlias(String alias, String aliased) throws UndefinedException {
        final FieldTypeBasic basicFieldType = Hardcoded.getBasicFieldType(aliased);
        Requirement req = new Requirement(aliased, Requirement.Kind.FIELD_TYPE);
        if (null != basicFieldType) {
            requirements.addPossibleRequirement(basicFieldType.createFieldType(alias));
        } else if (requirements.isAwareOfPotentialProvider(req)) {
            requirements.addPossibleRequirement(((FieldTypeAlias)requirements.getPotentialProvider(req))
                    .getBasicType().createFieldType(alias));
        } else {
            notFoundWhenNeeded.add(req);
        }
    }

    public boolean hasTypeAlias(String name) {
        return requirements.isAwareOfPotentialProvider(new Requirement(name, Requirement.Kind.FIELD_TYPE));
    }

    public void registerPacket(String name, int number, List<Field.WeakField> fields) throws PacketCollisionException, UndefinedException {
        if (hasPacket(name)) {
            throw new PacketCollisionException("Packet name " + name + " already in use");
        } else if (hasPacket(number)) {
            throw new PacketCollisionException("Packet number " + number + " already in use");
        }

        List<Field> fieldList = new LinkedList<Field>();
        for (Field.WeakField fieldType: fields) {
            Requirement req = new Requirement(fieldType.getType(), Requirement.Kind.FIELD_TYPE);
            if (!requirements.isAwareOfPotentialProvider(req)) {
                notFoundWhenNeeded.add(req);
                return;
            }
            fieldList.add(new Field(fieldType.getName(), ((FieldTypeAlias)requirements.getPotentialProvider(req)),
                    fieldType.getDeclarations()));
        }

        Packet packet = new Packet(name, number, hasTwoBytePacketNumber, fieldList.toArray(new Field[0]));
        requirements.addWanted(packet);
        packets.put(name, packet);
        packetsByNumber.put(number, name);
    }

    public boolean hasPacket(String name) {
        return packets.containsKey(name);
    }

    public boolean hasPacket(int number) {
        return packetsByNumber.containsKey(number);
    }

    public Packet getPacket(String name) {
        return packets.get(name);
    }

    public Collection<ClassWriter> getJavaCode() {
        Collection<IDependency> inn = requirements.getResolved();
        HashSet<ClassWriter> out = new HashSet<ClassWriter>();

        ClassWriter constants =
                new ClassWriter(org.freeciv.packet.Packet.class.getPackage(), new String[0],
                        "Freeciv C code", "Constants", null);

        for (IDependency dep : inn)
            if (dep instanceof ClassWriter)
                out.add((ClassWriter) dep);
            else if (dep instanceof Constant)
                constants.addClassConstant(ClassWriter.Visibility.PUBLIC, "int",
                        ((Constant) dep).getName(), ((Constant) dep).getExpression());

        if (out.size() < inn.size())
            out.add(constants);

        return out;
    }

    public Collection<Requirement> getUnsolvedRequirements() {
        TreeSet<Requirement> out = new TreeSet<Requirement>(requirements.getMissingRequirements());
        out.addAll(notFoundWhenNeeded);
        return out;
    }

    public String getPacketList() {
        String out = "";

        for (int number: packetsByNumber.keySet()) {
            Packet packet = packets.get(packetsByNumber.get(number));
            if (requirements.dependenciesFound(packet))
                out += packet.getNumber() + "\t" + packet.getPackage() + "." + packet.getName() + "\n";
        }
        return out;
    }

    public void addDependency(IDependency fulfillment) {
        if (fulfillment instanceof Enum)
            for (IDependency constant : ((Enum) fulfillment).getEnumConstants())
                requirements.addPossibleRequirement(constant);
        requirements.addPossibleRequirement(fulfillment);
    }
}
