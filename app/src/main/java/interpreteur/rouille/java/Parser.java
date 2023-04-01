package interpreteur.rouille.java;

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

  Optional<Expr> parse() {
    try {
      return Optional.of(expression());
    } catch (ParserError e) {
      return Optional.empty();
    }
  }

  private Expr expression() {
    return equality();
  }

  private Expr equality() {
    Expr expr = comparison();

    if (match(EQUAL_EQUAL, BANG_EQUAL)) {
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr comparison() {
    Expr expr = term();

    if (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
      Token operator = previous();
      Expr right = term();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr term() {
    Expr expr = factor();

    while (match(MINUS, PLUS)) {
      Token operator = previous();
      Expr right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr factor() {
    Expr expr = bitwise();

    while (match(SLASH, STAR)) {
      Token operator = previous();
      Expr right = bitwise();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr bitwise() {
    Expr expr = unary();

    while (match(BITWISE_OR, BITWISE_AND)) {
      Token operator = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr unary() {
    if (match(BANG, MINUS)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }

    return primary();
  }

  private Expr primary() {
    if (match(FALSE))
      return new Expr.Literal(false);
    if (match(TRUE))
      return new Expr.Literal(true);

    if (match(INTEGER, FLOAT, STRING))
      return new Expr.Literal(previous().literal.get());

    if (match(LEFT_PAREN)) {
      Expr expr = expression();
      consume(RIGHT_PAREN, "mismatched closing delimiter: `(`");
      return new Expr.Grouping(expr);
    }

    throw error(peek(), "Expected expression, found keyword.");
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
}
