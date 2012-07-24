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

import org.freeciv.packetgen.javaGenerator.expression.util.Formatted;
import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.*;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.NoValue;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.Returnable;
import org.freeciv.packetgen.javaGenerator.CodeAtoms;
import org.freeciv.packetgen.javaGenerator.HasAtoms;

import java.util.Collections;
import java.util.LinkedList;

public class Block extends Formatted implements NoValue {
    private final LinkedList<Statement> statements = new LinkedList<Statement>();

    public Block(Returnable... firstStatements) {
        for (Returnable statement : firstStatements)
            statements.add(new Statement(statement));
    }

    public void addStatement(Statement statement) {
        statements.add(statement);
    }

    public void addStatement(Returnable statement) {
        statements.add(new Statement(statement));
    }

    public String[] getJavaCodeLines() {
        return basicFormatBlock();
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        for (HasAtoms statement : statements) {
            statement.writeAtoms(to);
        }
    }

    @Deprecated public static Block fromStrings(String... firstStatements) {
        Block out = new Block();
        for (String statement : firstStatements)
            out.addStatement(asVoid(statement));
        return out;
    }
}
