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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.freeciv.utility.Validation.validateMethodIsAConverter;
import static org.freeciv.utility.Validation.validateMethodIsStatic;
import static org.freeciv.utility.Validation.validateNotNull;

public class Setting<Kind> {
    private final String name;
    private final Kind defaultValue;

    private final Class<Kind> kind;
    private final Method fromString;
    private final String description;

    public Setting(String name, Kind defaultValue, Class<Kind> kind, Method fromString, String description) {
        validateSettingName(name);
        this.name = name;

        validateNotNull(defaultValue, "default value");
        this.defaultValue = defaultValue;

        validateNotNull(kind, "kind");
        this.kind = kind;

        Setting.<Kind>validateMethodSignature(kind, fromString);
        this.fromString = fromString;

        validateNotNull(description, "description");
        this.description = description;
    }

    private void validateSettingName(String name) {
        validateNotNull(name, "name");
        if ("".equals(name) || '-' == name.charAt(0) || name.contains(" "))
            throw new IllegalArgumentException("Name of setting \"" + name + "\" not allowed.");
    }

    private static <Kind> void validateMethodSignature(Class<Kind> kind, Method fromString) {
        validateNotNull(fromString, "from String converter");
        validateMethodIsStatic(fromString);
        validateMethodIsAConverter(fromString, kind, String.class);
    }

    public String getName() {
        return name;
    }

    public Settable getSettable() {
        return new Settable();
    }

    public class Settable {
        private Kind value = null;

        public Kind get() {
            if (null == value)
                return defaultValue;
            else
                return value;
        }

        /**
         * Set the value of the setting by interpreting the String parameter
         * @param value a String interpretable by the Setting's interpreter method
         */
        public void setTo(String value) throws InvocationTargetException {
            if (null == value)
                if (Boolean.class.equals(kind))
                    value = "true";
                else
                    throw new IllegalArgumentException(name + " set to null");

            try {
                this.value = kind.cast(fromString.invoke(null, value));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Wrong conversion data provided to setting " + name, e);
            }
        }

        public String name() {
            return name;
        }

        public String describe() {
            return description;
        }
    }


    private static Method getBuiltInMethod(Class<?> on, String named, Class<?> paramType) {
        try {
            return on.getMethod(named, paramType);
        } catch (NoSuchMethodException e) {
            throw new Error("Major assumption did not hold", e);
        }
    }

    public static class IntSetting extends Setting<Integer> {
        public IntSetting(String name, Integer defaultValue, String helpText) {
            super(name, defaultValue, Integer.class, getBuiltInMethod(Integer.class, "decode", String.class), helpText);
        }
    }

    public static class BoolSetting extends Setting<Boolean> {
        public BoolSetting(String name, Boolean defaultValue, String helpText) {
            super(name, defaultValue, Boolean.class, getBuiltInMethod(Boolean.class, "valueOf", String.class), helpText);
        }
    }

    public static class StringSetting extends Setting<String> {
        public StringSetting(String name, String defaultValue, String helpText) {
            super(name, defaultValue, String.class, getBuiltInMethod(String.class, "valueOf", Object.class), helpText);
        }
    }
}
