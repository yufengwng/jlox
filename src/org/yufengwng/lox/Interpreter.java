package org.yufengwng.lox;

import java.util.List;
import java.util.Objects;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Object> {

    public void interpret(List<Stmt> statements) {
        try {
            statements.stream()
                .filter(Objects::nonNull)
                .forEach(stmt -> execute(stmt));
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case EQ_EQ:
                return isEqual(left, right);
            case NOT_EQ:
                return !isEqual(left, right);
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQ:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQ:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case PLUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left + (double) right;
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double) left / (double) right;
        }

        throw new RuntimeError(expr.operator, "Unhandled binary expression.");
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object value = evaluate(expr.right);
        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(value);
            case MINUS:
                checkNumberOperand(expr.operator, value);
                return (0.0 - (double) value);
        }
        throw new RuntimeError(expr.operator, "Unhandled unary expression.");
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return null; // todo: lookup name in environment scope
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null)              return false;
        return a.equals(b);
    }

    private boolean isTruthy(Object value) {
        if (value == null)              return false;
        if (value instanceof Boolean)   return (boolean) value;
        return true;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private String stringify(Object value) {
        if (value == null) return "nil";

        if (value instanceof Double) {
            return prettifyNumber((Double) value);
        }

        return value.toString();
    }

    private String prettifyNumber(Double value) {
        String text = value.toString();
        if (text.endsWith(".0")) {
            text = text.substring(0, text.length() - 2);
        }
        return text;
    }
}
