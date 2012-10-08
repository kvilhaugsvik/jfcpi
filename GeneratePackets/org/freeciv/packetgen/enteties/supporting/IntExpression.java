package org.freeciv.packetgen.enteties.supporting;

import org.freeciv.packetgen.dependency.IDependency;
import org.freeciv.packetgen.dependency.Requirement;
import org.freeciv.packetgen.enteties.Constant;
import scala.Function1;

import java.util.*;

public class IntExpression {
    private final String operatorOrValue;
    private final IntExpression lhs;
    private final IntExpression rhs;
    private final HashSet<Requirement> reqs;

    private IntExpression(String operatorOrValue, IntExpression lhs, IntExpression rhs, Requirement... iDemand) {
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

    public boolean hasNoVariables() {
        return reqs.isEmpty();
    }

    // The one use of the Scala library from Java.
    // Since an interface like it can be made in 1 minute it won't cause problems should Scala be dropped.
    // TODO: Optimize if long expression is met.
    public IntExpression valueMap(Function1<IntExpression, String> mapper) {
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
    public String toString() {
        if (isValue())
            return operatorOrValue;
        else if (noPrefix())
            return operatorOrValue + wrapNeeded(rhs);
        else if (noPostfix())
            return wrapNeeded(lhs) + operatorOrValue;
        else
            return wrapNeeded(lhs) + " " + operatorOrValue + " " + wrapNeeded(rhs);
    }

    public String toStringNotJava() {
        return Constant.stripJavaCodeFromReference(toString());
    }

    private boolean noPostfix() {
        return null == rhs;
    }

    private boolean noPrefix() {
        return null == lhs;
    }

    public static IntExpression binary(String operator, IntExpression lhs, IntExpression rhs) {
        return new IntExpression(operator, lhs, rhs);
    }

    public static IntExpression unary(String operator, IntExpression rhs) {
        return new IntExpression(operator, null, rhs);
    }

    public static IntExpression suf(IntExpression lhs, String operator) {
        return new IntExpression(operator, lhs, null);
    }

    public static IntExpression integer(String value) {
        return handled(value);
    }

    public static IntExpression handled(String expression, Requirement... reqs) {
        return new IntExpression(expression, null, null, reqs);
    }

    public static IntExpression readFromOther(IDependency other, String readStatement) {
        return new IntExpression(readStatement, null, null, other.getIFulfillReq());
    }

    public static IntExpression variable(String name) {
        Requirement valueDefinition = new Requirement(name, Requirement.Kind.VALUE);
        return new IntExpression(Constant.referToInJavaCode(valueDefinition), null, null, valueDefinition);
    }

    public Collection<Requirement> getReqs() {
        return Collections.<Requirement>unmodifiableCollection(reqs);
    }
}
