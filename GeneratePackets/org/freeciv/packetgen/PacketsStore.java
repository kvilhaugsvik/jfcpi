package org.freeciv.packetgen;

import java.util.HashMap;

public class PacketsStore {
    private boolean devMode;

    private HashMap<String, JavaSrc> types = new HashMap<String, JavaSrc>();

    // To avoid duplication of structures have packets store the packets and packetsByNumber translate the keys
    // Idea from http://stackoverflow.com/q/822701
    private HashMap<String, Packet> packets = new HashMap<String, Packet>();
    private HashMap<Integer, String> packetsByNumber = new HashMap<Integer, String>();

    public PacketsStore(boolean devMode) {
        this.devMode = devMode;
    }

    public void registerTypeAlias(String alias, String aliased) throws UndefinedException {
        if (null != Hardcoded.getJTypeFor(aliased)) {
            types.put(alias, Hardcoded.getJTypeFor(aliased));
        } else if (types.containsKey(aliased)) {
            types.put(alias, types.get(aliased));
        } else {
            String errorMessage = aliased + " not declared before used in " + alias + ".";
            if (devMode) {
                System.err.println(errorMessage);
                System.err.println("Continuing since in development mode...");
            } else {
                throw new UndefinedException(errorMessage);
            }
        }
    }

    public boolean hasTypeAlias(String name) {
        return types.containsKey(name);
    }

    public void registerPacket(Packet packet) throws PacketCollisionException {
        if (packets.containsKey(packet.getName())) {
            throw new PacketCollisionException("Packet name " + packet.getName() + " already in use");
        } else if (packetsByNumber.containsKey(packet.getNumber())) {
            throw new PacketCollisionException("Packet number " + packet.getNumber() + " already in use");
        }

        packets.put(packet.getName(), packet);
        packetsByNumber.put(packet.getNumber(), packet.getName());
    }

    public boolean hasPacket(String name) {
        return packets.containsKey(name);
    }

    public boolean hasPacket(int number) {
        return packetsByNumber.containsKey(number);
    }

    public HashMap<String, String> getJavaCode() {
        HashMap<String, String> out = new HashMap<String, String>();
        for (String name: types.keySet()) {
            out.put(name, types.get(name).toString(name));
        }
        return out;
    }
}
