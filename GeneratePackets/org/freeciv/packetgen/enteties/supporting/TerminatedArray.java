package org.freeciv.packetgen.enteties.supporting;

import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.Constant;
import org.freeciv.packetgen.enteties.FieldTypeBasic;
import org.freeciv.packetgen.javaGenerator.Var;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.creators.ExprFrom1;
import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.*;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AString;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;

import java.util.Arrays;

import static org.freeciv.packetgen.Hardcoded.arrayEaterScopeCheck;

// TODO: Generalize to NetworkIO. Then use for String.
// Perhaps also have the generalized version output an Array of the referenced objects in stead of their number.
public class TerminatedArray extends FieldTypeBasic {
    public TerminatedArray(String dataIOType, String publicType, final Requirement maxSizeConstant, final Requirement terminator) {
        super(dataIOType, publicType, "byte[]",
                new ExprFrom1<Block, Var>() {
                    @Override
                    public Block x(Var to) {
                        return new Block(
                                arrayEaterScopeCheck(Constant.referToInJavaCode(maxSizeConstant) + " < value.length"),
                                to.assign(asAValue("value")));
                    }
                },
                new ExprFrom1<Block, Var>() {
                    @Override
                    public Block x(Var to) {
                        Var buf = Var.local("byte[]", "buffer",
                                asAValue("new byte[" +
                                        Constant.referToInJavaCode(maxSizeConstant) + "]"));
                        Var current = Var.local("byte", "current",
                                asAValue("from.readByte()"));
                        Var pos = Var.local("int", "pos", asAnInt("0"));
                        return new Block(buf, current, pos,
                                WHILE(asBool("((byte)" + Constant.referToInJavaCode(terminator) + ") != current"),
                                        new Block(asVoid("buffer[pos] = current"),
                                                asVoid("pos++"),
                                                IF(asBool("pos < " + Constant.referToInJavaCode(maxSizeConstant)),
                                                        new Block(current.assign(asAnInt("from.readByte()"))),
                                                        new Block(asVoid("break"))))),
                                to.assign((asAValue("java.util.Arrays.copyOf(buffer, pos)"))));
                    }
                },
              "to.write(value);\n" +
                      "if (value.length < " + Constant.referToInJavaCode(maxSizeConstant) + ") {" + "\n" +
                      "to.writeByte(" + Constant.referToInJavaCode(terminator) + ");" + "\n" +
                      "}",
              "return " + "value.length + (value.length < " + Constant.referToInJavaCode(maxSizeConstant) + "?1:0);",
              new ExprFrom1<AString, AValue>() {
                  @Override
                  public AString x(AValue arg1) {
                      return asAString("org.freeciv.Util.joinStringArray(" + arg1 + ", \" \")");
                  }
              },
              true, Arrays.asList(maxSizeConstant, terminator));

    }
}
