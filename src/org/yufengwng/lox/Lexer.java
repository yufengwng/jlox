package org.yufengwng.lox;

import static org.yufengwng.lox.TokenType.*;

import java.util.ArrayList;
import java.util.List;

class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;

    Lexer(String source) {
        this.source = source;
    }

    List<Token> scan() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, null, "", line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(PAREN_L); break;
            case ')': addToken(PAREN_R); break;
            case '{': addToken(BRACE_L); break;
            case '}': addToken(BRACE_R); break;
            case ',': addToken(COMMA); break;
            case ';': addToken(SEMI); break;
            case '.': addToken(DOT); break;

            case '!': addToken(match('=') ? NOT_EQ : BANG); break;
            case '=': addToken(match('=') ? EQ_EQ : EQ); break;
            case '<': addToken(match('=') ? LESS_EQ : LESS); break;
            case '>': addToken(match('=') ? GREATER_EQ : GREATER); break;

            case '+': addToken(PLUS); break;
            case '-': addToken(MINUS); break;
            case '*': addToken(STAR); break;

            case '/':
                if (match('/')) {
                    finishLineComment();
                } else {
                    addToken(SLASH);
                }
                break;

            // Whitespace is ignored.
            case ' ':  // fall-through
            case '\r': // fall-through
            case '\t': break;

            // Newline increments line count.
            case '\n':
                line += 1;
                break;

            case '"': finishString(); break;

            default:
                if (isDigit(c)) {
                    finishNumber();
                } else if (isAlpha(c)) {
                    finishIdentifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z')
            || (c >= 'A' && c <= 'Z')
            || (c == '_');
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private char advance() {
        current += 1;
        return source.charAt(current - 1);
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) {
            return false;
        }
        current += 1;
        return true;
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current + 1);
    }

    private String currentLexeme() {
        return source.substring(start, current);
    }

    private void addToken(TokenType type) {
        addToken(type, null, currentLexeme());
    }

    private void addToken(TokenType type, Object literal, String lexeme) {
        tokens.add(new Token(type, literal, lexeme, line));
    }

    private void finishLineComment() {
        while (!isAtEnd() && peek() != '\n') {
            advance();
        }
    }

    private void finishIdentifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }
        String text = currentLexeme();
        TokenType type = KEYWORDS.getOrDefault(text, IDENT);
        addToken(type, null, text);
    }

    private void finishNumber() {
        while (isDigit(peek())) {
            advance();
        }
        tryFraction();
        String text = currentLexeme();
        addToken(NUM, toLoxNumber(text), text);
    }

    private void tryFraction() {
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) {
                advance();
            }
        }
    }

    private Double toLoxNumber(String lexeme) {
        return Double.parseDouble(lexeme);
    }

    private void finishString() {
        while (!isAtEnd() && peek() != '"') {
            if (peek() == '\n') {
                line += 1;
            }
            advance();
        }

        if (!closeString()) {
            return;
        }

        String text = currentLexeme();
        addToken(STR, toLoxString(text), text);
    }

    private boolean closeString() {
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return false;
        } else {
            advance();
            return true;
        }
    }

    private String toLoxString(String lexeme) {
        return lexeme.substring(1, lexeme.length() - 1);
    }
}
