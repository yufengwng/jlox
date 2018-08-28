package org.yufengwng.lox;

import java.util.List;
import java.util.Objects;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Object> {

    public void interpret(List<Stmt> statements) {
        statements.stream()
            .filter(Objects::nonNull)
            .forEach(stmt -> execute(stmt));
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
                return (double) left < (double) right;
            case LESS_EQ:
                return (double) left <= (double) right;
            case GREATER:
                return (double) left > (double) right;
            case GREATER_EQ:
                return (double) left >= (double) right;
            case PLUS:
                return (double) left + (double) right;
            case MINUS:
                return (double) left - (double) right;
            case STAR:
                return (double) left * (double) right;
            case SLASH:
                return (double) left / (double) right;
        }

        return null;
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
                // todo: check it's a num
                return (0 - (double) value);
        }
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return null; // todo
    }

    private boolean isTruthy(Object value) {
        if (value == null)              return false;
        if (value instanceof Boolean)   return (boolean) value;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null)              return false;
        return a.equals(b);
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
