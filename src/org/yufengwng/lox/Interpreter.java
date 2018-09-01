package org.yufengwng.lox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Object> {
    final Environment globals = new Environment();
    private Environment current = globals;

    Interpreter() {
        globals.define(NativeClock.NAME, new NativeClock());
    }

    public void interpret(List<Stmt> statements) {
        try {
            statements.stream()
                .filter(Objects::nonNull)
                .forEach(stmt -> execute(stmt));
        } catch (RuntimeError error) {
            Reporter.runtimeError(error);
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.current;
        try {
            this.current = environment;
            statements.stream()
                .filter(Objects::nonNull)
                .forEach(stmt -> execute(stmt));
        } finally {
            this.current = previous;
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(current));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        LoxFunction function = new LoxFunction(stmt);
        current.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.then);
        } else if (stmt.otherwise != null) {
            execute(stmt.otherwise);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) {
            value = evaluate(stmt.value);
        }
        throw new ReturnSignal(value);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        current.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        current.assign(expr.name, value);
        return value;
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
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");
        }

        LoxCallable function = (LoxCallable) callee;
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected "
                    + function.arity() + " arguments but got "
                    + arguments.size() + ".");
        }

        return function.call(this, arguments);
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
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
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
        return current.fetch(expr.name);
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
