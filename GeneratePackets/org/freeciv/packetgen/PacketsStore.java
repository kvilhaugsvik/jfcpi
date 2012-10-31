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

        requirements.addWanted(Constant.isInt(Util.PACKET_NUMBER_SIZE_NAME,
                                            IntExpression.integer(bytesInPacketNumber + "")));
    }

    public void registerTypeAlias(final String alias, String iotype, String ptype) throws UndefinedException {
        Requirement basic = new Requirement(iotype + "(" + ptype + ")", FieldTypeBasic.class);
        requirements.addMaker(new IDependency.Maker.Simple(new Requirement(alias, FieldTypeAlias.class), basic) {
            @Override
            public IDependency produce(Requirement toProduce, IDependency... wasRequired) throws UndefinedException {
                return ((FieldTypeBasic)wasRequired[0]).createFieldType(alias);
            }
        });
        requirements.blameMissingOn(basic,
                new Requirement(ptype, DataType.class), new Requirement(iotype, NetworkIO.class));
    }

    public void registerTypeAlias(final String alias, String aliased) throws UndefinedException {
        requirements.addMaker(new IDependency.Maker.Simple(new Requirement(alias, FieldTypeAlias.class),
                new Requirement(aliased, FieldTypeAlias.class)) {
            @Override
            public IDependency produce(Requirement toProduce, IDependency... wasRequired) throws UndefinedException {
                return ((FieldTypeAlias)wasRequired[0]).getBasicType().createFieldType(alias);
            }
        });
    }

    public boolean doesFieldTypeAliasResolve(String name) {
        return requirements.isAwareOfProvider(new Requirement(name, FieldTypeBasic.FieldTypeAlias.class));
    }

    public void registerPacket(String name, int number, List<WeakFlag> flags, List<WeakField> fields)
            throws PacketCollisionException, UndefinedException {
        validateNameAndNumber(name, number);
        List<Annotate> packetFlags = extractFlags(flags);

        List<Field> fieldList = new LinkedList<Field>();
        HashSet<Requirement> allNeeded = new HashSet<Requirement>();
        HashSet<Requirement> missingWhenNeeded = new HashSet<Requirement>();
        for (WeakField fieldType : fields) {
            Requirement req = new Requirement(fieldType.getType(), FieldTypeBasic.FieldTypeAlias.class);
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
                    new NotCreated(new Requirement(name, Packet.class), allNeeded, missingWhenNeeded));
        }
    }

    private List<Annotate> extractFlags(List<WeakFlag> flags) {
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
        return packetFlags;
    }

    private void validateNameAndNumber(String name, int number) throws PacketCollisionException {
        if (hasPacket(name)) {
            throw new PacketCollisionException("Packet name " + name + " already in use");
        } else if (hasPacket(number)) {
            throw new PacketCollisionException("Packet number " + number + " already in use");
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
                new TreeSet<Constant>(new TotalOrderNoCircles(inn));

        for (IDependency dep : inn)
            if (dep instanceof ClassWriter)
                out.add((ClassWriter)dep);
            else if (dep instanceof Constant)
                sortedConstants.add((Constant)dep);

        int border = Util.VERSION_DATA_CLASS.lastIndexOf('.');
        ClassWriter constants =
                new ClassWriter(ClassKind.CLASS, new TargetPackage(Util.VERSION_DATA_CLASS.substring(0, border)), null,
                        "Freeciv C code", Collections.<Annotate>emptyList(), Util.VERSION_DATA_CLASS.substring(border + 1),
                        TargetClass.fromName(null), Collections.<TargetClass>emptyList());

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
                    understandsPackets[number] = BuiltIn.literal(packet.getPackage() + "." + packet.getName());
                } else {
                    understandsPackets[number] = BuiltIn.literal(RawPacket.class.getCanonicalName()); // DEVMODE is handled elsewhere
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
        requirements.demand(new Requirement(constant, Constant.class));
    }

    public void requestType(String type) {
        requirements.demand(new Requirement(type, DataType.class));
    }
}
