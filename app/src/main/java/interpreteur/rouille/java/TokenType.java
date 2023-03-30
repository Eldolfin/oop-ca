package interpreteur.rouille.java;

enum TokenType {
  LEFT_PAREN("\\("), RIGHT_PAREN("\\)"), LEFT_BRACE("\\{"), RIGHT_BRACE("}"),
  COMMA(","), DOT("\\."), MINUS("-"), PLUS("\\+"), SEMICOLON(","), SLASH("/"), STAR("\\*"),

  BANG("!"), BANG_EQUAL("!="),
  EQUAL("="), EQUAL_EQUAL("=="),
  GREATER(">"), GREATER_EQUAL(">="),
  LESS("<"), LESS_EQUAL("<="),
  AND("&&"), OR("||"),

  IDENTIFIER("[a-zA-Z_][a-zA-Z_0-9]*"), STRING("\".*\""),
  INTEGER("-?\\d+"), FLOAT("-?\\d+\\.\\d+"),

  STRUCT("structure"), IF("si"), ELSE("sinon"), FUN("fonction"),
  TRUE("vrai"), FALSE("faux"),
  FOR("pour"), WHILE("tant"),
  PRINT("affiche"), RETURN("renvoie"), SELF("soi"), LET("soit"),

  EOF("(?!.*)"); // unmatchable regex

  public final String regex;

  private TokenType(String regex) {
    this.regex = regex;
  }

}
