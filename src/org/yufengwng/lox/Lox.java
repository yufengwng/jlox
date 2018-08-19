package org.yufengwng.lox;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static final int EX_DATAERR = 64;
    private static final int EX_SOFTWARE = 70;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
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
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scan();
        tokens.forEach(t -> System.out.println(t.toDisplay()));
    }
}
