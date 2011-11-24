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

    public void registerPacket(Packet packet) throws PacketCollisionException, UndefinedException {
        if (packets.containsKey(packet.getName())) {
            throw new PacketCollisionException("Packet name " + packet.getName() + " already in use");
        } else if (packetsByNumber.containsKey(packet.getNumber())) {
            throw new PacketCollisionException("Packet number " + packet.getNumber() + " already in use");
        }

        for (Field fieldType: packet.getFields()) {
            if (!types.containsKey(fieldType.getType())) {
                String errorMessage = "Field type" + fieldType.getType() +
                        " not declared before use in packet " + packet.getName() + ".";
                if (devMode) {
                    System.err.println(errorMessage);
                    System.err.println("Skipping packet since in development mode...");
                    return;
                } else {
                    throw new UndefinedException(errorMessage);
                }
            }
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
        for (String name: packets.keySet()) {
            out.put(name, packets.get(name).toString());
        }
        return out;
    }
}
