package org.yufengwng.lox;

class Reporter {
    private static boolean hadError = false;
    private static boolean hadRuntimeError = false;

    static void reset() {
        hadError = false;
        hadRuntimeError = false;
    }

    static boolean errored() {
        return hadError;
    }

    static boolean runtimeErrored() {
        return hadRuntimeError;
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(String.format(
                    "%s\n[line %d]", error.getMessage(), error.token.line));
        hadRuntimeError = true;
    }

    private static void report(int line, String where, String message) {
        System.err.println(String.format(
                    "[line %d] Error%s: %s", line, where, message));
        hadError = true;
    }
}
