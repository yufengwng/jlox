package org.yufengwng.lox;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static final String NAME = "jlox";
    private static final int EX_USAGE = 64;
    private static final int EX_DATAERR = 65;
    private static final int EX_SOFTWARE = 70;

    private static final Interpreter interpreter = new Interpreter();

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

        if (Reporter.errored())        System.exit(EX_DATAERR);
        if (Reporter.runtimeErrored()) System.exit(EX_SOFTWARE);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) {
                break;
            }

            run(line);
            Reporter.reset();
        }
    }

    private static void run(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scan();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        if (Reporter.errored()) return;

        interpreter.interpret(statements);
    }
}
