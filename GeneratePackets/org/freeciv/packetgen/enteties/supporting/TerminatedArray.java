package org.freeciv.packetgen.enteties.supporting;

import org.freeciv.packetgen.Hardcoded;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.Constant;
import org.freeciv.packetgen.enteties.FieldTypeBasic;
import org.freeciv.packetgen.javaGenerator.*;
import org.freeciv.packetgen.javaGenerator.expression.Block;
import org.freeciv.packetgen.javaGenerator.expression.creators.ExprFrom1;
import static org.freeciv.packetgen.javaGenerator.expression.util.BuiltIn.*;
import static org.freeciv.packetgen.Hardcoded.fMaxSize;
import static org.freeciv.packetgen.Hardcoded.pMaxSize;

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
    private static final TargetClass byteArray = new TargetArray(byte[].class, true);
    private static final Var pValue = Var.param(byteArray, "value");
    public TerminatedArray(String dataIOType, String publicType, final Requirement terminator) {
        super(dataIOType, publicType, byteArray,
                new ExprFrom1<Block, Var>() {
                    @Override
                    public Block x(Var to) {
                        return new Block(
                                arrayEaterScopeCheck(isSmallerThan(pMaxSize.<AnInt>ref(),
                                        pValue.read("length"))),
                                fMaxSize.assign(pMaxSize.ref()),
                                to.assign(pValue.ref()));
                    }
                },
                new ExprFrom2<Block, Var, Var>() {
                    @Override
                    public Block x(Var to, Var from) {
                        Var buf = Var.local(byteArray, "buffer",
                                byteArray.newInstance(pMaxSize.<AnInt>ref()));
                        Var current = Var.local("byte", "current", from.<AValue>call("readByte"));
                        Var pos = Var.local("int", "pos", asAnInt("0"));
                        return new Block(buf, current, pos,
                                WHILE(isNotSame(cast(byte.class, asAnInt(Constant.referToInJavaCode(terminator))), current.ref()),
                                        new Block(arraySetElement(buf, pos.ref(), current.ref()),
                                                inc(pos),
                                                IF(isSmallerThan(pos.ref(), pMaxSize.<AnInt>ref()),
                                                        new Block(current.assign(from.<AValue>call("readByte"))),
                                                        new Block(asVoid("break"))))),
                                fMaxSize.assign(pMaxSize.ref()),
                                to.assign(new MethodCall<AValue>("java.util.Arrays.copyOf",
                                        buf.ref(), pos.ref())));
                    }
                },
                new ExprFrom2<Block, Var, Var>() {
                    @Override
                    public Block x(Var val, Var to) {
                        return new Block(
                                to.call("write", val.ref()),
                                IF(isSmallerThan(val.<AnInt>read("length"), fMaxSize.<AnInt>ref()),
                                        new Block(to.call("writeByte", asAValue(Constant.referToInJavaCode(terminator))))));
                    }
                },
              new ExprFrom1<Typed<AnInt>, Var>() {
                    @Override
                    public Typed<AnInt> x(Var value) {
                        return BuiltIn.<AnInt>sum(
                                value.read("length"),
                                R_IF(isSmallerThan(value.<AnInt>read("length"), fMaxSize.<AnInt>ref()),
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
              true, Arrays.asList(terminator));

    }
}
