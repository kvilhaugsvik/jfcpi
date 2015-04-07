/*
 * Copyright (c) 2012, 2013. Sveinung Kvilhaugsvik
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

package org.freeciv.connection;

import org.freeciv.utility.Util;
import org.freeciv.packet.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ProtocolData implements BasicProtocolData {
    private final Map<Set<String>, Map<Integer, Method>> protocolVariants;
    private final Set<String> capsOptional;

    private final Class<? extends PacketHeader> packetNumberBytes;
    private final Map<Integer, ReflexReaction> protoRulesPostReceive;
    private final Map<Integer, ReflexReaction> protoRulesPostSend;
    private final boolean isDeltaEnabled;
    private final boolean isBoolFoldEnabled;
    private final String capStringMandatory;
    private final String capStringOptional;
    private final String versionLabel;
    private final long versionMajor;
    private final long versionMinor;
    private final long versionPatch;
    private final int compressionBorder;
    private final int jumboSize;
    private final Class<Packet> serverJoinRequest;
    private final Method serverJoinRequestFromValues;
    private final Class<Packet> serverJoinReply;
    private final Method serverJoinReplyFromValues;

    public ProtocolData() {
        try {
            Class constants = Class.forName(Util.VERSION_DATA_CLASS);
            Class[] understoodPackets = (Class[])constants.getField(Util.PACKET_MAP_NAME).get(null);
            packetNumberBytes =
                    (Class<? extends PacketHeader>) constants.getField(Util.HEADER_NAME).get(null);
            this.isDeltaEnabled = constants.getField("enableDelta").getBoolean(null);
            this.isBoolFoldEnabled = constants.getField("enableDeltaBoolFolding").getBoolean(null);

            capStringMandatory = (String)constants.getField("NETWORK_CAPSTRING_MANDATORY").get(null);
            capStringOptional = (String)constants.getField("NETWORK_CAPSTRING_OPTIONAL").get(null);
            versionLabel = (String)constants.getField("VERSION_LABEL").get(null);
            versionMajor = Long.parseLong((String)constants.getField("MAJOR_VERSION").get(null));
            versionMinor = Long.parseLong((String)constants.getField("MINOR_VERSION").get(null));
            versionPatch = Long.parseLong((String)constants.getField("PATCH_VERSION").get(null));

            this.compressionBorder = constants.getField("COMPRESSION_BORDER").getInt(null);
            this.jumboSize = constants.getField("JUMBO_SIZE").getInt(null);

            final String[] optionalCaps = this.getCapStringOptional().split(" ");
            this.capsOptional = new HashSet<String>(Arrays.asList(optionalCaps));


            Class<Packet> serverJoinRequest = null;
            Class<Packet> serverJoinReply = null;
            Class<Packet> ping = null;
            Class<Packet> pong = null;

            final Map<Set<String>, Map<Integer, Method>> globalVariants = initGlobalVariants(optionalCaps);

            for (Class understood : understoodPackets) {
                final int pNumber;

                final Map<Set<String>, List<Map<Integer, Method>>> localToGlobal =
                        mapVariantsLocalToAllGlobal(globalVariants, understood);

                try {
                    pNumber = understood.getField("number").getInt(null);
                } catch (NoSuchFieldException e) {
                    throw new BadProtocolData(understood.getSimpleName() + " is not compatible.\n" +
                            "(The static field number is missing)", e);
                }

                switch (pNumber) {
                    case 4:
                        serverJoinRequest = understood;
                        break;
                    case 5:
                        serverJoinReply = understood;
                        break;
                    case 88:
                        ping = understood;
                        break;
                    case 89:
                        pong = understood;
                        break;
                }

                for (Method cadidate : understood.getDeclaredMethods())
                    if (cadidate.getName().startsWith("fromHeaderAndStream"))
                        for (Map<Integer, Method> variant : localToGlobal.get(new HashSet<String>(Arrays.asList(cadidate.getAnnotation(CapabilityCombination.class).value()))))
                            variant.put(pNumber, cadidate);
            }

            this.protocolVariants = globalVariants;

            validatePacketWasFound(serverJoinRequest, "server join request");
            this.serverJoinRequest = serverJoinRequest;
            this.serverJoinRequestFromValues = extractFromValues(serverJoinRequest, "server join request",
                    String.class, String.class, String.class, Long.class, Long.class, Long.class);

            validatePacketWasFound(serverJoinReply, "server join reply");
            this.serverJoinReply = serverJoinReply;
            this.serverJoinReplyFromValues = extractFromValues(serverJoinReply, "server join reply",
                    Boolean.class, String.class, String.class, String.class, Integer.class);

            /* Ping is used in Connection's newPing() */
            validatePacketWasFound(ping, "ping");

            /* Pong is used in Connection's newPong() */
            validatePacketWasFound(pong, "pong");

            HashMap<Integer, ReflexReaction> neededPostSend = new HashMap<Integer, ReflexReaction>();
            HashMap<Integer, ReflexReaction> neededPostReceive = new HashMap<Integer, ReflexReaction>();
            for (ReflexRule rule : (ReflexRule[])constants.getField(Util.RULES_NAME).get(null)) {
                switch (rule.getWhen()) {
                    case POST_SEND:
                        neededPostSend.put(rule.getNumber(), rule.getAction());
                        break;
                    case POST_RECEIVE:
                        neededPostReceive.put(rule.getNumber(), rule.getAction());
                        break;
                    default:
                        throw new UnsupportedOperationException("Don't know how to execute a rule " + rule.getWhen());
                }
            }
            this.protoRulesPostSend = Collections.unmodifiableMap(neededPostSend);
            this.protoRulesPostReceive = Collections.unmodifiableMap(neededPostReceive);
        } catch (ClassNotFoundException e) {
            throw new BadProtocolData("Version information missing", e);
        } catch (NoSuchFieldException e) {
            throw new BadProtocolData("Version information not compatible", e);
        } catch (ClassCastException e) {
            throw new BadProtocolData("Version information not compatible", e);
        } catch (IllegalAccessException e) {
            throw new BadProtocolData("Refused to read version information", e);
        }
    }

    private static Method extractFromValues(Class<Packet> packet, String name, Class... sig) {
        /* The two final parameters are common for all packets. Add them here. */
        Class[] fullSig = new Class[sig.length + 2];
        System.arraycopy(sig, 0, fullSig, 0, sig.length);
        fullSig[sig.length] = HeaderData.class;
        fullSig[sig.length + 1] = java.util.Map.class;

        try {
            return packet.getMethod("fromValues", fullSig);
        } catch (NoSuchMethodException e) {
            throw new BadProtocolData("No constructor from values for packet " + name, e);
        }
    }

    private static void validatePacketWasFound(Class<Packet> serverJoinRequest, String name) {
        if (null == serverJoinRequest)
            throw new BadProtocolData("Packet " + name + " missing");
    }

    @Override
    public HeaderData getNewPacketHeaderData() {
        return new HeaderData(packetNumberBytes);
    }

    public ProtocolVariantManually getNewPacketMapper() {
        return new ProtocolVariantManually(this, this.protocolVariants);
    }

    public Map<Integer, ReflexReaction> getRequiredPostReceiveRules() {
        return protoRulesPostReceive;
    }

    public Map<Integer, ReflexReaction> getRequiredPostSendRules() {
        return protoRulesPostSend;
    }

    public String getCapStringMandatory() {
        return capStringMandatory;
    }

    public String getCapStringOptional() {
        return capStringOptional;
    }

    /**
     * Get the set of supported Freeciv protocol capabilities.
     * @return all supported capabilities.
     */
    public Set<String> getAllSettableCaps() {
        return Collections.unmodifiableSet(capsOptional);
    }

    public String getVersionLabel() {
        return versionLabel;
    }

    public long getVersionMajor() {
        return versionMajor;
    }

    public long getVersionMinor() {
        return versionMinor;
    }

    public long getVersionPatch() {
        return versionPatch;
    }

    /**
     * Is this the delta protocol or the simpler variant?
     * @return true if the delta protocol is enabled
     */
    public boolean isDelta() {
        return isDeltaEnabled;
    }

    /**
     * Is this the protocol variant that puts boolean packet variables into its delta header?
     * @return true if boolean variables are folded into the delta header.
     */
    public boolean isBoolFold() {
        return isBoolFoldEnabled;
    }

    @Override
    public int getJumboSize() {
        return jumboSize;
    }

    @Override
    public int getCompressionBorder() {
        return compressionBorder;
    }

    public Class<Packet> getPacket(int number) {
        return (Class<Packet>) protocolVariants.get(Collections.<String>emptySet()).get(number).getDeclaringClass();
    }

    private static Map<Set<String>, List<Map<Integer, Method>>> mapVariantsLocalToAllGlobal(Map<Set<String>, Map<Integer, Method>> globalVariants, Class packet) {
        final Map<Set<String>, List<Map<Integer, Method>>> out = new HashMap<Set<String>, List<Map<Integer, Method>>>();

        final String[] localVariants = ((Capabilities) packet.getAnnotation(Capabilities.class)).value();

        for (Set<String> localVariant : allCombinations(localVariants))
            out.put(localVariant, new LinkedList<Map<Integer, Method>>());

        for (Set<String> globalVariant : globalVariants.keySet()) {
            Set<String> localVariant = new HashSet<String>();

            for (String s : localVariants)
                if(globalVariant.contains(s))
                    localVariant.add(s);

            out.get(localVariant).add(globalVariants.get(globalVariant));
        }

        return out;
    }

    private Map<Set<String>, Map<Integer, Method>> initGlobalVariants(String[] optionalCaps) {
        final Map<Set<String>, Map<Integer, Method>> globalVariant = new HashMap<Set<String>, Map<Integer, Method>>();
        for (Set<String> combination : allCombinations(optionalCaps))
            globalVariant.put(combination, new HashMap<Integer, Method>());
        return globalVariant;
    }

    private static Set<Set<String>> allCombinations(String[] caps) {
        if (1 == caps.length && "".equals(caps[0]))
            caps = new String[0];

        HashSet<Set<String>> out = new HashSet<Set<String>>();
        final double combinations = Math.pow(2, caps.length);
        for (int combination = 0; combination < combinations; combination++) {
            Set<String> comb = new HashSet<String>();
            for (int i = 0; i < caps.length; i++)
                if ((combination & (1 << i)) != 0) {
                    comb.add(caps[i]);
                }
            out.add(comb);
        }
        return out;
    }
}
