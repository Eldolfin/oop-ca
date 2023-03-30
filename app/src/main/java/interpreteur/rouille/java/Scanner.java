package interpreteur.rouille.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static interpreteur.rouille.java.TokenType.*;

public class Scanner {
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

    tokens.add(new Token(EOF, "", Optional.empty(), line, column));
    return tokens;
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  private void scanToken() {
    var next = source.substring(current);
    TokenType maxToken = null;
    var maxTokenLenght = 0;
    for (var entry : patterns.entrySet()) {
      var token = entry.getKey();
      var pattern = entry.getValue();
      var matcher = pattern.matcher(next);
      if (matcher.find()) {
        var length = matcher.end() - matcher.start();
        if (length > maxTokenLenght || length == maxTokenLenght && maxToken == IDENTIFIER) {
          maxToken = token;
          maxTokenLenght = length;
        }
      }
    }
    if (maxToken == null) {
      App.error(line, "Unknown token.");
      current++;
      return;
    }

    current += maxTokenLenght;
    addToken(maxToken);
  }

  private void addToken(TokenType type) {
    addToken(type, Optional.empty());
  }

  private void addToken(TokenType type, Optional<Object> literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, Optional.of(literal), line, column));
  }
}
