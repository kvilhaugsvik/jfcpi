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

import org.freeciv.connection.BadProtocolData;
import org.freeciv.connection.ProtocolData;
import org.freeciv.packet.Packet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WantHackProtocol extends SinkProcess {
    final Lock fileNameLock;
    final Condition newFileName;
    final Condition wroteTheFile;
    private final Method getChallenge_fileValue;
    private final Method getTokenValue;

    private static final String fcData = (isWindows() ? appDataOrHome() : getUserHome()) + "/.freeciv/";

    String challengeFileName;

    public WantHackProtocol(ProtocolData versionKnowledge) {
        super(new FilterPacketKind(Arrays.asList(5, 160, 161)), versionKnowledge);

        try {
            getChallenge_fileValue = versionKnowledge.getServerJoinReply().getMethod("getChallenge_fileValue");
        } catch (NoSuchMethodException e) {
            throw new BadProtocolData("Not able to access challenge file name", e);
        }
        try {
            getTokenValue = versionKnowledge.getPacket(160).getMethod("getTokenValue");
        } catch (NoSuchMethodException e) {
            throw new BadProtocolData("Not able to access challenge value", e);
        }

        fileNameLock = new ReentrantLock();
        newFileName = fileNameLock.newCondition();
        wroteTheFile = fileNameLock.newCondition();
        challengeFileName = null;
    }

    @Override
    public void write(Packet packet, boolean clientToServer, int connectionID) throws IOException {
        final Packet interpreted = interpret(clientToServer, packet);

        switch (packet.getHeader().getPacketKind()) {
            case 5:
                /* The server uses SERVER_JOIN_REPLY to specify the name of
                 * the file to write the token to. */
                fileNameLock.lock();
                try {
                    try {
                        challengeFileName = fcData + getChallenge_fileValue.invoke(interpreted);
                    } catch (IllegalAccessException e) {
                        throw new BadProtocolData("Not allowed to access challenge file name", e);
                    } catch (InvocationTargetException e) {
                        throw new BadProtocolData("Problem while accessing challenge file name", e);
                    }
                    newFileName.signal();
                } finally {
                    fileNameLock.unlock();
                }
                break;
            case 160:
                /* Write the client selected token to the server specified challenge file.
                 * Send the token to the server using SINGLE_WANT_HACK_REQ. */
                fileNameLock.lock();
                try {
                    while (null == challengeFileName)
                        newFileName.awaitUninterruptibly();

                    String challengeString = null;
                    try {
                        challengeString = "[challenge]\ntoken=\"" + getTokenValue.invoke(interpreted) + "\"\n";
                    } catch (IllegalAccessException e) {
                        throw new BadProtocolData("Not allowed to access challenge value", e);
                    } catch (InvocationTargetException e) {
                        throw new BadProtocolData("Problem while accessing challenge value", e);
                    }

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
                /* The server uses SINGLE_WANT_HACK_REPLY to say it has
                 * looked at the challenge file. Delete it. */
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

    private static boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    private static String appDataOrHome() {
        // Freeciv prefers HOME if it is set. See utility/shared.c
        String home = System.getenv("HOME");
        if (null != home)
            return home;

        // try to get the proper AppData folder without having to call SHGetSpecialFolderLocation from Java
        String appdata = System.getenv("APPDATA");
        if (null != appdata)
            return appdata;

        // try to guess the correct location if APPDATA isn't set
        return getUserHome() + "/AppData/Roaming";
    }

    private static String getUserHome() {
        return System.getProperty("user.home");
    }
}
