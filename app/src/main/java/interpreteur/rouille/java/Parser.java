package interpreteur.rouille.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static interpreteur.rouille.java.TokenType.*;

class Parser {
  private class ParserError extends RuntimeException {
  }

  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  List<Stmt> parse() {
    var statements = new ArrayList<Stmt>();
    skip_semicolons();
    while (!isAtEnd()) {
      statements.add(declaration());
      skip_semicolons();
    }
    return statements;
  }

  private Stmt declaration() {
    try {
      if (match(FUN)) {
        return function("fonction");
      } else if (match(LET)) {
        return varDeclaration();
      }

      return statement();
    } catch (ParserError e) {
      synchronize();
      return null;
    }
  }

  private Stmt function(String kind) {
    var name = consume(IDENTIFIER, "Nom de " + kind + "attendu");
    consume(LEFT_PAREN, "Un `(` est attendu après un nom de " + kind);
    var parameters = new ArrayList<Token>();
    if (!check(RIGHT_PAREN)) {
      do {
        parameters.add(consume(IDENTIFIER, "Nom de paramètre attendu"));
      } while (match(COMMA));
    }
    consume(RIGHT_PAREN, "Un `)` est attendu après les paramètres de " + kind);

    // TODO: use `-> type` syntax for function return type hints
    if (match(ARROW))
      consume(IDENTIFIER, "Un type de retour est attendu après un `->`");

    consume(LEFT_BRACE, "Un block est attendu après la déclaration de " + kind);
    var body = block();
    var statements = body.statements;
    if (body.returnedValue.isPresent()) {
      statements.add(new Stmt.Return(previous(), body.returnedValue.get()));
    }
    return new Stmt.Function(name, parameters, statements);
  }

  private Stmt varDeclaration() {
    var mutable = match(MUT);
    var name = consume(IDENTIFIER, "Un nom de variable était attendu après un `soit`");
    consume(EQUAL, "Une variable déclarée doit aussi être initialisée");
    var initializer = expression();
    consume(SEMICOLON, "Un `;` était attendu après la déclaration d'une variable");
    return new Stmt.Var(name, initializer, mutable);
  }

  private Stmt statement() {
    if (match(PRINT))
      return printStatement();
    else if (match(RETURN))
      return returnStatement();
    else if (match(WHILE))
      return whileStatement();
    else if (match(LOOP))
      return loopStatement();
    else
      return assignment();
  }

  private Stmt printStatement() {
    var expr = expression();
    consume(SEMICOLON, "Expect `;` after print statement.");
    return new Stmt.Print(expr);
  }

  private Stmt returnStatement() {
    var token = previous();
    Expr value = new Expr.Tuple(Arrays.asList()); // this is the empty tuple ()
    if (!check(SEMICOLON)) {
      value = expression();
    }

    consume(SEMICOLON, "Un `;` est attendu après un `renvoie`");
    return new Stmt.Return(token, value);
  }

  private Stmt whileStatement() {
    var token = previous();
    var condition = expression();
    consume(LEFT_BRACE, "Il manque un bloc à l'expression `tant`");
    var body = block();

    return new Stmt.While(token, condition, body);
  }

  private Stmt loopStatement() {
    var token = previous();
    var condition = new Expr.Literal(true);
    consume(LEFT_BRACE, "Il manque un bloc à l'expression `boucle`");
    var body = block();

    return new Stmt.While(token, condition, body);
  }

  private Stmt assignment() {
    var expr = expression();

    if (match(EQUAL, PLUS_EQUAL, MINUS_EQUAL, SLASH_EQUAL, STAR_EQUAL, PERCENT_EQUAL, OR_EQUAL, AND_EQUAL)) {
      var equals = previous();
      var value = expression();

      if (equals.type != EQUAL) {
        // hack to implement all other operators in few lines
        var fakeLexeme = equals.lexeme.substring(0, 1);
        var tokenType = TokenType.detect(fakeLexeme);
        var fakeToken = new Token(tokenType, fakeLexeme, equals.line, equals.column);
        value = new Expr.Binary(expr, fakeToken, value);
      }

      if (expr instanceof Expr.Variable) {
        Token name = ((Expr.Variable) expr).name;
        consume(SEMICOLON, "Un `;` était attendu après l'affectation de la variable");
        return new Stmt.Assign(name, value);
      } else {
        error(equals, "Le côté gauche de l'affectation n'est pas valide");
      }
    }

    if (!(expr instanceof Expr.If) && !(expr instanceof Expr.Block)) {
      consume(SEMICOLON, "Un `;` était attendu après la ligne de code");
    }

    return new Stmt.Expression(expr);
  }

  private Expr expression() {
    if (match(LEFT_BRACE)) {
      return block();
    } else if (match(IF)) {
      return ifExpression();
    }

    return or();
  }

