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
    private final HashMap<Integer, String> packetsByNumber = new HashMap<Integer, String>();

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

    public void registerPacket(String name, int number, List<String[]> fields) throws PacketCollisionException, UndefinedException {
        if (packets.containsKey(name)) {
            throw new PacketCollisionException("Packet name " + name + " already in use");
        } else if (packetsByNumber.containsKey(number)) {
            throw new PacketCollisionException("Packet number " + number + " already in use");
        }

        List<Field> fieldList = new LinkedList<Field>();
        for (String[] fieldType: fields) {
            assert (1 != (fieldType.length % 2));
            Requirement req = new Requirement(fieldType[0], Requirement.Kind.FIELD_TYPE);
            if (!requirements.isAwareOfPotentialProvider(req)) {
                notFoundWhenNeeded.add(req);
                return;
            }
            LinkedList<Field.ArrayDeclaration> declarations = new LinkedList<Field.ArrayDeclaration>();
            for (int i = 2; i < fieldType.length; i += 2) {
                declarations.add(new Field.ArrayDeclaration(fieldType[i], fieldType[i + 1]));
            }
            fieldList.add(new Field(fieldType[1], ((FieldTypeAlias)requirements.getPotentialProvider(req)),
                    declarations.toArray(new Field.ArrayDeclaration[0])));
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
        HashSet<ClassWriter> out = new HashSet<ClassWriter>();
        for (IDependency dep : requirements.getResolved())
            out.add((ClassWriter) dep);
        return out;
    }

    public Collection<Requirement> getUnsolvedRequirements() {
        HashSet<Requirement> out = new HashSet<Requirement>(requirements.getMissingRequirements());
        out.addAll(notFoundWhenNeeded);
        return out;
    }

    public String getPacketList() {
        String out = "";

        for (int number: packetsByNumber.keySet()) {
            Packet packet = packets.get(packetsByNumber.get(number));
            out += packet.getNumber() + "\t" + packet.getPackage() + "." + packet.getName() + "\n";
        }
        return out;
    }

    public void addDependency(IDependency fulfillment) {
        requirements.addPossibleRequirement(fulfillment);
    }
}
