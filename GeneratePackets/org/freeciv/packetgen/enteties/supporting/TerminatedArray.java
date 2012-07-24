package org.freeciv.packetgen.enteties.supporting;

import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.Constant;
import org.freeciv.packetgen.enteties.FieldTypeBasic;
import org.freeciv.packetgen.javaGenerator.expression.creators.ExprFrom1;
import org.freeciv.packetgen.javaGenerator.expression.util.WrapCodeString;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AString;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;

import java.util.Arrays;

import static org.freeciv.packetgen.Hardcoded.arrayEaterScopeCheck;

// TODO: Generalize to NetworkIO. Then use for String.
// Perhaps also have the generalized version output an Array of the referenced objects in stead of their number.
public class TerminatedArray extends FieldTypeBasic {
    public TerminatedArray(String dataIOType, String publicType, Requirement maxSizeConstant, Requirement terminator) {
        super(dataIOType, publicType, "byte[]",
              new String[]{
                      arrayEaterScopeCheck(Constant.referToInJavaCode(maxSizeConstant) + " < value.length"),
                      "this.value = value;"
              },
              "byte[] buffer = new byte[" + Constant.referToInJavaCode(maxSizeConstant) + "];" + "\n" +
                      "byte current = from.readByte();" + "\n" +
                      "int pos = 0;" + "\n" +
                      "while (((byte)" + Constant.referToInJavaCode(terminator) + ") != current) {" + "\n" +
                      "buffer[pos] = current;\n" +
                      "pos++;" + "\n" +
                      "if (pos < " + Constant.referToInJavaCode(maxSizeConstant) + ") {" + "\n" +
                      "current = from.readByte();" + "\n" +
                      "} else {" + "\n" +
                      "break;" + "\n" +
                      "}" + "\n" +
                      "}" + "\n" +
                      "value = java.util.Arrays.copyOf(buffer, pos);",
              "to.write(value);\n" +
                      "if (value.length < " + Constant.referToInJavaCode(maxSizeConstant) + ") {" + "\n" +
                      "to.writeByte(" + Constant.referToInJavaCode(terminator) + ");" + "\n" +
                      "}",
              "return " + "value.length + (value.length < " + Constant.referToInJavaCode(maxSizeConstant) + "?1:0);",
              new ExprFrom1<AString, AValue>() {
                  @Override
                  public AString getCodeFor(AValue arg1) {
                      return WrapCodeString.asAString("org.freeciv.Util.joinStringArray(" + arg1 + ", \" \")");
                  }
              },
              true, Arrays.asList(maxSizeConstant, terminator));

    }
}
