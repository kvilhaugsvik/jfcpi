package org.freeciv.test;

import org.freeciv.packetgen.Packet;
import org.freeciv.packetgen.Field;
import org.freeciv.packetgen.Hardcoded;

public class GenerateTest {
    public static void main(String[] args) {
        System.out.println(Hardcoded.out[0].toString("UINT32"));
        System.out.println(Hardcoded.out[1].toString("STRING"));
        System.out.println(new Packet("SERVER_JOIN_REQ",
                4,
                new Field("username", "STRING", "String"),
                new Field("capability", "STRING", "String"),
                new Field("version_label", "STRING", "String"),
                new Field("major_version", "UINT32", "Long"),
                new Field("minor_version", "UINT32", "Long"),
                new Field("patch_version", "UINT32", "Long")));
        System.out.println(new Packet("CONN_PONG", 89));
    }
}
