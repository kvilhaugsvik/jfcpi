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

package org.freeciv.recorder.traceFormat2;

import java.io.DataInputStream;
import java.io.IOException;

public class TF2 {
    static final int FORMAT_VERSION = 2;

    static void headerSkip(DataInputStream inAsData, long toSkip) throws IOException {
        if (toSkip < 0)
            throw new IOException("Wrong format");
        while (0 != toSkip) {
            final long skipped = inAsData.skip(toSkip);
            if (0 < skipped)
                toSkip = toSkip - skipped;
        }
    }
}
