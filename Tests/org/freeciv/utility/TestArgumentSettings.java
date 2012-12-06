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

import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestArgumentSettings {
    @Test
    public void getExistingSetting() {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults());

        assertEquals("Existing setting (came as a default) is missing", "5", settings.getSetting("setting1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNonExistingSetting() {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults());

        settings.getSetting("randomStuffThat");
    }

    @Test
    public void defaultNotChangedIsStillDefault() {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults(), "--setting2=3");

        assertEquals("This default shouldn't change", "15", settings.getSetting("setting3"));
    }

    @Test
    public void defaultChangedIsNewValue() {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults(), "--setting2=3");

        assertEquals("Failed to return new value", "3", settings.getSetting("setting2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void doNotAcceptSettingWithNoDefault() {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults(), "--days=560");
        settings.getSetting("days");
    }

    @Test
    public void syntaxMinusMinusEquals() {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults(), "--setting3=true");

        assertEquals("Wrong value", "true", settings.getSetting("setting3"));
    }

    @Test
    public void syntaxMinusEquals() {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults(), "-setting3=true");

        assertEquals("Wrong value", "true", settings.getSetting("setting3"));
    }

    @Test
    public void syntaxMinusMinus() {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults(), "--setting3");

        assertEquals("Wrong value", "true", settings.getSetting("setting3"));
    }

    @Test
    public void syntaxMinus() {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults(), "-setting3");

        assertEquals("Wrong value", "true", settings.getSetting("setting3"));
    }

    @Test
    public void defaultSurvivesUnknown() {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults(), "--settingggg=3");

        assertEquals("This default shouldn't change", "15", settings.getSetting("setting3"));
    }

    @Test
    public void laterOptionSurvivesUnknown() {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults(), "--settingggg=3", "-setting3");

        assertEquals("This default shouldn't change", "true", settings.getSetting("setting3"));
    }

    @Test
    public void unrecognized_PROBABLE_MISSPELLINGS() {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults(), "--settingggg=3", "-setting3", "7");

        List<String> unrecognized = settings.getUnrecognized(ArgumentSettings.UnrecognizedKind.PROBABLE_MISSPELLINGS);

        assertTrue("Probable misspelling missing", unrecognized.contains("--settingggg=3"));
        assertFalse("Unlikely misspelling included", unrecognized.contains("7"));
        assertFalse("Valid setting included", unrecognized.contains("-setting3"));
    }

    @Test
    public void unrecognized_UNKNOWN() {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults(), "--settingggg=3", "-setting3", "7");

        List<String> unrecognized = settings.getUnrecognized(ArgumentSettings.UnrecognizedKind.UNKNOWN);

        assertFalse("Probable misspelling missing included", unrecognized.contains("--settingggg=3"));
        assertTrue("Unknown misspelling missing", unrecognized.contains("7"));
        assertFalse("Valid setting included", unrecognized.contains("-setting3"));
    }

    @Test
    public void unrecognized_ALL() {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults(), "--settingggg=3", "-setting3", "7");

        List<String> unrecognized = settings.getUnrecognized(ArgumentSettings.UnrecognizedKind.ALL);

        assertTrue("Probable misspelling missing", unrecognized.contains("--settingggg=3"));
        assertTrue("Unknown misspelling missing", unrecognized.contains("7"));
        assertFalse("Valid setting included", unrecognized.contains("-setting3"));
    }


    private HashMap<String, String> simpleDefaults() {
        HashMap<String, String> defaults = new HashMap<String, String>();
        defaults.put("setting1", "5");
        defaults.put("setting2", "10");
        defaults.put("setting3", "15");

        return defaults;
    }
}
