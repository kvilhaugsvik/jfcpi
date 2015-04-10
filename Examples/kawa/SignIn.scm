; Copyright (c) 2015. Sveinung Kvilhaugsvik
;
; This program is free software; you can redistribute it and/or
; modify it under the terms of the GNU General Public License
; as published by the Free Software Foundation; either version 2
; of the License, or (at your option) any later version.
;
; This program is distributed in the hope that it will be useful,
; but WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
; GNU General Public License for more details.

; Connection parameters
(define host "localhost")
(define port 5556)
(define user-name "FreecivFromScheme")

(define logger-name java.util.logging.Logger:GLOBAL_LOGGER_NAME)

; Needed to understand the packets
(define protocol
  (org.freeciv.connection.ProtocolData))

; Let the user know what is happening.
(display
  ($string$ "Signing in to " $<<$ host $>>$ " on port " $<<$ port $>>$
            " as the user " $<<$ user-name $>>$ "...\n"))

; Connect to the server
(define raw-connection (java.net.Socket host port))

; Wrap the raw connection in a Freeciv protocol connection
(define fc-connection ::org.freeciv.connection.ConnectionHasFullProtoData
  (org.freeciv.connection.Connection:interpreted
    (raw-connection:getInputStream)
    (raw-connection:getOutputStream)
    ; note: no reflex to respond to ping packets is added
    (protocol:getRequiredPostReceiveRules)
    (protocol:getRequiredPostSendRules)
    protocol
    logger-name))

; Sign in to the server
(fc-connection:send
  (fc-connection:newServerJoinRequest
    user-name
    (protocol:getCapStringOptional)))

; handle each individual packet.
(define (handle-packet packet ::org.freeciv.packet.Packet)
  (cond
    ; Manually resond to ping since it isn't handled
    ; in a post receive rule.
    ((= 88 ((packet:getHeader):getPacketKind))
     (fc-connection:send
       (fc-connection:newPong)))
    ; Announce the sign in after joining
    ((= 5 ((packet:getHeader):getPacketKind))
     (fc-connection:send
       (fc-connection:newPacketFromValues
         26 "Hi!"))
     (fc-connection:send
       (fc-connection:newPacketFromValues
         26 "I'm written in Scheme."))
     (fc-connection:send
       (fc-connection:newPacketFromValues
         26 "I can connect to a Freeciv server."))
     (fc-connection:send
       (fc-connection:newPacketFromValues
         26 "I can tell you what I'm telling you now."))
     (fc-connection:send
       (fc-connection:newPacketFromValues
         26 "That is all I can do."))
     (fc-connection:send
       (fc-connection:newPacketFromValues
         26 "You should probably /kick me now.")))))

; receive the next packet as long as the connection is open.
(define (receive-next-packet)
  (cond ((not (fc-connection:isOpen))
         ; nothing more to do.
         (display "No longer connected.\n")
         (exit))
        ((not (fc-connection:packetReady))
         ; wait for the next packet
         (java.lang.Thread:yield)
         (receive-next-packet))
        (else
          ; handle the packet
          (handle-packet (fc-connection:getPacket))
          (receive-next-packet))))

; start receiving packets
(receive-next-packet)
