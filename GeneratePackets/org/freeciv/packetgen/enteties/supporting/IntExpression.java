package org.freeciv.packetgen.enteties.supporting;

import com.kvilhaugsvik.javaGenerator.representation.CodeAtoms;
import com.kvilhaugsvik.javaGenerator.representation.HasAtoms;
import com.kvilhaugsvik.javaGenerator.representation.IR;
import com.kvilhaugsvik.javaGenerator.typeBridge.Typed;
import com.kvilhaugsvik.javaGenerator.typeBridge.willReturn.AnInt;
import com.kvilhaugsvik.javaGenerator.util.BuiltIn;
import com.kvilhaugsvik.javaGenerator.util.Formatted;
import org.freeciv.packetgen.dependency.Dependency;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.Constant;
import scala.Function1;

import java.util.*;

public class IntExpression extends Formatted implements Typed<AnInt> {
    private final HasAtoms operatorOrValue;
    private final IntExpression lhs;
    private final IntExpression rhs;
    private final HashSet<Requirement> reqs;

    private IntExpression(HasAtoms operatorOrValue, IntExpression lhs, IntExpression rhs, Requirement... iDemand) {
        this.operatorOrValue = operatorOrValue;
        this.lhs = lhs;
        this.rhs = rhs;
        this.reqs = new HashSet<Requirement>();
        for (Requirement req : iDemand)
            reqs.add(req);
        if (null != lhs)
            reqs.addAll(lhs.getReqs());
        if (null != rhs)
            reqs.addAll(rhs.getReqs());
    }

    private boolean isValue() {
        return (noPrefix() && noPostfix());
    }

    private static String wrapNeeded(IntExpression expr) {
        return expr.isValue() ? expr.toString() : "(" + expr + ")";
    }

    private static void wrapIfNeeded(CodeAtoms to, IntExpression expr) {
        if (expr.isValue()) {
            expr.writeAtoms(to);
        } else {
            to.add(LPR);
            expr.writeAtoms(to);
            to.add(RPR);
        }
    }

    public boolean hasNoVariables() {
        return reqs.isEmpty();
    }

    // The one use of the Scala library from Java.
    // Since an interface like it can be made in 1 minute it won't cause problems should Scala be dropped.
    // TODO: Optimize if long expression is met.
    public IntExpression valueMap(Function1<IntExpression, HasAtoms> mapper) {
        if (isValue())
            return new IntExpression(mapper.apply(this), lhs, rhs, new Requirement[0]);
        else if (noPostfix())
            return new IntExpression(operatorOrValue, lhs.valueMap(mapper), rhs, new Requirement[0]);
        else if (noPrefix())
            return new IntExpression(operatorOrValue, lhs, rhs.valueMap(mapper), new Requirement[0]);
        else
            return new IntExpression(operatorOrValue, lhs.valueMap(mapper), rhs.valueMap(mapper), new Requirement[0]);
    }

    @Override
    public void writeAtoms(CodeAtoms to) {
        if (isValue()) {
            operatorOrValue.writeAtoms(to);
        } else if (noPrefix()) {
            operatorOrValue.writeAtoms(to);
            wrapIfNeeded(to, rhs);
        } else if (noPostfix()) {
            wrapIfNeeded(to, lhs);
            operatorOrValue.writeAtoms(to);
        } else {
            wrapIfNeeded(to, lhs);
            operatorOrValue.writeAtoms(to);
            wrapIfNeeded(to, rhs);
        }
    }

    public String nameOfConstant() {
        if (1 == reqs.size())
            return reqs.iterator().next().getName();
        else
            throw new UnsupportedOperationException("Not a constant (unless constant no longer have exactly one req)");
    }

    public boolean mayBeConstant() {
        return null == rhs && null == lhs && 1 == reqs.size();
    }

    @Deprecated
    public String toStringNotJava() {
        return Constant.stripJavaCodeFromReference(toString());
    }

    private boolean noPostfix() {
        return null == rhs;
    }

    private boolean noPrefix() {
        return null == lhs;
    }

    private static IR.CodeAtom atomForMe(String operatorOrValue) {
        if (operatorOrValue.equals("+")) {
            return ADD;
        } else if (operatorOrValue.equals("-")) {
            return SUB;
        } else if (operatorOrValue.equals("*")) {
            return MUL;
        } else if (operatorOrValue.equals("/")) {
            return DIV;
        } else if (operatorOrValue.equals("%")) {
            return REM;
        } else if (operatorOrValue.equals("++")) {
            return INC;
        } else if (operatorOrValue.equals("--")) {
            return DEC;
        } else {
            return new IR.CodeAtom(operatorOrValue);
        }
    }

    public static IntExpression binary(String operator, IntExpression lhs, IntExpression rhs) {
        return new IntExpression(atomForMe(operator), lhs, rhs);
    }

    public static IntExpression unary(String operator, IntExpression rhs) {
        return new IntExpression(atomForMe(operator), null, rhs);
    }

    public static IntExpression suf(IntExpression lhs, String operator) {
        return new IntExpression(atomForMe(operator), lhs, null);
    }

    public static IntExpression integer(String value) {
        return handled(new IR.CodeAtom(value));
    }

    public static IntExpression handled(HasAtoms expression, Requirement... reqs) {
        return new IntExpression(expression, null, null, reqs);
    }

    public static IntExpression readFromOther(Dependency.Item other, Typed<? extends AnInt> readStatement) {
        return new IntExpression(readStatement, null, null, other.getIFulfillReq());
    }

    public static IntExpression variable(String name) {
        Requirement valueDefinition = new Requirement(name, Constant.class);
        return new IntExpression(BuiltIn.toCode(Constant.referToInJavaCode(valueDefinition)), null, null, valueDefinition);
    }

    public Collection<Requirement> getReqs() {
        return Collections.<Requirement>unmodifiableCollection(reqs);
    }
}
