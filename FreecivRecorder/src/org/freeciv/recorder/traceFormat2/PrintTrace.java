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

import org.freeciv.connection.DoneReading;
import org.freeciv.connection.OverImpl;
import org.freeciv.connection.PacketsMapping;
import org.freeciv.recorder.ProxyRecorder;
import org.freeciv.utility.ArgumentSettings;
import org.freeciv.utility.Setting;
import org.freeciv.utility.UI;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

public class PrintTrace {
    private static final String TRACE_FILE = "file";

    public static final LinkedList<Setting<?>> SETTINGS = new LinkedList<Setting<?>>() {{
        add(new Setting.StringSetting(TRACE_FILE,
                ProxyRecorder.DEFAULT_TRACE_PREFIX + "0" + ProxyRecorder.DEFAULT_TRACE_SUFFIX,
                "the file containing the trace to play back"));

        add(UI.HELP_SETTING);
    }};

    public static void main(String[] args) throws IOException, InvocationTargetException {
        ArgumentSettings settings = new ArgumentSettings(SETTINGS, args);

        final String fileName = settings.<String>getSetting(TRACE_FILE);

        final FileInputStream file = new FileInputStream(fileName);
        try {
            final PacketsMapping packetsMapping = new PacketsMapping();
            TraceFormat2Read trace = new TraceFormat2Read(
                    file,
                    new OverImpl() {
                        @Override
                        protected void whenOverImpl() {
                            // clean up by hand
                        }
                    },
                    new ReentrantLock(),
                    packetsMapping.getNewPacketHeaderData(),
                    packetsMapping.getRequiredPostSendRules()
            );

            System.out.println(trace.getHumanReadableHeader());
            while (true) {
                System.out.println(trace.readRecord().toString());
            }
        } catch (DoneReading e) {
            System.out.println("Done");
        } finally {
            file.close();
        }
    }
}
