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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestArgumentSettings {
    @Test
    public void getExistingSetting() throws InvocationTargetException {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults());

        assertEquals("Existing setting (came as a default) is missing", 5, settings.getSetting("setting1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNonExistingSetting() throws InvocationTargetException {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults());

        settings.getSetting("randomStuffThat");
    }

    @Test
    public void defaultNotChangedIsStillDefault() throws InvocationTargetException {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults(), "--setting2=3");

        assertEquals("This default shouldn't change", false, settings.getSetting("setting3"));
    }

    @Test
    public void defaultChangedIsNewValue() throws InvocationTargetException {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults(), "--setting2=3");

        assertEquals("Failed to return new value", 3, settings.getSetting("setting2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void doNotAcceptSettingWithNoDefault() throws InvocationTargetException {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults(), "--days=560");
        settings.getSetting("days");
    }

    @Test
    public void syntaxMinusMinusEquals() throws InvocationTargetException {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults(), "--setting3=true");

        assertEquals("Wrong value", true, settings.getSetting("setting3"));
    }

    @Test
    public void syntaxMinusEquals() throws InvocationTargetException {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults(), "-setting3=true");

        assertEquals("Wrong value", true, settings.getSetting("setting3"));
    }

    @Test
    public void syntaxMinusMinus() throws InvocationTargetException {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults(), "--setting3");

        assertEquals("Wrong value", true, settings.getSetting("setting3"));
    }

    @Test
    public void syntaxMinus() throws InvocationTargetException {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults(), "-setting3");

        assertEquals("Wrong value", true, settings.getSetting("setting3"));
    }

    @Test
    public void defaultSurvivesUnknown() throws InvocationTargetException {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults(), "--settingggg=3");

        assertEquals("This default shouldn't change", false, settings.getSetting("setting3"));
    }

    @Test
    public void laterOptionSurvivesUnknown() throws InvocationTargetException {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults(), "--settingggg=3", "-setting3");

        assertEquals("This default should change", true, settings.getSetting("setting3"));
    }

    @Test
    public void unrecognized_PROBABLE_MISSPELLINGS() throws InvocationTargetException {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults(), "--settingggg=3", "-setting3", "7");

        List<String> unrecognized = settings.getUnrecognized(ArgumentSettings.UnrecognizedKind.PROBABLE_MISSPELLINGS);

        assertTrue("Probable misspelling missing", unrecognized.contains("--settingggg=3"));
        assertFalse("Unlikely misspelling included", unrecognized.contains("7"));
        assertFalse("Valid setting included", unrecognized.contains("-setting3"));
    }

    @Test
    public void unrecognized_UNKNOWN() throws InvocationTargetException {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults(), "--settingggg=3", "-setting3", "7");

        List<String> unrecognized = settings.getUnrecognized(ArgumentSettings.UnrecognizedKind.UNKNOWN);

        assertFalse("Probable misspelling missing included", unrecognized.contains("--settingggg=3"));
        assertTrue("Unknown misspelling missing", unrecognized.contains("7"));
        assertFalse("Valid setting included", unrecognized.contains("-setting3"));
    }

    @Test
    public void unrecognized_ALL() throws InvocationTargetException {
        ArgumentSettings settings = new ArgumentSettings(simpleDefaults(), "--settingggg=3", "-setting3", "7");

        List<String> unrecognized = settings.getUnrecognized(ArgumentSettings.UnrecognizedKind.ALL);

        assertTrue("Probable misspelling missing", unrecognized.contains("--settingggg=3"));
        assertTrue("Unknown misspelling missing", unrecognized.contains("7"));
        assertFalse("Valid setting included", unrecognized.contains("-setting3"));
    }

    @Test
    public void name_permitMinusInSettingName() throws InvocationTargetException {
        List<Setting<?>> defaults = new LinkedList<Setting<?>>();
        defaults.add(new Setting.StringSetting("setting-like-this", "fail"));
        ArgumentSettings settings = new ArgumentSettings(defaults, "--setting-like-this=win");

        assertEquals("Should override default", "win", settings.getSetting("setting-like-this"));
    }

    @Test
    public void implementationDetail_ArgumentSettings_prepareSettable_converts() throws NoSuchMethodException {
        final List<Setting<?>> settings = new LinkedList<Setting<?>>();
        settings.add(new Setting<Integer>("number", 5, Integer.class, Integer.class.getMethod("decode", String.class)));

        final HashMap<String, Setting.Settable> result = ArgumentSettings.prepareSettable(settings);

        assertTrue(result.containsKey("number"));
        assertEquals(5, result.get("number").get());
    }


    private List<Setting<?>> simpleDefaults() {
        List<Setting<?>> defaults = new LinkedList<Setting<?>>();
        defaults.add(new Setting.IntSetting("setting1", 5));
        defaults.add(new Setting.IntSetting("setting2", 10));
        defaults.add(new Setting.BoolSetting("setting3", false));

        return defaults;
    }
}
