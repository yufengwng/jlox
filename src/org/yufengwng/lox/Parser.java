package org.yufengwng.lox;

import static org.yufengwng.lox.TokenType.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Parser {
    @SuppressWarnings("serial")
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(tryDeclaration());
        }
        return statements;
    }

    private Stmt tryDeclaration() {
        try {
            if (match(VAR)) return finishVarDeclaration();
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt finishVarDeclaration() {
        Token name = consume(IDENT, "Expect variable name.");

        Expr initializer = null;
        if (match(EQ)) {
            initializer = expression();
        }

        consume(SEMI, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (match(BRACE_L)) return new Stmt.Block(finishBlockStatement());
        if (match(FOR))     return finishForStatement();
        if (match(IF))      return finishIfStatement();
        if (match(PRINT))   return finishPrintStatement();
        if (match(WHILE))   return finishWhileStatement();
        return expressionStatement();
    }

    private List<Stmt> finishBlockStatement() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd() && !check(BRACE_R)) {
            statements.add(tryDeclaration());
        }
        consume(BRACE_R, "Expect '}' after block.");
        return statements;
    }

    private Stmt finishForStatement() {
        consume(PAREN_L, "Expect '(' after 'for'.");

        Stmt initializer;
        if (match(SEMI)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = finishVarDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(SEMI)) {
            condition = expression();
        }
        consume(SEMI, "Expect ';' after for loop condition.");

        Expr increment = null;
        if (!check(PAREN_R)) {
            increment = expression();
        }
        consume(PAREN_R, "Expect ')' after for loop clauses.");

        Stmt body = statement();
        body = desugarForToWhile(initializer, condition, increment, body);

        return body;
    }

    private Stmt desugarForToWhile(Stmt init, Expr cond, Expr incr, Stmt body) {
        if (incr != null) {
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(incr)));
        }

        if (cond == null) cond = new Expr.Literal(true);
        body = new Stmt.While(cond, body);

        if (init != null) {
            body = new Stmt.Block(Arrays.asList(init , body));
        }

        return body;
    }

    private Stmt finishIfStatement() {
        consume(PAREN_L, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(PAREN_R, "Expect ')' after if condition.");

        Stmt then = statement();
        Stmt otherwise = null;
        if (match(ELSE)) {
            otherwise = statement();
        }

        return new Stmt.If(condition, then, otherwise);
    }

    private Stmt finishPrintStatement() {
        Expr value = expression();
        consume(SEMI, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt finishWhileStatement() {
        consume(PAREN_L, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(PAREN_R, "Expect ')' after while condition.");

        Stmt body = statement();
        return new Stmt.While(condition, body);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMI, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = logicalOr();

        if (match(EQ)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr logicalOr() {
        Expr expr = logicalAnd();

        while (match(OR)) {
            Token operator = previous();
            Expr right = logicalAnd();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr logicalAnd() {
        Expr expr = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(EQ_EQ, NOT_EQ)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = addition();

        while (match(LESS, LESS_EQ, GREATER, GREATER_EQ)) {
            Token operator = previous();
            Expr right = addition();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr addition() {
        Expr expr = multiplication();

        while (match(PLUS, MINUS)) {
            Token operator = previous();
            Expr right = multiplication();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr multiplication() {
        Expr expr = unary();

        while (match(STAR, SLASH)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    private Expr primary() {
        if (match(NIL))   return new Expr.Literal(null);
        if (match(TRUE))  return new Expr.Literal(true);
        if (match(FALSE)) return new Expr.Literal(false);

        if (match(NUM, STR)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(IDENT)) {
            return new Expr.Variable(previous());
        }

        if (match(PAREN_L)) {
            Expr expr = expression();
            consume(PAREN_R, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean check(TokenType type) {
        return !isAtEnd() && peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) {
            current += 1;
        }
        return previous();
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Reporter.error(token, message);
        return new ParseError();
    }

    // Synchronize parsing on statement boundaries.
    private void synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type == SEMI) return;
            switch (peek().type) {
                case CLASS:
                case FOR:
                case FUN:
                case IF:
                case PRINT:
                case RETURN:
                case VAR:
                case WHILE:
                    return;
            }
            advance();
        }
    }
}
