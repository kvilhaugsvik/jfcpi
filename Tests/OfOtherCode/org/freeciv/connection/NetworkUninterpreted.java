/*
 * Copyright (c) 2012. Sveinung Kvilhaugsvik
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

import org.freeciv.packet.Header_2_1;
import org.freeciv.packet.Header_2_2;
import org.freeciv.packet.Packet;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.*;

import static org.junit.Assert.*;

public class NetworkUninterpreted {
    @Test
    public void pureHeader() throws IOException, ExecutionException, TimeoutException, InterruptedException, NotReadyYetException {
        Socket other = helperDataSender(new byte[]{0, 4, 0, 0});
        Uninterpreted self = new Uninterpreted(other, Header_2_2.class,
                Collections.<Integer, ReflexReaction>emptyMap(), Collections.<Integer, ReflexReaction>emptyMap());

        Packet packet = assertPacketIsThere(self);

        assertEquals("Wrong kind", 0, packet.getHeader().getPacketKind());
        assertEquals("Wrong size", 4, packet.getHeader().getHeaderSize());
    }

    @Test
    public void headerTypeIsChangedAfterReceiving() throws IOException, InterruptedException, ExecutionException, TimeoutException, NotReadyYetException {
        Socket other = helperDataSender(new byte[]{
                0, 3, 1,
                0, 3, 5,
                0, 7, 0, 10, 0, 0, 0,
                0, 4, 0, 0
        });

        HashMap<Integer, ReflexReaction> postReceive = new HashMap<Integer, ReflexReaction>();
        postReceive.put(5, new ReflexReaction() {
            @Override
            public void apply(Packet incoming, FreecivConnection connection) {
                connection.setHeaderTypeTo(org.freeciv.packet.Header_2_2.class);
            }
        });

        Uninterpreted self = new Uninterpreted(other, Header_2_1.class, postReceive,
                Collections.<Integer, ReflexReaction>emptyMap());

        Packet packetBeforeChange = assertPacketIsThere(self);

        assertEquals("Wrong kind", 1, packetBeforeChange.getHeader().getPacketKind());
        assertEquals("Wrong size", 3, packetBeforeChange.getHeader().getHeaderSize());

        Packet packetChangeAfter = assertPacketIsThere(self);

        assertEquals("Wrong kind", 5, packetChangeAfter.getHeader().getPacketKind());
        assertEquals("Wrong size", 3, packetChangeAfter.getHeader().getHeaderSize());

        Packet packetAfterBodyAsWell = assertPacketIsThere(self);

        assertEquals("Wrong kind", 10, packetAfterBodyAsWell.getHeader().getPacketKind());
        assertEquals("Wrong size", 4, packetAfterBodyAsWell.getHeader().getHeaderSize());

        Packet packetAfterHeaderOnly = assertPacketIsThere(self);

        assertEquals("Wrong kind", 0, packetAfterHeaderOnly.getHeader().getPacketKind());
        assertEquals("Wrong size", 4, packetAfterHeaderOnly.getHeader().getHeaderSize());
    }

    /*********************************************************************************************************************
     * Helpers
     ********************************************************************************************************************/

    /* Create and start a service that will send the data in toSend to the returned socket */
    private static Socket helperDataSender(byte[] toSend) throws IOException {
        DataSourceSocket helper = new DataSourceSocket(toSend);
        helper.start();
        return helper.connectToMe();
    }

    public static class DataSourceSocket extends Thread {
        private final static int TEST_PORT = 10007;

        private final byte[] toSend;
        private final ServerSocket me;

        public DataSourceSocket(byte[] toSend) throws IOException {
            this.toSend = toSend;
            this.me = new ServerSocket(TEST_PORT);
        }

        public Socket connectToMe() throws IOException {
            return new Socket("localhost", TEST_PORT);
        }

        @Override
        public void run() {
            try {
                Socket target = me.accept();
                target.getOutputStream().write(toSend);
                target.close();
            } catch (IOException e) {
                fail("Possible bug in test: " + e.getMessage());
            } finally {
                try {
                    me.close();
                } catch (IOException e) {
                    fail("Possible bug in test: " + e.getMessage());
                }
            }
        }
    }

    /* Wait for a packet to appear but no longer than x seconds */
    private static void helperWaitSomeSecondsForAPacket(Uninterpreted on, int seconds)
            throws ExecutionException, TimeoutException, InterruptedException {
        Executors.newSingleThreadExecutor().submit(new YieldUnlessNewPacket(on)).get(seconds, TimeUnit.SECONDS);
    }

    public static class YieldUnlessNewPacket implements Runnable {
        private final FreecivConnection conn;

        public YieldUnlessNewPacket(Uninterpreted conn) {
            this.conn = conn;
        }

        @Override
        public void run() {
            while (!conn.packetReady()) {
                Thread.yield();
            }
        }
    }

    private static Packet assertPacketIsThere(Uninterpreted self) throws ExecutionException, TimeoutException, InterruptedException, NotReadyYetException {
        helperWaitSomeSecondsForAPacket(self, 4);

        assertTrue("There should be a packet here", self.packetReady());
        return self.getPacket();
    }
}
