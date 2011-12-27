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
    private final boolean devMode;
    private final boolean hasTwoBytePacketNumber;

    private final HashMap<String, FieldTypeAlias> types = new HashMap<String, FieldTypeAlias>();
    private final HashMap<String, ClassWriter> requirements = new HashMap<String, ClassWriter>();
    private final Hardcoded hardcoded = new Hardcoded();

    // To avoid duplication of structures have packets store the packets and packetsByNumber translate the keys
    // Idea from http://stackoverflow.com/q/822701
    private final HashMap<String, Packet> packets = new HashMap<String, Packet>();
    private final HashMap<Integer, String> packetsByNumber = new HashMap<Integer, String>();

    public PacketsStore(boolean devMode, boolean hasTwoBytePacketNumber) {
        this.devMode = devMode;
        this.hasTwoBytePacketNumber = hasTwoBytePacketNumber;
    }

    public void registerTypeAlias(String alias, String aliased) throws UndefinedException {
        final FieldTypeBasic basicFieldType = hardcoded.getBasicFieldType(aliased);
        if (null != basicFieldType) {
            if (basicFieldType.hasRequired())
                types.put(alias, basicFieldType.createFieldType(alias));
            else
                skipOrCrash("Required type " + basicFieldType.getPublicType() + " used in " + aliased + " not found.");
        } else if (types.containsKey(aliased)) {
            types.put(alias, types.get(aliased).getBasicType().createFieldType(alias));
        } else {
            skipOrCrash(aliased + " not declared before used in " + alias + ".");
        }
    }

    private void skipOrCrash(String errorMessage) throws UndefinedException {
        if (devMode) {
            System.err.println(errorMessage);
            System.err.println("Skipping since in development mode...");
        } else {
            throw new UndefinedException(errorMessage);
        }
    }

    public boolean hasTypeAlias(String name) {
        return types.containsKey(name);
    }

    public void registerPacket(String name, int number) throws PacketCollisionException, UndefinedException {
        registerPacket(name, number, new LinkedList<String[]>());
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
            if (!types.containsKey(fieldType[0])) {
                skipOrCrash("Field type " + fieldType[0] + " not declared before use in packet " + name + ".");
                return;
            }
            LinkedList<Field.ArrayDeclaration> declarations = new LinkedList<Field.ArrayDeclaration>();
            for (int i = 2; i < fieldType.length; i += 2) {
                declarations.add(new Field.ArrayDeclaration(fieldType[i], fieldType[i + 1]));
            }
            fieldList.add(new Field(fieldType[1], types.get(fieldType[0]),
                    declarations.toArray(new Field.ArrayDeclaration[0])));
        }

        packets.put(name, new Packet(name, number, hasTwoBytePacketNumber, fieldList.toArray(new Field[0])));
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
        ArrayList<ClassWriter> out = new ArrayList<ClassWriter>(types.values());
        out.addAll(requirements.values());
        out.addAll(packets.values());
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

    public void addRequirement(String publicType, ClassWriter requirement) {
        requirements.put(publicType, requirement);
        for (FieldTypeBasic basicType: hardcoded.values()) {
            if (basicType.getPublicType().equals(publicType)) basicType.requirementsFound();
        }
    }
}
