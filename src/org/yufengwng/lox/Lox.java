package org.yufengwng.lox;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    private static final String NAME = "loxscript";
    private static final int EX_USAGE = 64;
    private static final int EX_DATAERR = 65;
    private static final int EX_SOFTWARE = 70;

    private static boolean hadError = false;
    private static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println(String.format("Usage: %s [script]", NAME));
            System.exit(EX_USAGE);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError)        System.exit(EX_DATAERR);
        if (hadRuntimeError) System.exit(EX_SOFTWARE);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        while (true) {
            System.out.print("lx> ");
            run(reader.readLine());
            hadError = false;
        }
    }

    private static void run(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scan();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        if (hadError) return;

        Interpreter interpreter = new Interpreter();
        interpreter.interpret(statements);
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
