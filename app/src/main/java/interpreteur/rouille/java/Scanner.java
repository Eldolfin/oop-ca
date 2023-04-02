package interpreteur.rouille.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static interpreteur.rouille.java.TokenType.*;

class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int current = 0;
  private int line = 0;
  private int column = 0;
  static private HashMap<TokenType, Pattern> patterns = new HashMap<>();

  static {
    for (TokenType type : TokenType.values()) {
      var regex = "^" + type.regex;
      patterns.put(type, Pattern.compile(regex, Pattern.UNIX_LINES));
    }
  }

  Scanner(String source) {
    this.source = source;
  }

  List<Token> scanTokens() {
    while (!isAtEnd()) {
      start = current;
      scanToken();
    }

    // free semicolon at the end for the repl
    tokens.add(new Token(SEMICOLON, ";", Optional.empty(), line, column));
    tokens.add(new Token(EOF, "", Optional.empty(), line, column));
    return tokens;
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  private void scanToken() {
    var next = source.substring(current);
    TokenType maxTokenType = null;
    String maxLexeme = null;
    var maxTokenLenght = 0;
    for (var entry : patterns.entrySet()) {
      var tokenType = entry.getKey();
      var pattern = entry.getValue();
      var matcher = pattern.matcher(next);
      if (matcher.find()) {
        var length = matcher.end() - matcher.start();
        if (length > maxTokenLenght || length == maxTokenLenght && maxTokenType == IDENTIFIER) {
          maxTokenType = tokenType;
          maxTokenLenght = length;
          maxLexeme = next.substring(matcher.start(), matcher.end());
        }
      }
    }

    if (maxTokenType == null) {
      var token = advance();
      if (!Character.isWhitespace(token)) {
        App.error(line, "Unknown token `" + token + "`.");
      } else if (token == '\n') {
        column = 0;
        line++;
      }
      return;
    }

    current += maxTokenLenght;
    column += maxTokenLenght;
    switch (maxTokenType) {
      case INTEGER:
        addToken(maxTokenType, Optional.of(Integer.parseInt(maxLexeme)));
        break;
      case FLOAT:
        addToken(maxTokenType, Optional.of(Double.parseDouble(maxLexeme)));
        break;
      case STRING:
        addToken(maxTokenType, Optional.of(maxLexeme.substring(1, maxLexeme.length() - 1)));
        break;
      default:
        addToken(maxTokenType);
        break;
    }
  }

  private char advance() {
    return source.charAt(current++);
  }

  private void addToken(TokenType type) {
    addToken(type, Optional.empty());
  }

  private void addToken(TokenType type, Optional<Object> literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line, column));
  }
}