  private Expr.Block block() {
    var statements = new ArrayList<Stmt>();
    Optional<Expr> returnedValue = Optional.empty();

    while (!check(RIGHT_BRACE) && !isAtEnd()) {
      if (hasNoSemicolon()) {
        returnedValue = Optional.of(expression());
      } else {
        statements.add(declaration());
      }
    }

    consume(RIGHT_BRACE, "un `}` était attendu après le bloc");
    return new Expr.Block(statements, returnedValue);
  }

  private Expr ifExpression() {
    var token = previous();
    var condition = expression();
    consume(LEFT_BRACE, "Il manque un bloc à l'expression `si`");
    var thenBranch = block();
    Optional<Expr> elseBranch = Optional.empty();
    if (match(ELSE)) {
      consume(LEFT_BRACE, "Il manque un bloc à l'expression `sinon`");
      elseBranch = Optional.of(block());
    }

    return new Expr.If(token, condition, thenBranch, elseBranch);
  }

  private Expr or() {
    var expr = and();

    while (match(OR)) {
      var operator = previous();
      var right = and();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr and() {
    var expr = equality();

    while (match(AND)) {
      var operator = previous();
      var right = equality();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr equality() {
    var expr = comparison();

    if (match(EQUAL_EQUAL, BANG_EQUAL)) {
      var operator = previous();
      var right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr comparison() {
    var expr = term();

    if (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
      var operator = previous();
      var right = term();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr term() {
    var expr = factor();

    while (match(MINUS, PLUS)) {
      var operator = previous();
      var right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr factor() {
    var expr = bitwise();

    while (match(SLASH, STAR, PERCENT)) {
      var operator = previous();
      var right = bitwise();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr bitwise() {
    var expr = unary();

    while (match(BITWISE_OR, BITWISE_AND)) {
      var operator = previous();
      var right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr unary() {
    if (match(BANG, MINUS)) {
      var operator = previous();
      var right = unary();
      return new Expr.Unary(operator, right);
    }

    return call();
  }

  private Expr call() {
    var expr = primary();

    while (true) {
      if (match(LEFT_PAREN)) {
        expr = finishCall(expr);
      } else {
        break;
      }
    }

    return expr;
  }

  private Expr finishCall(Expr callee) {
    var arguments = new ArrayList<Expr>();
    if (!check(RIGHT_PAREN)) {
      do {
        arguments.add(expression());
      } while (match(COMMA));
    }

    var paren = consume(RIGHT_PAREN, "Un `)` est attendu après les arguments");

    return new Expr.Call(callee, paren, arguments);
  }

  private Expr primary() {
    if (match(FALSE))
      return new Expr.Literal(false);
    if (match(TRUE))
      return new Expr.Literal(true);

    if (match(INTEGER, FLOAT, STRING))
      return new Expr.Literal(previous().literal.get());

    if (match(IDENTIFIER)) {
      return new Expr.Variable(previous());
    }

    if (match(LEFT_PAREN)) {
      var expr = expression();
      if (match(RIGHT_PAREN)) {
        return new Expr.Grouping(expr);
      } else if (check(COMMA)) {
        var expressions = new ArrayList<Expr>();
        expressions.add(expr);
        while (match(COMMA)) {
          expressions.add(expression());
        }
        expr = new Expr.Tuple(expressions);
        consume(RIGHT_PAREN, "`(` du tuple jamais fermé");
        return expr;
      }
    }

    throw error(peek(), "Expression attendue, mot clé trouvé");
  }

  private Token consume(TokenType type, String message) {
    if (check(type))
      return advance();

    throw error(peek(), message);
  }

  private void synchronize() {
    advance();

    while (!isAtEnd()) {
      if (previous().type == SEMICOLON)
        return;

      switch (peek().type) {
        case STRUCT:
        case FUN:
        case LET:
        case FOR:
        case IF:
        case WHILE:
        case PRINT:
        case RETURN:
          return;

        default:
          break;
      }

      advance();
    }
  }

  private ParserError error(Token token, String message) {
    App.error(token, message);
    return new ParserError();
  }

  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }

    return false;
  }

  private Token advance() {
    if (!isAtEnd())
      current++;
    return previous();
  }

  private boolean check(TokenType type) {
    return !isAtEnd() && peek().type == type;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private boolean isAtEnd() {
    return peek().type == EOF;
  }

  private Token previous() {
    return tokens.get(current - 1);
  }

  private void skip_semicolons() {
    while (match(SEMICOLON)) {
    }
  }

  // Returns true if the next statement or expression has
  // no semicolon ending it. (if it sees a closing bracket before)
  private boolean hasNoSemicolon() {
    var max = tokens.size();
    for (int i = current; i < max; i++) {
      switch (tokens.get(i).type) {
        case SEMICOLON:
          return false;
        case RIGHT_BRACE:
          return true;
        default:
          break;
      }
    }
    return true;
  }
}
