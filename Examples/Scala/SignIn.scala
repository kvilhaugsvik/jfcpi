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

  /* Where to log problems. */
  val loggerName = java.util.logging.Logger.GLOBAL_LOGGER_NAME

  /*  Needed to understand the packets. */
  val protocol = new ProtocolData()

  def main(args: Array[String]): Unit = {
    /* Let the user know what is happening. */
    println("Signing in to " + host + " on port " + port + " as the user "
      + userName + "...\n")

    /* Connect to the server */
    val rawConnection = new java.net.Socket(host, port)

    /* Wrap the raw connection in a Freeciv protocol connection */
    val fcConnection = Connection.interpreted(
      rawConnection.getInputStream,
      rawConnection.getOutputStream,
      /* note: no reflex to respond to ping packets is added */
      protocol.getRequiredPostReceiveRules,
      protocol.getRequiredPostSendRules,
      protocol,
      loggerName)

    /* Sign in to the server */
    fcConnection.send(
      fcConnection.newServerJoinRequest(
        userName,
        protocol.getCapStringOptional))

    /* Handle each individual packet. */
    def handlePacket(packet: Packet) {
      /* display all packets */
      println(packet.toString)
      println()

      /* some packets have responses */
      packet.getHeader.getPacketKind match {
        /* Manually respond to ping since it isn't handled in a post
         * receive rule. */
        case 88 => fcConnection.send(fcConnection.newPong())

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