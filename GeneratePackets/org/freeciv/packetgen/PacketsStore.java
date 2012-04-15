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

import org.freeciv.packetgen.enteties.FieldTypeBasic.FieldTypeAlias;
import org.freeciv.packetgen.dependency.*;
import org.freeciv.packetgen.enteties.*;
import org.freeciv.packetgen.enteties.Enum;
import org.freeciv.packetgen.enteties.supporting.*;
import org.freeciv.packetgen.javaGenerator.ClassWriter;

import java.util.*;

public class PacketsStore {
    private final boolean hasTwoBytePacketNumber;

    private final DependencyStore requirements;

    // To avoid duplication of structures have packets store the packets and packetsByNumber translate the keys
    // Idea from http://stackoverflow.com/q/822701
    private final HashMap<String, Packet> packets = new HashMap<String, Packet>();
    private final TreeMap<Integer, String> packetsByNumber = new TreeMap<Integer, String>();

    public PacketsStore(boolean hasTwoBytePacketNumber) {
        this.hasTwoBytePacketNumber = hasTwoBytePacketNumber;
        requirements = new DependencyStore();
        for (IDependency primitive : Hardcoded.values()) {
            requirements.addPossibleRequirement(primitive);
        }
    }

    private FieldTypeBasic tryToCreatePrimitive(String iotype, String ptype, Requirement neededBasic) {
        Requirement wantPType = new Requirement(ptype, Requirement.Kind.AS_JAVA_DATATYPE);
        if (!requirements.isAwareOfPotentialProvider(wantPType)) {
            return failAndBlame(wantPType, neededBasic, ptype);
        }
        IDependency pubType = requirements.getPotentialProvider(wantPType);
        if (!(pubType instanceof FieldTypeBasic.Generator)) {
            return failAndBlame(wantPType, neededBasic, ptype);
        }

        Requirement wantIOType = new Requirement(iotype, ((FieldTypeBasic.Generator)pubType).needsDataInFormat());
        if (!requirements.isAwareOfPotentialProvider(wantIOType)) {
            return failAndBlame(wantIOType, neededBasic, ptype);
        }
        IDependency ioType = requirements.getPotentialProvider(wantIOType);
        if (!(ioType instanceof NetworkIO)) {
            return failAndBlame(wantIOType, neededBasic, ptype);
        }

        FieldTypeBasic basicFieldType = ((FieldTypeBasic.Generator)pubType).getBasicFieldTypeOnInput((NetworkIO)ioType);
        requirements.addPossibleRequirement(basicFieldType);
        return basicFieldType;
    }

    private FieldTypeBasic failAndBlame(Requirement missing, Requirement wanted, String ptype) {
        requirements.addPossibleRequirement(new NotCreated(
                wanted,
                Arrays.asList(new Requirement(ptype, Requirement.Kind.AS_JAVA_DATATYPE), missing),
                Arrays.asList(missing)));
        return null;
    }

    public void registerTypeAlias(String alias, String iotype, String ptype) throws UndefinedException {
        Requirement neededBasic = new Requirement(iotype + "(" + ptype + ")", Requirement.Kind.PRIMITIVE_FIELD_TYPE);
        FieldTypeBasic basicFieldType = (FieldTypeBasic)requirements.getPotentialProvider(neededBasic);

        if (null == basicFieldType)
            basicFieldType = tryToCreatePrimitive(iotype, ptype, neededBasic);

        if (null == basicFieldType)
            requirements.addPossibleRequirement(new NotCreated(
                    new Requirement(alias, Requirement.Kind.FIELD_TYPE), Arrays.asList(neededBasic)));
        else
            requirements.addPossibleRequirement(basicFieldType.createFieldType(alias));
    }

    public void registerTypeAlias(String alias, String aliased) throws UndefinedException {
        Requirement req = new Requirement(aliased, Requirement.Kind.FIELD_TYPE);
        if (requirements.isAwareOfPotentialProvider(req)) {
            requirements.addPossibleRequirement(((FieldTypeAlias)requirements.getPotentialProvider(req))
                                                        .getBasicType().createFieldType(alias));
        } else {
            requirements.addPossibleRequirement(new NotCreated(
                    new Requirement(alias, Requirement.Kind.FIELD_TYPE), Arrays.asList(req)));
        }
    }

