package interpreteur.rouille.java;

enum TokenType {
  LEFT_PAREN("\\("), RIGHT_PAREN("\\)"),
  LEFT_BRACE("\\{"), RIGHT_BRACE("}"),
  COMMA(","), DOT("\\."),
  MINUS("-"), PLUS("\\+"),
  SLASH("/"), STAR("\\*"), PERCENT("%"),
  SEMICOLON(";"),
  PLUS_EQUAL("\\+="), MINUS_EQUAL("-="), SLASH_EQUAL("/="), STAR_EQUAL("\\*="),
  PERCENT_EQUAL("%="), OR_EQUAL("\\|="), AND_EQUAL("&="),

  BANG("!"), BANG_EQUAL("!="),
  EQUAL("="), EQUAL_EQUAL("=="),
  GREATER(">"), GREATER_EQUAL(">="),
  LESS("<"), LESS_EQUAL("<="),
  AND("&&"), OR("\\|\\|"),
  BITWISE_AND("&"), BITWISE_OR("\\|"),

  IDENTIFIER("([a-zA-Z]|[à-ü]|[À-Ü]|_)([a-zA-Z]|[à-ü]|[À-Ü]|[0-9]|_)*"),
  STRING("[\"]([^\"\\\n]|\\.|\\\n)*[\"]"),
  INTEGER("-?\\d+"), FLOAT("-?\\d+\\.\\d+"),

  STRUCT("structure"), IF("si"), ELSE("sinon"),
  FUN("fonction"), ARROW("->"),
  TRUE("vrai"), FALSE("faux"),
  FOR("pour"), WHILE("tant"), LOOP("boucle"),
  BREAK("arrête"),
  PRINT("affiche!"), RETURN("renvoie"), SELF("soi"),
  LET("soit"), MUT("mutable"),

  EOF("(?!.*)"); // unmatchable regex

  public final String regex;

  private TokenType(String regex) {
    this.regex = regex;
  }

  public static TokenType detect(String token) {
    var scanner = new Scanner(token);
    return scanner.scanTokens().get(0).type;
  }
}
