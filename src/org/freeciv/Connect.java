package org.freeciv;

import org.freeciv.packet.Packet;

import java.io.*;
import java.net.Socket;

//TODO: Implement delta protocol
//TODO: Implement compression in protocol
public class Connect {
    OutputStream out;
    DataInputStream in;
    Socket server;

    public Connect(String address, int port) throws IOException {

        server = new Socket(address, port);
        in = new DataInputStream(server.getInputStream());

        out = server.getOutputStream();
    }

    public void toSend(Packet toSend) throws IOException {
        ByteArrayOutputStream packetSerialized = new ByteArrayOutputStream(toSend.getEncodedSize());
        DataOutputStream packet = new DataOutputStream(packetSerialized);

        toSend.encodeTo(packet);

        out.write(packetSerialized.toByteArray());
    }

    public void printpackage() throws IOException {
        int packetSize = in.readUnsignedShort();
        System.out.print("size: " + packetSize);
        int type = in.readUnsignedByte();
        System.out.println("\ttype: " + type);
        byte[] body = new byte[packetSize - 3];
        in.read(body);
        for (byte part: body) {
            System.out.println((int)part);
        }
    }

}
