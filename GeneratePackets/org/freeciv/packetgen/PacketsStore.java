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
    private final HashMap<String, Requirement> packets = new HashMap<String, Requirement>();
    private final TreeMap<Integer, String> packetsByNumber = new TreeMap<Integer, String>();

    @Deprecated public PacketsStore(int bytesInPacketNumber) {
        this(bytesInPacketNumber, GeneratorDefaults.LOG_TO);
    }

    public PacketsStore(int bytesInPacketNumber, String logger) {
        requirements = new DependencyStore();
        for (IDependency primitive : Hardcoded.values()) {
            requirements.addPossibleRequirement(primitive);
        }

        requirements.addMaker(new FieldAliasArrayMaker());

        this.logger = logger;

        switch (bytesInPacketNumber) {
            case 1:
                packetHeaderType = TargetClass.newKnown(Header_2_1.class);
                break;
            case 2:
                packetHeaderType = TargetClass.newKnown(Header_2_2.class);
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
                return ((FieldTypeAlias) wasRequired[0]).getBasicType().createFieldType(alias);
            }
        });
    }

    public boolean doesFieldTypeAliasResolve(String name) {
        return requirements.isAwareOfProvider(new Requirement(name, FieldTypeBasic.FieldTypeAlias.class));
    }

    public void registerPacket(final String name, final int number, List<WeakFlag> flags, final List<WeakField> fields)
            throws PacketCollisionException, UndefinedException {
        validateNameAndNumber(name, number);

        Requirement me = new Requirement(name, Packet.class);
        reserveNameAndNumber(name, number, me);

        final List<Annotate> packetFlags = extractFlags(flags);
        List<Requirement> allNeeded = extractFieldRequirements(fields);

        requirements.addMaker(new IDependency.Maker.Simple(me, allNeeded.toArray(new Requirement[allNeeded.size()])) {
            @Override
            public IDependency produce(Requirement toProduce, IDependency... wasRequired) throws UndefinedException {
                assert wasRequired.length == fields.size() : "Wrong number of arguments";
                List<Field> fieldList = new LinkedList<Field>();
                for (int i = 0; i < fields.size(); i++) {
                    WeakField fieldType = fields.get(i);
                    fieldList.add(new Field(fieldType.getName(),
                            (FieldTypeAlias)wasRequired[i],
                            name,
                            fieldType.getFlags(),
                            fieldType.getDeclarations()));
                }

                return new Packet(name, number, packetHeaderType, logger, packetFlags, fieldList.toArray(new Field[0]));
            }
        });

        requirements.demand(me);
    }

    private void validateNameAndNumber(String name, int number) throws PacketCollisionException {
        if (hasPacket(name)) {
            throw new PacketCollisionException("Packet name " + name + " already in use");
        } else if (hasPacket(number)) {
            throw new PacketCollisionException("Packet number " + number + " already in use");
        }
    }

    private void reserveNameAndNumber(String name, int number, Requirement packetID) {
        packets.put(name, packetID);
        packetsByNumber.put(number, name);
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

    private List<Requirement> extractFieldRequirements(List<WeakField> fields) {
        LinkedList<Requirement> allNeeded = new LinkedList<Requirement>();
        for (WeakField fieldType : fields) {
            String type = fieldType.getType();

            if (0 < fieldType.getDeclarations().length)
                type = type + "_" + fieldType.getDeclarations().length;

            allNeeded.add(new Requirement(type, FieldTypeAlias.class));
        }
        return allNeeded;
    }

    public boolean hasPacket(String name) {
        return packets.containsKey(name);
    }

    public boolean hasPacket(int number) {
        return packetsByNumber.containsKey(number);
    }

    public Packet getPacket(String name) {
        return (Packet)requirements.getPotentialProvider(packets.get(name));
    }

    public Packet getPacket(int number) {
        return getPacket(packetsByNumber.get(number));
    }

    public Collection<ClassWriter> getJavaCode() {
        Collection<IDependency> inn = requirements.getResolved();
        HashSet<ClassWriter> out = new HashSet<ClassWriter>();

        TreeMap<Integer, String> resolvedPackets = new TreeMap<Integer, String>();
        TreeSet<Constant> sortedConstants =
                new TreeSet<Constant>(new TotalOrderNoCircles(inn));

        for (IDependency dep : inn)
            if (dep instanceof ClassWriter) {
                out.add((ClassWriter) dep);
                if (dep instanceof Packet) {
                    resolvedPackets.put(((Packet) dep).getNumber(),
                            ((Packet) dep).getPackage() + "." + ((Packet) dep).getName());
                }
            } else if (dep instanceof Constant)
                sortedConstants.add((Constant)dep);

        out.add(generateVersionData(resolvedPackets, sortedConstants));

        return out;
    }

    static ClassWriter generateVersionData(TreeMap<Integer, String> packets, Set<Constant> constants) {
        int border = Util.VERSION_DATA_CLASS.lastIndexOf('.');
        ClassWriter versionData =
                new ClassWriter(ClassKind.CLASS, TargetPackage.from(Util.VERSION_DATA_CLASS.substring(0, border)), null,
                        "Freeciv C code", Collections.<Annotate>emptyList(), Util.VERSION_DATA_CLASS.substring(border + 1),
                        ClassWriter.DEFAULT_PARENT, Collections.<TargetClass>emptyList());

        for (Constant dep : constants)
            versionData.addField(dep);

        versionData.addClassConstant(Visibility.PUBLIC, "String[]",
                Util.PACKET_MAP_NAME, new ArrayLiteral(formatPacketMap(packets)));

        return versionData;
    }

    private static Typed<AString>[] formatPacketMap(TreeMap<Integer, String> pNumToPName) {
        if (pNumToPName.isEmpty())
            return new Typed[0];

        Typed<AString> raw = BuiltIn.literal(RawPacket.class.getCanonicalName());

        Typed<AString>[] packets = new Typed[pNumToPName.lastKey() + 1];
        for (int packetNumber = 0; packetNumber <= pNumToPName.lastKey(); packetNumber++)
            if (pNumToPName.containsKey(packetNumber))
                packets[packetNumber] = BuiltIn.literal(pNumToPName.get(packetNumber));
            else
                packets[packetNumber] = raw;
        return packets;
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
