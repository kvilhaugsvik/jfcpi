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

// Perhaps also have the generalized version output an Array of the referenced objects in stead of their number.
public class TerminatedArray extends FieldTypeBasic {
    private static final TargetClass byteArray = new TargetArray(byte[].class, true);

    public TerminatedArray(String dataIOType, String publicType, final Requirement terminator) {
        this(dataIOType, publicType, byteArray, terminator,
                new ExprFrom1<Typed<AnInt>, Var>() {
                    @Override
                    public Typed<AnInt> x(Var value) {
                        return value.read("length");
                    }
                },
                new ExprFrom1<Typed<AValue>, Var>() {
                    @Override
                    public Typed<AValue> x(Var everything) {
                        return everything.ref();
                    }
                },
                new ExprFrom1<Typed<AValue>, Typed<AValue>>() {
                    @Override
                    public Typed<AValue> x(Typed<AValue> bytes) {
                        return bytes;
                    }
                },
                new ExprFrom1<Typed<AString>, Var>() {
                    @Override
                    public Typed<AString> x(Var arg1) {
                        return new MethodCall<AString>("org.freeciv.Util.joinStringArray",
                                arg1.ref(), literalString(" "));
                    }
                });
    }

    public TerminatedArray(String dataIOType, String publicType, final TargetClass javaType,
                           final Requirement terminator,
                           final ExprFrom1<Typed<AnInt>, Var> sizeGetter,
                           final ExprFrom1<Typed<AValue>, Var> fullToByteArray,
                           final ExprFrom1<Typed<AValue>, Typed<AValue>> byteArrayToFull,
                           ExprFrom1<Typed<AString>, Var> toString) {
        super(dataIOType, publicType, javaType,
                new ExprFrom1<Block, Var>() {
                    @Override
                    public Block x(Var to) {
                        final Var pValue = Var.param(javaType, "value");
                        return new Block(
                                arrayEaterScopeCheck(isSmallerThan(pMaxSize.<AnInt>ref(),
                                        sizeGetter.x(pValue))),
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
                                to.assign(byteArrayToFull.x(new MethodCall<AValue>("java.util.Arrays.copyOf",
                                        buf.ref(), pos.ref()))));
                    }
                },
                new ExprFrom2<Block, Var, Var>() {
                    @Override
                    public Block x(Var val, Var to) {
                        return new Block(
                                to.call("write", fullToByteArray.x(val)),
                                IF(isSmallerThan(sizeGetter.x(val), fMaxSize.<AnInt>ref()),
                                        new Block(to.call("writeByte", asAValue(Constant.referToInJavaCode(terminator))))));
                    }
                },
              new ExprFrom1<Typed<AnInt>, Var>() {
                    @Override
                    public Typed<AnInt> x(Var value) {
                        return BuiltIn.<AnInt>sum(
                                sizeGetter.x(value),
                                R_IF(isSmallerThan(sizeGetter.x(value), fMaxSize.<AnInt>ref()),
                                        asAnInt("1"),
                                        asAnInt("0")));
                    }
              },
              toString,
              true, Arrays.asList(terminator));

    }
}
