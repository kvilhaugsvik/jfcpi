/*
 * Copyright (c) 2013 Sveinung Kvilhaugsvik.
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

import java.io.*;
import java.net.SocketException;

public class PacketInputStream {
    public static byte[] readXBytesFrom(int wanted, byte[] start, InputStream from, Over state)
            throws IOException {
        assert 0 <= wanted : "Can't read a negative number of bytes";

        byte[] out = new byte[wanted + start.length];
        System.arraycopy(start, 0, out, 0, start.length);

        int alreadyRead = 0;
        while(alreadyRead < wanted) {
            final int bytesRead;
            try {
                bytesRead = from.read(out, alreadyRead + start.length, wanted - alreadyRead);
            } catch (EOFException e) {
                throw done(wanted, start, alreadyRead);
            } catch (SocketException e) {
                throw done(wanted, start, alreadyRead);
            }

            if (0 < bytesRead) // If some bytes were read
                alreadyRead += bytesRead; // take note of it
            else if (-1 == bytesRead || // If there stream is closed or
                    state.shouldIStopReadingWhenOutOfInput()) // nothing was read and should stop reading in that case
                throw done(wanted, start, alreadyRead); // it's done

            Thread.yield();
        }
        return out;
    }

    private static IOException done(int wanted, byte[] start, int alreadyRead) throws DoneReading, EOFException {
        final boolean clean = 0 == start.length;
        if (clean && 0 == alreadyRead)
            return new DoneReading("Nothing to read and nothing is waiting");
        else
            return new EOFException("Nothing to read and nothing is waiting." +
                    "Read " + alreadyRead + " of " + wanted + " bytes");
    }
}
