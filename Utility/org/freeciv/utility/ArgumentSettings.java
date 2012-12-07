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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArgumentSettings {
    private static final Pattern extractor = Pattern.compile("--?(\\w[-\\w]*)(=(.*))?");

    private final List<String> unrecognized_all;
    private final List<String> unrecognized_optionish;
    private final List<String> unrecognized_unknown;

    private final HashMap<String, String> settings;

    /**
     * A store for setting that may be overridden on the command line
     * @param defaults The default settings. No setting without a default will be considered.
     * @param args arguments given as "--setting=value". "--setting" is a shortcut for "--setting=true"
     */
    public ArgumentSettings(Map<String, String> defaults, String... args) {
        for (String name : defaults.keySet())
            if (!(extractor.matcher("--" + name).matches()))
                throw new IllegalArgumentException("Name of setting \"" + name + "\" not allowed.");

        this.settings = new HashMap<String, String>(defaults);

        List<String> probablyMisspelled = new LinkedList<String>();
        List<String> unknown = new LinkedList<String>();
        for (String argument : args) {
            Matcher result = extractor.matcher(argument);
            if (result.matches()) {
                String key = result.group(1);
                if (defaults.containsKey(key)) {
                    String value = result.group(3);
                    if (null == value) value = "true";
                    settings.put(key, value);
                } else {
                    probablyMisspelled.add(argument);
                }
            } else {
                unknown.add(argument);
            }
        }

        unrecognized_optionish = Collections.unmodifiableList(probablyMisspelled);
        unrecognized_unknown = Collections.unmodifiableList(unknown);
        LinkedList<String> all_unrecognized = new LinkedList<String>(unrecognized_optionish);
        all_unrecognized.addAll(unrecognized_unknown);
        unrecognized_all = Collections.unmodifiableList(all_unrecognized);
    }

    public String getSetting(String named) {
        if (settings.containsKey(named))
            return settings.get(named);
        else
            throw new IllegalArgumentException("No setting " + named + " stored");
    }

    public List<String> getUnrecognized(UnrecognizedKind kind) {
        switch (kind) {
            case PROBABLE_MISSPELLINGS:
                return unrecognized_optionish;
            case UNKNOWN:
                return unrecognized_unknown;
            default:
                return unrecognized_all;
        }
    }

    public static enum UnrecognizedKind {PROBABLE_MISSPELLINGS, UNKNOWN, ALL}
}
