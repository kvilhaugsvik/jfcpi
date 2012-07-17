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

import org.freeciv.packet.Header_2_1;
import org.freeciv.packet.Header_2_2;
import org.freeciv.packetgen.enteties.FieldTypeBasic.FieldTypeAlias;
import org.freeciv.packetgen.dependency.*;
import org.freeciv.packetgen.enteties.*;
import org.freeciv.packetgen.enteties.Enum;
import org.freeciv.packetgen.enteties.supporting.*;
import org.freeciv.packetgen.javaGenerator.ClassWriter;
import org.freeciv.packetgen.javaGenerator.TargetPackage;
import org.freeciv.packetgen.javaGenerator.expression.StringTyped;

import java.util.*;

public class PacketsStore {
    private final String packetHeaderType;

    private final DependencyStore requirements;

    // To avoid duplication of structures have packets store the packets and packetsByNumber translate the keys
    // Idea from http://stackoverflow.com/q/822701
    private final HashMap<String, Packet> packets = new HashMap<String, Packet>();
    private final TreeMap<Integer, String> packetsByNumber = new TreeMap<Integer, String>();

    public PacketsStore(int bytesInPacketNumber) {
        requirements = new DependencyStore();
        for (IDependency primitive : Hardcoded.values()) {
            requirements.addPossibleRequirement(primitive);
        }

        switch (bytesInPacketNumber) {
            case 1:
                packetHeaderType = Header_2_1.class.getCanonicalName();
                break;
            case 2:
                packetHeaderType = Header_2_2.class.getCanonicalName();
                break;
            default: throw new IllegalArgumentException("No other sizes than one or two bytes are supported" +
                                                                "for packet kind field in packet header");
        }

        requirements.addWanted(new Constant("networkHeaderPacketNumberBytes",
                IntExpression.integer(bytesInPacketNumber + "")));

        requirements.addWanted(new Constant("NETWORK_CAPSTRING_MANDATORY", new StringTyped("\"+Freeciv.Devel-2.5-2012.Jun.28-2\"")));
        requirements.addWanted(new Constant("NETWORK_CAPSTRING_OPTIONAL", new StringTyped("\"\"")));
        requirements.addWanted(new Constant("VERSION_LABEL", new StringTyped("\"-dev\"")));
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
                fieldList.add(new Field(fieldType.getName(),
                                        (FieldTypeAlias)requirements.getPotentialProvider(req),
                                        name,
                                        fieldType.getDeclarations()));
            } else
                missingWhenNeeded.add(req);
        }

        if (missingWhenNeeded.isEmpty()) {
            Packet packet = new Packet(name, number, packetHeaderType, fieldList.toArray(new Field[0]));
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
                    new ClassWriter(ClassWriter.ClassKind.CLASS, new TargetPackage(
                            GeneratorDefaults.CONSTANT_LOCATION.substring(0, border)), new String[0],
                                    "Freeciv C code", GeneratorDefaults.CONSTANT_LOCATION.substring(border + 1),
                                    null, null);

            for (Constant dep : sortedConstants)
                constants.addClassConstant(ClassWriter.Visibility.PUBLIC, dep.getType(), dep.getName(), dep.getExpression());

            out.add(constants);
        }

        int border = GeneratorDefaults.VERSION_DATA_LOCATION.lastIndexOf('.');
        ClassWriter version = new ClassWriter(
                ClassWriter.ClassKind.CLASS,
                new TargetPackage(GeneratorDefaults.VERSION_DATA_LOCATION.substring(0, border)),
                new String[0],
                "extracted and hard coded Freeciv data",
                GeneratorDefaults.VERSION_DATA_LOCATION.substring(border + 1),
                null, null);

        version.addClassConstant(ClassWriter.Visibility.PUBLIC, "long", "MAJOR_VERSION", "2");
        version.addClassConstant(ClassWriter.Visibility.PUBLIC, "long", "MINOR_VERSION", "4");
        version.addClassConstant(ClassWriter.Visibility.PUBLIC, "long", "PATCH_VERSION", "99");

        String[] understandsPackets;
        if (packetsByNumber.isEmpty()) {
            understandsPackets = new String[0];
        } else {
            understandsPackets = new String[packetsByNumber.lastKey() + 1];
            for (int number = 0; number <= packetsByNumber.lastKey(); number++) {
                if (packetsByNumber.containsKey(number) && requirements.dependenciesFound(packets.get(packetsByNumber.get(number)))) {
                    Packet packet = packets.get(packetsByNumber.get(number));
                    understandsPackets[number] = "\"" + packet.getPackage() + "." + packet.getName() + "\"";
                } else {
                    understandsPackets[number] = "\"" + org.freeciv.packet.RawPacket.class.getCanonicalName() + "\""; // DEVMODE is handled elsewhere
                }
            }
        }
        version.addClassConstant(ClassWriter.Visibility.PUBLIC, "String[]", "understandsPackets", org.freeciv.Util.joinStringArray(understandsPackets, ",\n\t", "{", "}"));

        out.add(version);

        return out;
    }

    public Collection<Requirement> getUnsolvedRequirements() {
        TreeSet<Requirement> out = new TreeSet<Requirement>(requirements.getMissingRequirements());
        return out;
    }

    public void addDependency(IDependency fulfillment) {
        if (fulfillment instanceof Enum)
            for (IDependency constant : ((Enum)fulfillment).getEnumConstants())
                requirements.addPossibleRequirement(constant);
        requirements.addPossibleRequirement(fulfillment);
    }
}
