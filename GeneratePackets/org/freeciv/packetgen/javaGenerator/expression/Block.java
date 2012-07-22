package org.freeciv.packetgen.javaGenerator.expression;

import org.freeciv.Util;
import org.freeciv.packetgen.javaGenerator.expression.util.WrapCodeString;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.NoValue;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.Returnable;

import java.util.LinkedList;

/***
 * Represents a block of statements.
 *
 * To avoid having to define statements as a separate abstraction pretend that void
 * is a Java type (even if it strictly isn't) like Scala's Unit. Treat expressions
 * as statements if added to a block. Also pretend that all expressions are valid
 * as statements.
 *
 * Disadvantages of this approach include some bugs won't be detected until the generated
 * code is compiled and that putting a comment on a statement becomes harder.
 *
 */
public class Block implements NoValue {
    private final LinkedList<Returnable> statements = new LinkedList<Returnable>();

    // Empty blocks that don't tell why is ugly
    public Block(Returnable firstStatement) {
        statements.add(firstStatement);
    }

    public Block(String firstStatement) {
        this(WrapCodeString.asVoid(firstStatement));
    }

    public void addStatement(Returnable statement) {
        statements.add(statement);
    }

    @Override
    public String getJavaCode() {
        return Util.joinStringArray(basicFormatBlock(), "\n", "{", "}");
    }

    private String[] basicFormatBlock() {
        String[] out = new String[statements.size()];
        for (int pos = 0; pos < out.length; pos++) {
            out[pos] = statements.get(pos).getJavaCode() + ";";
        }
        return out;
    }

    public String[] getJavaCodeLines() {
        return basicFormatBlock();
    }
}
