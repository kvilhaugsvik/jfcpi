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

import org.freeciv.connection.*;
import org.freeciv.packet.DeltaKey;
import org.freeciv.packet.Packet;
import org.freeciv.utility.ArgumentSettings;
import org.freeciv.utility.Setting;
import org.freeciv.utility.UI;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

// Needs classes generated by GenerateTest
public class SignInAndWait {
    private static final String ADDRESS = "address";
    private static final String PORT = "port";
    private static final String USER_NAME = "user-name";
    private static final String START = "start";

    public static void main(String[] cmd) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, ClassNotFoundException {
        ArgumentSettings settings = new ArgumentSettings(
                new LinkedList<Setting<?>>(){{
                    add(new Setting.StringSetting(ADDRESS, "127.0.0.1", "connect to the Freeciv server on this address"));
                    add(new Setting.IntSetting(PORT, 5556, "connect to the Freeciv server on ths port"));
                    add(new Setting.StringSetting(USER_NAME, "FreecivJava", "sign inn using user name"));
                    add(new Setting.IntSetting(START, 0, "seconds to wait before starting the game or 0 to disable."));
                    add(UI.HELP_SETTING);
                }},
                cmd);

        UI.printAndExitOnHelp(settings, SignInAndWait.class);

        String address = settings.getSetting(ADDRESS);
        int portNumber = settings.<Integer>getSetting(PORT);
        String userName = settings.getSetting(USER_NAME);

        /* The start after X seconds option is only enabled if the seconds
         * to wait are larger than 0. */
        boolean start = 0 < settings.<Integer>getSetting(START);
        final long start_time = settings.<Integer>getSetting(START) * 1000 + System.currentTimeMillis();

        final ProtocolData interpreter = new ProtocolData();

        HashMap<Integer, ReflexReaction> reflexes = new HashMap<Integer, ReflexReaction>();
        reflexes.put(88, new ReflexReaction<PacketWrite>() {
            @Override
            public void apply(PacketWrite dest) {
                /* Must be a ConnectionHasFullProtoData since SignInAndWait
                 * only use this code with a ConnectionHasFullProtoData. */
                ConnectionHasFullProtoData connection = (ConnectionHasFullProtoData)dest;

                try {
                    dest.send(connection.newPong());
                } catch (Exception e) {
                    System.err.println("Failed to respond");
                }
            }
        });
        reflexes.put(8, new ReflexReaction<Over>() {
            @Override
            public void apply(Over connection) {
                connection.setStopReadingWhenOutOfInput();
            }
        });
        try {
            final Socket connection = new Socket(address, portNumber);
            final ConnectionHasFullProtoData con = Connection.interpreted(connection.getInputStream(), connection.getOutputStream(),
                    ReflexPacketKind.layer(interpreter.getRequiredPostReceiveRules(), reflexes),
                    interpreter.getRequiredPostSendRules(), interpreter, Logger.GLOBAL_LOGGER_NAME);

            con.send(con.newServerJoinRequest(userName, interpreter.getCapStringOptional()));

            while(con.isOpen() || con.packetReady()) {
                if (start && start_time < System.currentTimeMillis()) {
                    /* Start the game. */
                    con.send(con.newPacketFromValues(26, "/start"));

                    /* Don't send the command more than once. */
                    start = false;
                }

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
