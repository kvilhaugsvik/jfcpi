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

import org.freeciv.Connect;
import org.freeciv.packet.PACKET_CONN_PONG;
import org.freeciv.packet.PACKET_SERVER_JOIN_REQ;

import java.io.IOException;

// Needs classes generated by GenerateTest
public class SignInAndWait {
    public static void main(String[] cmd) {
        try {
            Connect con = new Connect("127.0.0.1", 5556);

            con.toSend(new PACKET_SERVER_JOIN_REQ("FreecivJava", "+Freeciv.Devel-2.5-2012.Jun.28-2", "-dev", 2L, 4L, 99L));

            System.out.println(con.getPacket());

            con.toSend(new PACKET_CONN_PONG());

            while(true) {
                System.out.println(con.getPacket());
                Thread.sleep(1000L);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
