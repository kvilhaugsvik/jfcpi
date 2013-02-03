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

package org.freeciv.test;

import org.freeciv.connection.Interpretated;
import org.freeciv.connection.NotReadyYetException;
import org.freeciv.connection.FreecivConnection;
import org.freeciv.connection.ReflexReaction;
import org.freeciv.packet.PACKET_CONN_PONG;
import org.freeciv.packet.PACKET_SERVER_JOIN_REQ;
import org.freeciv.packet.RawPacket;
import org.freeciv.utility.ArgumentSettings;
import org.freeciv.utility.Setting;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;

// Needs classes generated by GenerateTest
public class SignInAndWait {
    private static final String ADDRESS = "address";
    private static final String PORT = "port";
    private static final String USER_NAME = "user-name";

    public static void main(String[] cmd) throws InvocationTargetException {
        ArgumentSettings settings = new ArgumentSettings(
                new LinkedList<Setting<?>>(){{
                    add(new Setting.StringSetting(ADDRESS, "127.0.0.1"));
                    add(new Setting.IntSetting(PORT, 5556));
                    add(new Setting.StringSetting(USER_NAME, "FreecivJava"));
                }},
                cmd);

        String address = settings.getSetting(ADDRESS);
        int portNumber = settings.<Integer>getSetting(PORT);
        String userName = settings.getSetting(USER_NAME);

        HashMap<Integer, ReflexReaction> reflexes = new HashMap<Integer, ReflexReaction>();
        reflexes.put(88, new ReflexReaction() {
            @Override
            public void apply(RawPacket incoming, FreecivConnection connection) {
                try {
                    connection.toSend(new PACKET_CONN_PONG());
                } catch (IOException e) {
                    System.err.println("Failed to respond");
                }
            }
        });
        reflexes.put(8, new ReflexReaction() {
            @Override
            public void apply(RawPacket incoming, FreecivConnection connection) {
                connection.setOver();
            }
        });
        try {
            Interpretated con = new Interpretated(address, portNumber, reflexes);

            con.toSend(new PACKET_SERVER_JOIN_REQ(userName,
                    con.getCapStringMandatory(),
                    con.getVersionLabel(),
                    con.getVersionMajor(),
                    con.getVersionMinor(),
                    con.getVersionPatch()));

            while(con.isOpen() || con.packetReady()) {
                try {
                    System.out.println(con.getPacket());
                    System.out.println();
                } catch (NotReadyYetException e) {
                    Thread.yield();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
