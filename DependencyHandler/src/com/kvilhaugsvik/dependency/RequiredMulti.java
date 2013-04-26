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

package com.kvilhaugsvik.dependency;

import java.util.regex.Pattern;

public class RequiredMulti implements Required {
    private final Class<? extends ReqKind> kind;
    protected final Pattern nameMatches;

    public RequiredMulti(Class<? extends ReqKind> kind, Pattern nameMatches) {
        this.kind = kind;
        this.nameMatches = nameMatches;
    }

    @Override
    public Class<? extends ReqKind> getKind() {
        return kind;
    }

    @Override
    public boolean canFulfill(Requirement req) {
        return kind.equals(req.getKind()) && nameMatches.matcher(req.getName()).matches();
    }

    @Override
    public String toString() {
        return "Fulfills any requirement of the kind " + kind + " that matches " + nameMatches.pattern();
    }
}
