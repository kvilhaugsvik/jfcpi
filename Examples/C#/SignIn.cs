/*
 * Copyright (c) 2015, Sveinung Kvilhaugsvik
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

/* This example is intended to be used with IKVM. Use ikvmc to put Core and
 * the generated Freeciv version specific protocol interpretation code into
 * a dll. Reference it and IKVM when compiling this program. */

using System;
using org.freeciv.connection;
using org.freeciv.packet;

public class SignIn {
  public static void Main() {
    /* Connection parameters */
    string host = "localhost";
    int port = 5556;
    string userName = "FreecivFromC#";

    /* Let the user know what is happening. */
    Console.WriteLine("Signing in to {0} on port {1} as the user {2}...\n",
        host, port, userName);

    /* Connect and sign in to a Freeciv server as a client. */
    ConnectionHasFullProtoData fcConnection =
        Connection.signInAsClient(host, port, userName);

    /* Receive the next packet as long as the connection is open. */
    while (fcConnection.isOpen()) {
      if (fcConnection.packetReady()) {
        /* Get the packet. */
        Packet packet = fcConnection.getPacket();

        /* Handle the packet. */
        handlePacket(fcConnection, packet);
      } else {
        /* Wait for the next packet */
        java.lang.Thread.yield();
      }
    }

    Console.WriteLine("No longer connected.");
  }

  private static void handlePacket(ConnectionHasFullProtoData fcConnection,
                                   Packet packet) {
    /* Display all received packets. */
    Console.WriteLine(packet);

    /* Some packets have responses */
    switch (packet.getHeader().getPacketKind()) {
    case 5:
      /* Announce the sign in after joining */
      fcConnection.send(fcConnection.newPacketFromValues(26, "Hi!"));
      fcConnection.send(fcConnection.newPacketFromValues(26,
          "I'm written in C#."));
      fcConnection.send(fcConnection.newPacketFromValues(26,
          "I can connect to a Freeciv server."));
      fcConnection.send(fcConnection.newPacketFromValues(26,
          "I can tell you what I'm telling you now."));
      fcConnection.send(fcConnection.newPacketFromValues(26,
          "That is all I can do."));
      fcConnection.send(fcConnection.newPacketFromValues(26,
          "You should probably /kick me now."));
      break;
    default:
      /* Not handled. */
      break;
    }
  }
}
