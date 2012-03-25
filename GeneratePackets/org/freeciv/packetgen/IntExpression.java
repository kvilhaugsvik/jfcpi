package org.freeciv.packetgen;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class IntExpression {
    private final String operatorOrValue;
    private final IntExpression lhs;
    private final IntExpression rhs;
    private final HashSet reqs;

    private IntExpression(String operatorOrValue, IntExpression lhs, IntExpression rhs, Requirement... iDemand) {
        this.operatorOrValue = operatorOrValue;
        this.lhs = lhs;
        this.rhs = rhs;
        this.reqs = new HashSet();
        for (Requirement req : iDemand)
            reqs.add(req);
        if (null != lhs)
            reqs.addAll(lhs.getReqs());
        if (null != rhs)
            reqs.addAll(rhs.getReqs());
    }

    private boolean isValue() {
        return (null == lhs && null == rhs);
    }

    private static String wrapNeeded(IntExpression expr) {
        return expr.isValue() ? expr.toString() : "(" + expr + ")";
    }

    @Override
    public String toString() {
        if (isValue())
            return operatorOrValue;
        else if (null == lhs)
            return operatorOrValue + wrapNeeded(rhs);
        else if (null == rhs)
            return wrapNeeded(lhs) + operatorOrValue;
        else
            return wrapNeeded(lhs) + " " + operatorOrValue + " " + wrapNeeded(rhs);
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
        return new IntExpression(value, null, null);
    }

    public static IntExpression readFromOther(IDependency other, String readStatement) {
        return new IntExpression(readStatement, null, null, other.getIFulfillReq());
    }

    public static IntExpression variable(String name) {
        return new IntExpression("Constants" + "." + name, null, null, new Requirement(name, Requirement.Kind.VALUE));
    }

    public Collection<Requirement> getReqs() {
        return Collections.unmodifiableCollection(reqs);
    }
}
