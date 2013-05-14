/*
 * Copyright (c) 2013. Sveinung Kvilhaugsvik
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

package org.freeciv.recorder;

import org.freeciv.connection.PacketsMapping;
import org.freeciv.packet.PACKET_SERVER_JOIN_REPLY;
import org.freeciv.packet.PACKET_SINGLE_WANT_HACK_REQ;
import org.freeciv.packet.Packet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WantHackProtocol extends SinkProcess {
    final Lock fileNameLock;
    final Condition newFileName;
    final Condition wroteTheFile;

    String challengeFileName;

    public WantHackProtocol(PacketsMapping versionKnowledge) {
        super(new FilterPacketKind(Arrays.asList(5, 160, 161)), versionKnowledge);
        fileNameLock = new ReentrantLock();
        newFileName = fileNameLock.newCondition();
        wroteTheFile = fileNameLock.newCondition();
        challengeFileName = null;
    }

    @Override
    public void write(boolean clientToServer, Packet packet) throws IOException {
        final Packet interpreted = interpret(clientToServer, packet);

        switch (packet.getHeader().getPacketKind()) {
            case 5:
                fileNameLock.lock();
                try {
                    challengeFileName = System.getProperty("user.home") + "/.freeciv/" +
                            ((PACKET_SERVER_JOIN_REPLY)interpreted).getChallenge_fileValue();
                    newFileName.signal();
                } finally {
                    fileNameLock.unlock();
                }
                break;
            case 160:
                fileNameLock.lock();
                try {
                    while (null == challengeFileName)
                        newFileName.awaitUninterruptibly();

                    String challengeString = "[challenge]\ntoken=\"" +
                            ((PACKET_SINGLE_WANT_HACK_REQ)interpreted).getTokenValue() + "\"\n";

                    FileOutputStream challengeWrite = new FileOutputStream(challengeFileName);
                    try {
                        challengeWrite.write(challengeString.getBytes());
                        wroteTheFile.signal();
                    } finally {
                        challengeWrite.close();
                    }
                } finally {
                    fileNameLock.unlock();
                }
                break;
            case 161:
                fileNameLock.lock();
                try {
                    File target = new File(challengeFileName);
                    while (!target.exists())
                        wroteTheFile.awaitUninterruptibly();

                    target.delete();
                } finally {
                    fileNameLock.unlock();
                }
                break;
            default:
                throw new IllegalArgumentException("The packet number " + packet.getHeader().getPacketKind() +
                        " should have been filtered");
        }
    }
}
