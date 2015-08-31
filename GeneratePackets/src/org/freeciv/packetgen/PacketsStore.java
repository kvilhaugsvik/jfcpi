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

import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import org.freeciv.utility.Util;
import org.freeciv.connection.ReflexRuleTime;
import org.freeciv.packet.*;
import com.kvilhaugsvik.dependency.*;
import org.freeciv.packetgen.enteties.*;
import org.freeciv.packetgen.enteties.Enum;
import org.freeciv.packetgen.enteties.Packet;
import org.freeciv.packetgen.enteties.supporting.*;
import com.kvilhaugsvik.javaGenerator.*;
import com.kvilhaugsvik.javaGenerator.expression.ArrayLiteral;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AValue;

import java.util.*;

public class PacketsStore {
    private final String configName;
    private final TargetClass packetHeaderType;

    private final DependencyStore requirements;

    // To avoid duplication of structures have packets store the packets and packetsByNumber translate the keys
    // Idea from http://stackoverflow.com/q/822701
    private final HashMap<String, Requirement> packets = new HashMap<String, Requirement>();
    private final TreeMap<Integer, String> packetsByNumber = new TreeMap<Integer, String>();

    public PacketsStore(String configName,
                        PacketHeaderKinds bytesInPacketNumber, Map<String, String> fieldTypeAliases,
                        boolean enableDelta, boolean enableDeltaBoolFolding) {
        requirements = new DependencyStore();
        for (Dependency.Item primitive : Hardcoded.values()) {
            requirements.addPossibleRequirement(primitive);
        }
        for (Dependency.Maker maker : Hardcoded.makers()) {
            requirements.addMaker(maker);
        }

        /* Field type aliases from the configuration. */
        for (String from : fieldTypeAliases.keySet()) {
            requirements.addMaker(new Wrapper(from, FieldType.class,
                    new Requirement(fieldTypeAliases.get(from), FieldType.class)));
        }

        /* Generic makers. */
        requirements.addMaker(new FieldAliasArrayMaker());
        requirements.addMaker(new DiffArrayElementDataType());
        requirements.addMaker(new DiffArrayElementFieldType());

        this.configName = configName;

        final TargetClass rule = TargetClass.from(org.freeciv.connection.ReflexRule.class);
        final Typed[] protoRules;
        switch (bytesInPacketNumber) {
            case FC_2_4:
                packetHeaderType = TargetClass.from(Header_2_1.class);
                protoRules = new Typed[0];
                break;
            case FC_2_4_99_2011_11_02:
                packetHeaderType = TargetClass.from(Header_2_2.class);
                protoRules = new Typed[0];
                break;
            case FC_trunk:
                packetHeaderType = TargetClass.from(Header_2_1.class);
                TargetClass place = TargetClass.from(ReflexRuleTime.class);
                TargetClass change = TargetClass.from(org.freeciv.connection.ReflexActionChangeHeaderKind.class);
                protoRules = new Typed[]{
                        rule.newInstance(place.callV("POST_RECEIVE"), BuiltIn.literal(5),
                                change.newInstance(TargetClass.from(Header_2_2.class).callV("class"))),
                        rule.newInstance(place.callV("POST_SEND"), BuiltIn.literal(5),
                                change.newInstance(TargetClass.from(Header_2_2.class).callV("class")))
                };
                break;
            default: throw new IllegalArgumentException("No other sizes than one or two bytes are supported" +
                                                                "for packet kind field in packet header");
        }

        requirements.addWanted(Constant.isOther(TargetArray.from(rule, 1), Util.RULES_NAME,
                new ArrayLiteral(protoRules)));

        requirements.addWanted(Constant.isClass(Util.HEADER_NAME,
                packetHeaderType.callV("class")));

        requirements.addWanted(Constant.isBool("enableDelta", BuiltIn.literal(enableDelta)));
        requirements.addWanted(Constant.isBool("enableDeltaBoolFolding", BuiltIn.literal(enableDeltaBoolFolding)));
    }

    public boolean doesFieldTypeResolve(String name) {
        return requirements.isAwareOfProvider(new Requirement(name, FieldType.class));
    }

