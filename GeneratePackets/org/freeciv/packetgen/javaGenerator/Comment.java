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
    private final List<HasAtoms> comment;

    private Comment(boolean doc, String... comment) {
        isDoc = doc;
        this.comment = wrapWords(comment);
    }

    private static List<HasAtoms> wrapWords(String... words) {
        List<HasAtoms> out = new LinkedList<HasAtoms>();
        for (String commentLine : words)
            out.add(new Word(commentLine));
        return out;
    }

    public static class Word extends IR.CodeAtom {
        public Word(String word) {
            super(word);
        }
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        if (comment.isEmpty())
            return;

        if (isDoc)
            to.add(JDocStart);
        else
            to.add(CCommentStart);
        for (HasAtoms part : comment)
            part.writeAtoms(to);
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
