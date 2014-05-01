/*
 * Copyright (c) 2014. Sveinung Kvilhaugsvik
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

package com.kvilhaugsvik.dependency;

import java.util.Collection;
import java.util.Collections;

/**
 * A dependency item that stores a String.
 */
public class StringItem implements Dependency.Item, ReqKind {
    private final String itemName;
    private final String value;

    /**
     * Construct a new String item.
     * @param itemName the name of the item. This is what will be asked for.
     * @param value the String value it stores.
     */
    public StringItem(String itemName, String value) {
        this.value = value;
        this.itemName = itemName;
    }

    /**
     * Get the stored String
     * @return the stored String
     */
    public String getValue() {
        return value;
    }

    @Override
    public Collection<Requirement> getReqs() {
        return Collections.<Requirement>emptySet();
    }

    @Override
    public Requirement getIFulfillReq() {
        return new Requirement(itemName, StringItem.class);
    }
}