    /**
     * A new packet has arrived. Check if it is valid and register it if it is.
     * @param packet the new packet
     * @throws PacketCollisionException if the packet name or number already is in use
     */
    private void processNewPacket(PacketMaker packet) throws PacketCollisionException {
        validateNameAndNumber(packet.getName(), packet.getNumber());
        reserveNameAndNumber(packet.getName(), packet.getNumber(),
                (Requirement) packet.getICanProduceReq());

        requirements.demand((Requirement)packet.getICanProduceReq());
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

    public Collection<SourceFile> getSource() {
        Collection<SourceFile> out = new LinkedList<SourceFile>();

        for (Dependency.Item dep : requirements.getResolved())
            if (dep instanceof SourceFile)
                out.add((SourceFile) dep);

        return out;
    }

    public Collection<ClassWriter> getJavaCode() {
        Collection<Dependency.Item> inn = requirements.getResolved();
        HashSet<ClassWriter> out = new HashSet<ClassWriter>();

        TreeMap<Integer, TargetClass> resolvedPackets = new TreeMap<Integer, TargetClass>();
        TreeSet<Constant> sortedConstants =
                new TreeSet<Constant>(new TotalOrderNoCircles(inn));

        for (Dependency.Item dep : inn)
            if (dep instanceof ClassWriter) {
                out.add((ClassWriter) dep);
                if (dep instanceof Packet) {
                    resolvedPackets.put(((Packet) dep).getNumber(), ((Packet) dep).getAddress());
                }
            } else if (dep instanceof Constant)
                sortedConstants.add((Constant)dep);

        out.add(generateVersionData(resolvedPackets, sortedConstants));

        return out;
    }

    static ClassWriter generateVersionData(TreeMap<Integer, TargetClass> packets, Set<Constant> constants) {
        int border = Util.VERSION_DATA_CLASS.lastIndexOf('.');
        ClassWriter versionData =
                new ClassWriter(ClassKind.CLASS, TargetPackage.from(Util.VERSION_DATA_CLASS.substring(0, border)), Imports.are(),
                        "Freeciv C code", Collections.<Annotate>emptyList(), Util.VERSION_DATA_CLASS.substring(border + 1),
                        ClassWriter.DEFAULT_PARENT, Collections.<TargetClass>emptyList());

        for (Constant dep : constants)
            versionData.addField(dep);

        versionData.addClassConstant(Visibility.PUBLIC, TargetArray.from(Class[].class),
                Util.PACKET_MAP_NAME, new ArrayLiteral(formatPacketMap(packets)));

        return versionData;
    }

    private static Typed<AValue>[] formatPacketMap(TreeMap<Integer, TargetClass> pNumToPName) {
        if (pNumToPName.isEmpty())
            return new Typed[0];

        List<Typed<AValue>> packets = new LinkedList<Typed<AValue>>();
        for (Integer packetNumber : pNumToPName.keySet())
            packets.add(pNumToPName.get(packetNumber).callV("class"));
        return packets.toArray(new Typed[packets.size()]);
    }

    @Deprecated
    public Collection<Requirement> getUnsolvedRequirements() {
        TreeSet<Requirement> out = new TreeSet<Requirement>(requirements.getMissingRequirements());
        return out;
    }

    public Collection<MissingItemExplained> explainMissing() {
        TreeSet<MissingItemExplained> out = new TreeSet<MissingItemExplained>();

        for (Requirement req : requirements.getMissing())
            out.add(requirements.explainMissing(req));

        return out;
    }

    public void addDependency(Dependency fulfillment) {
        if (fulfillment instanceof PacketMaker)
            processNewPacket((PacketMaker) fulfillment);
        if (fulfillment instanceof Enum)
            for (Dependency.Item constant : ((Enum)fulfillment).getEnumConstants())
                requirements.addPossibleRequirement(constant);
        if (fulfillment instanceof Dependency.Item)
            requirements.addPossibleRequirement((Dependency.Item) fulfillment);
        else if (fulfillment instanceof Dependency.Maker)
            requirements.addMaker((Dependency.Maker) fulfillment);
        else
            throw new IllegalArgumentException("Not a maker. Not an item. What is it?");
    }

    public void addSource(SourceFile source) {
        requirements.addWanted(source);
    }

    public void requestConstant(String constant) {
        requirements.demand(new Requirement(constant, Constant.class));
    }

    public void requestType(String type) {
        requirements.demand(new Requirement(type, DataType.class));
    }
}
