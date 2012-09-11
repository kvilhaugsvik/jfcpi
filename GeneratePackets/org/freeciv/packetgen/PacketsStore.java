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

import org.freeciv.Util;
import org.freeciv.packet.*;
import org.freeciv.packetgen.enteties.FieldTypeBasic.FieldTypeAlias;
import org.freeciv.packetgen.dependency.*;
import org.freeciv.packetgen.enteties.*;
import org.freeciv.packetgen.enteties.Enum;
import org.freeciv.packetgen.enteties.Packet;
import org.freeciv.packetgen.enteties.supporting.*;
import org.freeciv.packetgen.javaGenerator.*;
import org.freeciv.packetgen.javaGenerator.expression.ArrayLiteral;
import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AString;

import java.util.*;

public class PacketsStore {
    private final TargetClass packetHeaderType;
    private final String logger;

    private final DependencyStore requirements;

    // To avoid duplication of structures have packets store the packets and packetsByNumber translate the keys
    // Idea from http://stackoverflow.com/q/822701
    private final HashMap<String, Packet> packets = new HashMap<String, Packet>();
    private final TreeMap<Integer, String> packetsByNumber = new TreeMap<Integer, String>();

    @Deprecated public PacketsStore(int bytesInPacketNumber) {
        this(bytesInPacketNumber, GeneratorDefaults.LOG_TO);
    }

    public PacketsStore(int bytesInPacketNumber, String logger) {
        requirements = new DependencyStore();
        for (IDependency primitive : Hardcoded.values()) {
            requirements.addPossibleRequirement(primitive);
        }

        this.logger = logger;

        switch (bytesInPacketNumber) {
            case 1:
                packetHeaderType = new TargetClass(Header_2_1.class, true);
                break;
            case 2:
                packetHeaderType = new TargetClass(Header_2_2.class, true);
                break;
            default: throw new IllegalArgumentException("No other sizes than one or two bytes are supported" +
                                                                "for packet kind field in packet header");
        }

        requirements.addWanted(new Constant(Util.PACKET_NUMBER_SIZE_NAME,
                                            IntExpression.integer(bytesInPacketNumber + "")));
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

    public void registerPacket(String name, int number, List<WeakFlag> flags, List<WeakField> fields)
            throws PacketCollisionException, UndefinedException {
        if (hasPacket(name)) {
            throw new PacketCollisionException("Packet name " + name + " already in use");
        } else if (hasPacket(number)) {
            throw new PacketCollisionException("Packet number " + number + " already in use");
        }

        List<Annotate> packetFlags = new LinkedList<Annotate>();
        byte sentBy = 0;
        LinkedList<String> canceled = new LinkedList<String>();
        for (WeakFlag flag : flags) {
            if ("sc".equals(flag.getName()))
                sentBy += 2;
            else if ("cs".equals(flag.getName()))
                sentBy += 1;
            else if ("no-delta".equals(flag.getName()))
                packetFlags.add(new Annotate(NoDelta.class.getSimpleName()));
            else if ("is-info".equals(flag.getName()))
                packetFlags.add(new Annotate(IsInfo.class.getSimpleName()));
            else if ("is-game-info".equals(flag.getName()))
                packetFlags.add(new Annotate(IsGameInfo.class.getSimpleName()));
            else if ("force".equals(flag.getName()))
                packetFlags.add(new Annotate(Force.class.getSimpleName()));
            else if ("cancel".equals(flag.getName()))
                canceled.add(flag.getArguments()[0]);

        }
        if (!canceled.isEmpty()) packetFlags.add(new Canceler(canceled));
        packetFlags.add(new Sender(sentBy));

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
                                        fieldType.getFlags(),
                                        fieldType.getDeclarations()));
            } else
                missingWhenNeeded.add(req);
        }

        if (missingWhenNeeded.isEmpty()) {
            Packet packet = new Packet(name, number, packetHeaderType, logger,
                                       packetFlags, fieldList.toArray(new Field[0]));
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

        int border = Util.VERSION_DATA_CLASS.lastIndexOf('.');
        ClassWriter constants =
                new ClassWriter(ClassKind.CLASS, new TargetPackage(Util.VERSION_DATA_CLASS.substring(0, border)), null,
                        "Freeciv C code", Collections.<Annotate>emptyList(), Util.VERSION_DATA_CLASS.substring(border + 1),
                        null, null);

        for (Constant dep : sortedConstants)
            constants.addField(dep);

        Typed<AString>[] understandsPackets;
        if (packetsByNumber.isEmpty()) {
            understandsPackets = new Typed[0];
        } else {
            understandsPackets = new Typed[packetsByNumber.lastKey() + 1];
            for (int number = 0; number <= packetsByNumber.lastKey(); number++) {
                if (packetsByNumber.containsKey(number) && requirements.dependenciesFound(packets.get(packetsByNumber.get(number)))) {
                    Packet packet = packets.get(packetsByNumber.get(number));
                    understandsPackets[number] = BuiltIn.asAString("\"" + packet.getPackage() + "." + packet.getName() + "\"");
                } else {
                    understandsPackets[number] = BuiltIn.asAString("\"" + org.freeciv.packet.RawPacket.class.getCanonicalName() + "\""); // DEVMODE is handled elsewhere
                }
            }
        }
        constants.addClassConstant(Visibility.PUBLIC, "String[]", Util.PACKET_MAP_NAME,
                                   new ArrayLiteral(understandsPackets));

        out.add(constants);

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

    public void requestConstant(String constant) {
        requirements.demand(new Requirement(constant, Requirement.Kind.VALUE));
    }

    public void requestType(String type) {
        requirements.demand(new Requirement(type, Requirement.Kind.AS_JAVA_DATATYPE));
    }
}
