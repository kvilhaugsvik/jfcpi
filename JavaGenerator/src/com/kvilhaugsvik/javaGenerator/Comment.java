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

package com.kvilhaugsvik.javaGenerator;

import com.kvilhaugsvik.javaGenerator.util.Formatted;
import com.kvilhaugsvik.javaGenerator.representation.CodeAtoms;
import com.kvilhaugsvik.javaGenerator.representation.HasAtoms;
import com.kvilhaugsvik.javaGenerator.representation.IR;

import java.util.*;

public class Comment extends Formatted implements HasAtoms {
    private final boolean isDoc;
    private final List<HasAtoms> comment;

    private Comment(boolean doc, HasAtoms... body) {
        isDoc = doc;
        this.comment = Arrays.asList(body);
    }

    public static class TextBlock implements HasAtoms {
        private final List<Word> text;

        public TextBlock(String... description) {
            text = new LinkedList<Word>();
            for (String part : description) {
                if ("".equals(part))
                    break;
                String[] desc = part.split("\\s+");
                for (String word : desc)
                    text.add(new Word(word));
            }
        }

        @Override
        public void writeAtoms(CodeAtoms to) {
            for (Word word : text)
                word.writeAtoms(to);
        }
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

    public static Comment no() {
        return new Comment(false);
    }

    public static Comment c(String... comment) {
        return new Comment(false, new TextBlock(comment));
    }

    public static Comment doc(String summary, String description, JDocTag... tags) {
        if (summaryIsInvalid(summary))
            throw new IllegalArgumentException("The summary can only contain one sentence.");

        HasAtoms[] body = new HasAtoms[2 + tags.length];
        body[0] = new TextBlock(summary);
        body[1] = new TextBlock(description);
        System.arraycopy(tags, 0, body, 2, tags.length);

        return new Comment(true, body);
    }

    private static boolean summaryIsInvalid(String summary) {
        return null == summary || summary.contains(".");
    }

    public static JDocTag param(Var parameter, String description) {
        return new JDocTag(new Annotate.Atom("param"), parameter.ref(), new TextBlock(description));
    }

    public static JDocTag docThrows(TargetClass throwz, String description) {
        return new JDocTag(new Annotate.Atom("throws"), throwz, new TextBlock(description));
    }

    public static JDocTag docReturns(String description) {
        return new JDocTag(new Annotate.Atom("return"), new TextBlock(description));
    }

    public static class JDocTag implements HasAtoms {
        private final Annotate.Atom tag;
        private final HasAtoms[] params;

        private JDocTag(Annotate.Atom tag, HasAtoms... params) {
            this.tag = tag;
            this.params = params;
        }

        @Override
        public void writeAtoms(CodeAtoms to) {
            tag.writeAtoms(to);
            for (HasAtoms param : params)
                param.writeAtoms(to);
        }
    }
}
