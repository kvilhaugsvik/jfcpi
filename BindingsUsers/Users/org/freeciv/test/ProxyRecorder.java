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

package org.freeciv.test;

import org.freeciv.Connect;
import org.freeciv.NotReadyYetException;
import org.freeciv.connection.ReflexReaction;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;

public class ProxyRecorder implements Runnable {
    private static final int ACCEPT_CLIENTS_AT = 5556;

    private final int proxyNumber;
    private final Connect clientCon;

    private boolean started = false;

    public static void main(String[] args) throws InterruptedException {
        try {
            ServerSocket serverProxy = new ServerSocket(ACCEPT_CLIENTS_AT);
            ArrayList<ProxyRecorder> connections = new ArrayList<ProxyRecorder>();
            while (!serverProxy.isClosed())
                try {
                    Connect clientCon =
                            new Connect(serverProxy.accept(), Collections.<Integer, ReflexReaction>emptyMap());
                    ProxyRecorder proxy = new ProxyRecorder(clientCon, connections.size());
                    connections.add(proxy);
                    (new Thread(proxy)).start();
                } catch (IOException e) {
                    System.err.println("Failed accepting new connection");
                    e.printStackTrace();
                }
        } catch (IOException e) {
            System.err.println("Port not free");
            e.printStackTrace();
        }
    }

    public ProxyRecorder(Connect clientCon, int proxyNumber) throws IOException, InterruptedException {
        this.proxyNumber = proxyNumber;
        this.clientCon = clientCon;
    }

    @Override
    public void run() {
        if (false == started)
            started = true;
        else
            throw new IllegalStateException("Already started");

        while (clientCon.isOpen() || clientCon.hasMorePackets()) {
            try {
                System.out.println(proxyNumber + " c2s: " + clientCon.getPacket());
            } catch (NotReadyYetException e) {
                Thread.yield();
            } catch (IOException e) {
                System.err.println("Couldn't get packet. Finishing...");
                e.printStackTrace();
                clientCon.setOver();
            }
        }

        System.out.println(proxyNumber + " is finished");
    }
}
