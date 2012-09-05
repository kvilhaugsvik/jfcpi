package org.freeciv.packetgen.enteties.supporting;

import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.Constant;
import org.freeciv.packetgen.enteties.FieldTypeBasic;
import org.freeciv.packetgen.javaGenerator.MethodCall;
import org.freeciv.packetgen.javaGenerator.TargetClass;
import org.freeciv.packetgen.javaGenerator.Var;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.creators.ExprFrom1;
import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.*;

import org.freeciv.packetgen.javaGenerator.expression.creators.ExprFrom2;
import org.freeciv.packetgen.javaGenerator.expression.creators.Typed;
import org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AString;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AValue;
import org.freeciv.packetgen.javaGenerator.expression.willReturn.AnInt;

import java.util.Arrays;

import static org.freeciv.packetgen.Hardcoded.arrayEaterScopeCheck;

// TODO: Generalize to NetworkIO. Then use for String.
// Perhaps also have the generalized version output an Array of the referenced objects in stead of their number.
public class TerminatedArray extends FieldTypeBasic {
    public TerminatedArray(String dataIOType, String publicType, final Requirement maxSizeConstant, final Requirement terminator) {
        super(dataIOType, publicType, new TargetClass("byte[]"),
                new ExprFrom1<Block, Var>() {
                    @Override
                    public Block x(Var to) {
                        return new Block(
                                arrayEaterScopeCheck(Constant.referToInJavaCode(maxSizeConstant) + " < value.length"),
                                to.assign(asAValue("value")));
                    }
                },
                new ExprFrom2<Block, Var, Var>() {
                    @Override
                    public Block x(Var to, Var from) {
                        Var buf = Var.local("byte[]", "buffer",
                                asAValue("new byte[" +
                                        Constant.referToInJavaCode(maxSizeConstant) + "]"));
                        Var current = Var.local("byte", "current", from.<AValue>call("readByte"));
                        Var pos = Var.local("int", "pos", asAnInt("0"));
                        return new Block(buf, current, pos,
                                WHILE(asBool("((byte)" + Constant.referToInJavaCode(terminator) + ") != current"),
                                        new Block(asVoid("buffer[pos] = current"),
                                                asVoid("pos++"),
                                                IF(asBool("pos < " + Constant.referToInJavaCode(maxSizeConstant)),
                                                        new Block(current.assign(from.<AValue>call("readByte"))),
                                                        new Block(asVoid("break"))))),
                                to.assign(new MethodCall<AValue>("java.util.Arrays.copyOf",
                                        buf.ref(), pos.ref())));
                    }
                },
                new ExprFrom2<Block, Var, Var>() {
                    @Override
                    public Block x(Var val, Var to) {
                        return new Block(
                                to.call("write", val.ref()),
                                IF(isSmallerThan(val.<AnInt>read("length"), asAnInt(Constant.referToInJavaCode(maxSizeConstant))),
                                        new Block(to.call("writeByte", asAValue(Constant.referToInJavaCode(terminator))))));
                    }
                },
              new ExprFrom1<Typed<AnInt>, Var>() {
                    @Override
                    public Typed<AnInt> x(Var value) {
                        return BuiltIn.<AnInt>sum(
                                asAnInt("this.value.length"),
                                R_IF(asBool("this.value.length < " + Constant.referToInJavaCode(maxSizeConstant)),
                                        asAnInt("1"),
                                        asAnInt("0")));
                    }
              },
              new ExprFrom1<Typed<AString>, Var>() {
                  @Override
                  public Typed<AString> x(Var arg1) {
                      return new MethodCall<AString>("org.freeciv.Util.joinStringArray",
                              arg1.ref(), literalString(" "));
                  }
              },
              true, Arrays.asList(maxSizeConstant, terminator));

    }
}
