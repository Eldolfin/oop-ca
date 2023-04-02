package interpreteur.rouille.java;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

class App {
  static boolean hadError = false;
  static boolean hadRuntimeError = false;
  private static final Interpreter interpreter = new Interpreter();

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: rouille [script]");
      System.exit(64);
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      interpreter.repl_mode = true;
      runPrompt();
    }
  }

  private static void runFile(String path) throws IOException {
    var bytes = Files.readAllBytes(Paths.get(path));
    var program = new String(bytes, Charset.defaultCharset());
    run(program);

    if (hadError)
      System.exit(65);
    if (hadRuntimeError)
      System.exit(70);
  }

  private static void runPrompt() throws IOException {
    var scanner = new java.util.Scanner(System.in);
    while (true) {
      System.out.print("> ");
      String line;
      try {
        line = scanner.nextLine();
      } catch (NoSuchElementException e) {
        break;
      }
      run(line);
      hadError = false;
    }
    scanner.close();
    System.out.println("Au revoir 👋!");
  }

  private static void run(String source) {
    var scanner = new Scanner(source);
    var tokens = scanner.scanTokens();

    var parser = new Parser(tokens);
    var statements = parser.parse();

    if (hadError)
      return;

    interpreter.interpret(statements);
  }

  static void error(Token token, String message) {
    if (token.type == TokenType.EOF)
      report(token.line, " at end ", message);
    else
      report(token.line, " at `" + token.lexeme + "` ", message);
  }

  static void runtimeError(RuntimeError error) {
    System.out.println(error.getMessage() +
        "\n[line " + error.token.line + " column " + error.token.column + "]");
    hadRuntimeError = true;
  }

  static void error(int line, String message) {
    report(line, "", message);
  }

  private static void report(int line, String where, String message) {
    System.err.println("[line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }
}
