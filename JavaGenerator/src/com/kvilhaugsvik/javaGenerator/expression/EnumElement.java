/*
 * Copyright (c) 2011, 2012, Sveinung Kvilhaugsvik
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

package com.kvilhaugsvik.javaGenerator.expression;

import com.kvilhaugsvik.javaGenerator.Comment;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AValue;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.NoValue;

public class EnumElement extends MethodCall<NoValue> {
    protected EnumElement(Comment comment, String elementName, Typed<? extends AValue>... params) {
        super(comment, elementName, params);
    }

    public String getEnumValueName() {
        return super.method.toString();
    }

    public static EnumElement newEnumValue(String enumValueName, Typed<? extends AValue>... params) {
        return new EnumElement(Comment.no(), enumValueName, params);
    }

    public static EnumElement newEnumValue(Comment comment, String enumValueName, Typed<? extends AValue>... params) {
        return new EnumElement(comment, enumValueName, params);
    }
}
