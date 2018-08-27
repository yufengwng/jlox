package org.yufengwng.lox;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static final String NAME = "loxscript";
    private static final int EX_DATAERR = 64;
    private static final int EX_SOFTWARE = 70;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println(String.format("Usage: %s [script]", NAME));
            System.exit(EX_DATAERR);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            System.out.println("No prompt");
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
    }

    private static void run(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scan();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        Interpreter interpreter = new Interpreter();
        interpreter.interpret(statements);
    }
}
