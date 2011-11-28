package org.freeciv.packetgen;

import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

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
            if (!types.containsKey(fieldType[0])) {
                String errorMessage = "Field type" + fieldType[0] +
                        " not declared before use in packet " + name + ".";
                if (devMode) {
                    System.err.println(errorMessage);
                    System.err.println("Skipping packet since in development mode...");
                    return;
                } else {
                    throw new UndefinedException(errorMessage);
                }
            }
            fieldList.add(new Field(fieldType[1], fieldType[0], types.get(fieldType[0]).getJavaType()));
        }

        packets.put(name, new Packet(name, number, fieldList.toArray(new Field[0])));
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

    public String getPacketList() {
        String out = "";

        for (int number: packetsByNumber.keySet()) {
            out += number + "\t" + "org.freeciv.packet." + packetsByNumber.get(number) + "\n";
        }
        return out;
    }
}
