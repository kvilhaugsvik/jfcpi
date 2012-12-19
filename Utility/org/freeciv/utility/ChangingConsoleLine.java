/*
 * Copyright (c) 2012. Sveinung Kvilhaugsvik
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

import java.io.PrintStream;

public class ChangingConsoleLine {
    private final String start;
    private final PrintStream target;

    private int lastLineLen = 0;

    public ChangingConsoleLine(String start, PrintStream target) {
        this.start = start;
        this.target = target;
    }

    public void printCurrent(String current) {
        StringBuilder msg = new StringBuilder(start);
        msg.append(current);

        target.print("\r");
        target.print(msg);

        if (msg.length() < lastLineLen)
            target.print(Strings.repeat(" ", lastLineLen - msg.length()));

        lastLineLen = msg.length();
    }

    public void finished() {
        target.println();
    }
}
