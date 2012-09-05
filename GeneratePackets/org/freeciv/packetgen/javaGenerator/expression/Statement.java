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

package org.freeciv.packetgen.javaGenerator.expression;

import org.freeciv.packetgen.javaGenerator.CodeAtoms;
import org.freeciv.packetgen.javaGenerator.Comment;
import org.freeciv.packetgen.javaGenerator.HasAtoms;
import org.freeciv.packetgen.javaGenerator.IR;
import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.util.Formatted;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.Returnable;

/**
 * Represents a complete statement
 */
public class Statement extends Formatted implements HasAtoms {
    private final Typed<? extends Returnable> statement;
    private final Comment comment;

    public Statement(Typed<? extends Returnable> statement) {
        this(statement, Comment.no());
    }

    @Deprecated
    public Statement(Typed<? extends Returnable> statement, String comment) {
        this(statement, Comment.oldCompat(comment));
    }

    public Statement(Typed<? extends Returnable> statement, Comment comment) {
        this.statement = statement;
        this.comment = comment;
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        comment.writeAtoms(to);
        statement.writeAtoms(to);
        to.add(HasAtoms.EOL);
    }
}
