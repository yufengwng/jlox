package org.yufengwng.lox;

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
        tokens.add(new Token(TokenType.EOF, null, "", line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(TokenType.PAREN_L); break;
            case ')': addToken(TokenType.PAREN_R); break;
            case '{': addToken(TokenType.BRACE_L); break;
            case '}': addToken(TokenType.BRACE_R); break;
            case ',': addToken(TokenType.COMMA); break;
            case ';': addToken(TokenType.SEMI); break;
            case '.': addToken(TokenType.DOT); break;

            case '!': addToken(match('=') ? TokenType.NOT_EQ : TokenType.BANG); break;
            case '=': addToken(match('=') ? TokenType.EQ_EQ : TokenType.EQ); break;
            case '<': addToken(match('=') ? TokenType.LESS_EQ : TokenType.LESS); break;
            case '>': addToken(match('=') ? TokenType.GREATER_EQ : TokenType.GREATER); break;

            case '+': addToken(TokenType.PLUS); break;
            case '-': addToken(TokenType.MINUS); break;
            case '*': addToken(TokenType.STAR); break;

            case '/':
                if (match('/')) {
                    finishLineComment();
                } else {
                    addToken(TokenType.SLASH);
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
                    // report error
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
        TokenType type = TokenType.KEYWORDS.getOrDefault(text, TokenType.IDENT);
        addToken(type, null, text);
    }

    private void finishNumber() {
        while (isDigit(peek())) {
            advance();
        }
        tryFraction();
        String text = currentLexeme();
        addToken(TokenType.NUM, toLoxNumber(text), text);
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
        addToken(TokenType.STR, toLoxString(text), text);
    }

    private boolean closeString() {
        if (isAtEnd()) {
            return false;     // report error
        } else {
            advance();
            return true;
        }
    }

    private String toLoxString(String lexeme) {
        return lexeme.substring(1, lexeme.length() - 1);
    }
}
