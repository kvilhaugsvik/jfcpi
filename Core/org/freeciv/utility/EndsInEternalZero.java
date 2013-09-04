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

package org.freeciv.utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class EndsInEternalZero extends InputStream {
    private final byte[] beginning;
    private int pos = -1;

    public EndsInEternalZero() {
        this(new byte[0]);
    }

    public EndsInEternalZero(byte[] beginning) {
        this.beginning = beginning;
    }

    @Override
    public int read() throws IOException {
        pos++;

        if (pos < beginning.length)
            return 0xff & beginning[pos];
        else
            return 0;
    }

    public static byte[] allOneBytes(int bytes) {
        byte[] out = new byte[bytes];
        Arrays.fill(out, (byte) -1);
        return out;
    }
}
