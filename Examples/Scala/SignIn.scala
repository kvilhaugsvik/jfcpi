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

/* This example requires JFCPI Core and the generated packet interpretation
 * code in its classpath. */

package Scala

import org.freeciv.connection._
import org.freeciv.packet.Packet

object SignIn {
  /* Connection parameters */
  val host = "localhost"
  val port = 5556
  val userName = "FreecivFromScala"

  def main(args: Array[String]): Unit = {
    /* Let the user know what is happening. */
    println("Signing in to " + host + " on port " + port + " as the user "
      + userName + "...\n")

    /* Connect and sign in to a Freeciv server as a client. */
    val fcConnection = ConnectionHelper.signInAsClient(host, port, userName)

    /* Handle each individual packet. */
    def handlePacket(packet: Packet) {
      /* display all packets */
      println(packet.toString)
      println()

      /* some packets have responses */
      packet.getHeader.getPacketKind match {
        /* Announce the sign in after joining */
        case 5 => {
          fcConnection.send(fcConnection.newPacketFromValues(26, "Hi!"))
          fcConnection.send(fcConnection.newPacketFromValues(26,
            "I'm written in Scala."))
          fcConnection.send(fcConnection.newPacketFromValues(26,
            "I can connect to a Freeciv server."))
          fcConnection.send(fcConnection.newPacketFromValues(26,
            "I can tell you what I'm telling you now."))
          fcConnection.send(fcConnection.newPacketFromValues(26,
            "That is all I can do."))
          fcConnection.send(fcConnection.newPacketFromValues(26,
            "You should probably /kick me now."))
        }

        /* No need to take any action. */
        case default =>
      }
    }

    /* receive the next packet as long as the connection is open. */
    while (fcConnection.isOpen) {
      if (!fcConnection.packetReady()) {
        /* wait for the next packet */
        Thread.`yield`
      } else {
        /* handle the packet */
        handlePacket(fcConnection.getPacket)
      }
    }

    /* nothing more to do. */
    println("No longer connected.")
  }
}