package org.freeciv.packetgen;

public class IntExpression {
    private final String operatorOrValue;
    private final IntExpression lhs;
    private final IntExpression rhs;

    private IntExpression(String operatorOrValue, IntExpression lhs, IntExpression rhs) {
        this.operatorOrValue = operatorOrValue;
        this.lhs = lhs;
        this.rhs = rhs;
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

    public static IntExpression variable(String name) {
        return new IntExpression(name, null, null);
    }
}
