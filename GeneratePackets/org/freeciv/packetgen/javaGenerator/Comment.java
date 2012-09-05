/*
 * Copyright (c) 2012, Sveinung Kvilhaugsvik
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

package org.freeciv.packetgen.javaGenerator;

import org.freeciv.packetgen.javaGenerator.expression.util.Formatted;

import java.util.*;

public class Comment extends Formatted implements HasAtoms {
    private final boolean isDoc;
    private final List<String> comment;

    private Comment(boolean doc, String... comment) {
        isDoc = doc;
        this.comment = new ArrayList<String>();
        for (String commentLine : comment)
            this.comment.add(commentLine);
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        if (comment.isEmpty())
            return;

        if (isDoc)
            to.add(JDocStart);
        else
            to.add(CCommentStart);
        for (String part : comment)
            to.add(new IR.CodeAtom(part));
        to.add(CCommentEnd);
    }

    public boolean isEmpty() {
        return comment.isEmpty();
    }

    @Deprecated
    public static Comment oldCompat(String comment) {
        if (null == comment || "".equals(comment))
            return Comment.no();
        else
            return Comment.c(comment.split("\n"));
    }

    public static Comment no() {
        return new Comment(false);
    }

    public static Comment c(String... comment) {
        return new Comment(false, comment);
    }

    public static Comment doc(String... comment) {
        return new Comment(true, comment);
    }
}
