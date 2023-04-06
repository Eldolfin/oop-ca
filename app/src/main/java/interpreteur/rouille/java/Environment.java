package interpreteur.rouille.java;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Environment {
  final Optional<Environment> enclosing;
  private final Map<String, Object> values = new HashMap<>();
  private final Map<String, Boolean> mutables = new HashMap<>();

  Environment() {
    enclosing = Optional.empty();
  }

  Environment(Environment enclosing) {
    this.enclosing = Optional.of(enclosing);
  }

  void define(String name, Object value, boolean mutable) {
    values.put(name, value);
    mutables.put(name, mutable);
  }

  void define(String name, Object value) {
    define(name, value, false);
  }

  Object get(Token name) {
    if (values.containsKey(name.lexeme)) {
      return values.get(name.lexeme);
    }

    if (enclosing.isPresent())
      return enclosing.get().get(name);

    throw new RuntimeError(name, "Undefined variable `" + name.lexeme + "`.");
  }

  public void assign(Token name, Object value) {
    if (values.containsKey(name.lexeme)) {
      if (!mutables.get(name.lexeme))
        throw new RuntimeError(name, "cannot assign twice to immutable variable `" + name.lexeme + "`");

      if (values.get(name.lexeme).getClass() != value.getClass())
        throw new RuntimeError(name, "mismatched types");

      values.put(name.lexeme, value);
      return;
    } else if (enclosing.isPresent()) {
      enclosing.get().assign(name, value);
    } else {
      throw new RuntimeError(name,
          "La variable `" + name.lexeme + "` ne peut pas être trouvé dans ce 'scope'");
    }
  }

  public void assignAt(Integer distance, Token name, Object value) {
    ancestor(distance).values.put(name.lexeme, value);
  }

  public Object getAt(Integer distance, String name) {
    return ancestor(distance).values.get(name);
  }

  Environment ancestor(int distance) {
    var environment = this;

    for (int i = 0; i < distance; i++) {
      environment = environment.enclosing.get();
    }

    return environment;
  }
}
