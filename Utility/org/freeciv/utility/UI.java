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

package org.freeciv.utility;

public class UI {
    private static final String HELP = "help";
    public static final Setting.BoolSetting HELP_SETTING =
            new Setting.BoolSetting(UI.HELP, false, "show this message and exit");

    public static void printAndExitOnHelp(ArgumentSettings settings, Class<?> running) {
        if (settings.<Boolean>getSetting(UI.HELP)) {
            System.out.println("Usage: " + running.getCanonicalName() + " [options]");
            for (Setting.Settable setting : settings.getAll()) {
                StringBuilder line = new StringBuilder("\t--");
                line.append(setting.name());
                line.append(" = ");
                line.append(setting.get());
                line.append("\t- ");
                line.append(setting.describe());

                System.out.println(line.toString());
            }
            System.out.println();
            System.exit(0);
        }
    }
}
