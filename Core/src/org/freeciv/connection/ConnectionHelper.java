/*
 * Copyright (c) 2015. Sveinung Kvilhaugsvik
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Handle the details of common Connection tasks.
 */
public class ConnectionHelper {
    /**
     * Connect as a Freeciv client to a Freeciv server.
     * Interpret the received packets.
     * Takes care of signing in. Responds when the server pings the client.
     * Logs problems to Logger.GLOBAL_LOGGER_NAME
     * @param address the address of the Freeciv server.
     * @param portNumber the port number of the Freeciv server.
     * @param userName the user name to sign in as
     * @return a connection to the Freeciv server.
     * @throws IOException if an I/O error occurs while setting up the
     * connection.
     * @throws InvocationTargetException if there is a problem while
     * creating the sign in packet.
     * @throws IllegalAccessException if accessing the sign in packet
     * is forbidden by Java's access control.
     * @throws BadProtocolData when the Freeciv protocol data is
     * incompatible.
     */
    public static ConnectionHasFullProtoData signInAsClient(
            final String address,
            final int portNumber,
            final String userName
    ) throws IOException, InvocationTargetException, IllegalAccessException, BadProtocolData {
        /* The connection to the server. */
        final ConnectionHasFullProtoData connection;

        /* Stuff to do before receiving the next packet. */
        final Map<Integer, ReflexReaction> postReceiveRules;

        /*  Needed to understand the packets. */
        final ProtocolData protoCode = new ProtocolData();

        /* Log problems to Logger.GLOBAL_LOGGER_NAME. */
        final String loggerName = Logger.GLOBAL_LOGGER_NAME;

        /* Connect to the server. */
        final Socket rawConnection = new Socket(address, portNumber);

        /* Automatically handle capabilities. */
        final ProtocolVariantAutomatic protocolVariant = new ProtocolVariantAutomatic(protoCode.getNewPacketMapper());

        /* Start with the reflexes required to proberly implement the protocol. */
        postReceiveRules = new HashMap<Integer, ReflexReaction>(protoCode.getRequiredPostReceiveRules());

        /* Add a reflex to respond when the server pings the client. */
        postReceiveRules.put(ReflexRule.CLIENT_ANSWER_PING.getNumber(), ReflexRule.CLIENT_ANSWER_PING.getAction());

        /* Wrap the raw connection in a Freeciv protocol connection */
        connection = new ConnectionHasFullProtoData(
                rawConnection.getInputStream(), rawConnection.getOutputStream(),
                postReceiveRules,
                protoCode.getRequiredPostSendRules(),
                protoCode,
                new InterpretWhenPossible(protocolVariant, loggerName),
                protocolVariant, loggerName);

        /* Sign in to the server */
        try {
            connection.send(connection.newServerJoinRequest(userName, protoCode.getCapStringOptional()));
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new BadProtocolData("Error while signing in", e);
        }

        /* The connection is now done. */
        return connection;
    }
}
