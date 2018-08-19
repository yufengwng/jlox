package org.yufengwng.lox;

import java.util.HashMap;
import java.util.Map;

enum TokenType {
    // Literals.
    IDENT, STR, NUM, TRUE, FALSE, NIL,

    // Keywords.
    AND, CLASS, ELSE, FOR, FUN, IF, OR, RETURN, SUPER, THIS, VAR, WHILE,

    // Builtins.
    PRINT,

    // Punctuations.
    BRACE_L, BRACE_R, PAREN_L, PAREN_R,
    COMMA, DOT, SEMI,

    // Operators.
    PLUS, MINUS, STAR, SLASH,
    LESS, LESS_EQ, GREATER, GREATER_EQ,
    EQ, EQ_EQ, BANG, NOT_EQ,

    // Marker.
    EOF;

    static final Map<String, TokenType> KEYWORDS;

    static {
        KEYWORDS = new HashMap<>();
        KEYWORDS.put("and",    AND);
        KEYWORDS.put("class",  CLASS);
        KEYWORDS.put("else",   ELSE);
        KEYWORDS.put("false",  FALSE);
        KEYWORDS.put("for",    FOR);
        KEYWORDS.put("fun",    FUN);
        KEYWORDS.put("if",     IF);
        KEYWORDS.put("nil",    NIL);
        KEYWORDS.put("or",     OR);
        KEYWORDS.put("print",  PRINT);
        KEYWORDS.put("return", RETURN);
        KEYWORDS.put("super",  SUPER);
        KEYWORDS.put("this",   THIS);
        KEYWORDS.put("true",   TRUE);
        KEYWORDS.put("var",    VAR);
        KEYWORDS.put("while",  WHILE);
    }

    // Get a string representation that satisfies original lox expectations.
    String toDisplay() {
        String name = this.name();
        if (this == BRACE_L)    name = "LEFT_BRACE";
        if (this == BRACE_R)    name = "RIGHT_BRACE";
        if (this == EQ)         name = "EQUAL";
        if (this == EQ_EQ)      name = "EQUAL_EQUAL";
        if (this == GREATER_EQ) name = "GREATER_EQUAL";
        if (this == IDENT)      name = "IDENTIFIER";
        if (this == LESS_EQ)    name = "LESS_EQUAL";
        if (this == PAREN_L)    name = "LEFT_PAREN";
        if (this == PAREN_R)    name = "RIGHT_PAREN";
        if (this == NOT_EQ)     name = "BANG_EQUAL";
        if (this == NUM)        name = "NUMBER";
        if (this == SEMI)       name = "SEMICOLON";
        if (this == STR)        name = "STRING";
        return name;
    }
}
