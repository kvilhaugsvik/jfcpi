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

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestSettings {
    @Test
    public void setting_constructor_works() throws NoSuchMethodException {
        new Setting<Integer>("number", 5, Integer.class, Integer.class.getMethod("decode", String.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setting_constructor_noName() throws NoSuchMethodException {
        new Setting<Integer>(null, 5, Integer.class, Integer.class.getMethod("decode", String.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setting_constructor_name_forbidMinusAtTheStart() throws NoSuchMethodException {
        new Setting<Integer>("-forgot", 5, Integer.class, Integer.class.getMethod("decode", String.class));
    }

    @Test
    public void setting_constructor_name_permitMinusInside() throws NoSuchMethodException {
        new Setting<Integer>("setting-like-this", 5, Integer.class, Integer.class.getMethod("decode", String.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setting_constructor_name_forbidSpaceInName() throws NoSuchMethodException {
        new Setting<Integer>("setting containing space", 5, Integer.class, Integer.class.getMethod("decode", String.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setting_constructor_name_forbidEmptyString() throws NoSuchMethodException {
        new Setting<Integer>("", 5, Integer.class, Integer.class.getMethod("decode", String.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setting_constructor_noKind() throws NoSuchMethodException {
        new Setting<Integer>("number", 5, null, Integer.class.getMethod("decode", String.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setting_constructor_noConverter() throws NoSuchMethodException {
        new Setting<Integer>("number", 5, Integer.class, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setting_constructor_wrongMethod_dynamic() throws NoSuchMethodException {
        new Setting<Integer>("number", 5, Integer.class, WrongMethods.class.getMethod("dynamic", String.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setting_constructor_wrongMethod_returnsObject() throws NoSuchMethodException {
        new Setting<Integer>("number", 5, Integer.class, WrongMethods.class.getMethod("wrongReturn", String.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setting_constructor_wrongMethod_twoParams() throws NoSuchMethodException {
        new Setting<Integer>("number", 5, Integer.class, WrongMethods.class.getMethod("two_params", String.class, String.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setting_constructor_wrongMethod_wrongParamType() throws NoSuchMethodException {
        new Setting<Integer>("number", 5, Integer.class, WrongMethods.class.getMethod("wrongParamType", Integer.class));
    }

    @Test
    public void setting_name() throws NoSuchMethodException {
        final Setting<Integer> setting =
                new Setting<Integer>("number", 5, Integer.class, Integer.class.getMethod("decode", String.class));

        assertEquals("number", setting.getName());
    }

    @Test
    public void setting_settable_Exist() throws NoSuchMethodException {
        final Setting<Integer> setting =
                new Setting<Integer>("number", 5, Integer.class, Integer.class.getMethod("decode", String.class));

        assertNotNull(setting.getSettable());
    }

    @Test
    public void setting_settable_defaultWhenNotSet() throws NoSuchMethodException {
        final Setting<Integer> setting =
                new Setting<Integer>("number", 5, Integer.class, Integer.class.getMethod("decode", String.class));
        final Setting<Integer>.Settable settable = setting.getSettable();

        assertEquals(5, settable.get().intValue());
    }

    @Test
    public void setting_settable_isSet() throws NoSuchMethodException, InvocationTargetException {
        final Setting<Integer> setting =
                new Setting<Integer>("number", 5, Integer.class, Integer.class.getMethod("decode", String.class));
        final Setting<Integer>.Settable settable = setting.getSettable();

        settable.setTo("3");

        assertEquals(3, settable.get().intValue());
    }

    @Test
    public void setting_settable_isSet_boolean() throws InvocationTargetException, NoSuchMethodException {
        final Setting<Boolean> setting =
                new Setting<Boolean>("switch", false, Boolean.class, Boolean.class.getMethod("valueOf", String.class));
        final Setting<Boolean>.Settable settable = setting.getSettable();

        settable.setTo("true");

        assertTrue(settable.get());
    }

    @Test
    public void setting_settable_boolean_isSet_nullIsTrue() throws NoSuchMethodException, InvocationTargetException {
        final Setting<Boolean> setting =
                new Setting<Boolean>("switch", false, Boolean.class, Boolean.class.getMethod("valueOf", String.class));
        final Setting<Boolean>.Settable settable = setting.getSettable();

        settable.setTo(null);

        assertTrue("To enable --verbose in stead of --verbose=true null should be seen as true", settable.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setting_settable_integerNotNull() throws NoSuchMethodException, InvocationTargetException {
        final Setting<Integer> setting =
                new Setting<Integer>("number", 5, Integer.class, Integer.class.getMethod("decode", String.class));
        final Setting<Integer>.Settable settable = setting.getSettable();

        settable.setTo(null);
    }

    @Test
    public void setting_boolHelper() throws InvocationTargetException {
        final Setting<Boolean>.Settable settable = new Setting.BoolSetting("switch", false).getSettable();

        settable.setTo("true");

        assertTrue(settable.get());
    }

    @Test
    public void setting_intHelper() throws InvocationTargetException {
        final Setting<Integer>.Settable settable = new Setting.IntSetting("switch", 5).getSettable();

        settable.setTo("3");

        assertEquals(3, settable.get().intValue());
    }

    @Test
    public void setting_stringHelper() throws InvocationTargetException {
        final Setting<String>.Settable settable = new Setting.StringSetting("field", "value").getSettable();

        settable.setTo("other");

        assertEquals("other", settable.get());
    }


    private static class WrongMethods {
        public Integer dynamic(String arg) {
            return 5;
        }

        public static Object wrongReturn(String arg) {
            return "5";
        }

        public static Integer two_params(String arg1, String arg2) {
            return 5;
        }

        public static Integer wrongParamType(Integer arg) {
            return arg;
        }
    }
}
