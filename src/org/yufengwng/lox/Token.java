package org.yufengwng.lox;

class Token {
    final TokenType type;
    final Object literal;
    final String lexeme;
    final int line;

    Token(TokenType type, Object literal, String lexeme, int line) {
        this.type = type;
        this.literal = literal;
        this.lexeme = lexeme;
        this.line = line;
    }

    // Get a string representation that satisfies original lox expectations.
    public String toDisplay() {
        return String.format("%s %s %s", type.toDisplay(), lexeme, literal);
    }

    @Override
    public String toString() {
        return String.format("Token{type=%s,literal=%s,lexeme=%s,line=%d}",
                type, literal, lexeme, line);
    }
}
