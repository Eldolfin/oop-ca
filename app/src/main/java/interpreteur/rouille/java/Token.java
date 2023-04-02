package interpreteur.rouille.java;

import java.util.Optional;

class Token {
  final TokenType type;
  final String lexeme;
  final Optional<Object> literal;
  final int line;
  final int column;

  public Token(TokenType type, String lexeme, int line, int column) {
    this(type, lexeme, Optional.empty(), line, column);
  }

  public Token(TokenType type, String lexeme, Optional<Object> literal, int line, int column) {
    this.type = type;
    this.lexeme = lexeme;
    this.literal = literal;
    this.line = line;
    this.column = column;
  }

  public String toString() {
    return type + "(" + lexeme + ")";
  }
}