    public boolean hasTypeAlias(String name) {
        return requirements.isAwareOfPotentialProvider(new Requirement(name, Requirement.Kind.FIELD_TYPE));
    }

    public void registerPacket(String name, int number, List<WeakField> fields)
            throws PacketCollisionException, UndefinedException {
        if (hasPacket(name)) {
            throw new PacketCollisionException("Packet name " + name + " already in use");
        } else if (hasPacket(number)) {
            throw new PacketCollisionException("Packet number " + number + " already in use");
        }

        List<Field> fieldList = new LinkedList<Field>();
        HashSet<Requirement> allNeeded = new HashSet<Requirement>();
        HashSet<Requirement> missingWhenNeeded = new HashSet<Requirement>();
        for (WeakField fieldType : fields) {
            Requirement req = new Requirement(fieldType.getType(), Requirement.Kind.FIELD_TYPE);
            allNeeded.add(req);
            if (requirements.isAwareOfPotentialProvider(req) &&
                    requirements.getPotentialProvider(req) instanceof FieldTypeAlias) {
                LinkedList<Field.ArrayDeclaration> declarations = new LinkedList<Field.ArrayDeclaration>();
                for (WeakField.ArrayDeclaration weak : fieldType.getDeclarations()) {
                    declarations.add(new Field.ArrayDeclaration(weak.maxSize, weak.elementsToTransfer));
                }
                fieldList.add(new Field(fieldType.getName(),
                                        (FieldTypeAlias)requirements.getPotentialProvider(req),
                                        declarations.toArray(new Field.ArrayDeclaration[0])));
            } else
                missingWhenNeeded.add(req);
        }

        if (missingWhenNeeded.isEmpty()) {
            Packet packet = new Packet(name, number, hasTwoBytePacketNumber, fieldList.toArray(new Field[0]));
            requirements.addWanted(packet);
            packets.put(name, packet);
            packetsByNumber.put(number, name);
        } else {
            requirements.addWanted(
                    new NotCreated(new Requirement(name, Requirement.Kind.PACKET), allNeeded, missingWhenNeeded));
        }
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

        TreeSet<Constant> sortedConstants =
                new TreeSet<Constant>(new IDependency.TotalOrderNoCircles(inn));

        for (IDependency dep : inn)
            if (dep instanceof ClassWriter)
                out.add((ClassWriter)dep);
            else if (dep instanceof Constant)
                sortedConstants.add((Constant)dep);

        if (out.size() < inn.size()) {
            int border = GeneratorDefaults.CONSTANT_LOCATION.lastIndexOf('.');
            ClassWriter constants =
                    new ClassWriter(ClassWriter.ClassKind.CLASS, new ClassWriter.TargetPackage(
                            GeneratorDefaults.CONSTANT_LOCATION.substring(0, border)), new String[0],
                                    "Freeciv C code", GeneratorDefaults.CONSTANT_LOCATION.substring(border + 1),
                                    null, null);

            for (Constant dep : sortedConstants)
                constants.addClassConstant(ClassWriter.Visibility.PUBLIC, "int", dep.getName(), dep.getExpression());

            out.add(constants);
        }

        return out;
    }

    public Collection<Requirement> getUnsolvedRequirements() {
        TreeSet<Requirement> out = new TreeSet<Requirement>(requirements.getMissingRequirements());
        return out;
    }

    public String getPacketList() {
        String out = "";

        for (int number : packetsByNumber.keySet()) {
            Packet packet = packets.get(packetsByNumber.get(number));
            if (requirements.dependenciesFound(packet))
                out += packet.getNumber() + "\t" + packet.getPackage() + "." + packet.getName() + "\n";
        }
        return out;
    }

    public void addDependency(IDependency fulfillment) {
        if (fulfillment instanceof Enum)
            for (IDependency constant : ((Enum)fulfillment).getEnumConstants())
                requirements.addPossibleRequirement(constant);
        requirements.addPossibleRequirement(fulfillment);
    }
}
