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

import org.freeciv.connection.Interpretated;
import org.freeciv.NotReadyYetException;
import org.freeciv.connection.ReflexReaction;
import org.freeciv.packet.Packet;

import java.io.*;
import java.net.ServerSocket;
import java.util.*;

public class ProxyRecorder implements Runnable {
    private static final int ACCEPT_CLIENTS_AT = 5556;
    private static final int REAL_SERVER_PORT = 55555;
    private static final String REAL_SERVER_ADDRESS = "127.0.0.1";

    private static final String TRACE_NAME_ROOT = "FreecivCon";
    private static final String TRACE_NAME_EXTENSION = ".fct";
    private static final boolean TRACE_DYNAMIC = true;


    private final int proxyNumber;
    private final Interpretated clientCon;
    private final Interpretated serverCon;
    private final DataOutputStream trace;

    private boolean started = false;

    public static void main(String[] args) throws InterruptedException {
        try {
            ServerSocket serverProxy = new ServerSocket(ACCEPT_CLIENTS_AT);
            ArrayList<ProxyRecorder> connections = new ArrayList<ProxyRecorder>();
            while (!serverProxy.isClosed())
                try {
                    Interpretated clientCon =
                            new Interpretated(serverProxy.accept(), Collections.<Integer, ReflexReaction>emptyMap());
                    ProxyRecorder proxy = new ProxyRecorder(clientCon, connections.size(),
                            new DataOutputStream(new BufferedOutputStream(
                                    new FileOutputStream(TRACE_NAME_ROOT + connections.size() + TRACE_NAME_EXTENSION))));
                    connections.add(proxy);
                    (new Thread(proxy)).start();
                } catch (IOException e) {
                    System.err.println("Failed accepting new connection");
                    e.printStackTrace();
                }
        } catch (IOException e) {
            System.err.println("Port not free");
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    public ProxyRecorder(Interpretated clientCon, int proxyNumber, DataOutputStream trace) throws IOException, InterruptedException {
        this.proxyNumber = proxyNumber;
        this.trace = trace;
        this.clientCon = clientCon;
        try {
            serverCon = new Interpretated(REAL_SERVER_ADDRESS, REAL_SERVER_PORT, Collections.<Integer, ReflexReaction>emptyMap());
        } catch (IOException e) {
            throw new IOException(proxyNumber + ": Unable to connect to server", e);
        }
    }

    @Override
    public void run() {
        if (!started)
            started = true;
        else
            throw new IllegalStateException("Already started");

        try {
            // the version of the trace format
            trace.writeChar(1);
            // is the time a packet arrived included in the trace
            trace.writeBoolean(TRACE_DYNAMIC);
        } catch (IOException e) {
            System.err.println(proxyNumber + ": Unable to write trace");
            e.printStackTrace();

            cleanUp();
            return;
        }

        while (clientCon.isOpen() && serverCon.isOpen()) {
            proxyPacket(clientCon, serverCon, true);
            proxyPacket(serverCon, clientCon, false);
        }

        cleanUp();

        System.out.println(proxyNumber + " is finished");
    }

    private void cleanUp() {
        clientCon.setOver();
        serverCon.setOver();
        try {
            trace.close();
        } catch (IOException e) {
            System.err.println("Some data may not have been written to the trace for connection " + proxyNumber);
            e.printStackTrace();
        }
    }

    private void proxyPacket(Interpretated readFrom, Interpretated writeTo, boolean clientToServer) {
        try {
            Packet fromClient = readFrom.getPacket();
            System.out.println(proxyNumber + (clientToServer ? " c2s: " : " s2c: ") + fromClient);
            trace.writeBoolean(clientToServer);
            if (TRACE_DYNAMIC)
                trace.writeLong(System.currentTimeMillis());
            fromClient.encodeTo(trace);
            writeTo.toSend(fromClient);
        } catch (NotReadyYetException e) {
            Thread.yield();
        } catch (IOException e) {
            System.err.println("Couldn't get packet. Finishing...");
            e.printStackTrace();
            readFrom.setOver();
            serverCon.setOver();
        }
    }
}
